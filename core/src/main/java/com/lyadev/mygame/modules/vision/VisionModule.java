package com.lyadev.mygame.modules.vision;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.function.Consumer;

import com.lyadev.mygame.base.entity.Entity;
import com.lyadev.mygame.base.entity.EntityModule;
import com.lyadev.mygame.base.entity.ModuleEventChannel;
import com.lyadev.mygame.debug.ModuleDebugPanel;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.modules.selectable.SelectableModule;
import com.lyadev.mygame.utils.CircleSector;
import com.lyadev.mygame.utils.LineFromRect;
import com.lyadev.mygame.utils.Position;
import com.lyadev.mygame.base.world.GlobalWorld;

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
    private Float debugRadiusOverride;

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
        // Vision debug shapes are batched once via VisionDebug.drawOverlays after stage.draw.
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
        return debugRadiusOverride != null ? debugRadiusOverride : settings.getVisibleRadius();
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

    @Override
    public void populateDebugScreen(ModuleDebugPanel panel) {
        super.populateDebugScreen(panel);
        updateVisionDegrees();
        panel.line("visibleRadius", getVisionScore());
        panel.line("visibleCount", getVisibleEntityCount());
        panel.line("backgroundReady", backgroundReady);
        panel.line("coneHalfDegrees", VISION_DEGREES / 2f);
        panel.line("coneFrom", String.format("%.1f°", currentDegrees1));
        panel.line("coneTo", String.format("%.1f°", currentDegrees2));
        panel.numberField("visible_radius", "Visible radius", getVisionScore(), 20d, 800d);
        panel.action("reset_vision", "Reset vision tracking");
    }

    @Override
    public String handleDebugFieldChange(String fieldId, String value) {
        if("visible_radius".equals(fieldId)){
            try {
                float radius = Float.parseFloat(value);
                if(radius <= 0f){
                    return "Radius must be > 0";
                }
                debugRadiusOverride = radius;
                return "Visible radius: " + radius;
            } catch(NumberFormatException error) {
                return "Invalid number: " + value;
            }
        }
        return super.handleDebugFieldChange(fieldId, value);
    }

    @Override
    public String handleDebugAction(String actionId) {
        if("reset_vision".equals(actionId)){
            resetVisionTracking();
            return "Vision tracking reset";
        }
        return super.handleDebugAction(actionId);
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
        float center = -90f;
        MovementModule movementModule = MovementModule.from(getEntity());
        if(movementModule != null){
            center = movementModule.getLookAngleForVision();
        }

        float half = VISION_DEGREES / 2f;
        currentDegrees1 = center + half;
        currentDegrees2 = center - half;
    }

    /** Called from {@link VisionDebug#drawOverlays} inside one ShapeRenderer begin/end. */
    void drawDebugShapes(ShapeRenderer shape) {
        if(visionTracker == null){
            return;
        }
        SelectableModule selectable = SelectableModule.from(getEntity());
        if(selectable != null && !selectable.shouldRender()){
            return;
        }
        visionTracker.drawContour(shape);
        if(selectable == null || !selectable.isActive()){
            return;
        }
        updateVisionDegrees();
        Position center = getEntity().getCenterPosition();
        float radius = getVisionScore();
        if(radius <= 0){
            return;
        }

        float x = center.getX() + radius * (float) Math.cos(Math.toRadians(currentDegrees1));
        float y = center.getY() + radius * (float) Math.sin(Math.toRadians(currentDegrees1));
        float x1 = center.getX() + radius * (float) Math.cos(Math.toRadians(currentDegrees2));
        float y1 = center.getY() + radius * (float) Math.sin(Math.toRadians(currentDegrees2));

        shape.setColor(Color.YELLOW);
        shape.circle(center.getX(), center.getY(), radius, 28);
        shape.line(center.getX(), center.getY(), x, y);
        shape.line(center.getX(), center.getY(), x1, y1);
    }
}
