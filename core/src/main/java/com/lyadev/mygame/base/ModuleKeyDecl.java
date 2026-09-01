package com.lyadev.mygame.base;

import lombok.Getter;

/**
 * Declarative key action — like an entry in {@link EntityModule#getRequiredModules()}.
 * Implementation lives in {@link EntityModule#onKeyBinding(String, ModuleKeyEvent)}.
 */
@Getter
public final class ModuleKeyDecl {
    public enum Trigger {
        DOWN,
        UP,
        DOWN_UP,
        /** Fires when pressed-state changes; see {@link ModuleKeyEvent#isHold()}. */
        HOLD
    }

    private final String id;
    private final ModuleKeyChord chord;
    private final Trigger trigger;
    private final String description;

    private ModuleKeyDecl(String id, ModuleKeyChord chord, Trigger trigger, String description) {
        this.id = id;
        this.chord = chord;
        this.trigger = trigger;
        this.description = description;
    }

    public static ModuleKeyDecl down(String id, ModuleKeyChord chord, String description) {
        return new ModuleKeyDecl(id, chord, Trigger.DOWN, description);
    }

    public static ModuleKeyDecl up(String id, ModuleKeyChord chord, String description) {
        return new ModuleKeyDecl(id, chord, Trigger.UP, description);
    }

    public static ModuleKeyDecl downUp(String id, ModuleKeyChord chord, String description) {
        return new ModuleKeyDecl(id, chord, Trigger.DOWN_UP, description);
    }

    public static ModuleKeyDecl hold(String id, ModuleKeyChord chord, String description) {
        return new ModuleKeyDecl(id, chord, Trigger.HOLD, description);
    }
}
