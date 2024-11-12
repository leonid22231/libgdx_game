package com.lyadev.mygame.world;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.lyadev.mygame.Assets;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntitySettings;
import com.lyadev.mygame.utils.Size;
import com.lyadev.mygame.utils.listeners.EntityListener;

public class GlobalWorld {
    public static List<Entity> entities;
    public static Entity player;
    public static List<EntityListener> listeners;

    public static void init(Stage stage){
        initFields();
        EntitySettings settings = new EntitySettings("Man", Assets.PERSON_MAN, Size.ENTITY_DEFAULT);
        Entity man = new Entity(settings);
        man.setRandomPositionInScreen();

        boolean allEntityInit = false;
        while(!allEntityInit){
            for(Entity entity : entities){
                if(!entity.getStatus().getIsInit()){
                    break;
                }
                allEntityInit = true;
            }
        }

        for(Entity entity : entities){
            stage.addActor(entity);
        }
    }
    private static void initFields(){
        entities = new ArrayList<>();
        listeners = new ArrayList<>();
    }
    public static void addEntity(Entity entity) {
        entities.add(entity);
    }

    public static void dispose() {
        for (Entity entity : entities) {
            entity.dispose();
        }
    }

}
