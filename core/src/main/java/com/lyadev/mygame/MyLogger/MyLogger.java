package com.lyadev.mygame.MyLogger;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.TimeUtils;
import com.lyadev.mygame.base.entity.Entity;
import com.lyadev.mygame.debug.DebugEntityService;
import com.lyadev.mygame.debug.DebugFeatures;
import com.lyadev.mygame.debug.DebugLogEntry;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.modules.vision.VisionDebug;
import com.lyadev.mygame.base.world.GlobalWorld;
import com.lyadev.mygame.base.world.WorldCameraControl;
import com.lyadev.mygame.base.world.WorldController;
import com.lyadev.mygame.base.world.topdown.OrthoTileHit;
import com.lyadev.mygame.base.world.topdown.TopDownMapWorld;

import lombok.Getter;
import lombok.Setter;

/**
 * Application logger + on-screen HUD. Drawn in screen space (not world camera).
 */
public class MyLogger implements ApplicationLogger {
    private static final String VERSION = "1.0";
    private static final int MAX_STORED_MESSAGES = 80;
    private static final int MAX_VISIBLE_LOGS = 18;
    private static final float LINE_HEIGHT = 16f;
    private static final float PAD = 10f;
    private static final float STATUS_REFRESH_SEC = 0.2f;
    private static final Color PANEL_BG = new Color(0.08f, 0.09f, 0.12f, 0.82f);
    private static final Color PANEL_LINE = new Color(0.25f, 0.28f, 0.35f, 0.95f);
    private static final Color STATUS_COLOR = new Color(0.78f, 0.88f, 0.95f, 1f);

    private final List<Message> messages = new ArrayList<>();
    private final List<String> cachedStatus = new ArrayList<>();
    private final GlyphLayout layout = new GlyphLayout();
    private final List<String> lastKeys = new ArrayList<>();

    @Getter
    @Setter
    private boolean active = true;
    @Getter
    @Setter
    private boolean focused = false;
    private float height = 0;
    private float width = 0;
    private float scrollOffset = 0;
    private float mousePositionX = 0;
    private float mousePositionY = 0;
    private float lastScroll = 0;
    private float statusAge = 99f;
    private int lastTapScreenX;
    private int lastTapScreenY;
    private int lastTapPointer;
    private int lastTapButton;
    private final long startTime = TimeUtils.millis();

    public void setLastTap(int screenX, int screenY, int pointer, int button) {
        lastTapScreenX = screenX;
        lastTapScreenY = screenY;
        lastTapPointer = pointer;
        lastTapButton = button;
    }

    public void setScroll(float scroll) {
        lastScroll = scroll;
        if(focused){
            scrollOffset += scroll * LINE_HEIGHT;
        }
    }

    public void mousePositionListener(float x, float y) {
        mousePositionX = x;
        mousePositionY = y;
        focused = mousePositionY < Gdx.graphics.getHeight() && mousePositionY > 0
                && mousePositionX < width && mousePositionX > 0;
    }

    public void addKey(String key) {
        if(lastKeys.size() <= 5){
            lastKeys.add(key);
        } else {
            lastKeys.remove(0);
            lastKeys.add(key);
        }
    }

    public void clearLogs() {
        synchronized(messages){
            messages.clear();
        }
    }

    public synchronized List<DebugLogEntry> getLogEntriesSnapshot() {
        List<DebugLogEntry> entries = new ArrayList<>();
        for(Message message : messages){
            DebugLogEntry.Level level = DebugLogEntry.Level.LOG;
            if(message.level == MessageLevel.ERROR){
                level = DebugLogEntry.Level.ERROR;
            } else if(message.level == MessageLevel.DEBUG){
                level = DebugLogEntry.Level.DEBUG;
            }
            entries.add(new DebugLogEntry(level, message.tag, message.message));
        }
        return entries;
    }

    public List<String> collectStatusLines() {
        refreshStatusIfNeeded(true);
        return new ArrayList<>(cachedStatus);
    }

    /**
     * HUD draw. Caller must set screen projection on batch/shape first.
     * Shape pass first, then text — avoids mid-batch flush per line.
     */
    public void draw(SpriteBatch batch, ShapeRenderer shape, BitmapFont font) {
        if(!active){
            return;
        }
        refreshStatusIfNeeded(false);

        List<Message> visibleLogs;
        synchronized(messages){
            int start = Math.max(0, messages.size() - MAX_VISIBLE_LOGS);
            visibleLogs = new ArrayList<>(messages.subList(start, messages.size()));
        }

        int statusCount = cachedStatus.size();
        int logCount = visibleLogs.size();
        int totalLines = statusCount + (logCount > 0 ? 1 + logCount : 0);
        float panelHeight = PAD * 2f + totalLines * LINE_HEIGHT;
        float screenH = Gdx.graphics.getHeight();
        float topY = screenH - PAD + scrollOffset;

        float maxWidth = 220f;
        font.getData().setScale(1f);
        for(String line : cachedStatus){
            layout.setText(font, line);
            maxWidth = Math.max(maxWidth, layout.width);
        }
        for(Message message : visibleLogs){
            layout.setText(font, message.toString());
            maxWidth = Math.max(maxWidth, layout.width);
        }
        float panelWidth = maxWidth + PAD * 2f;
        float panelY = topY - panelHeight + LINE_HEIGHT;

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeType.Filled);
        shape.setColor(PANEL_BG);
        shape.rect(0f, panelY - PAD, panelWidth, panelHeight + PAD);
        shape.end();
        shape.begin(ShapeType.Line);
        shape.setColor(PANEL_LINE);
        shape.rect(0f, panelY - PAD, panelWidth, panelHeight + PAD);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();

        font.setColor(STATUS_COLOR);
        float y = topY;
        for(String line : cachedStatus){
            font.draw(batch, line, PAD, y);
            y -= LINE_HEIGHT;
        }
        if(logCount > 0){
            font.setColor(STATUS_COLOR);
            font.draw(batch, "----------------------------", PAD, y);
            y -= LINE_HEIGHT;
            for(Message message : visibleLogs){
                font.setColor(message.level.getColor());
                font.draw(batch, message.toString(), PAD, y);
                y -= LINE_HEIGHT;
            }
        }
        font.setColor(Color.WHITE);
        width = panelWidth;
        height = panelHeight;
    }

    private void refreshStatusIfNeeded(boolean force) {
        statusAge += Gdx.graphics != null ? Gdx.graphics.getDeltaTime() : STATUS_REFRESH_SEC;
        if(!force && statusAge < STATUS_REFRESH_SEC){
            return;
        }
        statusAge = 0f;
        cachedStatus.clear();
        if(GlobalWorld.entities == null){
            cachedStatus.add("World not initialized");
            return;
        }

        long elapsedTime = TimeUtils.timeSinceMillis(startTime);
        double seconds = elapsedTime / 1000.0;
        String activeTime = seconds < 60
                ? String.format("%02d s.", elapsedTime / 1000)
                : String.format("%02d m. %02d s.", (elapsedTime / 1000) / 60, (elapsedTime / 1000) % 60);

        cachedStatus.add(String.format("Logger V.%s  |  Uptime: %s", VERSION, activeTime));
        cachedStatus.add("Active world: " + GlobalWorld.activeWorldLabel());
        cachedStatus.add(String.format("worlds=%d active=%s",
                WorldController.allWorlds().size(),
                WorldController.getActive() != null ? WorldController.getActive().getId() : "none"));
        if(Gdx.graphics != null){
            cachedStatus.add(String.format("FPS: %s  |  Delta: %.2f ms",
                    Gdx.graphics.getFramesPerSecond(), Gdx.graphics.getDeltaTime() * 1000));
        }
        cachedStatus.add(String.format("Mouse: %.0f, %.0f  |  Focus: %s",
                mousePositionX, mousePositionY, focused ? "Yes" : "No"));
        OrthoTileHit tileHit = TopDownMapWorld.getLastHit();
        if(tileHit != null){
            cachedStatus.add(String.format("Tile: [%d,%d] id=%d %s",
                    tileHit.getCol(), tileHit.getRow(), tileHit.getTileId(),
                    tileHit.isInBounds() ? "OK" : "OOB"));
        }
        cachedStatus.add(String.format("Cam follow: %s  |  Pan: %s",
                WorldCameraControl.isFollowEnabled() ? "ON" : "OFF",
                WorldCameraControl.isPanning() ? "drag" : "-"));

        int playables = 0;
        int blocks = 0;
        List<String> inactive = new ArrayList<>();
        for(Entity entity : GlobalWorld.entities){
            if(DebugEntityService.isMapBlock(entity)){
                blocks++;
                continue;
            }
            playables++;
            SelectableModule selectable = SelectableModule.from(entity);
            if(selectable != null && !selectable.isActive()){
                inactive.add(entity.getTAG());
            }
        }
        cachedStatus.add(String.format("Entities: %d playable + %d blocks", playables, blocks));
        cachedStatus.add("Current: " + (GlobalWorld.player != null ? GlobalWorld.player.getTAG() : "None"));
        for(String tag : inactive){
            cachedStatus.add("Inactive: " + tag);
        }
        cachedStatus.add(String.format("Vision: %s modules | lines %s | overlay %s",
                VisionDebug.getActiveModuleCount(),
                DebugFeatures.isVisionLinesVisible() ? "ON" : "OFF",
                DebugFeatures.isOverlayVisible() ? "ON" : "OFF"));
    }

    private void store(Message message) {
        synchronized(messages){
            messages.add(message);
            while(messages.size() > MAX_STORED_MESSAGES){
                messages.remove(0);
            }
        }
    }

    private void writeLog(DebugLogEntry.Level level, String tag, String message, boolean errorStream) {
        String line = String.format("[%s][%s] %s", level.name(), tag, message);
        if(errorStream){
            System.err.println(line);
        } else {
            System.out.println(line);
        }
        DebugFeatures.logExternal(new DebugLogEntry(level, tag, message));
    }

    @Override
    public void log(String tag, String message) {
        writeLog(DebugLogEntry.Level.LOG, tag, message, false);
        store(new Message(MessageLevel.LOG, tag, message));
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        writeLog(DebugLogEntry.Level.LOG, tag, message, false);
        if(exception != null){
            exception.printStackTrace(System.out);
        }
        store(new Message(MessageLevel.LOG, tag, message));
    }

    @Override
    public void error(String tag, String message) {
        writeLog(DebugLogEntry.Level.ERROR, tag, message, true);
        store(new Message(MessageLevel.ERROR, tag, message));
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        writeLog(DebugLogEntry.Level.ERROR, tag, message, true);
        if(exception != null){
            exception.printStackTrace(System.err);
        }
        store(new Message(MessageLevel.ERROR, tag, message));
    }

    @Override
    public void debug(String tag, String message) {
        writeLog(DebugLogEntry.Level.DEBUG, tag, message, false);
        store(new Message(MessageLevel.DEBUG, tag, message));
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        writeLog(DebugLogEntry.Level.DEBUG, tag, message, false);
        if(exception != null){
            exception.printStackTrace(System.out);
        }
        store(new Message(MessageLevel.DEBUG, tag, message));
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}

class Message {
    final MessageLevel level;
    final String tag;
    final String message;

    Message(MessageLevel level, String tag, String message) {
        this.level = level;
        this.tag = tag;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("[%s]%s: %s", level, tag, message);
    }
}

enum MessageLevel {
    LOG(new Color(0.35f, 0.9f, 0.4f, 1f)),
    ERROR(new Color(1f, 0.35f, 0.35f, 1f)),
    DEBUG(new Color(0.35f, 0.85f, 1f, 1f));

    private final Color color;

    MessageLevel(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
