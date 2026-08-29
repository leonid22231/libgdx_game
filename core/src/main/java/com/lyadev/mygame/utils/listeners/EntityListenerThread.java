package com.lyadev.mygame.utils.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.entity_modules.VisionModule;
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
    private Object entity;
    private List<EntityListenerThread> allThreads;
    private LineFromRect[] lines;
    private boolean isInitilize = false;

    @Builder
    public EntityListenerThread(String name) {
        setName(name);
        start();
    }

    public void setEntity(Object entity) {
        this.entity = entity;
        if(!(entity instanceof Entity)){
            Gdx.app.error(String.format("Thread[%s]", getName()),
                    "Unsupported entity type: " + entity.getClass().getName());
        }
    }

    @Override
    public void run() {
        if(entity == null){
            Gdx.app.error(String.format("Thread[%s]", getName()), "Entity is not presset");
            active = false;
        }
        while(active && !Thread.currentThread().isInterrupted()){
            runningTime = (TimeUtils.millis() - startTime) / 1000;
            if(allThreads == null){
                allThreads = new ArrayList<>();
            } else {
                allThreads.clear();
            }
            for(int i = 0; i < GlobalWorld.listeners.size(); i++){
                if(GlobalWorld.listeners.get(i).thread != this){
                    allThreads.add(GlobalWorld.listeners.get(i).thread);
                }
            }

            boolean allThreadReady = true;
            for(EntityListenerThread thread : allThreads){
                if(!thread.isInitilize){
                    allThreadReady = false;
                }
            }

            if(entity instanceof Entity){
                Entity observer = (Entity) entity;
                lines = observer.getVision().getRectLines();
                isInitilize = true;

                if(GlobalWorld.isReady() && observer.getStatus().isActive() && allThreadReady){
                    VisionModule visionModule = VisionModule.from(observer);
                    if(visionModule != null){
                        float visionScore = visionModule.getVisionScore();
                        CircleSector circleSector = visionModule.getCircleSector();
                        Position pos = observer.getCenterPosition();

                        for(EntityListenerThread thread : allThreads){
                            if(thread.isInitilize && thread.isActive()){
                                LineFromRect[] anotherLines = thread.getLines();
                                if(anotherLines == null || !(thread.entity instanceof Entity)){
                                    continue;
                                }
                                Entity target = (Entity) thread.entity;
                                if(target == observer){
                                    continue;
                                }

                                boolean isEntityVisible = false;
                                List<LineFromRect> spottedLines = new ArrayList<>();
                                check:for(LineFromRect line : anotherLines){
                                    if(inCircle(pos.getX(), pos.getY(), visionScore, line.start, line.end, circleSector)){
                                        isEntityVisible = true;
                                        spottedLines.add(line);
                                        continue check;
                                    }
                                }
                                LineFromRect[] visibleLines = spottedLines.toArray(new LineFromRect[0]);
                                if(isEntityVisible){
                                    observer.getVision().addVisibleEntity(target, visibleLines);
                                } else {
                                    observer.getVision().removeVisibleEntity(target);
                                }
                            }
                        }
                    }
                }
            }

            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch(InterruptedException e){
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
        for(Point point : getPointsOnLine(start, end)){
            if(calculateDistance(centerX, centerY, point) <= radius){
                if(!circleSector.inOutSector(point)){
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

        float dx = end.x - start.x;
        float dy = end.y - start.y;
        float steps = Math.max(Math.abs(dx), Math.abs(dy));
        double xIncrement = (double) dx / steps;
        double yIncrement = (double) dy / steps;

        for(int i = 0; i <= steps; i++){
            int x = (int) Math.round(start.x + i * xIncrement);
            int y = (int) Math.round(start.y + i * yIncrement);
            points.add(new Point(x, y));
        }

        return points;
    }
}
