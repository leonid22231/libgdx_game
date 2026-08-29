package com.lyadev.mygame.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.lyadev.mygame.debug.DebugFeatures;
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
    private Texture backgroundTexture;

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
        logger = new MyLogger();
        Gdx.app.setApplicationLogger(logger);
        Gdx.app.log("MainService", "Application logger initialized");
        GlobalWorld.init(stage);
        keyboardInputService = new KeyboardInputService();
        Gdx.input.setInputProcessor(keyboardInputService);
        backgroundTexture = new Texture("background.png");
        isInit = true;
    }

    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        stage.act(delta);
        stage.draw();
        if(DebugFeatures.isOverlayVisible()){
            batch.begin();
            logger.draw();
            batch.end();
        }

    }

    public void dispose() {
        if(backgroundTexture != null){
            backgroundTexture.dispose();
        }
        batch.dispose();
        font.dispose();
        shape.dispose();
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
