package com.lyadev.mygame.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.lyadev.mygame.UI;
import com.lyadev.mygame.enums.EntityType;
import com.lyadev.mygame.enums.MoveType;
import com.lyadev.mygame.models.MoveEventSetting;
import com.lyadev.mygame.models.PlayerSettings;
import com.lyadev.mygame.utils.CircleSector;
import com.lyadev.mygame.utils.LineFromRect;
import com.lyadev.mygame.utils.Point;
import com.lyadev.mygame.utils.Position;
import com.lyadev.mygame.utils.listeners.EntityListener;
import com.lyadev.mygame.utils.listeners.EntityListenerThread;

public class PlayebleEntity extends Actor {
    private PlayerSettings settings; //DONE
    private boolean isActive = false;
    private float positionX = 0;
    private float positionY = 0;
    private float speed = 0;
    private Boolean isFocused = false; //DONE
    private float height; //DONE
    private float width; //DONE
    private Boolean moveLeft = false;
    private Boolean moveRight = false;
    private Boolean moveUp = false;
    private Boolean moveDown = false;
    private int regions = 0; //DONE
    private MoveType lastMoveType = MoveType.DOWN;
    private Boolean isSprinting = false;
    private int scaleFactor = 4; //DONE
    private float visionScore = 0; 
    private boolean showLines = false; 
    private float mainDegrees = 90; 
    private boolean isVisible = false;
    private boolean drawDebugLines = false;
    private List<Object> visibleObjects = new ArrayList<Object>(); //DONE
    private boolean[] visibleLines = { false, false, false, false }; //DONE
    private float currentDegrees1 = 0;
    private float currentDegrees2 = 0;
    LineFromRect[] linesFromRect = new LineFromRect[4];
    EntityListener listener;
    List<TextureRegion> allTextureRegions = new ArrayList<TextureRegion>(); //DONE

    public void drawDebugLinesToggle() {
        drawDebugLines = !drawDebugLines;
    }

    public PlayebleEntity(PlayerSettings settings) {
        this.settings = settings;
        visionScore = settings.visibleRadius;
        setupTexture();
        EntityListenerThread thread = new EntityListenerThread(TAG());
        thread.setEntity(this);
        listener = new EntityListener(
                this,
                thread);
        //GlobalEntity.entityListeners.add(listener);
    }

    public LineFromRect[] getRectLines() {
        return linesFromRect;
    }

    public void setVisibleLines(LineFromRect[] list) {
        List<Integer> visibleIndexes = new ArrayList<Integer>();
        for (int i = 0; i < list.length; i++) {
            int index = indexOf(linesFromRect, list[i]);
            if (index != -1) {
                visibleIndexes.add(index);
                visibleLines[index] = true;
            }
        }
        for (int i = 0; i < visibleLines.length; i++) {
            boolean isVisible = false;
            for (int j = 0; j < visibleIndexes.size(); j++) {
                if (i == visibleIndexes.get(j) && visibleLines[i]) {
                    isVisible = true;
                }
            }
            if (!isVisible) {
                visibleLines[i] = false;
            }
        }
    }

    public void removeVisibleLine() {
        for (int i = 0; i < visibleLines.length; i++) {
            visibleLines[i] = false;
        }
    }

    public static int indexOf(Object[] array, Object key) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == key) {
                return i; // Return the index if found
            }
        }
        return -1; // Return -1 if not found
    }

    public void addVisibleObject(Object object, LineFromRect[] visibleLines) {
        if (!objectIsVisible(object)) {
            visibleObjects.add(object);
        }
        if (object.getClass() == EntityType.PLAYEBLE.getRefClass() && isActive) {
            PlayebleEntity playebleEntity = (PlayebleEntity) object;
            playebleEntity.setVisible(true);
        }

        if (object.getClass() == EntityType.PLAYEBLE.getRefClass() && isActive) {
            PlayebleEntity playebleEntity = (PlayebleEntity) object;
            playebleEntity.setVisibleLines(visibleLines);
        }
    }

    public void removeVisibleObject(Object object) {
        if (objectIsVisible(object)) {
            if (object.getClass() == EntityType.PLAYEBLE.getRefClass()) {
                PlayebleEntity playebleEntity = (PlayebleEntity) object;
                playebleEntity.setVisible(false);
                playebleEntity.removeVisibleLine();
            }
            visibleObjects.remove(object);
        }
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public Boolean objectIsVisible(Object object) {
        return visibleObjects.contains(object);
    }

    void updateLinesFromRect() {
        linesFromRect[0] = new LineFromRect(new Point(positionX, positionY), new Point((positionX + width), positionY));
        linesFromRect[1] = new LineFromRect(new Point((positionX + width), positionY),
                new Point((positionX + width), (positionY + height)));
        linesFromRect[2] = new LineFromRect(new Point(positionX, (positionY + height)),
                new Point((positionX + width), (positionY + height)));
        linesFromRect[3] = new LineFromRect(new Point(positionX, positionY),
                new Point(positionX, (positionY + height)));
    }

    public Position getCenterPosition() {
        return new Position(positionX + width / 2, positionY + height / 2);
    }

    public Float getVisionScore() {
        return visionScore;
    }

    public Float getVisionRadius() {
        return mainDegrees;
    }

    void setupTexture() {
        int regionHeight = 32;
        int regionWidth = 16;
        TextureRegion region = new TextureRegion(new Texture(settings.texture));
        regions = region.getRegionWidth() / regionWidth;

        for (int i = 0; i < regions; i++) {
            region = new TextureRegion(new Texture(settings.texture));
            region.setRegion(i * regionWidth, 0, regionWidth, regionHeight);
            Texture texture = region.getTexture();
            if (!texture.getTextureData().isPrepared()) {
                texture.getTextureData().prepare();
            }
            Pixmap pixmap = texture.getTextureData().consumePixmap();

            int minHeight = regionHeight;
            Color colorNull = new Color(0, 0, 0, 0);
            column: for (int j = 0; j < regionWidth; j++) {
                for (int k = 0; k < regionHeight; k++) {
                    if (!getColorAsPixmap(pixmap, j, k).equals(colorNull)) {
                        if (minHeight > k) {
                            minHeight = k;
                            continue column;
                        }
                    }
                }
            }
            height = regionHeight - minHeight;
            width = regionWidth;
            region.setRegion(i * regionWidth, minHeight, regionWidth, regionHeight - minHeight);
            allTextureRegions.add(region);
        }
        height = height * scaleFactor;
        width = width * scaleFactor;

    }

    Color getColorAsPixmap(Pixmap pixmap, int x, int y) {
        return new Color(pixmap.getPixel(x, y));
    }

    public void mousePositionListener(float mousePositionX, float mousePositionY) {
        if (mousePositionY > getOriginalY() - height && mousePositionY < getOriginalY()
                && mousePositionX < positionX + width && mousePositionX > positionX) {
            setFocused(true);
        } else {
            setFocused(false);
        }
    }

    public void clickEvent() {
        if (isFocused) {
            setActive(true);
        } else {
            setActive(false);
        }
    }

    public void setActive(boolean active) {
        if (active) {
            //GlobalEntity.currentPlayebleEntity = this;
        } else {
            stopMoving();
        }
        isActive = active;
    }

    public void stopMoving() {
        moveLeft = false;
        moveRight = false;
        moveUp = false;
        moveDown = false;
    }

    public void setFocused(boolean focused) {
        isFocused = focused;
    }

    public float getOriginalY() {
        return Gdx.graphics.getHeight() - positionY;
    }

    public void setRandomPosition() {
        Random random = new Random();

        positionX = width + random.nextFloat() * ((Gdx.graphics.getWidth() - width) - width);
        positionY = height + random.nextFloat() * ((Gdx.graphics.getHeight() - height) - height);
    }

    public void moveLeftToggle() {
        moveLeft = !moveLeft;
    }

    public void moveRightToggle() {
        moveRight = !moveRight;
    }

    public void moveUpToggle() {
        moveUp = !moveUp;
    }

    public void moveDownToggle() {
        moveDown = !moveDown;
    }

    public void sprintToggle() {
        isSprinting = !isSprinting;
    }

    String TAG() {
        return String.format("Player[%s]", settings.tag);
    }

    public Boolean isActive() {
        return isActive;
    }

    @Override
    public String toString() {
        return String.format(
                "%s x[%.3f], y[%.3f], h[%s], w[%s], active[%s], focused[%s], sprite[%s], visibleObjects[%s]",
                TAG(), positionX,
                getOriginalY(), height, width, isActive, isFocused, getCurrentSprite(), visibleObjects.size());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        move();
        if (isVisible || isActive) {
            batch.draw(allTextureRegions.get(getCurrentSprite()), positionX, positionY, width, height);
        }
        updateLinesFromRect();
        batch.end();
        drawVision();
        if (drawDebugLines) {
            drawContur();
            drawArrowFromCenter();
        }
        batch.begin();
    }

    int getCurrentSprite() {
        MoveEventSetting[] move = settings.moveSettings.moveEvents;
        for (MoveEventSetting event : move) {
            if (event.getType() == lastMoveType) {
                return event.getSpriteNumber();
            }
        }
        return move[0].getSpriteNumber();
    }

    public CircleSector getCircleSector() {
        return new CircleSector(getCenterPosition().getX(), getCenterPosition().getY(), visionScore, currentDegrees1,
                currentDegrees2);
    }

    void drawVision() {
        float deg = mainDegrees / 2;

        float degrees1;
        float degrees2;
        float round = 180 - deg * 2;
        switch (lastMoveType) {
            case DOWN:
                degrees1 = (90 - deg) * -1;
                degrees2 = (90 + deg) * -1;
                break;
            case UP:
                degrees1 = (90 + deg);
                degrees2 = (90 - deg);
                break;
            case LEFT:
                degrees1 = (deg + round) * -1;
                degrees2 = deg + round;
                break;
            case RIGHT:
                degrees1 = deg;
                degrees2 = deg * -1;
                break;
            default:
                degrees1 = deg;
                degrees2 = deg * -1;
                break;
        }
        Position centerPosition = getCenterPosition();

        double x = centerPosition.getX() + visionScore * Math.cos(Math.toRadians(degrees1));
        double y = centerPosition.getY() + visionScore * Math.sin(Math.toRadians(degrees1));
        double x1 = centerPosition.getX() + visionScore * Math.cos(Math.toRadians(degrees2));
        double y1 = centerPosition.getY() + visionScore * Math.sin(Math.toRadians(degrees2));
        float test1 = (float) normalize360(degrees1);
        float test2 = (float) normalize360(degrees2);

        currentDegrees1 = degrees1;
        currentDegrees2 = degrees2;
        // currentDegrees1 = (float)Math.toRadians(currentDegrees1);
        // currentDegrees2 = (float)Math.toRadians(currentDegrees2);
        if (drawDebugLines) {
            drawCircle(visionScore);
            drawLine((float) x, (float) y, (float) x1, (float) y1);
        }

    }

    public static float normalize360(float angle) {
        angle = angle % 360;
        if (angle < 0) {
            angle = angle + 360;
        }
        return angle;
    }

    void drawContur() {
        UI.shape.begin(ShapeType.Line);
        Color color;
        for (int i = 0; i < linesFromRect.length; i++) {
            color = new Color(0, 1, 0, 1);
            if (visibleLines[i]) {
                color = new Color(1, 0, 0, 1);
            }
            UI.shape.setColor(color);
            UI.shape.line(linesFromRect[i].start.x, linesFromRect[i].start.y, linesFromRect[i].end.x,
                    linesFromRect[i].end.y);
        }
        UI.shape.end();
    }

    void drawCircle(float radius) {
        // DRAW CIRCLE
        UI.shape.begin(ShapeType.Line);
        UI.shape.setColor(0, 0, 1, 1);
        UI.shape.circle(getCenterPosition().getX(), getCenterPosition().getY(), radius);
        UI.shape.end();

        if (showLines) {
            // SET LINE WIDTH
            Gdx.gl.glLineWidth(2);
            // DRAW LINES HORIZONTAL
            UI.shape.begin(ShapeType.Line);
            UI.shape.setColor(0, 1, 0, 1);
            UI.shape.line(getCenterPosition().getX(), getCenterPosition().getY() - radius, getCenterPosition().getX(),
                    getCenterPosition().getY() + radius);
            UI.shape.end();

            // DRAW LINES VERTICAL
            UI.shape.begin(ShapeType.Line);
            UI.shape.setColor(0, 1, 0, 1);
            UI.shape.line(getCenterPosition().getX() - radius, getCenterPosition().getY(),
                    getCenterPosition().getX() + radius, getCenterPosition().getY());
            UI.shape.end();
            Gdx.gl.glLineWidth(1);
        }

    }

    void drawArrowFromCenter() {
        float deg = 0;
        switch (lastMoveType) {
            case DOWN:
                deg = -90;
                break;
            case UP:
                deg = 90;
                break;
            case LEFT:
                deg = 180;
                break;
            case RIGHT:
                deg = 0;
                break;
            default:
                deg = 0;
                break;
        }
        double x = getCenterPosition().getX() + visionScore * Math.cos(Math.toRadians(deg));
        double y = getCenterPosition().getY() + visionScore * Math.sin(Math.toRadians(deg));

        drawSingleLineFromCenter((float) x, (float) y, Color.GREEN);

        int rad = 50;
        float deg1 = 45;
        float degrees1;
        float degrees2;
        float round = 180 - deg * 2;
        switch (lastMoveType) {
            case DOWN:
                degrees1 = (90 - deg1) * -1;
                degrees2 = (90 + deg1) * -1;
                degrees1 = 180 + degrees1;
                degrees2 = 180 + degrees2;
                break;
            case UP:
                degrees1 = (90 + deg1);
                degrees2 = (90 - deg1);
                degrees1 = 180 + degrees1;
                degrees2 = 180 + degrees2;
                break;
            case LEFT:
                degrees1 = (deg1 + round) * -1;
                degrees2 = (deg1 + round);
                degrees1 = 180 - degrees1;
                degrees2 = 180 - degrees2;
                break;
            case RIGHT:
                degrees1 = deg1;
                degrees2 = deg1 * -1;
                degrees1 = 180 - degrees1;
                degrees2 = 180 - degrees2;
                break;
            default:
                degrees1 = deg1;
                degrees2 = deg1 * -1;
                break;
        }
        double x1 = x + rad * Math.cos(Math.toRadians(degrees1));
        double y1 = y + rad * Math.sin(Math.toRadians(degrees1));

        double x2 = x + rad * Math.cos(Math.toRadians(degrees2));
        double y2 = y + rad * Math.sin(Math.toRadians(degrees2));

        UI.shape.begin(ShapeType.Filled);
        UI.shape.setColor(Color.GREEN);
        UI.shape.triangle((float) x, (float) y, (float) x1, (float) y1, (float) x2, (float) y2);
        UI.shape.end();
    }

    void drawSingleLineFromCenter(float x, float y) {
        UI.shape.begin(ShapeType.Line);
        UI.shape.setColor(1, 0, 0, 1);
        UI.shape.line(getCenterPosition().getX(), getCenterPosition().getY(), x, y);
        UI.shape.end();
    }

    void drawSingleLineFromCenter(float x, float y, Color color) {
        Gdx.gl.glLineWidth(2);
        UI.shape.begin(ShapeType.Line);
        UI.shape.setColor(color);
        UI.shape.line(getCenterPosition().getX(), getCenterPosition().getY(), x, y);
        UI.shape.end();
        Gdx.gl.glLineWidth(1);
    }

    void drawLine(float x, float y, float x1, float y1) {
        UI.shape.begin(ShapeType.Line);
        UI.shape.setColor(1, 0, 0, 1);
        UI.shape.line(getCenterPosition().getX(), getCenterPosition().getY(), x, y);
        UI.shape.line(getCenterPosition().getX(), getCenterPosition().getY(), x1, y1);
        UI.shape.end();
    }

    void move() {
        if (isSprinting) {
            speed = settings.defaultSprintSpeed;
        } else {
            speed = settings.defaultWalkSpeed;
        }
        if (moveLeft) {
            positionX -= Gdx.graphics.getDeltaTime() * speed;
            lastMoveType = MoveType.LEFT;
        }
        if (moveRight) {
            positionX += Gdx.graphics.getDeltaTime() * speed;
            lastMoveType = MoveType.RIGHT;
        }
        if (moveUp) {
            positionY += Gdx.graphics.getDeltaTime() * speed;
            lastMoveType = MoveType.UP;
        }
        if (moveDown) {
            positionY -= Gdx.graphics.getDeltaTime() * speed;
            lastMoveType = MoveType.DOWN;
        }
    }

    public void dispose() {
        listener.dispose();
    }
}
