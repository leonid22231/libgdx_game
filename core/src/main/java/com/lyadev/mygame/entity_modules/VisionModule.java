package com.lyadev.mygame.entity_modules;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.base.PlayableEntitySettings;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.lyadev.mygame.enums.MoveType;
import com.lyadev.mygame.services.MainService;
import com.lyadev.mygame.utils.CircleSector;
import com.lyadev.mygame.utils.Position;
import com.lyadev.mygame.utils.listeners.EntityListener;
import com.lyadev.mygame.utils.listeners.EntityListenerThread;
import com.lyadev.mygame.world.GlobalWorld;

public class VisionModule extends EntityModule {
    private static final float VISION_DEGREES = 90f;
    private float currentDegrees1 = 0;
    private float currentDegrees2 = 0;

    @Override
    public String getName() {
        return "vision_module";
    }

    @Override
    public void init() {
        Entity entity = getEntity();
        EntityListenerThread thread = EntityListenerThread.builder()
                .name(entity.getTAG())
                .build();
        thread.setEntity(entity);

        EntityListener listener = new EntityListener(thread);
        entity.getStatus().setListener(listener);
        GlobalWorld.listeners.add(listener);
    }

    @Override
    public void dispose() {
        EntityListener listener = getEntity().getStatus().getListener();
        if(listener != null){
            GlobalWorld.listeners.remove(listener);
            listener.dispose();
            getEntity().getStatus().setListener(null);
        }
    }

    public float getVisionScore() {
        PlayableEntitySettings settings = getEntity().getPlayableSettings();
        if(settings == null){
            return 0f;
        }
        return settings.getVisibleRadius();
    }

    public CircleSector getCircleSector() {
        updateVisionDegrees();
        Position center = getEntity().getCenterPosition();
        return new CircleSector(center.getX(), center.getY(), getVisionScore(), currentDegrees1, currentDegrees2);
    }

    public static VisionModule from(Entity entity) {
        EntityModule module = entity.getModule("vision_module");
        if(module instanceof VisionModule){
            return (VisionModule) module;
        }
        return null;
    }

    private void updateVisionDegrees() {
        MoveType lastMoveType = MoveType.DOWN;
        SpriteModule spriteModule = SpriteModule.from(getEntity());
        if(spriteModule != null){
            lastMoveType = spriteModule.getLastMoveType();
        }

        float deg = VISION_DEGREES / 2;
        float degrees1 = deg;
        float degrees2 = deg * -1;
        float round = 180 - deg * 2;
        switch(lastMoveType){
            case DOWN:
                degrees1 = (90 - deg) * -1;
                degrees2 = (90 + deg) * -1;
                break;
            case UP:
                degrees1 = (90 + deg);
                degrees2 = (90 - deg);
                break;
            case LEFT:
                degrees1 = (deg + round) * -1;
                degrees2 = deg + round;
                break;
            case RIGHT:
                degrees1 = deg;
                degrees2 = deg * -1;
                break;
        }
        currentDegrees1 = degrees1;
        currentDegrees2 = degrees2;
    }

    public void drawDebugVision() {
        if(!getEntity().getStatus().isActive()){
            return;
        }
        updateVisionDegrees();
        Position center = getEntity().getCenterPosition();
        float radius = getVisionScore();
        if(radius <= 0){
            return;
        }

        double x = center.getX() + radius * Math.cos(Math.toRadians(currentDegrees1));
        double y = center.getY() + radius * Math.sin(Math.toRadians(currentDegrees1));
        double x1 = center.getX() + radius * Math.cos(Math.toRadians(currentDegrees2));
        double y1 = center.getY() + radius * Math.sin(Math.toRadians(currentDegrees2));

        MainService.getInstance().getShapeRenderer().begin(ShapeType.Line);
        MainService.getInstance().getShapeRenderer().setColor(Color.YELLOW);
        MainService.getInstance().getShapeRenderer().circle(center.getX(), center.getY(), radius);
        MainService.getInstance().getShapeRenderer().line(center.getX(), center.getY(), (float) x, (float) y);
        MainService.getInstance().getShapeRenderer().line(center.getX(), center.getY(), (float) x1, (float) y1);
        MainService.getInstance().getShapeRenderer().end();
    }
}
