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

    private int width;
    private int height;
}
