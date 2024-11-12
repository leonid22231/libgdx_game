package com.lyadev.mygame.base;

import java.util.ArrayList;
import java.util.List;

import com.lyadev.mygame.utils.LineFromRect;
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
    private List<Object> visibleObjects = new ArrayList<Object>();
    private boolean[] visibleLines = { false, false, false, false };
    private LineFromRect[] linesFromRect = new LineFromRect[4];
    private EntityListener listener;
}
