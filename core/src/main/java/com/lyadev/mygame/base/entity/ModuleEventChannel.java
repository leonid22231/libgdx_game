package com.lyadev.mygame.base.entity;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Канал событий модуля-издателя. Подписчики — только {@link EntityModule} той же entity.
 * {@link #publish(Object)} можно вызывать из фонового потока — доставка в {@link #flushPending()}
 * на main thread (вызови flush из {@code act()} издателя).
 */
public final class ModuleEventChannel<E> {
    private final CopyOnWriteArrayList<Subscription<E>> subscriptions = new CopyOnWriteArrayList<>();
    private final ConcurrentLinkedQueue<E> pendingEvents = new ConcurrentLinkedQueue<>();
    private final EntityModule publisher;

    ModuleEventChannel(EntityModule publisher) {
        this.publisher = publisher;
    }

    public void subscribe(EntityModule subscriber, Consumer<E> handler) {
        if(subscriber.getEntity() != publisher.getEntity()){
            throw new IllegalStateException(
                    publisher.getName() + " events can only be subscribed by modules on the same entity");
        }
        Subscription<E> subscription = new Subscription<>(subscriber, handler);
        subscriptions.add(subscription);
        subscriber.registerEventCleanup(() -> remove(subscription));
    }

    /** Поставить событие в очередь (thread-safe). Без подписчиков — no-op. */
    public void publish(E event) {
        if(!hasSubscribers()){
            return;
        }
        pendingEvents.offer(event);
    }

    /** Вызвать из {@code act()} модуля-издателя — доставляет события подписчикам на main thread. */
    public void flushPending() {
        E event;
        while((event = pendingEvents.poll()) != null){
            dispatch(event);
        }
    }

    public boolean hasSubscribers() {
        for(Subscription<E> subscription : subscriptions){
            if(subscription.isActive()){
                return true;
            }
        }
        return false;
    }

    public void clear() {
        pendingEvents.clear();
        subscriptions.clear();
    }

    private void dispatch(E event) {
        for(Subscription<E> subscription : subscriptions){
            if(!subscription.isActive()){
                continue;
            }
            if(!subscription.subscriber.isRuntimeActive()){
                continue;
            }
            subscription.handler.accept(event);
        }
    }

    private void remove(Subscription<E> subscription) {
        subscription.unsubscribe();
        subscriptions.remove(subscription);
    }

    private static final class Subscription<E> {
        private final EntityModule subscriber;
        private final Consumer<E> handler;
        private boolean active = true;

        Subscription(EntityModule subscriber, Consumer<E> handler) {
            this.subscriber = subscriber;
            this.handler = handler;
        }

        boolean isActive() {
            return active;
        }

        void unsubscribe() {
            active = false;
        }
    }
}
