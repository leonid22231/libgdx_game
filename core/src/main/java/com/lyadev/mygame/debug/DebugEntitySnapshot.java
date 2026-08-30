package com.lyadev.mygame.debug;

import java.util.ArrayList;
import java.util.List;

public class DebugEntitySnapshot {
    public final String tag;
    public final String displayTag;
    public final float x;
    public final float y;
    public final int width;
    public final int height;
    public final boolean active;
    public final boolean focused;
    public final boolean show;
    public final boolean actorVisible;
    public final boolean init;
    public final int visibleEntities;
    public final int spriteIndex;
    public final boolean currentPlayer;
    public final List<DebugModuleSnapshot> modules;

    public DebugEntitySnapshot(
            String tag,
            String displayTag,
            float x,
            float y,
            int width,
            int height,
            boolean active,
            boolean focused,
            boolean show,
            boolean actorVisible,
            boolean init,
            int visibleEntities,
            int spriteIndex,
            boolean currentPlayer,
            List<DebugModuleSnapshot> modules) {
        this.tag = tag;
        this.displayTag = displayTag;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.active = active;
        this.focused = focused;
        this.show = show;
        this.actorVisible = actorVisible;
        this.init = init;
        this.visibleEntities = visibleEntities;
        this.spriteIndex = spriteIndex;
        this.currentPlayer = currentPlayer;
        this.modules = modules == null ? List.of() : new ArrayList<>(modules);
    }

    public String listLabel() {
        String marker = currentPlayer ? " *" : "";
        String state = active ? "active" : "idle";
        return tag + marker + " [" + state + "]";
    }
}
