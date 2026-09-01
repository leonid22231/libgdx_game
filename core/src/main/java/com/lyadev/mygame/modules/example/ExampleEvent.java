package com.lyadev.mygame.modules.example;

import com.lyadev.mygame.base.entity.Entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Пример события, которое публикует {@link ExampleModule}. */
@Getter
@RequiredArgsConstructor
public final class ExampleEvent {
    private final Entity entity;
    private final String message;
}
