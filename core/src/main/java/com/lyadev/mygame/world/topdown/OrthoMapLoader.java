package com.lyadev.mygame.world.topdown;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Loads orthogonal maps from JSON under {@code assets/maps/}.
 *
 * <pre>
 * {
 *   "name": "forest_lake",
 *   "tileSize": 16,
 *   "tileset": "tiles/topdown/Tileset.png",
 *   "tilesetColumns": 8,
 *   "width": 40,
 *   "height": 28,
 *   "ground": [ "0,0,0,...", "0,1,0,..." ],
 *   "decor": [
 *     { "src": [8,8,48,64], "col": 4, "row": 7 }
 *   ]
 * }
 * </pre>
 * {@code ground} — one string per row, CSV of local tile ids (-1 = empty).
 * Tile id = row * tilesetColumns + col in the tileset image.
 */
public final class OrthoMapLoader {
    private static final String TAG = "OrthoMapLoader";

    private OrthoMapLoader() {
        throw new UnsupportedOperationException();
    }

    public static OrthoMapData load(String mapPath) {
        FileHandle file = Gdx.files.internal(mapPath);
        if(!file.exists()){
            throw new IllegalStateException("Map not found: " + mapPath);
        }
        JSONObject root = new JSONObject(file.readString("UTF-8"));
        String name = root.optString("name", mapPath);
        int tileSize = root.optInt("tileSize", 16);
        String tileset = root.getString("tileset");
        int tilesetColumns = root.optInt("tilesetColumns", 8);
        int width = root.getInt("width");
        int height = root.getInt("height");

        OrthoTileMap map = new OrthoTileMap(width, height);
        JSONArray ground = root.getJSONArray("ground");
        if(ground.length() != height){
            Gdx.app.error(TAG, name + " ground rows=" + ground.length() + " expected height=" + height);
        }
        // First JSON line = top of map (north / high Y). Engine row 0 = bottom.
        int rows = Math.min(height, ground.length());
        for(int fileRow = 0; fileRow < rows; fileRow++){
            int mapRow = height - 1 - fileRow;
            String line = ground.getString(fileRow).trim();
            String[] cells = line.split(",");
            for(int col = 0; col < Math.min(width, cells.length); col++){
                map.set(col, mapRow, Integer.parseInt(cells[col].trim()));
            }
        }

        int spawnCol = width / 2;
        int spawnRow = height / 2;
        JSONObject spawn = root.optJSONObject("spawn");
        if(spawn != null){
            spawnCol = spawn.optInt("col", spawnCol);
            spawnRow = spawn.optInt("row", spawnRow);
        }

        List<OrthoMapData.DecorSpec> decor = new ArrayList<>();
        String decorAtlas = root.optString("decorAtlas", "tiles/topdown/decor/Decorations.png");
        JSONArray decorArr = root.optJSONArray("decor");
        if(decorArr != null){
            for(int i = 0; i < decorArr.length(); i++){
                JSONObject d = decorArr.getJSONObject(i);
                JSONArray src = d.getJSONArray("src");
                String assetId = d.optString("asset", "prop_" + i);
                decor.add(new OrthoMapData.DecorSpec(
                        assetId,
                        decorAtlas,
                        src.getInt(0), src.getInt(1), src.getInt(2), src.getInt(3),
                        d.getInt("col"), d.getInt("row")));
            }
        }

        Gdx.app.log(TAG, "Loaded map '" + name + "' " + width + "x" + height
                + " decor=" + decor.size());
        return new OrthoMapData(name, tileSize, tileset, tilesetColumns, decorAtlas, map, decor, spawnCol, spawnRow);
    }
}
