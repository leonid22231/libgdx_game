package com.lyadev.mygame.modules.vision;

import java.util.ArrayList;
import java.util.List;

import com.lyadev.mygame.utils.CircleSector;
import com.lyadev.mygame.utils.LineFromRect;
import com.lyadev.mygame.utils.Point;
import com.lyadev.mygame.utils.Position;

final class VisionScanner {
    private VisionScanner() {
        throw new UnsupportedOperationException();
    }

    static ScanResult scan(VisionModule observer, VisionModule target) {
        LineFromRect[] targetLines = target.getRectLines();
        if(targetLines == null || targetLines.length == 0){
            return ScanResult.notVisible();
        }
        Position observerCenter = observer.getCenterPosition();
        float radius = observer.getVisionScore();
        CircleSector sector = observer.getCircleSector();

        List<LineFromRect> spottedLines = new ArrayList<>();
        for(LineFromRect line : targetLines){
            if(isLineVisible(observerCenter.getX(), observerCenter.getY(), radius, line, sector)){
                spottedLines.add(line);
            }
        }
        if(spottedLines.isEmpty()){
            return ScanResult.notVisible();
        }
        return ScanResult.visible(spottedLines.toArray(new LineFromRect[0]));
    }

    private static boolean isLineVisible(
            float centerX,
            float centerY,
            float radius,
            LineFromRect line,
            CircleSector sector) {
        for(Point point : pointsOnLine(line.start, line.end)){
            if(distance(centerX, centerY, point) <= radius && !sector.inOutSector(point)){
                return true;
            }
        }
        return false;
    }

    private static double distance(float x1, float y1, Point point) {
        return Math.sqrt(Math.pow(point.x - x1, 2) + Math.pow(point.y - y1, 2));
    }

    private static List<Point> pointsOnLine(Point start, Point end) {
        List<Point> points = new ArrayList<>();
        float dx = end.x - start.x;
        float dy = end.y - start.y;
        float steps = Math.max(Math.abs(dx), Math.abs(dy));
        if(steps == 0){
            points.add(new Point(start.x, start.y));
            return points;
        }
        double xIncrement = dx / steps;
        double yIncrement = dy / steps;
        for(int i = 0; i <= steps; i++){
            points.add(new Point(
                    Math.round(start.x + i * xIncrement),
                    Math.round(start.y + i * yIncrement)));
        }
        return points;
    }

    static final class ScanResult {
        private final boolean visible;
        private final LineFromRect[] spottedLines;

        private ScanResult(boolean visible, LineFromRect[] spottedLines) {
            this.visible = visible;
            this.spottedLines = spottedLines;
        }

        static ScanResult visible(LineFromRect[] lines) {
            return new ScanResult(true, lines);
        }

        static ScanResult notVisible() {
            return new ScanResult(false, new LineFromRect[0]);
        }

        boolean isVisible() {
            return visible;
        }

        LineFromRect[] getSpottedLines() {
            return spottedLines;
        }
    }
}
