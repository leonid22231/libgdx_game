package com.lyadev.mygame.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.lyadev.mygame.debug.DebugFeatures;
import com.lyadev.mygame.debug.DebugSelectionHighlight;
import com.lyadev.mygame.MyLogger.MyLogger;
import com.lyadev.mygame.modules.vision.VisionDebug;
import com.lyadev.mygame.world.GlobalWorld;
import com.lyadev.mygame.world.WorldCameraSettings;
import com.lyadev.mygame.world.topdown.OrthoMapEditor;
import com.lyadev.mygame.world.topdown.TopDownMapWorld;

public class MainService {
    private static MainService instance;
    private Boolean isInit = false;

    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shape;
    private MyLogger logger;
    private Stage stage;
    private ExtendViewport worldViewport;
    private final Matrix4 screenProjection = new Matrix4();
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
        worldViewport = new ExtendViewport(
                WorldCameraSettings.WORLD_WIDTH,
                WorldCameraSettings.WORLD_HEIGHT);
        stage = new Stage(worldViewport, batch);
        logger = new MyLogger();
        Gdx.app.setApplicationLogger(logger);
        Gdx.app.log("MainService", "Application logger initialized");
        Gdx.app.log("MainService", "World viewport "
                + WorldCameraSettings.WORLD_WIDTH + "x" + WorldCameraSettings.WORLD_HEIGHT);
        GlobalWorld.init(stage);
        keyboardInputService = new KeyboardInputService();
        Gdx.input.setInputProcessor(keyboardInputService);
        backgroundTexture = new Texture("background.png");
        isInit = true;
    }

    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(0.12f, 0.14f, 0.16f, 1f);
        if(!TopDownMapWorld.drawsSkyBackground()){
            Gdx.gl.glClearColor(0.08f, 0.07f, 0.06f, 1f);
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        worldViewport.apply();
        OrthographicCamera camera = (OrthographicCamera) worldViewport.getCamera();
        batch.setProjectionMatrix(camera.combined);
        shape.setProjectionMatrix(camera.combined);
        if(TopDownMapWorld.drawsSkyBackground() && backgroundTexture != null){
            batch.begin();
            batch.draw(
                    backgroundTexture,
                    camera.position.x - camera.viewportWidth * camera.zoom * 0.5f,
                    camera.position.y - camera.viewportHeight * camera.zoom * 0.5f,
                    camera.viewportWidth * camera.zoom,
                    camera.viewportHeight * camera.zoom);
            batch.end();
        }
        stage.act(delta);
        GlobalWorld.sortEntitiesByDepth();
        stage.draw();
        shape.setProjectionMatrix(camera.combined);
        VisionDebug.drawOverlays(shape);
        DebugSelectionHighlight.draw(shape);
        OrthoMapEditor.drawOverlay(batch, shape, font);
        drawModuleUiLayer();
        if(DebugFeatures.isOverlayVisible()){
            drawLoggerOverlay();
        }
    }

    /** Module HUD: screen pixels, above world, under logger. */
    private void drawModuleUiLayer() {
        screenProjection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(screenProjection);
        shape.setProjectionMatrix(screenProjection);
        batch.begin();
        GlobalWorld.drawModuleUi(batch, 1f);
        batch.end();
    }

    /** Logger is HUD: screen pixels, not world camera units. */
    private void drawLoggerOverlay() {
        screenProjection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(screenProjection);
        shape.setProjectionMatrix(screenProjection);
        batch.begin();
        logger.draw(batch, shape, font);
        batch.end();
    }

    public void resize(int width, int height) {
        if(worldViewport != null){
            worldViewport.update(width, height, true);
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

    public ExtendViewport getWorldViewport() {
        return worldViewport;
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

    public Stage getStage() {
        return stage;
    }
}
