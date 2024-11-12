package com.lyadev.mygame.models;

public class PlayerSettings {
    public String tag;
    public MoveSettings moveSettings;
    public int defaultWalkSpeed;
    public int defaultSprintSpeed;
    public String texture;
    public Float visibleRadius;
    public PlayerSettings(String tag, MoveSettings moveSettings, int defaultWalkSpeed, int defaultSprintSpeed, String texture, Float visibleRadius) {
        this.tag = tag;
        this.moveSettings = moveSettings;
        this.defaultWalkSpeed = defaultWalkSpeed;
        this.defaultSprintSpeed = defaultSprintSpeed;
        this.texture = texture;
        this.visibleRadius = visibleRadius;
    }
    public PlayerSettings copy(){
        return new PlayerSettings(tag, moveSettings, defaultWalkSpeed, defaultSprintSpeed, texture, visibleRadius);
    }
}
