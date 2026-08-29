package com.lyadev.mygame.utils.listeners;

public class EntityListener {
    public EntityListenerThread thread;

    public EntityListener(EntityListenerThread thread) {
        this.thread = thread;
    }

    public void dispose() {
        thread.setActive(false);
        thread.interrupt();
    }
}
