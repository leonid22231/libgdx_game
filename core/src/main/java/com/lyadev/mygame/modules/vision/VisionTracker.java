package com.lyadev.mygame.modules.vision;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.services.MainService;
import com.lyadev.mygame.utils.LineFromRect;
import com.lyadev.mygame.utils.Point;

/** Runtime-state контура и списка видимых entity. Без cross-module вызовов. */
final class VisionTracker {
    private final Entity entity;
    private final List<Entity> visibleEntities = new ArrayList<>();
    private final boolean[] visibleLines = { false, false, false, false };
    private final LineFromRect[] linesFromRect = new LineFromRect[4];
    private boolean initialized = false;

    VisionTracker(Entity entity) {
        this.entity = entity;
    }

    void updateContour() {
        updateRectLines();
        initialized = true;
    }

    LineFromRect[] getRectLines() {
        return linesFromRect;
    }

    int getVisibleEntityCount() {
        return visibleEntities.size();
    }

    void applySpottedLines(LineFromRect[] lines) {
        setVisibleLines(lines);
    }

    void clearSpottedLines() {
        clearVisibleLines();
    }

    void rememberVisible(Entity target) {
        if(!visibleEntities.contains(target)){
            visibleEntities.add(target);
        }
    }

    boolean isTrackingVisible(Entity target) {
        return visibleEntities.contains(target);
    }

    void forgetVisible(Entity target) {
        visibleEntities.remove(target);
    }

    void resetVisionTracking() {
        visibleEntities.clear();
        clearVisibleLines();
    }

    List<Entity> snapshotVisibleEntities() {
        return new ArrayList<>(visibleEntities);
    }

    void drawContour() {
        if(!initialized){
            return;
        }
        MainService.getInstance().getShapeRenderer().begin(ShapeType.Line);
        for(int i = 0; i < linesFromRect.length; i++){
            Color color = visibleLines[i] ? Color.RED : Color.GREEN;
            MainService.getInstance().getShapeRenderer().setColor(color);
            MainService.getInstance().getShapeRenderer().line(
                    linesFromRect[i].start.x,
                    linesFromRect[i].start.y,
                    linesFromRect[i].end.x,
                    linesFromRect[i].end.y);
        }
        MainService.getInstance().getShapeRenderer().end();
    }

    private void setVisibleLines(LineFromRect[] lines) {
        List<Integer> visibleIndexes = new ArrayList<>();
        for(int i = 0; i < lines.length; i++){
            int index = indexOf(linesFromRect, lines[i]);
            if(index != -1){
                visibleIndexes.add(index);
                visibleLines[index] = true;
            }
        }
        for(int i = 0; i < visibleLines.length; i++){
            boolean lineVisible = false;
            for(Integer visibleIndex : visibleIndexes){
                if(i == visibleIndex && visibleLines[i]){
                    lineVisible = true;
                }
            }
            if(!lineVisible){
                visibleLines[i] = false;
            }
        }
    }

    private void clearVisibleLines() {
        for(int i = 0; i < visibleLines.length; i++){
            visibleLines[i] = false;
        }
    }

    private void updateRectLines() {
        float x = entity.getX();
        float y = entity.getY();
        float width = entity.getWidth();
        float height = entity.getHeight();

        linesFromRect[0] = new LineFromRect(new Point(x, y), new Point(x + width, y));
        linesFromRect[1] = new LineFromRect(new Point(x + width, y), new Point(x + width, y + height));
        linesFromRect[2] = new LineFromRect(new Point(x, y + height), new Point(x + width, y + height));
        linesFromRect[3] = new LineFromRect(new Point(x, y), new Point(x, y + height));
    }

    private static int indexOf(Object[] array, Object key) {
        for(int i = 0; i < array.length; i++){
            if(array[i] == key){
                return i;
            }
        }
        return -1;
    }
}
