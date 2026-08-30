package com.lyadev.mygame.debug;

import java.util.ArrayList;
import java.util.List;

public class DebugModuleSnapshot {
    public final String name;
    public final boolean resolved;
    public final boolean runtimePaused;
    public final String disabledReason;
    public final List<String> requiredModules;

    public DebugModuleSnapshot(
            String name,
            boolean resolved,
            boolean runtimePaused,
            String disabledReason,
            List<String> requiredModules) {
        this.name = name;
        this.resolved = resolved;
        this.runtimePaused = runtimePaused;
        this.disabledReason = disabledReason;
        this.requiredModules = requiredModules == null ? List.of() : new ArrayList<>(requiredModules);
    }

    public String stateLabel() {
        if(!resolved){
            return "disabled";
        }
        if(runtimePaused){
            return "paused";
        }
        return "active";
    }
}
