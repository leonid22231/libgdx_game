package com.lyadev.mygame.modules.animation;

import com.lyadev.mygame.utils.Size;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class AnimationClip {
    private final String id;
    private final String sheetPath;
    private final Size frameSize;
    private final int rows;
    private final int columns;
    private final float fps;
}
