package com.lyadev.mygame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.lyadev.mygame.debug.DebugBootstrap;
import com.lyadev.mygame.services.MainService;
import com.lyadev.mygame.services.SettingsService;

public class Main extends ApplicationAdapter{
    private final String TAG = "Core";
    
    @Override
    public void create() {
        DebugBootstrap.onGameCreate();
        MainService.getInstance().init();
        Gdx.app.log( TAG, "Created the application!");
        SettingsService.init();

    }

    @Override
    public void render() {
        MainService.getInstance().render();
    }

    @Override
    public void dispose() {
        DebugBootstrap.onGameDispose();
        MainService.getInstance().dispose(); 
    }

}


