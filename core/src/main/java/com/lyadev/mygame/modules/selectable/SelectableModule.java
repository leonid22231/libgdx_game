package com.lyadev.mygame.modules.selectable;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.base.ModuleKeyChord;
import com.lyadev.mygame.base.ModuleKeyDecl;
import com.lyadev.mygame.base.ModuleKeyEvent;
import com.lyadev.mygame.debug.ModuleDebugPanel;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.services.MainService;
import com.lyadev.mygame.world.GlobalWorld;
import com.lyadev.mygame.world.topdown.OrthoMapEditor;

import lombok.Getter;
import lombok.Setter;

/**
 * Active player + fog-of-war visibility.
 * <p>
 * Pick: hold <b>Ctrl+Alt</b> ({@link #getKeyBindings()}) → LMB on target.
 */
public class SelectableModule extends EntityModule {
    private static final String LOG_TAG = "SelectableModule";

    @Getter
    private static boolean pickMode;
    private static boolean suppressPickUntilRelease;

    @Getter
    private boolean active;
    @Getter
    @Setter
    private boolean focused;
    @Getter
    private boolean visibleByVision;
    @Getter
    @Setter
    private String debugNote = "";

    private final Vector3 mouseWorld = new Vector3();

    @Override
    public String getName() {
        return "selectable_module";
    }

    @Override
    public List<ModuleKeyDecl> getKeyBindings() {
        return List.of(
                ModuleKeyDecl.hold(
                        "pick_mode",
                        ModuleKeyChord.ofGroups(
                                ModuleKeyChord.any(Keys.CONTROL_LEFT, Keys.CONTROL_RIGHT),
                                ModuleKeyChord.any(Keys.ALT_LEFT, Keys.ALT_RIGHT)),
                        "Hold Ctrl+Alt: reveal all characters, then LMB to select"));
    }

    @Override
    protected void onKeyBinding(String actionId, ModuleKeyEvent event) {
        if(!"pick_mode".equals(actionId)){
            return;
        }
        // Only one instance should drive global pick mode.
        syncPickMode(event.isHold());
    }

    @Override
    public void init() {
        getEntity().setVisible(false);
    }

    @Override
    public void act(float delta) {
        updateVisibility();
    }

    /** Called when Ctrl+Alt held-state changes. */
    public static void syncPickMode(boolean modifiersHeld) {
        if(OrthoMapEditor.isEnabled()){
            setPickModeInternal(false);
            suppressPickUntilRelease = false;
            return;
        }
        if(!modifiersHeld){
            suppressPickUntilRelease = false;
            setPickModeInternal(false);
            return;
        }
        if(suppressPickUntilRelease){
            setPickModeInternal(false);
            return;
        }
        setPickModeInternal(true);
    }

    private static void setPickModeInternal(boolean enabled) {
        if(pickMode == enabled){
            return;
        }
        pickMode = enabled;
        GlobalWorld.refreshEntityVisibility();
        Gdx.app.log(LOG_TAG, pickMode ? "Pick mode ON — click a character" : "Pick mode OFF");
    }

    private static void endPickAfterSelect() {
        suppressPickUntilRelease = true;
        setPickModeInternal(false);
    }

    public void setActive(boolean active) {
        this.active = active;
        updateVisibility();
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
        if(!pickMode || !focused){
            return;
        }
        for(Entity entity : GlobalWorld.entities){
            SelectableModule other = from(entity);
            if(other != null){
                other.clearVisionVisibility();
            }
        }
        activateEntity();
        endPickAfterSelect();
    }

    public void clearVisionVisibility() {
        setVisibleByVision(false);
    }

    public boolean shouldRender() {
        if(OrthoMapEditor.isEnabled()){
            return false;
        }
        if(pickMode){
            return true;
        }
        return active || visibleByVision;
    }

    private void activateEntity() {
        GlobalWorld.setActivePlayer(getEntity());
    }

    private Entity findActivePlayer() {
        for(Entity entity : GlobalWorld.entities){
            SelectableModule selectable = from(entity);
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
        Viewport viewport = MainService.getInstance().getWorldViewport();
        if(viewport == null){
            return false;
        }
        mouseWorld.set(screenX, screenY, 0f);
        viewport.unproject(mouseWorld);

        Entity entity = getEntity();
        return mouseWorld.x >= entity.getX()
                && mouseWorld.x <= entity.getX() + entity.getWidth()
                && mouseWorld.y >= entity.getY()
                && mouseWorld.y <= entity.getY() + entity.getHeight();
    }

    public static SelectableModule from(Entity entity) {
        return entity.getModule(SelectableModule.class);
    }

    @Override
    public void populateDebugScreen(ModuleDebugPanel panel) {
        super.populateDebugScreen(panel);
        panel.line("pickMode", pickMode);
        panel.line("active", active);
        panel.line("focused", focused);
        panel.line("visibleByVision", visibleByVision);
        panel.line("shouldRender", shouldRender());
        panel.line("actorVisible", getEntity().isVisible());
        panel.checkbox("active", "Active", active);
        panel.checkbox("focused", "Focused", focused);
        panel.checkbox("visible_by_vision", "Visible by vision", visibleByVision);
        panel.textField("debug_note", "Debug note", debugNote);
        panel.action("toggle_active", active ? "Deactivate" : "Set active");
    }

    @Override
    public String handleDebugFieldChange(String fieldId, String value) {
        if("active".equals(fieldId)){
            if(Boolean.parseBoolean(value)){
                GlobalWorld.setActivePlayer(getEntity());
                return "Active player: " + getEntity().getTag();
            }
            setActive(false);
            stopMovement(getEntity());
            if(getEntity() == GlobalWorld.player){
                GlobalWorld.player = findActivePlayer();
            }
            GlobalWorld.refreshEntityVisibility();
            return "Deactivated";
        }
        if("focused".equals(fieldId)){
            setFocused(Boolean.parseBoolean(value));
            return "Focused: " + focused;
        }
        if("visible_by_vision".equals(fieldId)){
            setVisibleByVision(Boolean.parseBoolean(value));
            return "Visible by vision: " + visibleByVision;
        }
        if("debug_note".equals(fieldId)){
            debugNote = value == null ? "" : value;
            return "Debug note updated";
        }
        return super.handleDebugFieldChange(fieldId, value);
    }

    @Override
    public String handleDebugAction(String actionId) {
        if("toggle_active".equals(actionId)){
            if(active){
                setActive(false);
                stopMovement(getEntity());
                if(getEntity() == GlobalWorld.player){
                    GlobalWorld.player = findActivePlayer();
                }
                GlobalWorld.refreshEntityVisibility();
                return "Deactivated";
            }
            GlobalWorld.setActivePlayer(getEntity());
            return "Active player: " + getEntity().getTag();
        }
        return super.handleDebugAction(actionId);
    }
}
