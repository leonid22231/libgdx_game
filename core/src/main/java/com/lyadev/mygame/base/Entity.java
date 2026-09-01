package com.lyadev.mygame.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.lyadev.mygame.modules.animation.AnimationModule;
import com.lyadev.mygame.modules.sprite.SpriteModule;
import com.lyadev.mygame.modules.visionmouse.VisionMouseModule;
import com.lyadev.mygame.utils.Position;
import com.lyadev.mygame.utils.Size;

import lombok.Getter;

/**
 * Минимальное ядро: tag + Actor transform + модули. Без настроек — конфиг передаётся в модули при register.
 */
public class Entity extends Actor {
    private final String tag;
    private final String TAG;
    private final ArrayList<EntityModule> modules = new ArrayList<>();
    private final ModuleRegistry moduleRegistry = new ModuleRegistry(this);
    @Getter
    private boolean modulesResolved = false;

    public Entity(String tag) {
        this.tag = tag;
        this.TAG = String.format("Player[%s]", tag);
    }

    public String getTag() {
        return tag;
    }

    public String getTAG() {
        return TAG;
    }

    @Override
    public void act(float delta) {
        EntityModule deferredVisualModule = null;
        VisionMouseModule visionMouseModule = null;
        for(EntityModule module : modules){
            if(!module.isRuntimeActive()){
                continue;
            }
            if(module instanceof SpriteModule || module instanceof AnimationModule){
                deferredVisualModule = module;
                continue;
            }
            if(module instanceof VisionMouseModule){
                visionMouseModule = (VisionMouseModule) module;
                continue;
            }
            module.act(delta);
        }
        if(visionMouseModule != null && visionMouseModule.isRuntimeActive()){
            visionMouseModule.act(delta);
        }
        if(deferredVisualModule != null && deferredVisualModule.isRuntimeActive()){
            deferredVisualModule.act(delta);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        for(EntityModule module : modules){
            if(module.isRuntimeActive()){
                module.draw(batch, parentAlpha);
            }
        }
    }

    /** Screen-space UI pass — see {@link EntityModule#drawUi}. */
    public void drawUi(Batch batch, float parentAlpha) {
        for(EntityModule module : modules){
            if(module.isRuntimeActive()){
                module.drawUi(batch, parentAlpha);
            }
        }
    }

    public void setRandomPositionInScreen() {
        Random random = new Random();
        float width = getWidth() > 0 ? getWidth() : Size.ENTITY_DEFAULT.getWidth();
        float height = getHeight() > 0 ? getHeight() : Size.ENTITY_DEFAULT.getHeight();

        setX(width + random.nextFloat() * ((Gdx.graphics.getWidth() - width) - width));
        setY(height + random.nextFloat() * ((Gdx.graphics.getHeight() - height) - height));
    }

    public Position getCenterPosition() {
        return new Position(getX() + getWidth() / 2f, getY() + getHeight() / 2f);
    }

    public void registerModule(EntityModule module) {
        moduleRegistry.register(module);
    }

    public void resolveModules() {
        moduleRegistry.resolveAndInit();
        modules.clear();
        modules.addAll(moduleRegistry.getActiveModules());
        modulesResolved = true;
    }

    /** @deprecated use {@link #registerModule(EntityModule)} and {@link #resolveModules()} */
    @Deprecated
    public void addModule(EntityModule module) {
        registerModule(module);
        resolveModules();
    }

    public boolean hasModule(String name) {
        return getModule(name) != null;
    }

    public EntityModule getModule(String name) {
        for(EntityModule module : modules){
            if(module.getName().equals(name)){
                return module;
            }
        }
        for(EntityModule module : moduleRegistry.getActiveModules()){
            if(module.getName().equals(name)){
                return module;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityModule> T getModule(Class<T> type) {
        for(EntityModule module : modules){
            if(type.isInstance(module)){
                return (T) module;
            }
        }
        for(EntityModule module : moduleRegistry.getActiveModules()){
            if(type.isInstance(module)){
                return (T) module;
            }
        }
        return null;
    }

    public EntityModule getRegisteredModule(String name) {
        for(EntityModule module : moduleRegistry.getRegisteredModules()){
            if(module.getName().equals(name)){
                return module;
            }
        }
        return null;
    }

    public List<EntityModule> getRegisteredModules() {
        return moduleRegistry.getRegisteredModules();
    }

    public void dispose() {
        for(EntityModule module : moduleRegistry.getActiveModules()){
            module.dispose();
        }
    }

    @Override
    public String toString() {
        return String.format(
                "%s x[%.3f], y[%.3f], w[%.1f], h[%.1f]",
                TAG, getX(), getY(), getWidth(), getHeight());
    }
}
