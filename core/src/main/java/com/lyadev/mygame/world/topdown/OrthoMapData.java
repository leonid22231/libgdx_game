package com.lyadev.mygame.world.topdown;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

@Getter
public final class OrthoMapData {
    private final String name;
    private final int tileSize;
    private final String tilesetPath;
    private final int tilesetColumns;
    private final String decorAtlas;
    private final OrthoTileMap map;
    private final List<DecorSpec> decor;
    private int spawnCol;
    private int spawnRow;

    public OrthoMapData(
            String name,
            int tileSize,
            String tilesetPath,
            int tilesetColumns,
            String decorAtlas,
            OrthoTileMap map,
            List<DecorSpec> decor,
            int spawnCol,
            int spawnRow) {
        this.name = name;
        this.tileSize = tileSize;
        this.tilesetPath = tilesetPath;
        this.tilesetColumns = tilesetColumns;
        this.decorAtlas = decorAtlas;
        this.map = map;
        this.decor = Collections.unmodifiableList(decor);
        this.spawnCol = spawnCol;
        this.spawnRow = spawnRow;
    }

    public void shiftSpawn(int dCol, int dRow) {
        spawnCol += dCol;
        spawnRow += dRow;
    }

    @Getter
    public static final class DecorSpec {
        private final String assetId;
        private final String atlasPath;
        private final int srcX;
        private final int srcY;
        private final int srcW;
        private final int srcH;
        private final int col;
        private final int row;

        public DecorSpec(
                String assetId,
                String atlasPath,
                int srcX, int srcY, int srcW, int srcH,
                int col, int row) {
            this.assetId = assetId;
            this.atlasPath = atlasPath;
            this.srcX = srcX;
            this.srcY = srcY;
            this.srcW = srcW;
            this.srcH = srcH;
            this.col = col;
            this.row = row;
        }
    }
}
