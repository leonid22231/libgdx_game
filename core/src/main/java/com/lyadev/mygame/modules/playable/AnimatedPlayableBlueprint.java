package com.lyadev.mygame.modules.playable;

import com.lyadev.mygame.modules.animation.AnimationSettings;
import com.lyadev.mygame.modules.movement.MovementSettings;
import com.lyadev.mygame.modules.vision.VisionSettings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Blueprint для animated playable (AnimationModule, без SpriteModule). */
@Getter
@RequiredArgsConstructor
public final class AnimatedPlayableBlueprint {
    private final String tag;
    private final AnimationSettings animation;
    private final MovementSettings movement;
    private final VisionSettings vision;

    public AnimatedPlayableBlueprint withTag(String newTag) {
        return new AnimatedPlayableBlueprint(newTag, animation, movement, vision);
    }
}
