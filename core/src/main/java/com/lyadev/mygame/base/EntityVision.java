package com.lyadev.mygame.base;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.lyadev.mygame.entity_modules.SelectableModule;
import com.lyadev.mygame.services.MainService;
import com.lyadev.mygame.utils.LineFromRect;
import com.lyadev.mygame.utils.Point;
import com.lyadev.mygame.utils.Size;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EntityVision {
    private List<Entity> visibleEntities = new ArrayList<>();
    private boolean[] visibleLines = { false, false, false, false };
    private LineFromRect[] linesFromRect = new LineFromRect[4];
    private Entity entity;
    private boolean isInit = false;

    public void init(Entity entity) {
        this.entity = entity;
        update();
    }

    public void update() {
        EntityPosition position = entity.getPosition();
        Size size = entity.getSize();

        updateRectLines(position, size);
        isInit = true;
    }

    public LineFromRect[] getRectLines() {
        return linesFromRect;
    }

    public void setVisibleLines(LineFromRect[] lines) {
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

    public void clearVisibleLines() {
        for(int i = 0; i < visibleLines.length; i++){
            visibleLines[i] = false;
        }
    }

    public void addVisibleEntity(Entity target, LineFromRect[] spottedLines) {
        if(!visibleEntities.contains(target)){
            visibleEntities.add(target);
        }
        if(!entity.getStatus().isActive()){
            return;
        }
        SelectableModule selectable = SelectableModule.from(target);
        if(selectable != null){
            selectable.setVisibleByVision(true);
        }
        target.getVision().setVisibleLines(spottedLines);
    }

    public void removeVisibleEntity(Entity target) {
        if(visibleEntities.contains(target)){
            SelectableModule selectable = SelectableModule.from(target);
            if(selectable != null){
                selectable.setVisibleByVision(false);
            }
            target.getVision().clearVisibleLines();
            visibleEntities.remove(target);
        }
    }

    public void resetVisionTracking() {
        for(Entity target : new ArrayList<>(visibleEntities)){
            removeVisibleEntity(target);
        }
        visibleEntities.clear();
        clearVisibleLines();
    }

    private static int indexOf(Object[] array, Object key) {
        for(int i = 0; i < array.length; i++){
            if(array[i] == key){
                return i;
            }
        }
        return -1;
    }

    private void updateRectLines(EntityPosition position, Size size) {
        linesFromRect[0] = new LineFromRect(new Point(position.getX(), position.getY()), new Point((position.getX() + size.getWidth()), position.getY()));
        linesFromRect[1] = new LineFromRect(new Point((position.getX() + size.getWidth()), position.getY()),
                new Point((position.getX() + size.getWidth()), (position.getY() + size.getHeight())));
        linesFromRect[2] = new LineFromRect(new Point(position.getX(), (position.getY() + size.getHeight())),
                new Point((position.getX() + size.getWidth()), (position.getY() + size.getHeight())));
        linesFromRect[3] = new LineFromRect(new Point(position.getX(), position.getY()),
                new Point(position.getX(), (position.getY() + size.getHeight())));
    }

    public void draw() {
        if(!isInit){
            throw new IllegalStateException("EntityVision not initialized");
        }

        drawContur();
    }

    void drawContur() {
        MainService.getInstance().getShapeRenderer().begin(ShapeType.Line);
        Color color;
        for(int i = 0; i < linesFromRect.length; i++){
            color = new Color(0, 1, 0, 1);
            if(visibleLines[i]){
                color = new Color(1, 0, 0, 1);
            }
            MainService.getInstance().getShapeRenderer().setColor(color);
            MainService.getInstance().getShapeRenderer().line(linesFromRect[i].start.x, linesFromRect[i].start.y, linesFromRect[i].end.x,
                    linesFromRect[i].end.y);
        }
        MainService.getInstance().getShapeRenderer().end();
    }
}
