package com.lyadev.mygame.place;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.lyadev.mygame.services.MainService;
import com.lyadev.mygame.utils.Point;
import com.lyadev.mygame.utils.Position;
import com.lyadev.mygame.utils.Size;

public final class Place extends Actor {
    Size size;
    Position position;
    Size blockSize = Size.BLOCK_DEFAULT;
    Size blocksCount = Size.NULL;
    Position[][] savePositions;
    int scale = 2;

    public Place(Size size) {
        this.size = size;
        init();
    }

    public void init() {
        calculateNormalSize();
        Size displaySize = new Size(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Point displayCenter = new Point(displaySize.getWidth() / 2, displaySize.getHeight() / 2);

        position = new Position(displayCenter.getX() - size.getWidth() / 2,
                displayCenter.getY() - size.getHeight() / 2);
        updatePositions();
    }

    public void calculateNormalSize() {
        int heightCount = Math.max(1, (int) Math.ceil((double) size.getHeight() / blockSize.getHeight()));
        int widthCount = Math.max(1, (int) Math.ceil((double) size.getWidth() / blockSize.getWidth()));

        blocksCount.setHeight(heightCount);
        blocksCount.setWidth(widthCount);
        size.setHeight(blockSize.getHeight() * heightCount * scale);
        size.setWidth(blockSize.getWidth() * widthCount * scale);
        Gdx.app.log("Place", "Size Normal: " + size.toString() + ", BlocksCount: " + blocksCount.toString());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        MainService.getInstance().getShapeRenderer().setColor(Color.BLACK);
        batch.end();

        MainService.getInstance().getShapeRenderer().begin(ShapeType.Line);
        for (int i = 0; i < blocksCount.getHeight(); i++) {
            for (int j = 0; j < blocksCount.getWidth(); j++) {
                drawRec(new Position(savePositions[i][j].getX(), savePositions[i][j].getY()));
            }
        }
        MainService.getInstance().getShapeRenderer().end();
        batch.begin();
        MainService.getInstance().getShapeRenderer().setColor(1, 1, 1, 1);
    }

    public void updatePositions() {
        savePositions = new Position[blocksCount.getHeight()][blocksCount.getWidth()];
        for (int i = 0; i < blocksCount.getHeight(); i++) {
            for (int j = 0; j < blocksCount.getWidth(); j++) {
                savePositions[i][j] = new Position(
                        position.getX() + j * blockSize.getWidth() * scale,
                        position.getY() + i * blockSize.getHeight() * scale);
            }
        }
    }

    private void drawRec(Position position) {
        if (!(position.getX() + blockSize.getWidth() * 4 > 0
                && position.getX() - blockSize.getWidth() * 4 < Gdx.graphics.getWidth()
                && position.getY() + blockSize.getHeight() * 4 > 0
                && position.getY() - blockSize.getHeight() * 4 < Gdx.graphics.getHeight())) {
            return;
        }
        MainService.getInstance().getShapeRenderer().rect(
                position.getX(),
                position.getY(),
                blockSize.getWidth() * scale,
                blockSize.getHeight() * scale);
    }
}
