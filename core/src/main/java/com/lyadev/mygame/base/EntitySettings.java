package com.lyadev.mygame.base;

import com.lyadev.mygame.utils.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EntitySettings {
    private String tag;
    private String texture;
    private Size textureSize;
}
