package com.lyadev.mygame.modules.animation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class AnimationSettings {
    private final AnimationClip idle;
    private final AnimationClip walk;
    private final AnimationClip run;
    /** 1 = native frame px; 0.25 makes 64×128 art match ~16×32 world characters. */
    private final float scaleFactor;

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

    /** Режим направлений задаётся на клипах (обычно одинаковый у idle/walk/run). */
    public AnimationDirectionMode getDirectionMode() {
        return idle.getDirectionMode();
    }
}
