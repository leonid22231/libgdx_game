package com.lyadev.mygame.utils;

public class CircleSector {
    float centerX, centerY, radius;
    float angleStart, angleEnd; // Углы в радианах

    public CircleSector(float centerX, float centerY, float radius, float angleStart, float angleEnd) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.angleStart = angleStart;
        this.angleEnd = angleEnd;
    }
    @Override
    public String toString() {
        return String.format("CircleSector[centerX=%s, centerY=%s, radius=%s, angleStart=%s, angleEnd=%s]",
                centerX, centerY, radius, angleStart, angleEnd);
    }
    public boolean inOutSector(Point p) {
        // Преобразование углов в радианы
        double angle1 = Math.toRadians(angleStart);
        double angle2 = Math.toRadians(angleEnd);
        
        // Вычисление угла точки
        double pointAngle = Math.atan2(p.y - centerY, p.x - centerX);

        // Проверка принадлежности угла сектору
        if (angle1 > angle2) { // Сектор охватывает 360 градусов
            return pointAngle >= angle1 || pointAngle <= angle2;
        } else {
            return pointAngle >= angle1 && pointAngle <= angle2;
        }
    }
    public boolean isPointInSector(Point p) {
        // Проверяем расстояние до центра
        float dx = p.x - centerX;
        float dy = p.y - centerY;

        if (dx * dx + dy * dy > radius * radius) {
            return false; // Точка вне окружности
        }

        // Вычисляем угол точки относительно центра сектора
        float angle = (float)Math.atan2(dy, dx); // Угол в радианах

        // Нормализуем углы для сравнения
        if (angle < 0) {
            angle += 2 * Math.PI; // Приводим угол к диапазону [0, 2π]
        }

        // Проверяем принадлежность угла сектору
        if (angleStart <= angle && angle <= angleEnd) {
            return true; // Точка принадлежит сектору
        }

        return false; // Точка не принадлежит сектору
    }
}