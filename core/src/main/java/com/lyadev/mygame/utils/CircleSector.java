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

    /**
     * Point inside the wedge between {@code angleStart} and {@code angleEnd} (degrees).
     * Uses shortest angular distance from the mid-angle so left/wrap (±180°) works with {@code atan2}.
     */
    public boolean contains(Point p) {
        double pointAngle = Math.toDegrees(Math.atan2(p.y - centerY, p.x - centerX));
        double mid = (angleStart + angleEnd) * 0.5;
        double halfWidth = Math.abs(angleStart - angleEnd) * 0.5;
        return Math.abs(deltaDegrees(pointAngle - mid)) <= halfWidth;
    }

    private static double deltaDegrees(double degrees) {
        double d = degrees % 360.0;
        if(d > 180.0){
            d -= 360.0;
        } else if(d < -180.0){
            d += 360.0;
        }
        return d;
    }
}
