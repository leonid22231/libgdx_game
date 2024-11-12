package com.lyadev.mygame.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.lyadev.mygame.UI;
import com.lyadev.mygame.utils.ViewPosition;

public class Views {
    public static GlyphLayout preDrawText(String text) {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(UI.font, text);
        return layout;
    }

    public static GlyphLayout drawText(String text, ViewPosition position) {
        float displayHeight = Gdx.graphics.getHeight();
        float displayWidth = Gdx.graphics.getWidth();

        float x;
        float y;
        GlyphLayout layout = new GlyphLayout();
        layout.setText(UI.font, text);
        if (position.getAligment() != null) {
            switch (position.getAligment()) {
                case TOP_LEFT:
                    x = 0 + 10;
                    y = displayHeight - 10;
                    break;
                case TOP_CENTER:
                    x = (displayWidth / 2) - layout.width / 2;
                    y = displayHeight - 10;
                    break;
                case TOP_RIGHT:
                    x = displayWidth - layout.width - 10;
                    y = displayHeight - 10;
                    break;
                case CENTER:
                    x = (displayWidth / 2) - layout.width / 2;
                    y = (displayHeight / 2) - layout.height / 2;
                    break;
                case BOTTOM_LEFT:
                    x = 0 + 10;
                    y = layout.height + 10;
                    break;
                case BOTTOM_CENTER:
                    x = (displayWidth / 2) - layout.width / 2;
                    y = layout.height + 10;
                    break;
                case BOTTOM_RIGHT:
                    x = displayWidth - layout.width - 10;
                    y = layout.height + 10;
                    break;
                default:
                    x = 0;
                    y = 0;
                    break;
            }
        } else {
            x = position.getX();
            y = position.getY();
        }
        
        return UI.font.draw(UI.batch, text, x, y);
    }
}
