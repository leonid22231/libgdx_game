package com.lyadev.mygame.services;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.lyadev.mygame.base.ModuleInputRegistry;
import com.lyadev.mygame.base.ModuleKeyChord;
import com.lyadev.mygame.base.ModuleKeyDecl;
import com.lyadev.mygame.base.ModuleKeyEvent;
import com.lyadev.mygame.debug.DebugFeatures;
import com.lyadev.mygame.world.GlobalWorld;
import com.lyadev.mygame.world.WorldCameraControl;
import com.lyadev.mygame.world.topdown.OrthoMapEditor;

/**
 * System key declarations (same shape as {@link com.lyadev.mygame.base.EntityModule#getKeyBindings()}).
 */
public final class GameSystemKeyBindings {
    private static final String OWNER = "system";
    private static boolean registered;

    private GameSystemKeyBindings() {
        throw new UnsupportedOperationException();
    }

    public static List<ModuleKeyDecl> getKeyBindings() {
        return List.of(
                ModuleKeyDecl.down("editor_toggle", ModuleKeyChord.of(Keys.M), "Toggle map editor"),
                ModuleKeyDecl.down("editor_ground", ModuleKeyChord.of(Keys.NUM_1), "Map editor: ground mode"),
                ModuleKeyDecl.down("editor_decor", ModuleKeyChord.of(Keys.NUM_2), "Map editor: decor mode"),
                ModuleKeyDecl.down("editor_save", ModuleKeyChord.of(Keys.F5), "Map editor: save map"),
                ModuleKeyDecl.down("camera_follow", ModuleKeyChord.of(Keys.F), "Resume camera follow"),
                ModuleKeyDecl.down("random_positions", ModuleKeyChord.of(Keys.E),
                        "Randomize playable positions (not in editor)"),
                ModuleKeyDecl.down("clear_logs", ModuleKeyChord.of(Keys.R), "Clear in-game logger buffer"),
                ModuleKeyDecl.up("debug_vision_lines", ModuleKeyChord.of(Keys.ALT_RIGHT),
                        "Toggle vision debug lines"),
                ModuleKeyDecl.up("debug_overlay", ModuleKeyChord.of(Keys.ALT_LEFT),
                        "Toggle in-game logger overlay"));
    }

    public static void registerAll() {
        if(registered){
            return;
        }
        registered = true;
        ModuleInputRegistry.resolveSystem(OWNER, getKeyBindings(), GameSystemKeyBindings::onKeyBinding);
    }

    private static void onKeyBinding(String actionId, ModuleKeyEvent event) {
        switch(actionId){
            case "editor_toggle":
                OrthoMapEditor.toggle();
                break;
            case "editor_ground":
                if(OrthoMapEditor.isEnabled()){
                    OrthoMapEditor.setMode(OrthoMapEditor.Mode.GROUND);
                }
                break;
            case "editor_decor":
                if(OrthoMapEditor.isEnabled()){
                    OrthoMapEditor.setMode(OrthoMapEditor.Mode.DECOR);
                }
                break;
            case "editor_save":
                OrthoMapEditor.save();
                break;
            case "camera_follow":
                WorldCameraControl.resumeFollow();
                break;
            case "random_positions":
                if(!OrthoMapEditor.isEnabled()){
                    GlobalWorld.setAllRandomPositions();
                }
                break;
            case "clear_logs":
                MainService.getInstance().getLogger().clearLogs();
                break;
            case "debug_vision_lines":
                if(!ctrlHeld()){
                    DebugFeatures.toggleVisionLines();
                }
                break;
            case "debug_overlay":
                if(!ctrlHeld()){
                    DebugFeatures.toggleOverlay();
                }
                break;
            default:
                break;
        }
    }

    private static boolean ctrlHeld() {
        return Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)
                || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
    }
}
