package com.lyadev.mygame.modules.visionmouse;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lyadev.mygame.base.entity.Entity;
import com.lyadev.mygame.base.entity.EntityModule;
import com.lyadev.mygame.debug.ModuleDebugPanel;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.services.MainService;

/**
 * Направляет взгляд active-entity на курсор.
 * {@link MovementModule#setLookAngleDegrees(float)} — непрерывный угол (vision).
 * {@link MovementModule#getFacingDirection()} — ближайшие 4 стороны (sprite/animation).
 */
public class VisionMouseModule extends EntityModule {
    private static final float AIM_DEAD_ZONE = 4f;

    private final Vector3 mouseStageCoords = new Vector3();

    @Override
    public String getName() {
        return "vision_mouse_module";
    }

    @Override
    public List<Class<? extends EntityModule>> getRequiredModules() {
        return List.of(MovementModule.class, SelectableModule.class);
    }

    @Override
    public void act(float delta) {
        SelectableModule selectable = require(SelectableModule.class);
        if(!selectable.isActive()){
            return;
        }

        Viewport viewport = MainService.getInstance().getStage().getViewport();
        mouseStageCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(mouseStageCoords);

        Entity entity = getEntity();
        float centerX = entity.getX() + entity.getWidth() / 2f;
        float centerY = entity.getY() + entity.getHeight() / 2f;
        float dx = mouseStageCoords.x - centerX;
        float dy = mouseStageCoords.y - centerY;

        if(Math.abs(dx) < AIM_DEAD_ZONE && Math.abs(dy) < AIM_DEAD_ZONE){
            return;
        }

        float lookAngleDegrees = (float) Math.toDegrees(Math.atan2(dy, dx));
        require(MovementModule.class).setLookAngleDegrees(lookAngleDegrees);
    }

    @Override
    public void populateDebugScreen(ModuleDebugPanel panel) {
        super.populateDebugScreen(panel);
        if(!isEnabled()){
            return;
        }
        panel.line("aimDeadZone", AIM_DEAD_ZONE);
        MovementModule movement = require(MovementModule.class);
        panel.line("lookAngle", String.format("%.1f°", movement.getLookAngleDegrees()));
        panel.line("facing", movement.getFacingDirection());
        panel.line("mouseStageX", String.format("%.1f", mouseStageCoords.x));
        panel.line("mouseStageY", String.format("%.1f", mouseStageCoords.y));
    }

    public static VisionMouseModule from(Entity entity) {
        return entity.getModule(VisionMouseModule.class);
    }
}
