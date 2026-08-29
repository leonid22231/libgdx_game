package com.lyadev.mygame.base;

import com.lyadev.mygame.utils.listeners.EntityListener;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class EntityStatus{
    private boolean isActive = false;
    private Boolean isFocused = false;
    private Boolean isShow = false;
    private Boolean isInit = false;
    private EntityListener listener;
}
