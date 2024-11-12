package com.lyadev.mygame.utils;

public class ViewPosition {
    Aligment aligment;
    float x;
    float y;
    public ViewPosition(Aligment aligment){
        this.aligment = aligment;
    }
    public ViewPosition(float x, float y){
        this.x = x;
        this.y = y;
    }
    public float getX(){
        return x;
    }
    public float getY(){
        return y;
    }
    
    public Aligment getAligment(){
        return aligment;
    }
}
