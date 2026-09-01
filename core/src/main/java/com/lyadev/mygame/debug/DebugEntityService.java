package com.lyadev.mygame.debug;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.lyadev.mygame.base.entity.Entity;
import com.lyadev.mygame.base.entity.EntityModule;
import com.lyadev.mygame.modules.animation.AnimationModule;
import com.lyadev.mygame.modules.prop.PropModule;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.modules.sprite.SpriteModule;
import com.lyadev.mygame.modules.tile.TileBlockModule;
import com.lyadev.mygame.modules.vision.VisionModule;
import com.lyadev.mygame.persons.cat.CatPerson;
import com.lyadev.mygame.persons.man.ManPerson;
import com.lyadev.mygame.persons.newgirl.NewgirlPerson;
import com.lyadev.mygame.persons.woomen.WoomenPerson;
import com.lyadev.mygame.base.world.GlobalWorld;

public final class DebugEntityService {
    private DebugEntityService() {
        throw new UnsupportedOperationException();
    }

    public static List<DebugEntitySnapshot> collectSnapshots() {
        return collectSnapshotsByKind(false);
    }

    /** Tile + prop map entities for the Blocks debug tab. */
    public static List<DebugEntitySnapshot> collectBlockSnapshots() {
        return collectSnapshotsByKind(true);
    }

    private static List<DebugEntitySnapshot> collectSnapshotsByKind(boolean blocksOnly) {
        List<DebugEntitySnapshot> snapshots = new ArrayList<>();
        if(GlobalWorld.entities == null){
            return snapshots;
        }
        for(Entity entity : GlobalWorld.entities){
            boolean isBlock = isMapBlock(entity);
            if(blocksOnly != isBlock){
                continue;
            }
            snapshots.add(toSnapshot(entity));
        }
        return snapshots;
    }

    public static boolean isMapBlock(Entity entity) {
        return TileBlockModule.from(entity) != null || PropModule.from(entity) != null;
    }

    public static Entity findEntityByTag(String tag) {
        if(GlobalWorld.entities == null || tag == null){
            return null;
        }
        for(Entity entity : GlobalWorld.entities){
            if(entity.getTag().equalsIgnoreCase(tag)){
                return entity;
            }
        }
        return null;
    }

    public static String setActivePlayer(String tag) {
        Entity entity = findEntityByTag(tag);
        if(entity == null){
            return "Entity not found: " + tag;
        }
        GlobalWorld.setActivePlayer(entity);
        return "Active player: " + entity.getTAG();
    }

    public static String setModuleRuntimePaused(String entityTag, String moduleName, boolean paused) {
        Entity entity = findEntityByTag(entityTag);
        if(entity == null){
            return "Entity not found: " + entityTag;
        }
        EntityModule module = entity.getRegisteredModule(moduleName);
        if(module == null){
            return "Module not registered: " + moduleName;
        }
        if(!module.isEnabled()){
            return "Module not resolved (disabled at init): " + moduleName;
        }
        module.setRuntimePaused(paused);
        return moduleName + " runtime " + (paused ? "paused" : "resumed");
    }

    public static ModuleDebugScreen collectModuleDebugScreen(String entityTag, String moduleName) {
        Entity entity = findEntityByTag(entityTag);
        if(entity == null){
            return ModuleDebugScreen.empty(moduleName, "Entity not found: " + entityTag);
        }
        EntityModule module = entity.getRegisteredModule(moduleName);
        if(module == null){
            return ModuleDebugScreen.empty(moduleName, "Module not registered: " + moduleName);
        }
        ModuleDebugPanel panel = new ModuleDebugPanel();
        module.populateDebugScreen(panel);
        return panel.build(module.getName(), module.getClass().getSimpleName());
    }

    public static String executeModuleDebugAction(String entityTag, String moduleName, String actionId) {
        Entity entity = findEntityByTag(entityTag);
        if(entity == null){
            return "Entity not found: " + entityTag;
        }
        EntityModule module = entity.getRegisteredModule(moduleName);
        if(module == null){
            return "Module not registered: " + moduleName;
        }
        String result = module.handleDebugAction(actionId);
        return result != null ? result : "Unknown action: " + actionId;
    }

    public static String applyModuleDebugField(String entityTag, String moduleName, String fieldId, String value) {
        Entity entity = findEntityByTag(entityTag);
        if(entity == null){
            return "Entity not found: " + entityTag;
        }
        EntityModule module = entity.getRegisteredModule(moduleName);
        if(module == null){
            return "Module not registered: " + moduleName;
        }
        String result = module.handleDebugFieldChange(fieldId, value);
        return result != null ? result : "Unknown field: " + fieldId;
    }

    public static String teleportRandom(String tag) {
        Entity entity = findEntityByTag(tag);
        if(entity == null){
            return "Entity not found: " + tag;
        }
        entity.setRandomPositionInScreen();
        GlobalWorld.resetVisionState();
        return "Teleported: " + tag;
    }

    public static String spawnPlayablePreset(String preset) {
        if(!GlobalWorld.isReady()){
            return "World is not ready yet";
        }
        Entity entity = spawnPresetEntity(preset);
        if(entity == null){
            return "Unknown preset: " + preset + " (man, woman, newgirl, cat)";
        }
        entity.setRandomPositionInScreen();
        GlobalWorld.resetVisionState();
        return "Spawned " + entity.getTag() + " (" + entity.getTAG() + ")";
    }

    private static Entity spawnPresetEntity(String preset) {
        if(preset == null){
            return null;
        }
        switch(preset.toLowerCase()){
            case "man":
                return ManPerson.spawnUnique();
            case "woman":
            case "woomen":
                return WoomenPerson.spawnUnique();
            case "newgirl":
                return NewgirlPerson.spawnUnique();
            case "cat":
                return CatPerson.spawnUnique();
            default:
                return null;
        }
    }

    private static DebugEntitySnapshot toSnapshot(Entity entity) {
        List<DebugModuleSnapshot> modules = new ArrayList<>();
        for(EntityModule module : entity.getRegisteredModules()){
            modules.add(new DebugModuleSnapshot(
                    module.getName(),
                    module.isEnabled(),
                    module.isRuntimePaused(),
                    module.getDisabledReason(),
                    module.getRequiredModuleNames()));
        }
        SelectableModule selectable = SelectableModule.from(entity);
        SpriteModule sprite = SpriteModule.from(entity);
        AnimationModule animation = AnimationModule.from(entity);
        VisionModule vision = VisionModule.from(entity);
        TileBlockModule tile = TileBlockModule.from(entity);
        PropModule prop = PropModule.from(entity);
        String kind = DebugEntitySnapshot.KIND_ACTOR;
        if(tile != null){
            kind = DebugEntitySnapshot.KIND_TILE;
        } else if(prop != null){
            kind = DebugEntitySnapshot.KIND_PROP;
        }
        int visualFrame = sprite != null
                ? sprite.getCurrentSpriteIndex()
                : (animation != null ? animation.getFrameIndex() : 0);
        if(tile != null){
            visualFrame = tile.getTileId();
        }
        return new DebugEntitySnapshot(
                kind,
                entity.getTag(),
                entity.getTAG(),
                entity.getX(),
                entity.getY(),
                (int) entity.getWidth(),
                (int) entity.getHeight(),
                selectable != null && selectable.isActive(),
                selectable != null && selectable.isFocused(),
                selectable != null && selectable.isVisibleByVision(),
                entity.isVisible(),
                entity.isModulesResolved(),
                vision != null ? vision.getVisibleEntityCount() : 0,
                visualFrame,
                entity == GlobalWorld.player,
                modules);
    }
}
