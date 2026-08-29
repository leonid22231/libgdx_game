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
    public static final int SCALEFACTOR_DEFAULT = 1;
    private int width;
    private int height;

    public Size getSizeFromScaleFactor(int scaleFactor) {
        return new Size(width * scaleFactor, height * scaleFactor);
    }

    @Override
    public String toString() {
        return "(" + width + ", " + height + ")";
    }
}
