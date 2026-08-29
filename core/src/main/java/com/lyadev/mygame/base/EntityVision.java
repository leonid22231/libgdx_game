package com.lyadev.mygame.base;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.lyadev.mygame.services.MainService;
import com.lyadev.mygame.utils.LineFromRect;
import com.lyadev.mygame.utils.Point;
import com.lyadev.mygame.utils.Size;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EntityVision {
    private List<Object> visibleObjects = new ArrayList<>();
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
        for (int i = 0; i < linesFromRect.length; i++) {
            color = new Color(0, 1, 0, 1);
            if (visibleLines[i]) {
                color = new Color(1, 0, 0, 1);
            }
            MainService.getInstance().getShapeRenderer().setColor(color);
            MainService.getInstance().getShapeRenderer().line(linesFromRect[i].start.x, linesFromRect[i].start.y, linesFromRect[i].end.x,
                    linesFromRect[i].end.y);
        }
        MainService.getInstance().getShapeRenderer().end();
    }
}
