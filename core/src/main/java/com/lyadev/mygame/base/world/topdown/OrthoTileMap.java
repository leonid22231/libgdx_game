package com.lyadev.mygame.base.world.topdown;

/** Simple orthogonal grid of local tile ids. -1 = empty. */
public final class OrthoTileMap {
    private int width;
    private int height;
    private int[][] tiles;

    public OrthoTileMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new int[height][width];
        for(int row = 0; row < height; row++){
            for(int col = 0; col < width; col++){
                tiles[row][col] = -1;
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int get(int col, int row) {
        if(col < 0 || row < 0 || col >= width || row >= height){
            return -1;
        }
        return tiles[row][col];
    }

    public void set(int col, int row, int localTileId) {
        if(col < 0 || row < 0 || col >= width || row >= height){
            return;
        }
        tiles[row][col] = localTileId;
    }

    /**
     * Grow grid so {@code (col,row)} is inside. Shifts content when expanding past 0.
     *
     * @return {@code {padLeft, padBottom}} applied to existing cell indices
     */
    public int[] expandToInclude(int col, int row, int maxSize) {
        int padLeft = Math.max(0, -col);
        int padBottom = Math.max(0, -row);
        int padRight = Math.max(0, col - (width - 1));
        int padTop = Math.max(0, row - (height - 1));
        if(padLeft == 0 && padBottom == 0 && padRight == 0 && padTop == 0){
            return new int[]{0, 0};
        }
        int newW = width + padLeft + padRight;
        int newH = height + padBottom + padTop;
        if(newW > maxSize || newH > maxSize){
            throw new IllegalArgumentException(
                    "Map would become " + newW + "x" + newH + " (max " + maxSize + ")");
        }
        int[][] next = new int[newH][newW];
        for(int r = 0; r < newH; r++){
            for(int c = 0; c < newW; c++){
                next[r][c] = -1;
            }
        }
        for(int r = 0; r < height; r++){
            System.arraycopy(tiles[r], 0, next[r + padBottom], padLeft, width);
        }
        tiles = next;
        width = newW;
        height = newH;
        return new int[]{padLeft, padBottom};
    }

    public void fill(int localTileId) {
        for(int row = 0; row < height; row++){
            for(int col = 0; col < width; col++){
                tiles[row][col] = localTileId;
            }
        }
    }
}
