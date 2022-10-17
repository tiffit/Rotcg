package net.tiffit.rotcg.render.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.tiffit.realmnetapi.assets.xml.XMLLoader;

public class GroundBlockModelLoader implements IGeometryLoader<GroundBlockModel>, ResourceManagerReloadListener {

    @Override
    public GroundBlockModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        int type = jsonObject.get("type").getAsInt();
        return new GroundBlockModel(XMLLoader.GROUNDS.get(type));
    }

    @Override
    public void onResourceManagerReload(ResourceManager pResourceManager) {
    }
}
