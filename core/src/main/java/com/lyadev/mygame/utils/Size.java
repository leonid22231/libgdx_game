package com.lyadev.mygame.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Size {
    //Note: The default sizes
    public static final Size ENTITY_DEFAULT = new Size(16, 32);
    public static final Size BLOCK_DEFAULT = new Size(32, 32);
    public static final Size NULL = new Size(0, 0);
    private int width;
    private int height;

    public Size getSizeFromScaleFactor(int scaleFactor) {
        return getSizeFromScaleFactor((float) scaleFactor);
    }

    public Size getSizeFromScaleFactor(float scaleFactor) {
        return new Size(
                Math.max(1, Math.round(width * scaleFactor)),
                Math.max(1, Math.round(height * scaleFactor)));
    }

    @Override
    public String toString() {
        return "(" + width + ", " + height + ")";
    }
}
