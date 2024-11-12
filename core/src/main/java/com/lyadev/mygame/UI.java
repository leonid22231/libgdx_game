package com.lyadev.mygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.lyadev.mygame.MyLogger.MyLogger;

public class UI {
    public static SpriteBatch batch;
    public static BitmapFont font;
    public static ShapeRenderer shape;
    public static MyLogger logger;
    public static Stage stage;
    static void init(){
        batch = new SpriteBatch();
        font = new BitmapFont();
        shape = new ShapeRenderer();
        logger = new MyLogger();
        stage = new Stage(new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        stageConfig();
    }
    public static void stageConfig(){
        Gdx.input.setInputProcessor(stage);
    }
}
