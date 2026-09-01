package com.lyadev.mygame.debug;

/** Snapshot of a registered {@link com.lyadev.mygame.world.WorldEntity} for the debug console. */
public final class DebugWorldSnapshot {
    public final String id;
    public final String displayName;
    public final String mapPath;
    public final boolean active;
    public final boolean loaded;
    public final int entityCount;
    public final int spawnCol;
    public final int spawnRow;

    public DebugWorldSnapshot(
            String id,
            String displayName,
            String mapPath,
            boolean active,
            boolean loaded,
            int entityCount,
            int spawnCol,
            int spawnRow) {
        this.id = id;
        this.displayName = displayName;
        this.mapPath = mapPath;
        this.active = active;
        this.loaded = loaded;
        this.entityCount = entityCount;
        this.spawnCol = spawnCol;
        this.spawnRow = spawnRow;
    }

    public String listLabel() {
        return (active ? "* " : "  ") + id + " — " + displayName
                + " [" + entityCount + "]"
                + (loaded ? "" : " (not loaded)");
    }
}
