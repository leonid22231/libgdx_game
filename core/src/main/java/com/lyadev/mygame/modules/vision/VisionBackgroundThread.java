package com.lyadev.mygame.modules.vision;

import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.utils.TimeUtils;

/**
 * Фоновый poll для одного VisionModule. Вся логика — в {@link VisionModule#runBackgroundScan()}.
 */
final class VisionBackgroundThread extends Thread {
    private final VisionModule owner;
    private volatile boolean active = true;
    private final long startTime = TimeUtils.millis();

    VisionBackgroundThread(VisionModule owner, String threadName) {
        super(threadName);
        this.owner = owner;
    }

    @Override
    public void run() {
        while(active && !Thread.currentThread().isInterrupted()){
            owner.runBackgroundScan();
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
    }

    void shutdown() {
        active = false;
        interrupt();
    }

    int getRuntimeSeconds() {
        return (int) ((TimeUtils.millis() - startTime) / 1000);
    }

    boolean isRunning() {
        return active && isAlive();
    }
}
