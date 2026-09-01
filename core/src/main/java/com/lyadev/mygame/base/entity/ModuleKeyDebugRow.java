package com.lyadev.mygame.base.entity;

import lombok.Getter;

/** One row for Debug Console Keys table / `keys` command. */
@Getter
public final class ModuleKeyDebugRow {
    private final String chord;
    private final String actionId;
    private final String owner;
    private final String trigger;
    private final String description;

    public ModuleKeyDebugRow(String chord, String actionId, String owner, String trigger, String description) {
        this.chord = chord;
        this.actionId = actionId;
        this.owner = owner;
        this.trigger = trigger;
        this.description = description;
    }
}
