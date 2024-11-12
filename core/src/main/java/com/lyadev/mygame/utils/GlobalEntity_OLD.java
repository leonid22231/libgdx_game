package com.lyadev.mygame.utils;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.lyadev.mygame.Assets;
import com.lyadev.mygame.entities.PlayebleEntity;
import com.lyadev.mygame.enums.MoveType;
import com.lyadev.mygame.models.MoveEventSetting;
import com.lyadev.mygame.models.MoveSettings;
import com.lyadev.mygame.models.PlayerSettings;
import com.lyadev.mygame.utils.listeners.EntityListener;

public class GlobalEntity_OLD {
    public static List<PlayebleEntity> entities = new ArrayList<PlayebleEntity>();
    public static PlayebleEntity currentPlayebleEntity;
    public static List<EntityListener> entityListeners = new ArrayList<EntityListener>();
    public static void init(){
        MoveSettings moveSettings = new MoveSettings(
            new MoveEventSetting(MoveType.UP, 2),
            new MoveEventSetting(MoveType.DOWN, 4),
            new MoveEventSetting(MoveType.LEFT, 3),
            new MoveEventSetting(MoveType.RIGHT, 1)
        );
        PlayerSettings settings = new PlayerSettings("Player_man", moveSettings, 100, 200, Assets.PERSON_MAN,(float) (Gdx.graphics.getWidth() * 0.2));
        PlayebleEntity player = new PlayebleEntity(settings.copy());
        player.setRandomPosition();
        settings.tag = "Player_woman";
        settings.texture = Assets.PERSON_WOMAN;
        settings.visibleRadius = 100f;
        PlayebleEntity player1 = new PlayebleEntity(settings);
        settings.tag = "Player_man_2";
        settings.texture = Assets.PERSON_MAN;
        settings.visibleRadius = 150f;
        PlayebleEntity player2 = new PlayebleEntity(settings.copy());
        player1.setRandomPosition();
        entities.add(player);
        entities.add(player1);
        entities.add(player2);
        for(PlayebleEntity entity : entities){
            if(entity.isActive()){
                currentPlayebleEntity = entity;
                break;
            }
        }
        if(currentPlayebleEntity==null){
            entities.get(0).setActive(true);
        }
    }
    public static void drawDebugLineToggle(){
        for(PlayebleEntity entity : entities){
            entity.drawDebugLinesToggle();
        }
    }
    public static void setAllRandomPositions(){
        for(PlayebleEntity entity : entities){
            entity.setRandomPosition();
        }
    }
    public static PlayebleEntity getPlayer(){
        return currentPlayebleEntity;
    }
    public static void updateEntityInfo(float mousePositionX, float mousePositionY){
        for(PlayebleEntity entity : entities){
            entity.mousePositionListener(mousePositionX, mousePositionY);
        }
    }
    public static void clickEvent(){
        for(PlayebleEntity entity : entities){
            entity.setVisible(false);
        }
        for(PlayebleEntity entity : entities){
            entity.clickEvent();
        }
    }
    public static void dispose(){
        for(PlayebleEntity entity : entities){
            entity.dispose();
        }
    }
}
