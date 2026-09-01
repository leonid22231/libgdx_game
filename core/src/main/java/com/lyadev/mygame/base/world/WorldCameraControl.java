package com.lyadev.mygame.base.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lyadev.mygame.services.MainService;

import lombok.Getter;
import lombok.Setter;

/**
 * Manual pan (middle mouse) + flag that pauses {@code CameraFollowModule}.
 */
public final class WorldCameraControl {
    private static final Vector3 PREV = new Vector3();
    private static final Vector3 CURR = new Vector3();

    @Getter
    @Setter
    private static boolean followEnabled = true;
    @Getter
    private static boolean panning;
    private static int lastScreenX;
    private static int lastScreenY;

    private WorldCameraControl() {
        throw new UnsupportedOperationException();
    }

    public static void resumeFollow() {
        followEnabled = true;
    }

    public static void pauseFollow() {
        followEnabled = false;
    }

    public static void beginPan(int screenX, int screenY) {
        panning = true;
        followEnabled = false;
        lastScreenX = screenX;
        lastScreenY = screenY;
    }

    public static void panDrag(int screenX, int screenY) {
        if(!panning){
            return;
        }
        Viewport viewport = MainService.getInstance().getWorldViewport();
        if(viewport == null || !(viewport.getCamera() instanceof OrthographicCamera)){
            return;
        }
        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        PREV.set(lastScreenX, lastScreenY, 0f);
        CURR.set(screenX, screenY, 0f);
        viewport.unproject(PREV);
        viewport.unproject(CURR);
        camera.position.add(PREV.x - CURR.x, PREV.y - CURR.y, 0f);
        camera.update();
        lastScreenX = screenX;
        lastScreenY = screenY;
    }

    public static void endPan() {
        panning = false;
    }
}
