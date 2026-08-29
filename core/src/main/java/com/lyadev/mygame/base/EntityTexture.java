package com.lyadev.mygame.base;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lyadev.mygame.utils.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntityTexture {
    private int regionsCount = 0;
    private int currentSpriteIndex = 0;
    private Size textureSize = Size.ENTITY_DEFAULT;
    private List<TextureRegion> allTextureRegions = new ArrayList<>();
    private Texture sheetTexture;

    public void init(EntitySettings settings) {
        int frameWidth = settings.getTextureSize().getWidth();
        int frameHeight = settings.getTextureSize().getHeight();
        String textureString = settings.getTexture();

        sheetTexture = new Texture(textureString);
        TextureRegion sheetRegion = new TextureRegion(sheetTexture);
        regionsCount = sheetRegion.getRegionWidth() / frameWidth;

        if(!sheetTexture.getTextureData().isPrepared()){
            sheetTexture.getTextureData().prepare();
        }
        Pixmap pixmap = sheetTexture.getTextureData().consumePixmap();
        try {
            int minTopOffset = frameHeight;
            for(int i = 0; i < regionsCount; i++){
                int topOffset = calculateTopTransparentRows(pixmap, i * frameWidth, frameWidth, frameHeight);
                if(topOffset < minTopOffset){
                    minTopOffset = topOffset;
                }
                int croppedHeight = frameHeight - topOffset;
                TextureRegion frameRegion = new TextureRegion(sheetTexture);
                frameRegion.setRegion(i * frameWidth, topOffset, frameWidth, croppedHeight);
                allTextureRegions.add(frameRegion);
            }

            Size minSizeFromTexture = new Size(frameWidth, frameHeight - minTopOffset);
            textureSize = minSizeFromTexture.getSizeFromScaleFactor(settings.getTextureScaleFactor());
        } finally {
            pixmap.dispose();
        }
    }

    public TextureRegion getCurrentTexture() {
        return allTextureRegions.get(currentSpriteIndex);
    }

    public void dispose() {
        if(sheetTexture != null){
            sheetTexture.dispose();
            sheetTexture = null;
        }
        allTextureRegions.clear();
    }

    private int calculateTopTransparentRows(Pixmap pixmap, int regionX, int regionWidth, int regionHeight) {
        Color colorNull = new Color(0, 0, 0, 0);
        int minTop = regionHeight;

        column:for(int x = 0; x < regionWidth; x++){
            for(int y = 0; y < regionHeight; y++){
                if(!getColorAsPixmap(pixmap, regionX + x, y).equals(colorNull)){
                    if(minTop > y){
                        minTop = y;
                        continue column;
                    }
                }
            }
        }
        return minTop;
    }

    private Color getColorAsPixmap(Pixmap pixmap, int x, int y) {
        return new Color(pixmap.getPixel(x, y));
    }
}
