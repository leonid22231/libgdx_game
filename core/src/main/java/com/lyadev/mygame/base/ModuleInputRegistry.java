package com.lyadev.mygame.base;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import com.badlogic.gdx.Gdx;
import com.lyadev.mygame.base.ModuleKeyDecl.Trigger;

/**
 * Resolves {@link EntityModule#getKeyBindings()} (and system decls) into a global registry.
 * Conflicts on the same chord+trigger are rejected.
 */
public final class ModuleInputRegistry {
    private static final String TAG = "ModuleInputRegistry";

    private static final CopyOnWriteArrayList<ResolvedBinding> bindings = new CopyOnWriteArrayList<>();
    private static final Map<String, Boolean> lastHeld = new HashMap<>();

    private ModuleInputRegistry() {
        throw new UnsupportedOperationException();
    }

    /**
     * Resolve declarations from a module (call once per enabled module after deps bind).
     * Duplicate actionId from another entity of the same module type → only subscribe as listener.
     */
    public static synchronized void resolve(EntityModule module) {
        List<ModuleKeyDecl> decls = module.getKeyBindings();
        if(decls == null || decls.isEmpty()){
            return;
        }
        for(ModuleKeyDecl decl : decls){
            String actionId = qualify(module.getName(), decl.getId());
            ResolvedBinding existing = findByActionId(actionId);
            if(existing != null){
                existing.addListener(module);
                continue;
            }
            rejectIfChordConflict(module.getName(), actionId, decl);
            ResolvedBinding resolved = new ResolvedBinding(
                    actionId, module.getName(), decl, null);
            resolved.addListener(module);
            bindings.add(resolved);
            Gdx.app.debug(TAG, "Resolved " + resolved.toDebugLine());
        }
    }

    /** System / non-module bindings (editor, debug). */
    public static synchronized void resolveSystem(
            String owner,
            List<ModuleKeyDecl> decls,
            BiConsumer<String, ModuleKeyEvent> handler) {
        if(decls == null || decls.isEmpty() || handler == null){
            return;
        }
        for(ModuleKeyDecl decl : decls){
            String actionId = qualify(owner, decl.getId());
            if(findByActionId(actionId) != null){
                continue;
            }
            rejectIfChordConflict(owner, actionId, decl);
            ResolvedBinding resolved = new ResolvedBinding(actionId, owner, decl, handler);
            bindings.add(resolved);
            Gdx.app.debug(TAG, "Resolved system " + resolved.toDebugLine());
        }
    }

    public static synchronized void unregisterModule(EntityModule module) {
        for(ResolvedBinding binding : bindings){
            binding.removeListener(module);
        }
    }

    public static List<String> collectDebugLines() {
        List<String> lines = new ArrayList<>();
        for(ModuleKeyDebugRow row : collectDebugRows()){
            lines.add(String.format(Locale.ROOT, "[%s] %s  (%s / %s) - %s",
                    row.getChord(), row.getActionId(), row.getOwner(), row.getTrigger(), row.getDescription()));
        }
        return lines;
    }

    public static List<ModuleKeyDebugRow> collectDebugRows() {
        List<ModuleKeyDebugRow> rows = new ArrayList<>();
        for(ResolvedBinding binding : snapshot()){
            rows.add(new ModuleKeyDebugRow(
                    binding.decl.getChord().getLabel(),
                    binding.actionId,
                    binding.owner,
                    binding.decl.getTrigger().name(),
                    binding.decl.getDescription()));
        }
        return rows;
    }

    public static boolean onKeyDown(int keycode) {
        pollHeld();
        for(ResolvedBinding binding : snapshot()){
            if(!binding.decl.getChord().containsKey(keycode)){
                continue;
            }
            if(!binding.decl.getChord().matches()){
                continue;
            }
            Trigger trigger = binding.decl.getTrigger();
            if(trigger == Trigger.DOWN || trigger == Trigger.DOWN_UP){
                binding.dispatch(new ModuleKeyEvent(binding.actionId, trigger, true, keycode));
                return true;
            }
        }
        return false;
    }

    public static boolean onKeyUp(int keycode) {
        for(ResolvedBinding binding : snapshot()){
            if(!binding.decl.getChord().containsKey(keycode)){
                continue;
            }
            Trigger trigger = binding.decl.getTrigger();
            if(trigger != Trigger.UP && trigger != Trigger.DOWN_UP){
                continue;
            }
            if(binding.decl.getChord().specificity() == 1 || trigger == Trigger.DOWN_UP
                    || otherGroupsStillHeld(binding.decl, keycode)){
                binding.dispatch(new ModuleKeyEvent(binding.actionId, trigger, false, keycode));
                pollHeld();
                return true;
            }
        }
        pollHeld();
        return false;
    }

    public static void pollHeld() {
        for(ResolvedBinding binding : bindings){
            if(binding.decl.getTrigger() != Trigger.HOLD){
                continue;
            }
            boolean held = binding.decl.getChord().matches();
            Boolean previous = lastHeld.get(binding.actionId);
            if(previous == null || previous.booleanValue() != held){
                lastHeld.put(binding.actionId, held);
                binding.dispatch(new ModuleKeyEvent(binding.actionId, Trigger.HOLD, held, -1));
            }
        }
    }

    private static void rejectIfChordConflict(String owner, String actionId, ModuleKeyDecl decl) {
        for(ResolvedBinding existing : bindings){
            if(existing.decl.getChord().signature().equals(decl.getChord().signature())
                    && existing.decl.getTrigger() == decl.getTrigger()){
                String message = "Key conflict: " + decl.getChord().getLabel()
                        + " already used by " + existing.owner + "/" + existing.actionId
                        + ", rejected " + owner + "/" + actionId;
                Gdx.app.error(TAG, message);
                throw new IllegalStateException(message);
            }
        }
    }

    private static ResolvedBinding findByActionId(String actionId) {
        for(ResolvedBinding binding : bindings){
            if(binding.actionId.equals(actionId)){
                return binding;
            }
        }
        return null;
    }

    private static String qualify(String owner, String id) {
        if(id.indexOf('.') >= 0){
            return id;
        }
        return owner + "." + id;
    }

    private static List<ResolvedBinding> snapshot() {
        List<ResolvedBinding> copy = new ArrayList<>(bindings);
        copy.sort(Comparator
                .comparing((ResolvedBinding b) -> b.decl.getChord().specificity()).reversed()
                .thenComparing(b -> b.owner)
                .thenComparing(b -> b.actionId));
        return copy;
    }

    private static boolean otherGroupsStillHeld(ModuleKeyDecl decl, int releasedKey) {
        for(int[] group : decl.getChord().getGroups()){
            boolean hasReleased = false;
            for(int key : group){
                if(key == releasedKey){
                    hasReleased = true;
                    break;
                }
            }
            if(hasReleased){
                continue;
            }
            boolean any = false;
            for(int key : group){
                if(Gdx.input.isKeyPressed(key)){
                    any = true;
                    break;
                }
            }
            if(!any){
                return false;
            }
        }
        return true;
    }

    private static final class ResolvedBinding {
        private final String actionId;
        private final String owner;
        private final ModuleKeyDecl decl;
        private final BiConsumer<String, ModuleKeyEvent> systemHandler;
        private final CopyOnWriteArrayList<EntityModule> listeners = new CopyOnWriteArrayList<>();

        private ResolvedBinding(
                String actionId,
                String owner,
                ModuleKeyDecl decl,
                BiConsumer<String, ModuleKeyEvent> systemHandler) {
            this.actionId = actionId;
            this.owner = owner;
            this.decl = decl;
            this.systemHandler = systemHandler;
        }

        private void addListener(EntityModule module) {
            if(module != null && !listeners.contains(module)){
                listeners.add(module);
            }
        }

        private void removeListener(EntityModule module) {
            listeners.remove(module);
        }

        private void dispatch(ModuleKeyEvent event) {
            if(systemHandler != null){
                systemHandler.accept(localId(), event);
            }
            for(EntityModule module : listeners){
                if(module.isRuntimeActive()){
                    module.onKeyBinding(localId(), event);
                }
            }
        }

        private String localId() {
            int dot = actionId.lastIndexOf('.');
            if(dot < 0 || dot == actionId.length() - 1){
                return actionId;
            }
            // If id was already qualified as owner.id, pass local part to onKeyBinding
            if(actionId.startsWith(owner + ".")){
                return actionId.substring(owner.length() + 1);
            }
            return actionId;
        }

        private String toDebugLine() {
            return String.format(Locale.ROOT, "[%s] %s  (%s) - %s",
                    decl.getChord().getLabel(), actionId, owner, decl.getDescription());
        }
    }
}
