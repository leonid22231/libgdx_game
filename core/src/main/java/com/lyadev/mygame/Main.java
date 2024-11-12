package com.lyadev.mygame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.lyadev.mygame.entities.PlayebleEntity;
import com.lyadev.mygame.services.KeyboardInputService;
import com.lyadev.mygame.services.SettingsService;
import com.lyadev.mygame.world.GlobalWorld;

public class Main extends ApplicationAdapter{
    private final String TAG = "Core";
    private Texture image;
    private SpriteBatch batch;
    private Stage stage;
    private KeyboardInputService keyboardInputService;
    @Override
    public void create() {
        UI.init();
        Gdx.app.setApplicationLogger(UI.logger);
        batch = UI.batch;
        stage = UI.stage;
        image = new Texture("background.png");
        keyboardInputService = new KeyboardInputService();
        Gdx.app.log( TAG, "Created the application!");
        Gdx.input.setInputProcessor(keyboardInputService);
        SettingsService.init();
        GlobalWorld.init(stage);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        batch.draw(image, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        UI.logger.draw();
        batch.end();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
        stage.dispose();
        GlobalWorld.dispose();
    }

}


