package com.lyadev.mygame.debug;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.modules.animation.AnimationModule;
import com.lyadev.mygame.modules.playable.AnimatedPlayableBlueprint;
import com.lyadev.mygame.modules.playable.PlayableBlueprint;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.modules.sprite.SpriteModule;
import com.lyadev.mygame.modules.vision.VisionModule;
import com.lyadev.mygame.world.GlobalWorld;

public final class DebugEntityService {
    private DebugEntityService() {
        throw new UnsupportedOperationException();
    }

    public static List<DebugEntitySnapshot> collectSnapshots() {
        List<DebugEntitySnapshot> snapshots = new ArrayList<>();
        if(GlobalWorld.entities == null){
            return snapshots;
        }
        for(Entity entity : GlobalWorld.entities){
            snapshots.add(toSnapshot(entity));
        }
        return snapshots;
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
        if("newgirl".equalsIgnoreCase(preset)){
            AnimatedPlayableBlueprint blueprint = copyAnimatedBlueprintWithUniqueTag(GlobalWorld.defaultNewgirlBlueprint());
            Entity entity = GlobalWorld.spawnAnimatedPlayableEntity(blueprint);
            return "Spawned " + entity.getTag() + " (" + entity.getTAG() + ")";
        }
        PlayableBlueprint blueprint = buildPresetBlueprint(preset);
        if(blueprint == null){
            return "Unknown preset: " + preset + " (man, woman, newgirl)";
        }
        blueprint = copyBlueprintWithUniqueTag(blueprint);
        Entity entity = GlobalWorld.spawnPlayableEntity(blueprint);
        return "Spawned " + entity.getTag() + " (" + entity.getTAG() + ")";
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
        int visualFrame = sprite != null
                ? sprite.getCurrentSpriteIndex()
                : (animation != null ? animation.getFrameIndex() : 0);
        return new DebugEntitySnapshot(
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

    private static PlayableBlueprint buildPresetBlueprint(String preset) {
        if(preset == null){
            return null;
        }
        switch(preset.toLowerCase()){
            case "man":
                return GlobalWorld.buildPlayableBlueprint(
                        "Man",
                        com.lyadev.mygame.Assets.PERSON_MAN,
                        Gdx.graphics.getWidth() * 0.2f);
            case "woman":
            case "woomen":
                return GlobalWorld.buildPlayableBlueprint(
                        "Woomen",
                        com.lyadev.mygame.Assets.PERSON_WOMAN,
                        100f);
            default:
                return null;
        }
    }

    private static AnimatedPlayableBlueprint copyAnimatedBlueprintWithUniqueTag(AnimatedPlayableBlueprint source) {
        String baseTag = source.getTag();
        String uniqueTag = baseTag;
        int index = 2;
        while(findEntityByTag(uniqueTag) != null){
            uniqueTag = baseTag + index;
            index++;
        }
        return source.withTag(uniqueTag);
    }

    private static PlayableBlueprint copyBlueprintWithUniqueTag(PlayableBlueprint source) {
        String baseTag = source.getTag();
        String uniqueTag = baseTag;
        int index = 2;
        while(findEntityByTag(uniqueTag) != null){
            uniqueTag = baseTag + index;
            index++;
        }
        return source.withTag(uniqueTag);
    }
}
