package com.lyadev.mygame.MyLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.TimeUtils;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.debug.DebugFeatures;
import com.lyadev.mygame.debug.DebugLogEntry;
import com.lyadev.mygame.services.MainService;
import com.lyadev.mygame.utils.ViewPosition;
import com.lyadev.mygame.utils.listeners.EntityListenerThread;
import com.lyadev.mygame.views.Views;
import com.lyadev.mygame.world.GlobalWorld;

public class MyLogger implements ApplicationLogger {
    final List<Message> messages = new ArrayList<>();
    final String VERSION = "1.0";
    Boolean active = true;
    Boolean isFocused = false;
    float height = 0;
    float width = 0;
    float scrollOffset = 0;
    float mousePositionX = 0;
    float mousePositionY = 0;
    List<String> lastKeys = new ArrayList<>();
    float lastScroll = 0;
    int lastTapScreenX; 
    int lastTapScreenY; 
    int lastTapPointer; 
    int lastTapButton;
    long startTime = TimeUtils.millis();
    public void setLastTap(int screenX, int screenY, int pointer, int button){
        lastTapScreenX = screenX;
        lastTapScreenY = screenY;
        lastTapPointer = pointer;
        lastTapButton = button;
    }
    public void setScroll(float scroll){
        lastScroll = scroll;
        if(isFocused)
            this.scrollOffset+= scroll * 20;
    }
    public void mousePositionListener(float x, float y){
        mousePositionX = x;
        mousePositionY = y;
        isFocused = mousePositionY < Gdx.graphics.getHeight() && mousePositionY > 0 && mousePositionX < width && mousePositionX > 0;
    }
    Boolean mouseInBox(){

        return false;
    }
    public void addKey(String key){
        if(lastKeys.size()<=5){
            lastKeys.add(key);
        }else{
            lastKeys.remove(0);
            lastKeys.add(key);
        }
    }
    public void clearLogs(){
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

    public synchronized List<Message> getMessagesSnapshot() {
        return new ArrayList<>(messages);
    }

    public List<String> collectStatusLines() {
        if(GlobalWorld.entities == null){
            return List.of("World not initialized");
        }
        long elapsedTime = TimeUtils.timeSinceMillis(startTime);
        double seconds = elapsedTime / 1000.0;
        String activeTime = seconds < 60
                ? String.format("%02d s.", elapsedTime / 1000)
                : String.format("%02d m. %02d s.", (elapsedTime / 1000) / 60, (elapsedTime / 1000) % 60);

        List<String> lines = new ArrayList<>();
        lines.add(String.format("Logger V.%s  |  Uptime: %s", VERSION, activeTime));
        if(Gdx.graphics != null){
            lines.add(String.format("FPS: %s  |  Delta: %.2f ms", Gdx.graphics.getFramesPerSecond(), Gdx.graphics.getDeltaTime() * 1000));
        }
        lines.add(String.format("Mouse: x=%.0f, y=%.0f  |  Focus: %s  |  Scroll: %s", mousePositionX, mousePositionY,
                isFocused ? "Yes" : "No", lastScroll < 0 ? "Up" : "Down"));
        lines.add(String.format("Last keys: %s", lastKeys.toString()));
        lines.add(String.format("Last tap: x=%s, y=%s, pointer=%s, button=%s", lastTapScreenX, lastTapScreenY, lastTapPointer, lastTapButton));
        lines.add(String.format("Players: %s  |  Current: %s", GlobalWorld.entities.size(),
                GlobalWorld.player != null ? GlobalWorld.player.toString() : "None"));
        for(Entity entity : GlobalWorld.entities){
            if(!entity.getStatus().isActive()){
                lines.add("Inactive: " + entity.toString());
            }
        }
        lines.add(String.format("Listener threads: %s", GlobalWorld.listeners != null ? GlobalWorld.listeners.size() : 0));
        if(GlobalWorld.listeners != null){
            for(int i = 0; i < GlobalWorld.listeners.size(); i++){
                EntityListenerThread thread = GlobalWorld.listeners.get(i).thread;
                if(thread.isActive()){
                    lines.add(String.format("  Listener[%s] running %ss", thread.getName(), thread.getRuntimeSeconds()));
                } else {
                    lines.add(String.format("  Listener[%s] stopped", thread.getName()));
                }
            }
        }
        lines.add(String.format("Vision lines: %s  |  Overlay: %s",
                DebugFeatures.isVisionLinesVisible() ? "ON" : "OFF",
                DebugFeatures.isOverlayVisible() ? "ON" : "OFF"));
        return lines;
    }

    public void draw() {
        if (active) {
            ColumnText columnText = new ColumnText(messages, collectStatusLines().toArray(new String[0]));
            columnText.scrollOffset = scrollOffset;
            columnText.draw();
            height = columnText.height;
            width = columnText.width;
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
        messages.add(new Message(MessageLevel.LOG, tag, message));
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        writeLog(DebugLogEntry.Level.LOG, tag, message, false);
        if(exception != null){
            exception.printStackTrace(System.out);
        }
        messages.add(new Message(MessageLevel.LOG, tag, message));
    }

    @Override
    public void error(String tag, String message) {
        writeLog(DebugLogEntry.Level.ERROR, tag, message, true);
        messages.add(new Message(MessageLevel.ERROR, tag, message));
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        writeLog(DebugLogEntry.Level.ERROR, tag, message, true);
        if(exception != null){
            exception.printStackTrace(System.err);
        }
        messages.add(new Message(MessageLevel.ERROR, tag, message));
    }

    @Override
    public void debug(String tag, String message) {
        writeLog(DebugLogEntry.Level.DEBUG, tag, message, false);
        messages.add(new Message(MessageLevel.DEBUG, tag, message));
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        writeLog(DebugLogEntry.Level.DEBUG, tag, message, false);
        if(exception != null){
            exception.printStackTrace(System.out);
        }
        messages.add(new Message(MessageLevel.DEBUG, tag, message));
    }
    public void setActive(Boolean value){
        active = value;
    }
    public Boolean getActive(){
        return active;
    }
}

class ColumnText {
    String[] strings;
    List<Message> messages = new ArrayList<>();
    float width = 0;
    float height = 0;
    float scrollOffset = 0;

    public ColumnText(List<Message> messages, String... strings) {
        this.strings = strings;
        this.messages = messages;
    }
    public void addString(String string) {
        strings = Arrays.copyOf(strings, strings.length + 1);
        strings[strings.length - 1] = string;
    }
    public void draw() {
        String ender = "____________________________";
        float displayHeight = Gdx.graphics.getHeight();
        List<Float> sizes = new ArrayList<>();
        List<Float> positions = new ArrayList<>();
        float maxWidth = 0;
        float enderHeight = 0;
        for (String topText : strings) {
            GlyphLayout layout = Views.preDrawText(topText);
            sizes.add(layout.height);
            if (maxWidth < layout.width) {
                maxWidth = layout.width;
            }
        }
        GlyphLayout layout = Views.preDrawText(ender);
        if (maxWidth < layout.width) {
            maxWidth = layout.width;
        }
        enderHeight = layout.height;
        for (int i = 0; i < strings.length; i++) {
            float margin = 10;
            float startY = displayHeight - sizes.get(0) - margin + scrollOffset;
            if (i == 0) {
                positions.add(startY);
            }
            if (i != 0) {
                float calculatedY = startY;
                for (int j = i; j > 0; j--) {
                    calculatedY -= sizes.get(j);
                    calculatedY -= margin;
                }
                positions.add(calculatedY);
            }
        }
        List<Float> sizedBottom = new ArrayList<>();
        List<Float> bottomPositions = new ArrayList<>();

        for(Message bottomTest : messages){
            GlyphLayout bottomLayout = Views.preDrawText(bottomTest.toString());
            sizedBottom.add(layout.height);
            if (maxWidth < bottomLayout.width) {
                maxWidth = bottomLayout.width;
            } 
        }

        for(int i = 0; i < messages.size(); i++ ){
            float margin = 10;
            float startY = positions.get(positions.size() - 1) - 10*2 - enderHeight;
            float calculatedY = startY;
            if(i>0){
                for (int j = i; j > 0; j--) {
                    calculatedY -= sizedBottom.get(j);
                    calculatedY -= margin;
                }
            }
            bottomPositions.add(calculatedY);
        }

        float rectHeight;
        float y;
        if(!bottomPositions.isEmpty()){
            rectHeight =  (displayHeight - bottomPositions.get(bottomPositions.size() - 1)) + sizedBottom.get(sizedBottom.size() - 1);
            y = bottomPositions.get(bottomPositions.size() - 1) - sizedBottom.get(sizedBottom.size() - 1) - 10;
        }else{
            rectHeight = (displayHeight - positions.get(positions.size() - 1)) + sizes.get(sizes.size() - 1);
            y = positions.get(positions.size() - 1) - sizes.get(sizes.size() - 1) - 10;
        }
        drawRect(y, maxWidth,
                rectHeight);
        MainService.getInstance().getFont().setColor(new Color(0.78f, 0.88f, 0.95f, 1f));
        for (int i = 0; i < strings.length; i++) {
            Views.drawText(strings[i], new ViewPosition(10, positions.get(i)));
        }
        if(!messages.isEmpty()){
            Views.drawText(ender, new ViewPosition(0, positions.get(positions.size() - 1) - 5));
        }

        for (int i = 0; i < messages.size(); i++) {
            MainService.getInstance().getFont().setColor(messages.get(i).level.getColor());
            Views.drawText(messages.get(i).toString(), new ViewPosition(10, bottomPositions.get(i)));
        }

        MainService.getInstance().getFont().setColor(Color.WHITE);
        width = maxWidth + 20;
        height = rectHeight;
    }

    void drawRect(float y, float width, float height) {
        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        MainService.getInstance().getSpriteBatch().end();
        MainService.getInstance().getShapeRenderer().begin(ShapeType.Filled);
        MainService.getInstance().getShapeRenderer().setColor(0.08f, 0.09f, 0.12f, 0.82f);
        MainService.getInstance().getShapeRenderer().rect(0, y, width + 20, height + 10);
        MainService.getInstance().getShapeRenderer().end();
        MainService.getInstance().getShapeRenderer().begin(ShapeType.Line);
        MainService.getInstance().getShapeRenderer().setColor(0.25f, 0.28f, 0.35f, 0.95f);
        MainService.getInstance().getShapeRenderer().rect(0, y, width + 20, height + 10);
        MainService.getInstance().getShapeRenderer().end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
        MainService.getInstance().getSpriteBatch().begin();
    }
}
class Message{
    MessageLevel level;
    String tag;
    String message;
    Message(MessageLevel level, String tag, String message){
        this.level = level;
        this.tag = tag;
        this.message = message;
    }
    @Override
    public String toString(){   
        return String.format("[%s]%s: %s",level,tag, message);
    }
}
enum MessageLevel{
    LOG(Color.GREEN),
    ERROR(Color.RED),
    DEBUG(Color.CYAN);
    private final Color color;

    public Color getColor(){
        return color;
    }
    MessageLevel(Color color){
        this.color = color;
    }
}