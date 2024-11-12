package com.lyadev.mygame.player;

import com.lyadev.mygame.base.EntitySettings;
import com.lyadev.mygame.utils.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerSettings extends EntitySettings{
    public PlayerSettings(String tag, String texture, Size textureSize) {
        super(tag, texture, textureSize);
    }
}
