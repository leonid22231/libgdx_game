package com.lyadev.mygame.modules.texture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lyadev.mygame.modules.animation.AnimationClip;
import com.lyadev.mygame.modules.animation.AnimationSettings;
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

    private final Map<String, AnimatedClip> animatedClips = new HashMap<>();
    private String activeClipId;
    private int activeRow;
    private int activeColumn;

    public void init(TextureSettings settings) {
        clearAnimated();
        int frameWidth = settings.getFrameSize().getWidth();
        int frameHeight = settings.getFrameSize().getHeight();
        String textureString = settings.getTexturePath();

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
            textureSize = minSizeFromTexture.getSizeFromScaleFactor(settings.getScaleFactor());
        } finally {
            pixmap.dispose();
        }
    }

    public void initAnimated(AnimationSettings settings) {
        disposeStaticSheet();
        allTextureRegions.clear();
        regionsCount = 0;
        currentSpriteIndex = 0;
        animatedClips.clear();

        AnimationClip firstClip = settings.getIdle();
        for(AnimationClip clip : List.of(settings.getIdle(), settings.getWalk(), settings.getRun())){
            animatedClips.put(clip.getId(), loadAnimatedClip(clip));
        }

        activeClipId = firstClip.getId();
        activeRow = 0;
        activeColumn = 0;
        textureSize = firstClip.getFrameSize().getSizeFromScaleFactor(settings.getScaleFactor());
    }

    public void setAnimatedFrame(String clipId, int row, int column) {
        AnimatedClip clip = animatedClips.get(clipId);
        if(clip == null){
            return;
        }
        activeClipId = clipId;
        activeRow = Math.max(0, Math.min(row, clip.rows - 1));
        activeColumn = Math.max(0, Math.min(column, clip.columns - 1));
    }

    public TextureRegion getCurrentTexture() {
        if(!animatedClips.isEmpty()){
            AnimatedClip clip = animatedClips.get(activeClipId);
            if(clip == null){
                return null;
            }
            return clip.regions[activeRow][activeColumn];
        }
        if(allTextureRegions.isEmpty()){
            return null;
        }
        return allTextureRegions.get(currentSpriteIndex);
    }

    public void dispose() {
        disposeStaticSheet();
        for(AnimatedClip clip : animatedClips.values()){
            if(clip.sheetTexture != null){
                clip.sheetTexture.dispose();
            }
        }
        animatedClips.clear();
        allTextureRegions.clear();
    }

    private void clearAnimated() {
        for(AnimatedClip clip : animatedClips.values()){
            if(clip.sheetTexture != null){
                clip.sheetTexture.dispose();
            }
        }
        animatedClips.clear();
        activeClipId = null;
        activeRow = 0;
        activeColumn = 0;
    }

    private void disposeStaticSheet() {
        if(sheetTexture != null){
            sheetTexture.dispose();
            sheetTexture = null;
        }
    }

    private AnimatedClip loadAnimatedClip(AnimationClip clip) {
        int frameWidth = clip.getFrameSize().getWidth();
        int frameHeight = clip.getFrameSize().getHeight();
        Texture texture = new Texture(clip.getSheetPath());
        TextureRegion[][] regions = new TextureRegion[clip.getRows()][clip.getColumns()];
        for(int row = 0; row < clip.getRows(); row++){
            int textureRow = clip.getRows() - 1 - row;
            for(int col = 0; col < clip.getColumns(); col++){
                TextureRegion region = new TextureRegion(texture);
                region.setRegion(col * frameWidth, textureRow * frameHeight, frameWidth, frameHeight);
                regions[row][col] = region;
            }
        }
        return new AnimatedClip(texture, regions, clip.getRows(), clip.getColumns());
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

    private static final class AnimatedClip {
        private final Texture sheetTexture;
        private final TextureRegion[][] regions;
        private final int rows;
        private final int columns;

        private AnimatedClip(Texture sheetTexture, TextureRegion[][] regions, int rows, int columns) {
            this.sheetTexture = sheetTexture;
            this.regions = regions;
            this.rows = rows;
            this.columns = columns;
        }
    }
}
