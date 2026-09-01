package com.lyadev.mygame.persons;

import com.lyadev.mygame.enums.MoveType;
import com.lyadev.mygame.models.MoveEventSetting;
import com.lyadev.mygame.models.MoveSettings;
import com.lyadev.mygame.modules.animation.AnimationSettings;
import com.lyadev.mygame.modules.movement.MovementSettings;
import com.lyadev.mygame.modules.playable.AnimatedPlayableBlueprint;
import com.lyadev.mygame.modules.playable.PlayableBlueprint;
import com.lyadev.mygame.modules.sprite.SpriteSettings;
import com.lyadev.mygame.modules.texture.TextureSettings;
import com.lyadev.mygame.modules.vision.VisionSettings;
import com.lyadev.mygame.utils.Size;

/** Сборка {@link PlayableBlueprint} / {@link AnimatedPlayableBlueprint} для persons.* */
public final class PersonBlueprints {
    private PersonBlueprints() {
        throw new UnsupportedOperationException();
    }

    public static PlayableBlueprint staticStrip(String tag, String texturePath, float visionRadius) {
        return new PlayableBlueprint(
                tag,
                new TextureSettings(texturePath, Size.ENTITY_DEFAULT, 1),
                new SpriteSettings(defaultMoveSettings()),
                new MovementSettings(60, 110),
                new VisionSettings(visionRadius));
    }

    public static AnimatedPlayableBlueprint animated(
            String tag,
            AnimationSettings animation,
            int walkSpeed,
            int sprintSpeed,
            float visionRadius) {
        return new AnimatedPlayableBlueprint(
                tag,
                animation,
                new MovementSettings(walkSpeed, sprintSpeed),
                new VisionSettings(visionRadius));
    }

    static MoveSettings defaultMoveSettings() {
        return new MoveSettings(
                new MoveEventSetting(MoveType.UP, 2),
                new MoveEventSetting(MoveType.DOWN, 4),
                new MoveEventSetting(MoveType.LEFT, 3),
                new MoveEventSetting(MoveType.RIGHT, 1));
    }
}
