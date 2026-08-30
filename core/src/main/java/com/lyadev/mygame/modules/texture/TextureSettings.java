package com.lyadev.mygame.modules.texture;

import com.lyadev.mygame.utils.Size;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class TextureSettings {
    private final String texturePath;
    private final Size frameSize;
    private final int scaleFactor;
}
