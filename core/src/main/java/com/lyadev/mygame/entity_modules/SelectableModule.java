package com.lyadev.mygame.entity_modules;

import com.badlogic.gdx.Gdx;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.world.GlobalWorld;

public class SelectableModule extends EntityModule {
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

    public void updateVisibility() {
        getEntity().setVisible(shouldRender());
    }

    public void mousePositionListener(float screenX, float screenY) {
        getEntity().getStatus().setIsFocused(isMouseOver(screenX, screenY));
    }

    public void clickEvent() {
        if(Boolean.TRUE.equals(getEntity().getStatus().getIsFocused())){
            activateEntity();
        } else {
            deactivateEntity();
        }
    }

    public void clearVisionVisibility() {
        getEntity().getStatus().setIsShow(false);
        updateVisibility();
    }

    public void setVisibleByVision(boolean visible) {
        getEntity().getStatus().setIsShow(visible);
        updateVisibility();
    }

    public boolean isVisibleByVision() {
        return Boolean.TRUE.equals(getEntity().getStatus().getIsShow());
    }

    public boolean shouldRender() {
        Entity entity = getEntity();
        return entity.getStatus().isActive() || isVisibleByVision();
    }

    private void activateEntity() {
        GlobalWorld.setActivePlayer(getEntity());
    }

    private void deactivateEntity() {
        Entity entity = getEntity();
        entity.getStatus().setActive(false);
        stopMovement(entity);
        if(entity == GlobalWorld.player){
            GlobalWorld.player = findActivePlayer();
        }
        GlobalWorld.refreshEntityVisibility();
    }

    private Entity findActivePlayer() {
        for(Entity entity : GlobalWorld.entities){
            if(entity.getStatus().isActive()){
                return entity;
            }
        }
        return null;
    }

    private void stopMovement(Entity entity) {
        EntityModule module = entity.getModule("movement_module");
        if(module instanceof MovementModule){
            ((MovementModule) module).stopMoving();
        }
    }

    private boolean isMouseOver(float screenX, float screenY) {
        Entity entity = getEntity();
        float entityX = entity.getPosition().getX();
        float entityY = entity.getPosition().getY();
        float width = entity.getSize().getWidth();
        float height = entity.getSize().getHeight();
        float screenBottomY = Gdx.graphics.getHeight() - entityY;
        float screenTopY = screenBottomY - height;

        return screenX > entityX
                && screenX < entityX + width
                && screenY > screenTopY
                && screenY < screenBottomY;
    }

    public static SelectableModule from(Entity entity) {
        EntityModule module = entity.getModule("selectable_module");
        if(module instanceof SelectableModule){
            return (SelectableModule) module;
        }
        return null;
    }
}
