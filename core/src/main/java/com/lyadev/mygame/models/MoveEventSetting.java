package com.lyadev.mygame.models;

import com.lyadev.mygame.enums.MoveType;

public class MoveEventSetting {
    private MoveType type;
    private int spriteNumber;
    public MoveEventSetting(MoveType type, int spriteNumber) {
        this.type = type;
        this.spriteNumber = spriteNumber;
    }
    public MoveType getType() {
        return type;
    }
    public int getSpriteNumber() {
        return spriteNumber - 1;
    }
}
