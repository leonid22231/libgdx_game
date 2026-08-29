package com.lyadev.mygame.debug;

public class DebugLogEntry {
    public enum Level {
        LOG,
        ERROR,
        DEBUG,
        COMMAND,
        STATUS
    }

    private final Level level;
    private final String tag;
    private final String message;

    public DebugLogEntry(Level level, String tag, String message) {
        this.level = level;
        this.tag = tag;
        this.message = message;
    }

    public Level getLevel() {
        return level;
    }

    public String getTag() {
        return tag;
    }

    public String getMessage() {
        return message;
    }

    public String formatLine() {
        if(tag == null || tag.isEmpty()){
            return message;
        }
        return String.format("[%s][%s] %s", level.name(), tag, message);
    }
}
