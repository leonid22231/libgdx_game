package com.lyadev.mygame.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.lyadev.mygame.UI;
import com.lyadev.mygame.entities.PlayebleEntity;
import com.lyadev.mygame.models.CustomThreadService;
import com.lyadev.mygame.world.GlobalWorld;

public class KeyboardInputService implements InputProcessor {
    private static final String TAG = "KeyboardInputService";

    Map<Integer, String> currentKeyCodes = new HashMap<Integer, String>();
    Map<Integer, String> tempKeyCodes = new HashMap<Integer, String>();

    List<CustomThreadService> threads = new ArrayList<CustomThreadService>();

    public KeyboardInputService() {
        Gdx.app.log(TAG, "Create the KeyboardInputService!");
        init();
    }

    void stopThread(Thread thread) {
        thread.interrupt();
    }

    void init() {
        Gdx.app.log(TAG, "Initialize the KeyboardInputService!");

    }

    void playerControll(int keycode) {
        if (GlobalWorld.player != null) {
            switch (keycode) {
                case Keys.W:
                    //GlobalEntity.getPlayer().moveUpToggle();
                    break;
                case Keys.S:
                    //GlobalEntity.getPlayer().moveDownToggle();
                    break;
                case Keys.A:
                    //GlobalEntity.getPlayer().moveLeftToggle();
                    break;
                case Keys.D:
                    //GlobalEntity.getPlayer().moveRightToggle();
                    break;
                case Keys.SHIFT_LEFT:
                    //GlobalEntity.getPlayer().sprintToggle();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        UI.logger.addKey(Keys.toString(keycode));
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
                //GlobalEntity.setAllRandomPositions();
                break;
            case Keys.R:
                UI.logger.clearLogs();
                break;
            case Keys.ALT_RIGHT:
                //GlobalEntity.drawDebugLineToggle();
                break;
            case Keys.ALT_LEFT:
                UI.logger.setActive(!UI.logger.getActive());
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
        UI.logger.setLastTap(screenX, screenY, pointer, button);
        //TODO: Implement entity click event
        //GlobalEntity.clickEvent();
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
        UI.logger.mousePositionListener(screenX, screenY);
        //TODO: Implement entity movement logic
        //GlobalEntity.updateEntityInfo(screenX, screenY);
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        UI.logger.setScroll(amountY);
        return true;
    }

}
