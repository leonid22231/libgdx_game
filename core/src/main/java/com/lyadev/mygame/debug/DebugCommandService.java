package com.lyadev.mygame.debug;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.services.MainService;
import com.lyadev.mygame.world.GlobalWorld;

public final class DebugCommandService {
    private DebugCommandService() {
        throw new UnsupportedOperationException();
    }

    public static String execute(String rawCommand) {
        if(rawCommand == null || rawCommand.trim().isEmpty()){
            return "Empty command";
        }
        String[] parts = rawCommand.trim().split("\\s+");
        String command = parts[0].toLowerCase();

        switch(command){
            case "help":
                return helpText();
            case "clear":
                MainService.getInstance().getLogger().clearLogs();
                return "In-game log buffer cleared";
            case "vision":
                return handleVision(parts);
            case "overlay":
                return handleOverlay(parts);
            case "entities":
                return listEntities();
            case "keys":
            case "bindings":
                return listKeyBindings();
            case "player":
                return handlePlayer(parts);
            case "spawn":
                return handleSpawn(parts);
            case "worlds":
                return DebugWorldService.listWorlds();
            case "world":
                return handleWorld(parts);
            default:
                return "Unknown command: " + command + ". Type help";
        }
    }

    private static String helpText() {
        return String.join(System.lineSeparator(),
                "Commands:",
                "  help",
                "  clear",
                "  vision [on|off|toggle]",
                "  overlay [on|off|toggle]",
                "  entities",
                "  keys | bindings",
                "  player <tag>",
                "  spawn <man|woman|newgirl|cat>",
                "  worlds",
                "  world travel <id> [col row]   — move active entity",
                "  world switch <id>             — activate world, no entity move",
                "Keys: see `keys` command / Debug Console Keys tab");
    }

    private static String listKeyBindings() {
        java.util.List<String> lines = com.lyadev.mygame.base.ModuleInputRegistry.collectDebugLines();
        if(lines.isEmpty()){
            return "No key bindings registered";
        }
        StringBuilder builder = new StringBuilder("Key bindings:").append(System.lineSeparator());
        for(String line : lines){
            builder.append("  ").append(line).append(System.lineSeparator());
        }
        return builder.toString().trim();
    }

    private static String handleVision(String[] parts) {
        if(parts.length == 1){
            DebugFeatures.toggleVisionLines();
        } else {
            switch(parts[1].toLowerCase()){
                case "on":
                    DebugFeatures.setVisionLinesVisible(true);
                    break;
                case "off":
                    DebugFeatures.setVisionLinesVisible(false);
                    break;
                case "toggle":
                    DebugFeatures.toggleVisionLines();
                    break;
                default:
                    return "Usage: vision [on|off|toggle]";
            }
        }
        return "Vision lines: " + (DebugFeatures.isVisionLinesVisible() ? "ON" : "OFF");
    }

    private static String handleOverlay(String[] parts) {
        if(parts.length == 1){
            DebugFeatures.toggleOverlay();
        } else {
            switch(parts[1].toLowerCase()){
                case "on":
                    DebugFeatures.setOverlayVisible(true);
                    break;
                case "off":
                    DebugFeatures.setOverlayVisible(false);
                    break;
                case "toggle":
                    DebugFeatures.toggleOverlay();
                    break;
                default:
                    return "Usage: overlay [on|off|toggle]";
            }
        }
        return "In-game overlay: " + (DebugFeatures.isOverlayVisible() ? "ON" : "OFF");
    }

    private static String listEntities() {
        if(GlobalWorld.entities == null || GlobalWorld.entities.isEmpty()){
            return "No entities";
        }
        StringBuilder builder = new StringBuilder();
        for(Entity entity : GlobalWorld.entities){
            builder.append(entity.toString()).append(System.lineSeparator());
        }
        return builder.toString().trim();
    }

    private static String handlePlayer(String[] parts) {
        if(parts.length < 2){
            return "Usage: player <tag>";
        }
        String tag = parts[1];
        for(Entity entity : GlobalWorld.entities){
            if(entity.getTag().equalsIgnoreCase(tag)){
                GlobalWorld.setActivePlayer(entity);
                return "Active player: " + entity.getTAG();
            }
        }
        return "Player not found: " + tag;
    }

    private static String handleSpawn(String[] parts) {
        if(parts.length < 2){
            return "Usage: spawn <man|woman|newgirl|cat>";
        }
        return DebugEntityService.spawnPlayablePreset(parts[1]);
    }

    private static String handleWorld(String[] parts) {
        if(parts.length < 2){
            return "Usage: world travel <id> [col row] | world switch <id>";
        }
        String mode = parts[1].toLowerCase();
        if("travel".equals(mode)){
            if(parts.length < 3){
                return "Usage: world travel <id> [col row]";
            }
            Integer col = null;
            Integer row = null;
            if(parts.length >= 5){
                try {
                    col = Integer.parseInt(parts[3]);
                    row = Integer.parseInt(parts[4]);
                } catch(NumberFormatException error) {
                    return "Usage: world travel <id> [col row]";
                }
            } else if(parts.length == 4){
                return "Usage: world travel <id> [col row]";
            }
            return DebugWorldService.travelWithActiveEntity(parts[2], col, row);
        }
        if("switch".equals(mode)){
            if(parts.length < 3){
                return "Usage: world switch <id>";
            }
            return DebugWorldService.switchWithoutEntity(parts[2]);
        }
        // Back-compat: `world <id>` = travel with active entity
        Integer col = null;
        Integer row = null;
        if(parts.length >= 3){
            if(parts.length >= 4){
                try {
                    col = Integer.parseInt(parts[2]);
                    row = Integer.parseInt(parts[3]);
                } catch(NumberFormatException error) {
                    return "Usage: world travel <id> [col row] | world switch <id>";
                }
            } else {
                return "Usage: world travel <id> [col row] | world switch <id>";
            }
        }
        return DebugWorldService.travelWithActiveEntity(parts[1], col, row);
    }
}
