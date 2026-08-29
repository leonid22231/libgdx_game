package com.lyadev.mygame.views;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.lyadev.mygame.services.MainService;
import com.lyadev.mygame.utils.ViewPosition;

public class Views {
    public static GlyphLayout preDrawText(String text) {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(MainService.getInstance().getFont(), text);
        return layout;
    }

    public static GlyphLayout drawText(String text, ViewPosition position) {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(MainService.getInstance().getFont(), text);
        return MainService.getInstance().getFont().draw(
                MainService.getInstance().getSpriteBatch(),
                text,
                position.getX(),
                position.getY());
    }
}
