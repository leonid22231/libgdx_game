package com.lyadev.mygame.base;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.lyadev.mygame.utils.Position;
import com.lyadev.mygame.utils.Size;
import com.lyadev.mygame.utils.listeners.EntityListener;
import com.lyadev.mygame.utils.listeners.EntityListenerThread;
import com.lyadev.mygame.world.GlobalWorld;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Entity extends Actor {
    private EntitySettings settings;
    private EntityTexture texture = new EntityTexture();
    private Position position = Position.DEFAULT;
    private EntityStatus status = new EntityStatus();
    private Size size;
    private String TAG;

    public Entity(EntitySettings settings) {
        this.settings = settings;
        setTag();
        texture.init(settings);
        setupSize(texture.getTextureSize());
        initThread();
        GlobalWorld.addEntity(this);
        status.setIsInit(true);
    }

    // NOTE: DRAWING METHODS
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture.getCurrentTexture(), position.getX(), position.getY(), size.getWidth(), size.getHeight());
    }
    
    // NOTE: GLOBAL METHODS
    public void setRandomPositionInScreen() {
        Random random = new Random();
        float width = size.getWidth();
        float height = size.getHeight();

        position.setX(width + random.nextFloat() * ((Gdx.graphics.getWidth() - width) - width));
        position.setY(height + random.nextFloat() * ((Gdx.graphics.getHeight() - height) - height));
    }

    // NOTE: LOGIC METHODS (PRIVATE METHODS)
    private void initThread() {
        EntityListenerThread thread = EntityListenerThread.builder()
                .name(TAG)
                .build();

        thread.setEntity(this);

        EntityListener listenerThread = new EntityListener(
                this,
                thread);
        status.setListener(listenerThread);
    }

    private void setupSize(Size size) {
        this.size = size;
    }
    // NOTE: Private
    private void setTag(){
       TAG = String.format("Player[%s]", settings.getTag());
    }
    // NOTE: Custom GETTERS
    public Size getSize() {
        if (size == null)
            return Size.ENTITY_DEFAULT;

        return size;
    }

    // NOTE: Dispose method
    public void dispose() {
        status.getListener().dispose();
    }

    // NOTE: toString
    @Override
    public String toString() {
        return String.format(
                "%s x[%.3f], y[%.3f], h[%s], w[%s], active[%s], focused[%s], sprite[%s], visibleObjects[%s]",
                TAG, position.getX(),
                position.getY(), size.getHeight(), size.getWidth(), status.isActive(), status.getIsFocused(),
                texture.getCurrentSpriteIndex(), status.getVisibleObjects().size());
    }
}
