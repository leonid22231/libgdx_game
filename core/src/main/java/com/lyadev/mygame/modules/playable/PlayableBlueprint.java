package com.lyadev.mygame.modules.playable;

import com.lyadev.mygame.modules.movement.MovementSettings;
import com.lyadev.mygame.modules.sprite.SpriteSettings;
import com.lyadev.mygame.modules.texture.TextureSettings;
import com.lyadev.mygame.modules.vision.VisionSettings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Blueprint для сборки playable-сущности. Не хранится на Entity — только при spawn. */
@Getter
@RequiredArgsConstructor
public final class PlayableBlueprint {
    private final String tag;
    private final TextureSettings texture;
    private final SpriteSettings sprite;
    private final MovementSettings movement;
    private final VisionSettings vision;

    public PlayableBlueprint withTag(String newTag) {
        return new PlayableBlueprint(newTag, texture, sprite, movement, vision);
    }
}
