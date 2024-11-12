package com.lyadev.mygame.models;

import java.util.function.Function;

import com.badlogic.gdx.Gdx;

public class CustomThreadService {
    private String name;
    private Boolean isRunning;
    private Runnable runnable, setup;
    private Thread thread;
    private long sleepTime = 0;

    public CustomThreadService(String name, Runnable runnable) {
        this.name = name;
        this.runnable = runnable;
    }
    public CustomThreadService(String name, Runnable runnable, long sleepTime) {
        this.name = name;
        this.runnable = runnable;
        this.sleepTime = sleepTime;
    }

    public void start() {
        Gdx.app.log(String.format("Thread[%s]", name), "is starting...");
        isRunning = true;
        if(setup!=null){
            setup.run();
        }
        thread = new Thread(() -> {
            Gdx.app.log(String.format("Thread[%s]", name), "is started.");
            while(isRunning){
                runnable.run();
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Gdx.app.error(String.format("Thread[%s]", name), "Interrupted", e);
                }
            }
            Gdx.app.log(String.format("Thread[%s]", name), "is stopped.");
        });
        thread.start();
    }
    public void stop(){
        isRunning = false;
        thread.interrupt();
        Gdx.app.log(String.format("Thread[%s]", name), "is stopping...");
    }
}
