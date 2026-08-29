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
    private int scaleFactor = 0;
    private int currentSpriteIndex = 0;
    private Size textureSize = Size.ENTITY_DEFAULT;
    private List<TextureRegion> allTextureRegions = new ArrayList<>();

    public void init(EntitySettings settings) {
        int height = settings.getTextureSize().getHeight();
        int width = settings.getTextureSize().getWidth();

        String textureString = settings.getTexture();

        Texture texture = new Texture(textureString);

        TextureRegion textureRegion = new TextureRegion(texture);

        regionsCount = textureRegion.getRegionWidth() / width;
        int minNullSectors = height;
        for (int i = 0; i < regionsCount; i++) {

            textureRegion = new TextureRegion(texture);

            textureRegion.setRegion(i * width, 0, width, height);

            int nullSectors = calculateNullSectors(textureRegion);
            if(minNullSectors > nullSectors){
                minNullSectors = nullSectors;
            }
            int textureHeight = height - nullSectors;

            textureRegion.setRegion(i * width, nullSectors, width, textureHeight);

            allTextureRegions.add(textureRegion);

            if(textureSize.getHeight()>textureHeight){
                textureSize.setHeight(textureHeight);
            }
        }
        Size minSizeFromTexture = new Size(width, height - minNullSectors);
        textureSize = minSizeFromTexture.getSizeFromScaleFactor(settings.getTextureScaleFactor());
    }

    public TextureRegion getCurrentTexture() {
        return allTextureRegions.get(currentSpriteIndex);
    }

    private int calculateNullSectors(TextureRegion textureRegion){

        int width = textureRegion.getRegionWidth();
        int height = textureRegion.getRegionHeight();

        Texture texture = textureRegion.getTexture();

        if(!texture.getTextureData().isPrepared()){
            texture.getTextureData().prepare();
        }
        Color colorNull = new Color(0, 0, 0, 0);
        
        Pixmap pixmap = texture.getTextureData().consumePixmap();

        int minNullSectors = height;

        check:for(int i = 0; i < width; i++){
            for(int j = 0;j< height;j++){
                if(!getColorAsPixmap(pixmap, i, j).equals(colorNull)){
                    if(minNullSectors > j){
                        minNullSectors = height - j;
                        continue check;
                    }
                }
            }
        }
        return height - minNullSectors;
    }
    private Color getColorAsPixmap(Pixmap pixmap, int x, int y) {
        return new Color(pixmap.getPixel(x, y));
    }
}
