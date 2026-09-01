package com.lyadev.mygame.debug;

import java.util.ArrayList;
import java.util.List;

import com.lyadev.mygame.base.entity.Entity;
import com.lyadev.mygame.base.world.GlobalWorld;
import com.lyadev.mygame.base.world.WorldController;
import com.lyadev.mygame.base.world.WorldEntity;
import com.lyadev.mygame.base.world.topdown.OrthoMapData;

/** Debug helpers for listing / switching {@link WorldEntity} instances. */
public final class DebugWorldService {
    private DebugWorldService() {
        throw new UnsupportedOperationException();
    }

    public static List<DebugWorldSnapshot> collectSnapshots() {
        List<DebugWorldSnapshot> snapshots = new ArrayList<>();
        for(WorldEntity world : WorldController.allWorlds()){
            OrthoMapData data = world.getMapData();
            int spawnCol = data != null ? data.getSpawnCol() : 0;
            int spawnRow = data != null ? data.getSpawnRow() : 0;
            snapshots.add(new DebugWorldSnapshot(
                    world.getId(),
                    world.getDisplayName(),
                    world.getMapPath(),
                    world.isActive(),
                    world.isLoaded(),
                    world.getEntities().size(),
                    spawnCol,
                    spawnRow));
        }
        return snapshots;
    }

    public static String listWorlds() {
        List<DebugWorldSnapshot> snapshots = collectSnapshots();
        if(snapshots.isEmpty()){
            return "No worlds registered";
        }
        StringBuilder builder = new StringBuilder("Worlds:").append(System.lineSeparator());
        for(DebugWorldSnapshot snapshot : snapshots){
            builder.append(snapshot.listLabel()).append(System.lineSeparator());
            builder.append("    map=").append(snapshot.mapPath)
                    .append(" spawn=[").append(snapshot.spawnCol).append(',')
                    .append(snapshot.spawnRow).append(']')
                    .append(System.lineSeparator());
        }
        WorldEntity active = WorldController.getActive();
        builder.append("active=").append(active != null ? active.getId() : "none");
        return builder.toString().trim();
    }

    /** Move active player into {@code worldId} (door-style travel). */
    public static String travelWithActiveEntity(String worldId, Integer spawnCol, Integer spawnRow) {
        if(worldId == null || worldId.isBlank()){
            return "Usage: world travel <id> [col row]";
        }
        WorldEntity target = WorldController.get(worldId);
        if(target == null){
            return "Unknown world: " + worldId + ". Use `worlds`";
        }
        Entity traveler = GlobalWorld.player;
        if(traveler == null){
            return "No active entity to travel";
        }
        WorldEntity active = WorldController.getActive();
        if(active != null && active.getId().equals(target.getId())){
            return "Already in " + target.getId();
        }
        OrthoMapData data = target.getMapData();
        int col = spawnCol != null ? spawnCol : (data != null ? data.getSpawnCol() : 0);
        int row = spawnRow != null ? spawnRow : (data != null ? data.getSpawnRow() : 0);
        WorldController.travel(target.getId(), traveler, col, row);
        return "Traveled active entity " + traveler.getTag() + " → " + target.getId()
                + " @[" + col + "," + row + "]";
    }

    /** Activate {@code worldId} without moving any entity (player stays parked in previous world). */
    public static String switchWithoutEntity(String worldId) {
        if(worldId == null || worldId.isBlank()){
            return "Usage: world switch <id>";
        }
        WorldEntity target = WorldController.get(worldId);
        if(target == null){
            return "Unknown world: " + worldId + ". Use `worlds`";
        }
        WorldEntity active = WorldController.getActive();
        if(active != null && active.getId().equals(target.getId())){
            return "Already active: " + target.getId();
        }
        WorldController.switchActive(target.getId());
        Entity player = GlobalWorld.player;
        return "Switched to " + target.getId() + " (no entity travel)"
                + (player != null ? "; control=" + player.getTag() : "; no playable in world");
    }

    /** @deprecated use {@link #travelWithActiveEntity(String, Integer, Integer)} */
    @Deprecated
    public static String travelTo(String worldId, Integer spawnCol, Integer spawnRow) {
        return travelWithActiveEntity(worldId, spawnCol, spawnRow);
    }
}
