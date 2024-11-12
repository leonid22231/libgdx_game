package com.lyadev.mygame.utils.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.lyadev.mygame.entities.PlayebleEntity;
import com.lyadev.mygame.enums.EntityType;
import com.lyadev.mygame.utils.CircleSector;
import com.lyadev.mygame.utils.LineFromRect;
import com.lyadev.mygame.utils.Point;
import com.lyadev.mygame.utils.Position;
import com.lyadev.mygame.world.GlobalWorld;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntityListenerThread extends Thread {
    private long startTime = TimeUtils.millis();
    private long errorTime = 0;
    private long runningTime = 0;
    private boolean active = true;
    private EntityType entityType;
    private Object entity;
    private List<EntityListenerThread> allThreads;
    private LineFromRect[] lines;
    private boolean isInitilize = false;

    @Builder
    public EntityListenerThread(String name) {
        setName(name);
        start();
    }

    int errorCount = 0;

    public void setEntity(Object entity) {
        this.entity = entity;
        entityType = EntityType.findByValue(entity.getClass());
        if (entityType == null) {
            Gdx.app.error(String.format("Thread[%s]", getName()),
                    "Unsupported entity type: " + entity.getClass().getName());
        }
    }

    @Override
    public void run() {
        if (entity == null) {
            Gdx.app.error(String.format("Thread[%s]", getName()), "Entity is not presset");
            active = false;
        }
        while (active && !Thread.currentThread().isInterrupted()) {
            runningTime = (TimeUtils.millis() - startTime) / 1000;
            if (allThreads == null) {
                allThreads = new ArrayList<>();
            } else {
                allThreads.clear();
            }
            for (int i = 0; i < GlobalWorld.listeners.size(); i++) {
                if (GlobalWorld.listeners.get(i).thread != this) {
                    allThreads.add(GlobalWorld.listeners.get(i).thread);
                }
            }
            boolean allThreadReady = true;
            for (EntityListenerThread thread : allThreads) {
                if (!thread.isInitilize) {
                    allThreadReady = false;
                }
            }
            if (entityType == EntityType.PLAYEBLE) {
                PlayebleEntity entity = (PlayebleEntity) this.entity;
                lines = entity.getRectLines();

                Float visionScore = entity.getVisionScore();
                CircleSector circleSector = entity.getCircleSector();

                Position pos = entity.getCenterPosition();
                isInitilize = true;
                if(!allThreadReady){
                    continue;
                }
                for (EntityListenerThread thread : allThreads) {
                    if (thread.isInitilize) {
                        if (thread.isActive()) {
                            LineFromRect[] anotherLine = thread.getLines();
                            if (anotherLine != null) {
                                boolean isEntityVisible = false;
                                List<LineFromRect> _lines = new ArrayList<>();
                                check: for (LineFromRect line : anotherLine) {
                                    if (inCircle(pos.getX(), pos.getY(), visionScore, line.start,
                                            line.end, circleSector)) {
                                        isEntityVisible = true;
                                        _lines.add(line);
                                        continue check;
                                    }
                                }
                                LineFromRect[] visibleLines = new LineFromRect[_lines.size()];
                                for (int i = 0; i < _lines.size(); i++) {
                                    visibleLines[i] = _lines.get(i);
                                }
                                if (isEntityVisible) {
                                    entity.addVisibleObject(thread.entity, visibleLines);
                                } else {
                                    entity.removeVisibleObject(thread.entity);
                                }
                            }
                        }
                    }
                }
            }

            if (lines == null && (errorTime == 0 || (TimeUtils.millis() - errorTime) / 100 > 10)) {
                errorCount++;
                errorTime = TimeUtils.millis();
            }
            if (errorCount > 10) {
                active = false;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    public Boolean isInitilize() {
        return isInitilize;
    }

    public LineFromRect[] getLines() {
        return lines;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public int getRuntimeSeconds() {
        return (int) runningTime;
    }

    public static boolean inCircle(float centerX, float centerY, float radius, Point start, Point end,
            CircleSector circleSector) {
        for (Point point : getPointsOnLine(start, end)) {
            if (calculateDistance(centerX, centerY, point) <= radius) {
                if (!circleSector.inOutSector(point)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static double calculateDistance(float x1, float y1, Point point) {
        return Math.sqrt(Math.pow(point.x - x1, 2) + Math.pow(point.y - y1, 2));
    }

    public static List<Point> getPointsOnLine(Point start, Point end) {
        List<Point> points = new ArrayList<>();

        // Вычисляем разности
        float dx = end.x - start.x;
        float dy = end.y - start.y;

        // Находим количество шагов
        float steps = Math.max(Math.abs(dx), Math.abs(dy));

        // Вычисляем инкременты
        double xIncrement = (double) dx / steps;
        double yIncrement = (double) dy / steps;

        // Генерируем точки
        for (int i = 0; i <= steps; i++) {
            int x = (int) Math.round(start.x + i * xIncrement);
            int y = (int) Math.round(start.y + i * yIncrement);
            points.add(new Point(x, y));
        }

        return points;
    }
}
