package com.lyadev.mygame.base;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.lyadev.mygame.utils.Position;
import com.lyadev.mygame.utils.Size;
import com.lyadev.mygame.debug.DebugFeatures;
import com.lyadev.mygame.entity_modules.SelectableModule;
import com.lyadev.mygame.entity_modules.VisionModule;
import com.lyadev.mygame.utils.listeners.EntityListener;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Entity extends Actor {
    private EntitySettings settings;
    private EntityTexture texture = new EntityTexture();
    private EntityPosition position = new EntityPosition(0, 0);
    private EntityStatus status = new EntityStatus();
    private EntityVision vision = new EntityVision();
    private Size size;
    private String TAG;
    private ArrayList<EntityModule> modules = new ArrayList<>();

    public Entity(EntitySettings settings) {
        this.settings = settings;
        setTag();
    }

    public void finishInit() {
        vision.init(this);
        status.setIsInit(true);
    }

    @Override
    public void act(float delta) {
        for(EntityModule module : modules){
            module.act(delta);
        }
        vision.update();
    }

    // NOTE: DRAWING METHODS
    @Override
    public void draw(Batch batch, float parentAlpha) {
        for(EntityModule module : modules){
            module.draw(batch, parentAlpha);
        }
        if(shouldDrawVision()){
            debugDraw(batch);
        }
    }

    private boolean shouldDrawVision() {
        if(!DebugFeatures.isVisionLinesVisible()){
            return false;
        }
        SelectableModule selectable = SelectableModule.from(this);
        if(selectable == null){
            return true;
        }
        return selectable.shouldRender();
    }

    private void debugDraw(Batch batch) {
        batch.end();
        vision.draw();
        VisionModule visionModule = VisionModule.from(this);
        if(visionModule != null){
            visionModule.drawDebugVision();
        }
        batch.begin();
    }

    // NOTE: GLOBAL METHODS
    public void setRandomPositionInScreen() {
        Random random = new Random();
        Size entitySize = getSize();
        float width = entitySize.getWidth();
        float height = entitySize.getHeight();

        position.setX(width + random.nextFloat() * ((Gdx.graphics.getWidth() - width) - width));
        position.setY(height + random.nextFloat() * ((Gdx.graphics.getHeight() - height) - height));
    }

    // NOTE: LOGIC METHODS (PRIVATE METHODS)
    private void setupSize(Size entitySize) {
        this.size = entitySize;
    }

    public void setSize(Size entitySize) {
        setupSize(entitySize);
    }

    // NOTE: Private
    private void setTag(){
       TAG = String.format("Player[%s]", settings.getTag());
    }

    // NOTE: Custom GETTERS
    public Position getCenterPosition() {
        return position.getCenterPositionFromSize(getSize());
    }

    public Size getSize() {
        if (size == null)
            return Size.ENTITY_DEFAULT;

        return size;
    }

    public PlayableEntitySettings getPlayableSettings() {
        if(settings instanceof PlayableEntitySettings){
            return (PlayableEntitySettings) settings;
        }
        return null;
    }

    //NOTE: Modules
    public void addModule(EntityModule module){
        module.attach(this);
        modules.add(module);
        module.init();
    }

    public EntityModule getModule(String name) {
        for(EntityModule module : modules){
            if(module.getName().equals(name)){
                return module;
            }
        }
        return null;
    }

    // NOTE: Dispose method
    public void dispose() {
        for(EntityModule module : modules){
            module.dispose();
        }
        texture.dispose();
        EntityListener listener = status.getListener();
        if(listener != null){
            listener.dispose();
        }
    }

    // NOTE: toString
    @Override
    public String toString() {
        Size entitySize = getSize();
        return String.format(
                "%s x[%.3f], y[%.3f], h[%s], w[%s], active[%s], focused[%s], sprite[%s], visibleObjects[%s]",
                TAG, position.getX(),
                position.getY(), entitySize.getHeight(), entitySize.getWidth(), status.isActive(), status.getIsFocused(),
                texture.getCurrentSpriteIndex(), vision.getVisibleEntities().size());
    }
}
