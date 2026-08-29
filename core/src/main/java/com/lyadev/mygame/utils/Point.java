package com.lyadev.mygame.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Point{
    public float x;
    public float y;
    public Point(float x, float y){
        this.x = x;
        this.y = y;
    }
}
