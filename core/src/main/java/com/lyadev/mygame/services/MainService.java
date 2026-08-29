package com.lyadev.mygame.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.lyadev.mygame.MyLogger.MyLogger;
import com.lyadev.mygame.world.GlobalWorld;

public class MainService {
    private static MainService instance;
    private Boolean isInit = false;

    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shape;
    private MyLogger logger;
    private Stage stage;
    private KeyboardInputService keyboardInputService;

    public static synchronized MainService getInstance() {
        if (instance == null) {
            instance = new MainService();
        }
        return instance;
    }

    public void init() {
        if (isInit) {
            return;
        }
        batch = new SpriteBatch();
        font = new BitmapFont();
        shape = new ShapeRenderer();
        stage = new Stage(new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        GlobalWorld.init(stage);
        logger = new MyLogger();
        keyboardInputService = new KeyboardInputService();
        setup();
        isInit = true;
    }

    private void setup() {
        if (isInit) {
            return;
        }
        Gdx.app.setApplicationLogger(logger);
        Gdx.input.setInputProcessor(stage);
        Gdx.input.setInputProcessor(keyboardInputService);
    }

    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        // TODO: Other background
        batch.draw(new Texture("background.png"), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        stage.act(delta);
        stage.draw();
        batch.begin();
        logger.draw();
        batch.end();

    }

    public void dispose() {
        batch.dispose();
        stage.dispose();
        GlobalWorld.dispose();
    }

    public ShapeRenderer getShapeRenderer() {
        return shape;
    }

    public SpriteBatch getSpriteBatch() {
        return batch;
    }

    public MyLogger getLogger() {
        return logger;
    }

    public BitmapFont getFont() {
        return font;
    }
}
