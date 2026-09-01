package com.lyadev.mygame.world.topdown;

import lombok.Getter;

@Getter
public final class OrthoTileHit {
    private final float worldX;
    private final float worldY;
    private final int col;
    private final int row;
    private final int tileId;
    private final boolean inBounds;

    public OrthoTileHit(float worldX, float worldY, int col, int row, int tileId, boolean inBounds) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.col = col;
        this.row = row;
        this.tileId = tileId;
        this.inBounds = inBounds;
    }
}
