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
import com.lyadev.mygame.UI;
import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.entities.PlayebleEntity;
import com.lyadev.mygame.utils.ViewPosition;
import com.lyadev.mygame.utils.listeners.EntityListenerThread;
import com.lyadev.mygame.views.Views;
import com.lyadev.mygame.world.GlobalWorld;

public class MyLogger implements ApplicationLogger {
    List<Message> messages = new ArrayList<Message>();
    final String VERSION = "1.0";
    Boolean active = true;
    Boolean isFocused = false;
    float height = 0;
    float width = 0;
    float scrollOffset = 0;
    float mousePositionX = 0;
    float mousePositionY = 0;
    List<String> lastKeys = new ArrayList<String>();
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
        if(mousePositionY < Gdx.graphics.getHeight() && mousePositionY > 0 && mousePositionX < width && mousePositionX > 0){
            isFocused = true;
        }else{
            isFocused = false;
        }
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
    public void draw() {
        if (active) {
            long elapsedTime = TimeUtils.timeSinceMillis(startTime);
            double seconds = elapsedTime / 1000;
            String activeTime;
            if(seconds<60){
                activeTime = String.format("%02d s.", elapsedTime / 1000);
            }else{
                activeTime = String.format("%02d m. %02d s.", (elapsedTime / 1000)/60, (elapsedTime / 1000)%60);
            }
            String version = String.format("Logger V. %s", VERSION);
            String fps = String.format("Fps:[%s], DeltaTime: [%s]", Gdx.graphics.getFramesPerSecond(), Gdx.graphics.getDeltaTime() * 1000);
            String mousePosition = String.format("Mouse Position: x[%s], y[%s]", mousePositionX, mousePositionY);
            String lastKey = String.format("Last Keys: %s", lastKeys.toString());
            String focused = String.format("Focused: %s", isFocused? "Yes" : "No");
            String lScroll = String.format("Scroll: %s", lastScroll<0?"Up": "Down");
            String lastTap = String.format("Last Tap: x[%s], y[%s], pointer[%s], button[%s]", lastTapScreenX, lastTapScreenY, lastTapPointer, lastTapButton);
            int userCount = GlobalWorld.entities.size();
            String players = String.format("Players count: %s", userCount);
            String currentPlayer = String.format("Current: %s", GlobalWorld.player!=null? GlobalWorld.player.toString() : "None");
            //TODO: Add info about current circle sector for player and all entities.
            //String currentCircle = String.format("Current Circle: %s", GlobalWorld.player!=null? GlobalWorld.player.getCircleSector().toString() : "None");
            ColumnText columnText = new ColumnText(messages, version,activeTime, focused, lScroll, fps, mousePosition, lastKey, lastTap, currentPlayer,players);
            
            for(Entity entity : GlobalWorld.entities){  
                if(!entity.getStatus().isActive()){
                    columnText.addString(entity.toString());
                    /* TODO: Add info about circle sector for entity and player. 
                     * columnText.addString(entity.getCircleSector().toString());
                     */
                    
                } 
            }
            String threadCount = String.format("Thread Count: %s", GlobalWorld.listeners.size());
            columnText.addString(threadCount);
            for(int i = 0; i < GlobalWorld.listeners.size(); i++){
                EntityListenerThread thread = GlobalWorld.listeners.get(i).thread;
                if(thread.isActive()){
                    columnText.addString(String.format("%s runningTime %s", thread.getName(), thread.getRuntimeSeconds()));
                }else{
                    columnText.addString(String.format("%s error", thread.getName()));
                }
                
            }
            columnText.scrollOffset = scrollOffset;
            
            columnText.draw();
            
            height = columnText.height;
            width = columnText.width;
        }
    }

    @Override
    public void log(String tag, String message) {
        messages.add(new Message(MessageLevel.LOG, tag, message));
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        messages.add(new Message(MessageLevel.LOG, tag, message));
    }

    @Override
    public void error(String tag, String message) {
        messages.add(new Message(MessageLevel.ERROR, tag, message));
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        messages.add(new Message(MessageLevel.ERROR, tag, message));
    }

    @Override
    public void debug(String tag, String message) {
        messages.add(new Message(MessageLevel.DEBUG, tag, message));
        
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
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
    List<Message> messages = new ArrayList<Message>();
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
        List<Float> sizes = new ArrayList<Float>();
        List<Float> positions = new ArrayList<Float>();
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
        List<Float> sizedBottom = new ArrayList<Float>();
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
        UI.font.setColor(Color.RED);
        for (int i = 0; i < strings.length; i++) {
            Views.drawText(strings[i], new ViewPosition(10, positions.get(i)));
        }
        if(!messages.isEmpty()){
            Views.drawText(ender, new ViewPosition(0, positions.get(positions.size() - 1) - 5));
        }
        
        for (int i = 0; i < messages.size(); i++) {
            UI.font.setColor(messages.get(i).level.getColor());
            Views.drawText(messages.get(i).toString(), new ViewPosition(10, bottomPositions.get(i)));
        }

        UI.font.setColor(Color.WHITE);
        width = maxWidth + 20;
        height = rectHeight;
    }

    void drawRect(float y, float width, float height) {
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        UI.batch.end();
        UI.shape.begin(ShapeType.Line);
        UI.shape.setColor(new Color(Color.BLUE.r, Color.BLUE.g, Color.BLUE.b, 0f));
        UI.shape.rect(0, y, width + 20, height + 10);
        UI.shape.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
        UI.batch.begin();
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
    private Color color;

    public Color getColor(){
        return color;
    }
    MessageLevel(Color color){
        this.color = color;
    }
}