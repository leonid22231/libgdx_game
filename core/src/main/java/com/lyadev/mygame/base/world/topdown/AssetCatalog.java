package com.lyadev.mygame.base.world.topdown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.lyadev.mygame.modules.prop.PropSettings;

import org.json.JSONArray;
import org.json.JSONObject;

/** Catalog of placeable ground tiles + decor props for the map editor. */
public final class AssetCatalog {
    public static final String DEFAULT_PATH = "maps/asset_catalog.json";

    private static AssetCatalog shared;

    private final String decorAtlas;
    private final List<PropDef> props;
    private final Map<String, PropDef> propsById;

    private AssetCatalog(String decorAtlas, List<PropDef> props) {
        this.decorAtlas = decorAtlas;
        this.props = Collections.unmodifiableList(props);
        Map<String, PropDef> map = new LinkedHashMap<>();
        for(PropDef prop : props){
            map.put(prop.id, prop);
        }
        this.propsById = Collections.unmodifiableMap(map);
    }

    public static AssetCatalog load() {
        return ensureLoaded();
    }

    public static AssetCatalog ensureLoaded() {
        if(shared == null){
            shared = load(DEFAULT_PATH);
        }
        return shared;
    }

    public static void disposeShared() {
        shared = null;
    }

    public static AssetCatalog load(String path) {
        FileHandle file = Gdx.files.internal(path);
        if(!file.exists()){
            Gdx.app.error("AssetCatalog", "Missing " + path + " — empty catalog");
            return new AssetCatalog("tiles/topdown/decor/Decorations.png", List.of());
        }
        JSONObject root = new JSONObject(file.readString("UTF-8"));
        String atlas = root.optString("decorAtlas", "tiles/topdown/decor/Decorations.png");
        List<PropDef> list = new ArrayList<>();
        JSONArray arr = root.optJSONArray("props");
        if(arr != null){
            for(int i = 0; i < arr.length(); i++){
                JSONObject p = arr.getJSONObject(i);
                JSONArray src = p.getJSONArray("src");
                list.add(new PropDef(
                        p.getString("id"),
                        atlas,
                        src.getInt(0), src.getInt(1), src.getInt(2), src.getInt(3)));
            }
        }
        Gdx.app.log("AssetCatalog", "Loaded " + list.size() + " props from " + path);
        return new AssetCatalog(atlas, list);
    }

    public String getDecorAtlas() {
        return decorAtlas;
    }

    public List<PropDef> getProps() {
        return props;
    }

    public PropDef getProp(String id) {
        return propsById.get(id);
    }

    public PropSettings toSettings(PropDef def, float scale) {
        return new PropSettings(def.id, def.atlasPath, def.srcX, def.srcY, def.srcW, def.srcH, scale);
    }

    public static final class PropDef {
        public final String id;
        public final String atlasPath;
        public final int srcX;
        public final int srcY;
        public final int srcW;
        public final int srcH;

        public PropDef(String id, String atlasPath, int srcX, int srcY, int srcW, int srcH) {
            this.id = id;
            this.atlasPath = atlasPath;
            this.srcX = srcX;
            this.srcY = srcY;
            this.srcW = srcW;
            this.srcH = srcH;
        }
    }
}
