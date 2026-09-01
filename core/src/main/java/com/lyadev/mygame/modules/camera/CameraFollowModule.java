package com.lyadev.mygame.modules.camera;

import java.util.List;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.debug.ModuleDebugPanel;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.services.MainService;
import com.lyadev.mygame.world.WorldCameraControl;

/**
 * Moves the world camera to the active entity center.
 * Only the entity with {@link SelectableModule#isActive()} drives the camera.
 */
public class CameraFollowModule extends EntityModule {
    private final CameraFollowSettings settings;
    private boolean wasActive;
    private float lastTargetX;
    private float lastTargetY;

    public CameraFollowModule() {
        this(CameraFollowSettings.DEFAULT);
    }

    public CameraFollowModule(CameraFollowSettings settings) {
        this.settings = settings;
    }

    @Override
    public String getName() {
        return "camera_follow_module";
    }

    @Override
    public List<Class<? extends EntityModule>> getRequiredModules() {
        return List.of(SelectableModule.class);
    }

    @Override
    public void act(float delta) {
        SelectableModule selectable = require(SelectableModule.class);
        if(!selectable.isActive()){
            wasActive = false;
            return;
        }
        if(!WorldCameraControl.isFollowEnabled()){
            return;
        }

        Entity entity = getEntity();
        float targetX = entity.getX() + entity.getWidth() * 0.5f;
        float targetY = entity.getY() + entity.getHeight() * 0.5f;
        lastTargetX = targetX;
        lastTargetY = targetY;

        OrthographicCamera camera = worldCamera();
        if(camera == null){
            return;
        }

        boolean justActivated = !wasActive;
        if(justActivated && settings.isSnapOnActivate()){
            camera.position.set(targetX, targetY, 0f);
        } else {
            float t = 1f - (float) Math.exp(-settings.getFollowSpeed() * delta);
            camera.position.x += (targetX - camera.position.x) * t;
            camera.position.y += (targetY - camera.position.y) * t;
        }
        camera.update();
        wasActive = true;
    }

    @Override
    public void populateDebugScreen(ModuleDebugPanel panel) {
        super.populateDebugScreen(panel);
        if(!isEnabled()){
            return;
        }
        panel.line("followSpeed", settings.getFollowSpeed());
        panel.line("snapOnActivate", settings.isSnapOnActivate());
        panel.line("followEnabled", WorldCameraControl.isFollowEnabled());
        panel.line("wasActive", wasActive);
        panel.line("target", String.format("%.1f, %.1f", lastTargetX, lastTargetY));
    }

    public CameraFollowSettings getSettings() {
        return settings;
    }

    public static CameraFollowModule from(Entity entity) {
        return entity.getModule(CameraFollowModule.class);
    }

    private static OrthographicCamera worldCamera() {
        Viewport viewport = MainService.getInstance().getWorldViewport();
        if(viewport == null || !(viewport.getCamera() instanceof OrthographicCamera)){
            return null;
        }
        return (OrthographicCamera) viewport.getCamera();
    }
}
