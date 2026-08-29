package com.lyadev.mygame.base;

import com.lyadev.mygame.models.MoveSettings;
import com.lyadev.mygame.utils.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayableEntitySettings extends EntitySettings {
    private MoveSettings moveSettings;
    private int defaultWalkSpeed;
    private int defaultSprintSpeed;
    private float visibleRadius;

    public PlayableEntitySettings(
            String tag,
            String texture,
            Size textureSize,
            int textureScaleFactor,
            MoveSettings moveSettings,
            int defaultWalkSpeed,
            int defaultSprintSpeed,
            float visibleRadius) {
        super(tag, texture, textureSize, textureScaleFactor);
        this.moveSettings = moveSettings;
        this.defaultWalkSpeed = defaultWalkSpeed;
        this.defaultSprintSpeed = defaultSprintSpeed;
        this.visibleRadius = visibleRadius;
    }
}
