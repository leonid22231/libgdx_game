package com.lyadev.mygame.utils.listeners;

public class EntityListener{
    public Object entity;
    public EntityListenerThread thread;
    public EntityListener(Object entity, EntityListenerThread thread){
        this.entity = entity;
        this.thread = thread;
    }
    public void dispose() {
        thread.setActive(false);
        thread.interrupt();
    }
}
