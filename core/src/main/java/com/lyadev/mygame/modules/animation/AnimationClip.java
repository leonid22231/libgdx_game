package com.lyadev.mygame.modules.animation;

import com.lyadev.mygame.utils.Size;

import lombok.Getter;

@Getter
public final class AnimationClip {
    private final String id;
    private final String sheetPath;
    private final Size frameSize;
    private final int rows;
    private final int columns;
    private final float fps;
    private final AnimationDirectionMode directionMode;

    public AnimationClip(String id, String sheetPath, Size frameSize, int rows, int columns, float fps) {
        this(id, sheetPath, frameSize, rows, columns, fps, AnimationDirectionMode.FOUR_DIRECTIONS);
    }

    public AnimationClip(
            String id,
            String sheetPath,
            Size frameSize,
            int rows,
            int columns,
            float fps,
            AnimationDirectionMode directionMode) {
        this.id = id;
        this.sheetPath = sheetPath;
        this.frameSize = frameSize;
        this.rows = rows;
        this.columns = columns;
        this.fps = fps;
        this.directionMode = directionMode == null
                ? AnimationDirectionMode.FOUR_DIRECTIONS
                : directionMode;
    }
}
