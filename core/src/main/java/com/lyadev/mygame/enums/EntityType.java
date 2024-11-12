package com.lyadev.mygame.enums;

import com.lyadev.mygame.entities.PlayebleEntity;

public enum EntityType {
    PLAYEBLE(PlayebleEntity.class);
    private Class<?> refClass;
    EntityType(Class<?> refClass) {
        this.refClass = refClass;
    }
    public static EntityType findByValue(Class<?> value) {
        for (EntityType type : values()) {
            if (type.refClass.equals(value)) {
                return type;
            }
        }
        return null;
    }
    public Class<?> getRefClass() {
        return refClass;
    }
}
