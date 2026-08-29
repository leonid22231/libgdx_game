package com.lyadev.mygame.utils;

public class CircleSector {
    float centerX, centerY, radius;
    float angleStart, angleEnd;

    public CircleSector(float centerX, float centerY, float radius, float angleStart, float angleEnd) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.angleStart = angleStart;
        this.angleEnd = angleEnd;
    }

    public boolean inOutSector(Point p) {
        double angle1 = Math.toRadians(angleStart);
        double angle2 = Math.toRadians(angleEnd);
        double pointAngle = Math.atan2(p.y - centerY, p.x - centerX);

        if (angle1 > angle2) {
            return pointAngle >= angle1 || pointAngle <= angle2;
        }
        return pointAngle >= angle1 && pointAngle <= angle2;
    }
}
