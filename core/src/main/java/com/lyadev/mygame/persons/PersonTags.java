package com.lyadev.mygame.persons;

import java.util.List;

import com.lyadev.mygame.base.Entity;

public final class PersonTags {
    private PersonTags() {
        throw new UnsupportedOperationException();
    }

    public static String unique(String baseTag, List<Entity> entities) {
        if(entities == null || baseTag == null){
            return baseTag;
        }
        String candidate = baseTag;
        int index = 2;
        while(tagExists(candidate, entities)){
            candidate = baseTag + index;
            index++;
        }
        return candidate;
    }

    private static boolean tagExists(String tag, List<Entity> entities) {
        for(Entity entity : entities){
            if(entity.getTag().equalsIgnoreCase(tag)){
                return true;
            }
        }
        return false;
    }
}
