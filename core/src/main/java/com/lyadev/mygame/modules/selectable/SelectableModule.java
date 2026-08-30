package com.lyadev.mygame.modules.selectable;

import com.badlogic.gdx.Gdx;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.world.GlobalWorld;

public class SelectableModule extends EntityModule {
    private boolean active = false;
    private boolean focused = false;
    private boolean visibleByVision = false;

    @Override
    public String getName() {
        return "selectable_module";
    }

    @Override
    public void init() {
        getEntity().setVisible(false);
    }

    @Override
    public void act(float delta) {
        updateVisibility();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        updateVisibility();
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean isVisibleByVision() {
        return visibleByVision;
    }

    public void setVisibleByVision(boolean visibleByVision) {
        this.visibleByVision = visibleByVision;
        updateVisibility();
    }

    public void updateVisibility() {
        getEntity().setVisible(shouldRender());
    }

    public void mousePositionListener(float screenX, float screenY) {
        setFocused(isMouseOver(screenX, screenY));
    }

    public void clickEvent() {
        if(focused){
            activateEntity();
        } else {
            deactivateEntity();
        }
    }

    public void clearVisionVisibility() {
        setVisibleByVision(false);
    }

    public boolean shouldRender() {
        return active || visibleByVision;
    }

    private void activateEntity() {
        GlobalWorld.setActivePlayer(getEntity());
    }

    private void deactivateEntity() {
        Entity entity = getEntity();
        setActive(false);
        stopMovement(entity);
        if(entity == GlobalWorld.player){
            GlobalWorld.player = findActivePlayer();
        }
        GlobalWorld.refreshEntityVisibility();
    }

    private Entity findActivePlayer() {
        for(Entity entity : GlobalWorld.entities){
            SelectableModule selectable = SelectableModule.from(entity);
            if(selectable != null && selectable.isActive()){
                return entity;
            }
        }
        return null;
    }

    private void stopMovement(Entity entity) {
        MovementModule movement = entity.getModule(MovementModule.class);
        if(movement != null){
            movement.stopMoving();
        }
    }

    private boolean isMouseOver(float screenX, float screenY) {
        Entity entity = getEntity();
        float entityX = entity.getX();
        float entityY = entity.getY();
        float width = entity.getWidth();
        float height = entity.getHeight();
        float screenBottomY = Gdx.graphics.getHeight() - entityY;
        float screenTopY = screenBottomY - height;

        return screenX > entityX
                && screenX < entityX + width
                && screenY > screenTopY
                && screenY < screenBottomY;
    }

    public static SelectableModule from(Entity entity) {
        return entity.getModule(SelectableModule.class);
    }
}
