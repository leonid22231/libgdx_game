package com.lyadev.mygame.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.debug.DebugFeatures;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.world.GlobalWorld;

public class KeyboardInputService implements InputProcessor {
    private static final String TAG = "KeyboardInputService";

    public KeyboardInputService() {
        Gdx.app.log(TAG, "Create the KeyboardInputService!");
    }

    void playerControll(int keycode) {
        if (GlobalWorld.player == null) {
            return;
        }
        MovementModule movement = getMovementModule(GlobalWorld.player);
        if (movement == null) {
            return;
        }
        if (GlobalWorld.player == null) {
            return;
        }
        SelectableModule selectable = SelectableModule.from(GlobalWorld.player);
        if (selectable == null || !selectable.isActive()) {
            return;
        }
        switch (keycode) {
            case Keys.W:
                movement.moveUpToggle();
                break;
            case Keys.S:
                movement.moveDownToggle();
                break;
            case Keys.A:
                movement.moveLeftToggle();
                break;
            case Keys.D:
                movement.moveRightToggle();
                break;
            case Keys.SHIFT_LEFT:
                movement.sprintToggle();
                break;
            default:
                break;
        }
    }

    private MovementModule getMovementModule(Entity entity) {
        return entity.getModule(MovementModule.class);
    }

    @Override
    public boolean keyDown(int keycode) {
        MainService.getInstance().getLogger().addKey(Keys.toString(keycode));
        playerControll(keycode);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        Gdx.app.debug(TAG, "Key up: " + keycode);
        switch (keycode) {
            case Keys.ESCAPE:
                Gdx.app.exit();
                break;
            case Keys.E:
                GlobalWorld.setAllRandomPositions();
                break;
            case Keys.R:
                MainService.getInstance().getLogger().clearLogs();
                break;
            case Keys.ALT_RIGHT:
                DebugFeatures.toggleVisionLines();
                Gdx.app.debug(TAG, "Vision lines: " + (DebugFeatures.isVisionLinesVisible() ? "ON" : "OFF"));
                break;
            case Keys.ALT_LEFT:
                DebugFeatures.toggleOverlay();
                Gdx.app.debug(TAG, "In-game overlay: " + (DebugFeatures.isOverlayVisible() ? "ON" : "OFF"));
                break;
            default:
                break;
        }
        playerControll(keycode);
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        MainService.getInstance().getLogger().setLastTap(screenX, screenY, pointer, button);
        GlobalWorld.handleEntityClick();
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        MainService.getInstance().getLogger().mousePositionListener(screenX, screenY);
        GlobalWorld.updateEntityMouseInfo(screenX, screenY);
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        MainService.getInstance().getLogger().setScroll(amountY);
        return true;
    }
}
