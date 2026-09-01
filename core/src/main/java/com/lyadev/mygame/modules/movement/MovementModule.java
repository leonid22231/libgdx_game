package com.lyadev.mygame.modules.movement;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.base.ModuleKeyChord;
import com.lyadev.mygame.base.ModuleKeyDecl;
import com.lyadev.mygame.base.ModuleKeyEvent;
import com.lyadev.mygame.debug.ModuleDebugPanel;
import com.lyadev.mygame.enums.MoveType;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.modules.visionmouse.VisionMouseModule;
import com.lyadev.mygame.world.GlobalWorld;
import com.lyadev.mygame.world.topdown.OrthoMapEditor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class MovementModule extends EntityModule {
    private final MovementSettings settings;

    @Getter
    @Setter
    private boolean moveLeft;
    @Getter
    @Setter
    private boolean moveRight;
    @Getter
    @Setter
    private boolean moveUp;
    @Getter
    @Setter
    private boolean moveDown;

    private boolean sprinting;

    @Getter
    private MoveType facingDirection = MoveType.DOWN;

    /**
     * Угол конуса vision (градусы, {@code atan2}). По умолчанию совпадает с {@link #facingDirection}.
     * Свободный угол задаёт {@link VisionMouseModule} через {@link #setLookAngleDegrees(float)}.
     */
    @Getter
    private float lookAngleDegrees = angleFromFacing(MoveType.DOWN);

    @Override
    public String getName() {
        return "movement_module";
    }

    @Override
    public List<Class<? extends EntityModule>> getRequiredModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ModuleKeyDecl> getKeyBindings() {
        return List.of(
                ModuleKeyDecl.downUp("up", ModuleKeyChord.of(Keys.W), "Move up (active player)"),
                ModuleKeyDecl.downUp("down", ModuleKeyChord.of(Keys.S), "Move down (active player)"),
                ModuleKeyDecl.downUp("left", ModuleKeyChord.of(Keys.A), "Move left (active player)"),
                ModuleKeyDecl.downUp("right", ModuleKeyChord.of(Keys.D), "Move right (active player)"),
                ModuleKeyDecl.downUp("sprint", ModuleKeyChord.of(Keys.SHIFT_LEFT), "Sprint toggle (active player)"));
    }

    @Override
    protected void onKeyBinding(String actionId, ModuleKeyEvent event) {
        if(!isControllable()){
            return;
        }
        boolean hold = event.isHold();
        switch(actionId){
            case "up":
                moveUp = hold;
                break;
            case "down":
                moveDown = hold;
                break;
            case "left":
                moveLeft = hold;
                break;
            case "right":
                moveRight = hold;
                break;
            case "sprint":
                sprinting = hold;
                break;
            default:
                break;
        }
    }

    private boolean isControllable() {
        if(OrthoMapEditor.isEnabled() || SelectableModule.isPickMode()){
            return false;
        }
        SelectableModule selectable = SelectableModule.from(getEntity());
        return selectable != null && selectable.isActive() && getEntity() == GlobalWorld.player;
    }

    /** Угол для {@link com.lyadev.mygame.modules.vision.VisionModule} (4 направления или free look). */
    public float getLookAngleForVision() {
        return lookAngleDegrees;
    }

    /** Свободный взгляд (мышь) — snap {@link #facingDirection} для sprite/animation. */
    public void setLookAngleDegrees(float lookAngleDegrees) {
        this.lookAngleDegrees = lookAngleDegrees;
        this.facingDirection = facingFromAngle(lookAngleDegrees);
    }

    /** Четыре стороны — источник правды для man/woman и snap-спрайта. Синхронизирует угол vision. */
    public void setFacingDirection(MoveType facingDirection) {
        if(facingDirection != null){
            this.facingDirection = facingDirection;
            this.lookAngleDegrees = angleFromFacing(facingDirection);
        }
    }

    public void rotate() {
        setFacingDirection(rotateFacing(facingDirection));
    }

    private static MoveType rotateFacing(MoveType facing) {
        switch(facing){
            case LEFT:
                return MoveType.RIGHT;
            case RIGHT:
                return MoveType.LEFT;
            case UP:
                return MoveType.DOWN;
            case DOWN:
            default:
                return MoveType.UP;
        }
    }

    static MoveType facingFromAngle(float degrees) {
        float normalized = (degrees % 360f + 360f) % 360f;
        if(normalized >= 315f || normalized < 45f){
            return MoveType.RIGHT;
        }
        if(normalized < 135f){
            return MoveType.UP;
        }
        if(normalized < 225f){
            return MoveType.LEFT;
        }
        return MoveType.DOWN;
    }

    static float angleFromFacing(MoveType facing) {
        switch(facing){
            case RIGHT:
                return 0f;
            case UP:
                return 90f;
            case LEFT:
                return 180f;
            case DOWN:
            default:
                return -90f;
        }
    }

    private boolean usesFreeLook() {
        return getEntity().getModule(VisionMouseModule.class) != null;
    }

    @Override
    public void act(float delta) {
        Entity entity = getEntity();
        SelectableModule selectable = SelectableModule.from(entity);
        if(selectable == null || !selectable.isActive()){
            return;
        }
        float speed = sprinting ? settings.getSprintSpeed() : settings.getWalkSpeed();
        boolean freeLook = usesFreeLook();

        if(moveLeft){
            entity.setX(entity.getX() - delta * speed);
            if(!freeLook){
                setFacingDirection(MoveType.LEFT);
            }
        }
        if(moveRight){
            entity.setX(entity.getX() + delta * speed);
            if(!freeLook){
                setFacingDirection(MoveType.RIGHT);
            }
        }
        if(moveUp){
            entity.setY(entity.getY() + delta * speed);
            if(!freeLook){
                setFacingDirection(MoveType.UP);
            }
        }
        if(moveDown){
            entity.setY(entity.getY() - delta * speed);
            if(!freeLook){
                setFacingDirection(MoveType.DOWN);
            }
        }
    }

    public boolean isMoving() {
        return moveLeft || moveRight || moveUp || moveDown;
    }

    public boolean isSprinting() {
        return sprinting && isMoving();
    }

    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }

    public void moveLeftToggle() {
        moveLeft = !moveLeft;
    }

    public void moveRightToggle() {
        moveRight = !moveRight;
    }

    public void moveUpToggle() {
        moveUp = !moveUp;
    }

    public void moveDownToggle() {
        moveDown = !moveDown;
    }

    public void sprintToggle() {
        sprinting = !sprinting;
    }

    public void stopMoving() {
        moveLeft = false;
        moveRight = false;
        moveUp = false;
        moveDown = false;
        sprinting = false;
    }

    /** After map switch: clear sticky flags, then mirror currently held keys. */
    public void resetControlState() {
        stopMoving();
        if(!isControllable()){
            return;
        }
        moveUp = Gdx.input.isKeyPressed(Keys.W);
        moveDown = Gdx.input.isKeyPressed(Keys.S);
        moveLeft = Gdx.input.isKeyPressed(Keys.A);
        moveRight = Gdx.input.isKeyPressed(Keys.D);
        sprinting = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT);
    }

    @Override
    public void populateDebugScreen(ModuleDebugPanel panel) {
        super.populateDebugScreen(panel);
        panel.line("facing", facingDirection);
        panel.line("lookAngle", String.format("%.1f°", lookAngleDegrees));
        panel.line("moving", isMoving());
        panel.line("sprinting", isSprinting());
        panel.line("walkSpeed", settings.getWalkSpeed());
        panel.line("sprintSpeed", settings.getSprintSpeed());
        panel.line("freeLook", usesFreeLook());
        panel.checkbox("sprint", "Sprinting", sprinting);
        panel.checkbox("move_left", "Move left", moveLeft);
        panel.checkbox("move_right", "Move right", moveRight);
        panel.checkbox("move_up", "Move up", moveUp);
        panel.checkbox("move_down", "Move down", moveDown);
        panel.numberField("look_angle", "Look angle °", lookAngleDegrees, -360d, 360d);
        panel.action("stop_moving", "Stop moving");
        panel.action("rotate_facing", "Rotate facing");
    }

    @Override
    public String handleDebugFieldChange(String fieldId, String value) {
        if("sprint".equals(fieldId)){
            sprinting = Boolean.parseBoolean(value);
            return "Sprinting: " + sprinting;
        }
        if("move_left".equals(fieldId)){
            moveLeft = Boolean.parseBoolean(value);
            return "Move left: " + moveLeft;
        }
        if("move_right".equals(fieldId)){
            moveRight = Boolean.parseBoolean(value);
            return "Move right: " + moveRight;
        }
        if("move_up".equals(fieldId)){
            moveUp = Boolean.parseBoolean(value);
            return "Move up: " + moveUp;
        }
        if("move_down".equals(fieldId)){
            moveDown = Boolean.parseBoolean(value);
            return "Move down: " + moveDown;
        }
        if("look_angle".equals(fieldId)){
            try {
                setLookAngleDegrees(Float.parseFloat(value));
                return "Look angle: " + lookAngleDegrees;
            } catch(NumberFormatException error) {
                return "Invalid number: " + value;
            }
        }
        return super.handleDebugFieldChange(fieldId, value);
    }

    @Override
    public String handleDebugAction(String actionId) {
        if("stop_moving".equals(actionId)){
            stopMoving();
            return "Movement stopped";
        }
        if("rotate_facing".equals(actionId)){
            rotate();
            return "Facing: " + facingDirection;
        }
        return super.handleDebugAction(actionId);
    }

    public static MovementModule from(Entity entity) {
        return entity.getModule(MovementModule.class);
    }
}
