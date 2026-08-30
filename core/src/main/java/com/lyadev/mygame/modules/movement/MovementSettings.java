package com.lyadev.mygame.modules.movement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class MovementSettings {
    private final int walkSpeed;
    private final int sprintSpeed;
}
