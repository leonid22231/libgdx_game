package com.lyadev.mygame.modules.door;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.lyadev.mygame.base.entity.Entity;
import com.lyadev.mygame.base.entity.EntityModule;
import com.lyadev.mygame.debug.ModuleDebugPanel;
import com.lyadev.mygame.modules.prop.PropModule;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.modules.tile.TileBlockModule;
import com.lyadev.mygame.base.world.GlobalWorld;
import com.lyadev.mygame.base.world.WorldController;

/**
 * Decor trigger: overlapping active player travels to another {@link com.lyadev.mygame.base.world.WorldEntity}.
 */
public class DoorModule extends EntityModule {
    private static final String TAG = "DoorModule";
    private static final float COOLDOWN_SECONDS = 1.25f;

    private static float sharedCooldown;

    private final DoorSettings settings;
    private boolean occupied;

    public DoorModule(DoorSettings settings) {
        this.settings = settings;
    }

    @Override
    public String getName() {
        return "door_module";
    }

    @Override
    public List<Class<? extends EntityModule>> getRequiredModules() {
        return List.of(PropModule.class);
    }

    @Override
    public void act(float delta) {
        if(sharedCooldown > 0f){
            sharedCooldown -= delta;
        }
        Entity door = getEntity();
        Entity traveler = findOverlappingPlayable(door);
        if(traveler == null){
            occupied = false;
            return;
        }
        if(occupied || sharedCooldown > 0f){
            return;
        }
        occupied = true;
        sharedCooldown = COOLDOWN_SECONDS;
        Gdx.app.log(TAG, door.getTAG() + " → world " + settings.getTargetWorldId()
                + " @[" + settings.getTargetCol() + "," + settings.getTargetRow() + "]"
                + " by " + traveler.getTAG());
        WorldController.travel(
                settings.getTargetWorldId(),
                traveler,
                settings.getTargetCol(),
                settings.getTargetRow());
    }

    public static void armCooldown() {
        sharedCooldown = COOLDOWN_SECONDS;
    }

    public DoorSettings getSettings() {
        return settings;
    }

    public static DoorModule from(Entity entity) {
        return entity.getModule(DoorModule.class);
    }

    private static Entity findOverlappingPlayable(Entity door) {
        if(GlobalWorld.entities == null){
            return null;
        }
        for(Entity entity : GlobalWorld.entities){
            if(entity == door || !isPlayable(entity)){
                continue;
            }
            if(entity != GlobalWorld.player){
                continue;
            }
            SelectableModule selectable = SelectableModule.from(entity);
            if(selectable == null || !selectable.isActive()){
                continue;
            }
            if(overlaps(door, entity)){
                return entity;
            }
        }
        return null;
    }

    private static boolean isPlayable(Entity entity) {
        if(TileBlockModule.from(entity) != null || PropModule.from(entity) != null){
            return false;
        }
        return SelectableModule.from(entity) != null;
    }

    private static boolean overlaps(Entity a, Entity b) {
        return a.getX() < b.getX() + b.getWidth()
                && a.getX() + a.getWidth() > b.getX()
                && a.getY() < b.getY() + b.getHeight()
                && a.getY() + a.getHeight() > b.getY();
    }

    @Override
    public void populateDebugScreen(ModuleDebugPanel panel) {
        super.populateDebugScreen(panel);
        panel.line("targetWorld", settings.getTargetWorldId());
        panel.line("spawn", settings.getTargetCol() + "," + settings.getTargetRow());
        panel.line("cooldown", String.format("%.2f", Math.max(0f, sharedCooldown)));
        panel.line("occupied", occupied);
    }
}
