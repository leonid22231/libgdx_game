package com.lyadev.mygame.modules.prop;

import lombok.Getter;

/** Static map prop (tree, rock, …) — crop from atlas. */
@Getter
public final class PropSettings {
    private final String assetId;
    private final String atlasPath;
    private final int srcX;
    private final int srcY;
    private final int srcW;
    private final int srcH;
    private final float scale;

    public PropSettings(String assetId, String atlasPath, int srcX, int srcY, int srcW, int srcH, float scale) {
        this.assetId = assetId;
        this.atlasPath = atlasPath;
        this.srcX = srcX;
        this.srcY = srcY;
        this.srcW = srcW;
        this.srcH = srcH;
        this.scale = scale;
    }
}
