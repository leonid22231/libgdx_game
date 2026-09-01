package com.lyadev.mygame.base;

import com.lyadev.mygame.base.ModuleKeyDecl.Trigger;

import lombok.Getter;

/** Dispatched to {@link EntityModule#onKeyBinding(String, ModuleKeyEvent)} after resolve. */
@Getter
public final class ModuleKeyEvent {
    private final String actionId;
    private final Trigger trigger;
    /** Pressed/hold state for {@link Trigger#HOLD} and {@link Trigger#DOWN_UP}. */
    private final boolean hold;
    private final int keycode;

    public ModuleKeyEvent(String actionId, Trigger trigger, boolean hold, int keycode) {
        this.actionId = actionId;
        this.trigger = trigger;
        this.hold = hold;
        this.keycode = keycode;
    }
}
