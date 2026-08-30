package com.lyadev.mygame.modules.vision;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import java.util.function.Consumer;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.base.ModuleEventChannel;
import com.lyadev.mygame.debug.DebugFeatures;
import com.lyadev.mygame.enums.MoveType;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.services.MainService;
import com.lyadev.mygame.utils.CircleSector;
import com.lyadev.mygame.utils.LineFromRect;
import com.lyadev.mygame.utils.Position;
import com.lyadev.mygame.world.GlobalWorld;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VisionModule extends EntityModule {
    private static final float VISION_DEGREES = 90f;

    private final VisionSettings settings;
    private final ModuleEventChannel<EntitySpottedEvent> entitySpottedEvents = createEventChannel();
    private VisionTracker visionTracker;
    private VisionBackgroundThread backgroundThread;
    private volatile boolean backgroundReady = false;
    private float currentDegrees1 = 0;
    private float currentDegrees2 = 0;

    @Override
    public String getName() {
        return "vision_module";
    }

    @Override
    public void init() {
        Entity entity = getEntity();
        visionTracker = new VisionTracker(entity);
        visionTracker.updateContour();

        VisionRegistry.register(this);
        backgroundThread = new VisionBackgroundThread(this, entity.getTAG());
        backgroundThread.start();
    }

    @Override
    public void act(float delta) {
        entitySpottedEvents.flushPending();
        if(visionTracker != null){
            visionTracker.updateContour();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(!DebugFeatures.isVisionLinesVisible() || visionTracker == null){
            return;
        }
        SelectableModule selectable = SelectableModule.from(getEntity());
        if(selectable != null && !selectable.shouldRender()){
            return;
        }
        batch.end();
        visionTracker.drawContour();
        drawDebugVision();
        batch.begin();
    }

    @Override
    public void dispose() {
        if(backgroundThread != null){
            backgroundThread.shutdown();
            backgroundThread = null;
        }
        VisionRegistry.unregister(this);
        entitySpottedEvents.clear();
        super.dispose();
    }

    /** Подписчик может передать {@code this} явно — надёжнее, чем только init()-контекст. */
    public void onEntitySpotted(EntityModule subscriber, Consumer<EntitySpottedEvent> handler) {
        entitySpottedEvents.subscribe(subscriber, handler);
    }

    /**
     * Подписка из {@code init()} другого модуля ({@link EntityModule#currentInitializingModule()}).
     * События доставляются в {@link #act(float)} на main thread.
     */
    public void onEntitySpotted(Consumer<EntitySpottedEvent> handler) {
        onEntitySpotted(currentInitializingModule(), handler);
    }

    /** Вызывается только из {@link VisionBackgroundThread} внутри пакета vision. */
    void runBackgroundScan() {
        if(visionTracker == null){
            return;
        }
        visionTracker.updateContour();
        backgroundReady = true;

        if(!shouldScan()){
            return;
        }
        if(!allPeerModulesReady()){
            return;
        }

        for(VisionModule peer : VisionRegistry.peers(this)){
            VisionScanner.ScanResult result = VisionScanner.scan(this, peer);
            if(result.isVisible()){
                onSpotted(peer, result.getSpottedLines());
            } else {
                onLost(peer);
            }
        }
    }

    void receiveSpottedLines(LineFromRect[] lines) {
        if(visionTracker != null){
            visionTracker.applySpottedLines(lines);
        }
    }

    void clearSpottedLinesFromPeer() {
        if(visionTracker != null){
            visionTracker.clearSpottedLines();
        }
    }

    LineFromRect[] getRectLines() {
        return visionTracker == null ? new LineFromRect[0] : visionTracker.getRectLines();
    }

    float getVisionScore() {
        return settings.getVisibleRadius();
    }

    Position getCenterPosition() {
        return getEntity().getCenterPosition();
    }

    CircleSector getCircleSector() {
        updateVisionDegrees();
        Position center = getEntity().getCenterPosition();
        return new CircleSector(center.getX(), center.getY(), getVisionScore(), currentDegrees1, currentDegrees2);
    }

    public void resetVisionTracking() {
        if(visionTracker == null){
            return;
        }
        for(Entity target : visionTracker.snapshotVisibleEntities()){
            VisionModule targetVision = VisionModule.from(target);
            if(targetVision != null){
                notifyTargetLost(targetVision);
            }
        }
        visionTracker.resetVisionTracking();
    }

    public int getVisibleEntityCount() {
        return visionTracker == null ? 0 : visionTracker.getVisibleEntityCount();
    }

    public static VisionModule from(Entity entity) {
        return entity.getModule(VisionModule.class);
    }

    VisionBackgroundThread getBackgroundThreadForDebug() {
        return backgroundThread;
    }

    boolean isBackgroundReady() {
        return backgroundReady;
    }

    private boolean shouldScan() {
        if(!GlobalWorld.isReady()){
            return false;
        }
        SelectableModule selectable = SelectableModule.from(getEntity());
        return selectable != null && selectable.isActive();
    }

    private boolean allPeerModulesReady() {
        for(VisionModule peer : VisionRegistry.peers(this)){
            if(!peer.isBackgroundReady()){
                return false;
            }
        }
        return true;
    }

    private void onSpotted(VisionModule target, LineFromRect[] spottedLines) {
        boolean newlyVisible = !visionTracker.isTrackingVisible(target.getEntity());
        visionTracker.rememberVisible(target.getEntity());
        notifyTargetSpotted(target, spottedLines);
        if(newlyVisible && entitySpottedEvents.hasSubscribers()){
            entitySpottedEvents.publish(
                    new EntitySpottedEvent(getEntity(), target.getEntity()));
        }
    }

    private void onLost(VisionModule target) {
        visionTracker.forgetVisible(target.getEntity());
        notifyTargetLost(target);
    }

    /** Модуль → модуль: observer сообщает target о spotted. */
    private void notifyTargetSpotted(VisionModule target, LineFromRect[] spottedLines) {
        SelectableModule targetSelectable = SelectableModule.from(target.getEntity());
        if(targetSelectable != null){
            targetSelectable.setVisibleByVision(true);
        }
        target.receiveSpottedLines(spottedLines);
    }

    /** Модуль → модуль: observer сообщает target что потерян из sight. */
    private void notifyTargetLost(VisionModule target) {
        SelectableModule targetSelectable = SelectableModule.from(target.getEntity());
        if(targetSelectable != null){
            targetSelectable.setVisibleByVision(false);
        }
        target.clearSpottedLinesFromPeer();
    }

    private void updateVisionDegrees() {
        MoveType facing = MoveType.DOWN;
        MovementModule movementModule = MovementModule.from(getEntity());
        if(movementModule != null){
            facing = movementModule.getFacingDirection();
        }

        float deg = VISION_DEGREES / 2;
        float degrees1 = deg;
        float degrees2 = deg * -1;
        float round = 180 - deg * 2;
        switch(facing){
            case DOWN:
                degrees1 = (90 - deg) * -1;
                degrees2 = (90 + deg) * -1;
                break;
            case UP:
                degrees1 = (90 + deg);
                degrees2 = (90 - deg);
                break;
            case LEFT:
                degrees1 = (deg + round) * -1;
                degrees2 = deg + round;
                break;
            case RIGHT:
                degrees1 = deg;
                degrees2 = deg * -1;
                break;
        }
        currentDegrees1 = degrees1;
        currentDegrees2 = degrees2;
    }

    private void drawDebugVision() {
        SelectableModule selectable = SelectableModule.from(getEntity());
        if(selectable == null || !selectable.isActive()){
            return;
        }
        updateVisionDegrees();
        Position center = getEntity().getCenterPosition();
        float radius = getVisionScore();
        if(radius <= 0){
            return;
        }

        double x = center.getX() + radius * Math.cos(Math.toRadians(currentDegrees1));
        double y = center.getY() + radius * Math.sin(Math.toRadians(currentDegrees1));
        double x1 = center.getX() + radius * Math.cos(Math.toRadians(currentDegrees2));
        double y1 = center.getY() + radius * Math.sin(Math.toRadians(currentDegrees2));

        MainService.getInstance().getShapeRenderer().begin(ShapeType.Line);
        MainService.getInstance().getShapeRenderer().setColor(Color.YELLOW);
        MainService.getInstance().getShapeRenderer().circle(center.getX(), center.getY(), radius);
        MainService.getInstance().getShapeRenderer().line(center.getX(), center.getY(), (float) x, (float) y);
        MainService.getInstance().getShapeRenderer().line(center.getX(), center.getY(), (float) x1, (float) y1);
        MainService.getInstance().getShapeRenderer().end();
    }
}
