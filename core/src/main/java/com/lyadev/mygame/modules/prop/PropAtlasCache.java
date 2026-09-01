package com.lyadev.mygame.modules.prop;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Shared atlas textures for props. Black (0,0,0) → transparent (Decorations.png key).
 */
public final class PropAtlasCache {
    private static final Map<String, Texture> TEXTURES = new HashMap<>();

    private PropAtlasCache() {
        throw new UnsupportedOperationException();
    }

    public static TextureRegion region(String atlasPath, int x, int y, int w, int h) {
        Texture texture = TEXTURES.get(atlasPath);
        if(texture == null){
            texture = loadWithBlackKey(atlasPath);
            TEXTURES.put(atlasPath, texture);
        }
        return new TextureRegion(texture, x, y, w, h);
    }

    public static void disposeAll() {
        for(Texture texture : TEXTURES.values()){
            texture.dispose();
        }
        TEXTURES.clear();
    }

    private static Texture loadWithBlackKey(String path) {
        Pixmap raw = new Pixmap(Gdx.files.internal(path));
        Pixmap out = new Pixmap(raw.getWidth(), raw.getHeight(), Pixmap.Format.RGBA8888);
        for(int py = 0; py < raw.getHeight(); py++){
            for(int px = 0; px < raw.getWidth(); px++){
                int rgba = raw.getPixel(px, py);
                int r = (rgba >>> 24) & 0xff;
                int g = (rgba >>> 16) & 0xff;
                int b = (rgba >>> 8) & 0xff;
                if(r < 8 && g < 8 && b < 8){
                    out.drawPixel(px, py, 0x00000000);
                } else {
                    out.drawPixel(px, py, rgba);
                }
            }
        }
        raw.dispose();
        Texture texture = new Texture(out);
        out.dispose();
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        Gdx.app.log("PropAtlasCache", "Loaded " + path);
        return texture;
    }
}
