package com.lyadev.mygame.services;

import java.io.File;
import java.nio.file.Files;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;

public class SettingsService {
    private final static String TAG = "SettingsService";
    public static void init(){
        boolean loadSettibgs = false;
        File file = Gdx.files.internal("config/settings.json").file();
        if(file.exists()){
            Gdx.app.log(TAG, "Settings file found: " + file.getAbsolutePath());
            loadSettibgs = true;
        }else{
            Gdx.app.log(TAG, String.format("Settings file not found! [%s] Load default settings. ", file.getAbsolutePath()));
        }
        if(!loadSettibgs)
            return;
        try{
            String json = Files.readString(file.toPath());
            JSONObject settings = new JSONObject(json);
            if(settings.has("controll")){
                loadControlSettings(settings.getJSONObject("controll"));
            }
        }catch(Exception e){
            Gdx.app.error(TAG, "Error loading settings: " + e.getMessage());
        }
    }
    private static void loadControlSettings(JSONObject controll){
        Gdx.app.log(TAG, "Loading control settings...");
        if(controll.has("keyboard")){
            loadKeyboardSettings(controll.getJSONObject("keyboard"));
        }
        if(controll.has("gamepad")){
            loadGamepadSettings(controll.getJSONObject("gamepad"));
        }
    }
    private static void loadKeyboardSettings(JSONObject keyboard){
        Gdx.app.log(TAG, "Loading keyboard settings...");
    }
    private static void loadGamepadSettings(JSONObject gamepad){
        Gdx.app.log(TAG, "Loading gamepad settings...");
    }
}
