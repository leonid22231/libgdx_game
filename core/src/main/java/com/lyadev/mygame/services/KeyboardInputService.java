package com.lyadev.mygame.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.lyadev.mygame.base.ModuleInputRegistry;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.world.GlobalWorld;
import com.lyadev.mygame.world.WorldCameraControl;
import com.lyadev.mygame.world.topdown.OrthoMapEditor;
import com.lyadev.mygame.world.topdown.TopDownMapWorld;

/**
 * Thin input dispatcher. Key bindings live in modules / {@link GameSystemKeyBindings}.
 */
public class KeyboardInputService implements InputProcessor {
    private static final String TAG = "KeyboardInputService";

    public KeyboardInputService() {
        GameSystemKeyBindings.registerAll();
        Gdx.app.log(TAG, "KeyboardInputService ready (module key registry)");
    }

    @Override
    public boolean keyDown(int keycode) {
        MainService.getInstance().getLogger().addKey(Keys.toString(keycode));
        if(keycode == Keys.ESCAPE){
            Gdx.app.exit();
            return true;
        }
        ModuleInputRegistry.onKeyDown(keycode);
        ModuleInputRegistry.pollHeld();
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        ModuleInputRegistry.onKeyUp(keycode);
        ModuleInputRegistry.pollHeld();
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        MainService.getInstance().getLogger().setLastTap(screenX, screenY, pointer, button);
        if(button == Buttons.MIDDLE){
            WorldCameraControl.beginPan(screenX, screenY);
            return true;
        }
        if(OrthoMapEditor.isEnabled()){
            if(button == Buttons.LEFT || button == Buttons.RIGHT){
                OrthoMapEditor.handleTouch(screenX, screenY, button == Buttons.LEFT);
            }
            return true;
        }
        if(button == Buttons.LEFT){
            TopDownMapWorld.projectScreenClick(screenX, screenY);
            if(SelectableModule.isPickMode()){
                GlobalWorld.handleEntityClick();
            }
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(button == Buttons.MIDDLE){
            WorldCameraControl.endPan();
        }
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        if(button == Buttons.MIDDLE){
            WorldCameraControl.endPan();
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(WorldCameraControl.isPanning()){
            WorldCameraControl.panDrag(screenX, screenY);
            return true;
        }
        if(OrthoMapEditor.isEnabled() && Gdx.input.isButtonPressed(Buttons.LEFT)){
            OrthoMapEditor.handleTouch(screenX, screenY, true);
        }
        OrthoMapEditor.updateHover(screenX, screenY);
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        MainService.getInstance().getLogger().mousePositionListener(screenX, screenY);
        OrthoMapEditor.updateHover(screenX, screenY);
        if(!OrthoMapEditor.isEnabled()){
            GlobalWorld.updateEntityMouseInfo(screenX, screenY);
        }
        ModuleInputRegistry.pollHeld();
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        MainService.getInstance().getLogger().setScroll(amountY);
        if(OrthoMapEditor.isEnabled()){
            if(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)){
                OrthoMapEditor.zoomByScroll(amountY);
            } else {
                OrthoMapEditor.cycleBrush(amountY > 0 ? -1 : 1);
            }
        }
        return true;
    }
}
