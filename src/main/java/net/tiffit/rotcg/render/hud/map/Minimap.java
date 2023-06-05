package net.tiffit.rotcg.render.hud.map;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.tiffit.realmnetapi.assets.spritesheet.SheetReference;
import net.tiffit.realmnetapi.assets.spritesheet.SpriteColor;
import net.tiffit.realmnetapi.assets.spritesheet.SpriteDefinition;
import net.tiffit.realmnetapi.assets.xml.Ground;
import net.tiffit.realmnetapi.assets.xml.Texture;
import net.tiffit.realmnetapi.map.RMap;
import net.tiffit.realmnetapi.util.math.Vec2i;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.util.TickExecutor;

import java.util.HashMap;
import java.util.Map;

public class Minimap {

    private static final int invisibleColor = 0x00000000;

    private static int mapIdCounter = 0;
    private final int id = mapIdCounter++;

    private final RMap map;
    private final NativeImage mapImage;
    private DynamicTexture dynamicTexture;
    public ResourceLocation mapRL;

    public Minimap(RMap map){
        this.map = map;
        mapImage = new NativeImage(NativeImage.Format.RGBA, map.getWidth(), map.getHeight(), true);
        mapImage.fillRect(0, 0, map.getWidth(), map.getHeight(), invisibleColor);
        TickExecutor.addRender(() -> {
            TextureManager manager = Minecraft.getInstance().getTextureManager();
            dynamicTexture = new DynamicTexture(mapImage);
            mapRL = manager.register(Rotcg.MODID + "map/" + id, dynamicTexture);
            RenderType.text(mapRL);
        });
    }


    public void setTiles(HashMap<Vec2i, Ground> newTiles) {
        for (Map.Entry<Vec2i, Ground> entry : newTiles.entrySet()) {
            Vec2i vec = entry.getKey();
            Ground ground = entry.getValue();
            int abgr = getTileColor(ground);
            mapImage.setPixelRGBA(vec.x(), vec.y(), abgr);
        }
        TickExecutor.addRender(() -> dynamicTexture.upload());
    }

    private static final HashMap<Ground, Integer> MAP_COLOR_CACHE = new HashMap<>();
    private int getTileColor(Ground ground){
        if(MAP_COLOR_CACHE.containsKey(ground))return MAP_COLOR_CACHE.get(ground);
        if(ground.color != 0){
            int color = (ground.color & 0x00_00_ff) << 16;
            color += (ground.color & 0x00_ff_00);
            color += (ground.color & 0xff_00_00) >> 16;
            color += 0xff_00_00_00;
            MAP_COLOR_CACHE.put(ground, color);
            return color;
        }
        if(ground.textures.size() == 0){
            MAP_COLOR_CACHE.put(ground, invisibleColor);
            return invisibleColor;
        }
        Texture texture = ground.textures.get(0);
        if(texture.file.equals("invisible")){
            MAP_COLOR_CACHE.put(ground, invisibleColor);
            return invisibleColor;
        }
        SpriteDefinition definition = SheetReference.getSpriteDefinition(texture.toSpriteLocation());
        SpriteColor color = definition.mostCommonColor;
        int abgr = color.getABGR();
        MAP_COLOR_CACHE.put(ground, abgr);
        return abgr;
    }

}
