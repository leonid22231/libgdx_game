package com.lyadev.mygame.modules.vision;

import com.lyadev.mygame.base.entity.Entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Observer entity увидела target entity. */
@Getter
@RequiredArgsConstructor
public final class EntitySpottedEvent {
    private final Entity observer;
    private final Entity target;
}
