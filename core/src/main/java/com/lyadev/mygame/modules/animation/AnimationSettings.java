package com.lyadev.mygame.modules.animation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class AnimationSettings {
    private final AnimationClip idle;
    private final AnimationClip walk;
    private final AnimationClip run;
    private final int scaleFactor;

    public AnimationClip clipById(String clipId) {
        if(idle.getId().equals(clipId)){
            return idle;
        }
        if(walk.getId().equals(clipId)){
            return walk;
        }
        if(run.getId().equals(clipId)){
            return run;
        }
        return idle;
    }
}
