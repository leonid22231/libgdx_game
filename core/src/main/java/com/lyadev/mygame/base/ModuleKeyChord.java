package com.lyadev.mygame.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

/**
 * Key chord: groups are AND; keys inside a group are OR
 * (e.g. CtrlLeft|CtrlRight + AltLeft|AltRight).
 */
public final class ModuleKeyChord {
    private final int[][] groups;
    private final String label;

    private ModuleKeyChord(int[][] groups, String label) {
        this.groups = groups;
        this.label = label;
    }

    public static ModuleKeyChord of(int keycode) {
        return new ModuleKeyChord(new int[][]{{keycode}}, Keys.toString(keycode));
    }

    /** AND of OR-groups. Example: {@code ofAny(CTRL_L, CTRL_R), ofAny(ALT_L, ALT_R)}. */
    public static ModuleKeyChord ofGroups(int[]... groups) {
        StringBuilder label = new StringBuilder();
        int[][] copy = new int[groups.length][];
        for(int i = 0; i < groups.length; i++){
            copy[i] = Arrays.copyOf(groups[i], groups[i].length);
            Arrays.sort(copy[i]);
            if(i > 0){
                label.append('+');
            }
            label.append(groupLabel(copy[i]));
        }
        return new ModuleKeyChord(copy, label.toString());
    }

    public static int[] any(int... keycodes) {
        return keycodes;
    }

    public boolean matches() {
        if(Gdx.input == null){
            return false;
        }
        for(int[] group : groups){
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

    /** True if this chord mentions the keycode in any group. */
    public boolean containsKey(int keycode) {
        for(int[] group : groups){
            for(int key : group){
                if(key == keycode){
                    return true;
                }
            }
        }
        return false;
    }

    public int specificity() {
        return groups.length;
    }

    public String getLabel() {
        return label;
    }

    /** Stable id for conflict checks. */
    public String signature() {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < groups.length; i++){
            if(i > 0){
                builder.append('|');
            }
            for(int j = 0; j < groups[i].length; j++){
                if(j > 0){
                    builder.append(',');
                }
                builder.append(groups[i][j]);
            }
        }
        return builder.toString();
    }

    public List<int[]> getGroups() {
        List<int[]> list = new ArrayList<>(groups.length);
        for(int[] group : groups){
            list.add(Arrays.copyOf(group, group.length));
        }
        return Collections.unmodifiableList(list);
    }

    private static String groupLabel(int[] group) {
        if(group.length == 1){
            return pretty(group[0]);
        }
        boolean allCtrl = true;
        boolean allAlt = true;
        for(int key : group){
            if(key != Keys.CONTROL_LEFT && key != Keys.CONTROL_RIGHT){
                allCtrl = false;
            }
            if(key != Keys.ALT_LEFT && key != Keys.ALT_RIGHT){
                allAlt = false;
            }
        }
        if(allCtrl){
            return "Ctrl";
        }
        if(allAlt){
            return "Alt";
        }
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < group.length; i++){
            if(i > 0){
                builder.append('/');
            }
            builder.append(pretty(group[i]));
        }
        return builder.toString();
    }

    private static String pretty(int keycode) {
        String raw = Keys.toString(keycode);
        if(raw == null){
            return String.valueOf(keycode);
        }
        return raw.replace("L-Ctrl", "Ctrl").replace("R-Ctrl", "Ctrl")
                .replace("L-Alt", "Alt").replace("R-Alt", "Alt")
                .replace("L-Shift", "Shift").replace("R-Shift", "Shift");
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object other) {
        if(this == other){
            return true;
        }
        if(!(other instanceof ModuleKeyChord)){
            return false;
        }
        return signature().equals(((ModuleKeyChord) other).signature());
    }

    @Override
    public int hashCode() {
        return signature().hashCode();
    }
}
