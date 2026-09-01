package com.lyadev.mygame.debug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/** In-world highlight for the block/prop selected in the debug console. */
public final class DebugSelectionHighlight {
    private static final Color FILL = new Color(0.2f, 0.85f, 1f, 0.28f);
    private static final Color LINE = new Color(0.25f, 0.95f, 1f, 0.95f);

    private static boolean active;
    private static String tag = "";
    private static float x;
    private static float y;
    private static float width;
    private static float height;

    private DebugSelectionHighlight() {
        throw new UnsupportedOperationException();
    }

    public static void set(String entityTag, float worldX, float worldY, float w, float h) {
        active = true;
        tag = entityTag == null ? "" : entityTag;
        x = worldX;
        y = worldY;
        width = Math.max(1f, w);
        height = Math.max(1f, h);
    }

    public static void clear() {
        active = false;
        tag = "";
    }

    public static boolean isActive() {
        return active;
    }

    public static String getTag() {
        return tag;
    }

    public static void draw(ShapeRenderer shape) {
        if(!active || shape == null){
            return;
        }
        shape.begin(ShapeType.Filled);
        shape.setColor(FILL);
        shape.rect(x, y, width, height);
        shape.end();
        shape.begin(ShapeType.Line);
        shape.setColor(LINE);
        shape.rect(x, y, width, height);
        shape.end();
    }
}
