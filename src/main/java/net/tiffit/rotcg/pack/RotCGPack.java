package net.tiffit.rotcg.pack;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.tiffit.realmnetapi.assets.spritesheet.AnimSpriteDefinition;
import net.tiffit.realmnetapi.assets.spritesheet.SheetReference;
import net.tiffit.realmnetapi.assets.spritesheet.SpriteLocation;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.assets.xml.Ground;
import net.tiffit.realmnetapi.assets.xml.Texture;
import net.tiffit.realmnetapi.util.math.Vec2i;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.registry.GroundBlock;
import net.tiffit.rotcg.registry.ModRegistry;
import net.tiffit.rotcg.util.RotCGResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Predicate;

public class RotCGPack implements PackResources, Serializable {

    private static Gson GSON = new GsonBuilder().create();
    public static RotCGResourceLocation WHITE = new RotCGResourceLocation("textures/white.png");
    public HashMap<RotCGResourceLocation, byte[]> resources = new HashMap<>();

    public RotCGPack(){
        File save = new File("./cache/pack");
        if(!save.exists()){
            init();
            try {
                Rotcg.LOGGER.info("Writing pack");
                save.getParentFile().mkdir();
                save.createNewFile();
                try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(save))) {
                    HashMap<String, byte[]> stringMap = new HashMap<>();
                    resources.forEach((rl, bytes) -> stringMap.put(rl.getPath(), bytes));
                    oo.writeObject(stringMap);
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }else{
            try{
                Rotcg.LOGGER.info("Reading pack");
                try(ObjectInputStream oi = new ObjectInputStream(new FileInputStream(save))){
                    HashMap<String, byte[]> stringMap = (HashMap<String, byte[]>)oi.readObject();
                    resources.clear();
                    stringMap.forEach((s, bytes) -> resources.put(new RotCGResourceLocation(s), bytes));
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    private void init(){
        Rotcg.LOGGER.info("Loading Custom Resource Pack");
        resources.clear();

        BufferedImage blackImg = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                blackImg.setRGB(x, y, 0xff_00_00_00);
            }
        }

        BufferedImage whiteImg = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                whiteImg.setRGB(x, y, 0xff_ff_ff_ff);
            }
        }
        resources.put(WHITE, imageToArray(whiteImg));

        {//Blockstate
            ModRegistry.R_GROUNDS.values().forEach(blockRegistry -> {
                GroundBlock block = blockRegistry.get();
                JsonObject obj = new JsonObject();

                JsonObject variants = new JsonObject();
                obj.add("variants", variants);

                for(int i = 0; i <= 16; i++) {
                    JsonObject model = new JsonObject();
                    if(i < block.ground.textures.size()) {
                        model.addProperty("model", Rotcg.MODID + ":block/" + blockRegistry.getId().getPath() + "_" + i);
                    }else{
                        model.addProperty("model", Rotcg.MODID + ":block/" + blockRegistry.getId().getPath() + "_0");
                    }
                    variants.add("textureused=" + i, model);
                }
                resources.put(new RotCGResourceLocation("blockstates/" + blockRegistry.getId().getPath() + ".json"), obj.toString().getBytes());
            });
            ModRegistry.R_WALLS.values().forEach(blockRegistry -> {
                JsonObject obj = new JsonObject();

                JsonObject variants = new JsonObject();
                obj.add("variants", variants);

                JsonObject model = new JsonObject();
                variants.add("", model);
                model.addProperty("model", Rotcg.MODID + ":block/" + blockRegistry.getId().getPath());
                resources.put(new RotCGResourceLocation("blockstates/" + blockRegistry.getId().getPath() + ".json"), obj.toString().getBytes());
            });
        }
        {//Block
            ModRegistry.R_GROUNDS.values().forEach(blockRegistry -> {
                Ground ground = blockRegistry.get().ground;
                for(int i = 0; i <= 16; i++) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("parent", "block/cube_all");

                    JsonObject textures = new JsonObject();
                    Texture texture = i < ground.textures.size() ? ground.textures.get(i) : ground.textures.get(0);
                    textures.addProperty("all", textToRl(texture).toString());
                    obj.add("textures", textures);
                    resources.put(new RotCGResourceLocation("models/block/" + blockRegistry.getId().getPath() + "_" + i + ".json"), obj.toString().getBytes());
                }
            });
            ModRegistry.R_WALLS.values().forEach(blockRegistry -> {
                JsonObject obj = new JsonObject();
                GameObject go = blockRegistry.get().go;
                obj.addProperty("parent", "block/cube_top");

                JsonObject textures = new JsonObject();
                Texture side = go.texture.get(0);
                textures.addProperty("side", textToRl(side).toString());
                textures.addProperty("top", textToRl(go.textureTop == null ? side : go.textureTop).toString());
                obj.add("textures", textures);
                resources.put(new RotCGResourceLocation("models/block/" + blockRegistry.getId().getPath() + ".json"), obj.toString().getBytes());
            });
        }
        {//Item: Equipment
            ModRegistry.R_EQUIPMENT.values().forEach(itemRegistry -> {
                GameObject go = itemRegistry.get().go;
                JsonObject obj = new JsonObject();
                obj.addProperty("parent", "item/generated");

                JsonObject textures = new JsonObject();

                textures.addProperty("layer0", textToRl(go.texture.get(0)).toString());
                obj.add("textures", textures);
                resources.put(new RotCGResourceLocation("models/item/" + itemRegistry.getId().getPath() + ".json"), obj.toString().getBytes());
            });
        }

        // Regular Sprites
        for (SpriteLocation loc : SheetReference.getSpriteLocations()) {
            BufferedImage img = SheetReference.getSprite(loc);
            if(img != null){
                if(img.getWidth() == 10 && img.getHeight() == 10){
                    img = img.getSubimage(1, 1, 8, 8);
                }
                resources.put(new RotCGResourceLocation("textures/" + loc.spritesheet.toLowerCase() + "_" + loc.index + ".png"), imageToArray(img));
            }
        }
        // Animated Sprites
        for (SpriteLocation loc : SheetReference.getAnimatedSpriteLocations()) {
            Map<Vec2i, List<AnimSpriteDefinition>> map = SheetReference.getAnimatedSprites(loc).getMap();
            for (Map.Entry<Vec2i, List<AnimSpriteDefinition>> entry : map.entrySet()) {
                int action = entry.getKey().x();
                int direction = entry.getKey().y();
                for (int i = 0; i < entry.getValue().size(); i++) {
                    AnimSpriteDefinition def = entry.getValue().get(i);
                    BufferedImage img = SheetReference.getSprite(def.spriteData);
                    if(img != null){
                        StringBuilder path = new StringBuilder("textures/a_");
                        path.append(loc.spritesheet.toLowerCase()).append("_");
                        path.append(loc.index);
                        if(action != 0 || direction != 0){
                            path.append("_").append(action).append("_").append(direction);
                        }
                        path.append(".png");
                        resources.put(new RotCGResourceLocation(path.toString()), imageToArray(img));
                    }
                }
            }
            BufferedImage img = SheetReference.getAnimatedSprite(loc);
            if(img != null){
                resources.put(new RotCGResourceLocation("textures/a_" + loc.spritesheet.toLowerCase() + "_" + loc.index + ".png"), imageToArray(img));
            }
        }
//        try {
//            loadSounds();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        Rotcg.LOGGER.info("Finished Loading Custom Resource Pack");
    }

    private void loadSounds() throws URISyntaxException, IOException {
//        List<String> sounds = new ArrayList<>();
//        for(GameObject go : OBJECTS.values()){
//            String[] soundArr = new String[]{go.hitSound, go.deathSound};
//            for(String sound : soundArr){
//                if(!sound.isEmpty() && !sounds.contains(sound)){
//                    sounds.add(sound.trim());
//                }
//            }
//        }
//
//        AudioAttributes audio = new AudioAttributes();
//
//        EncodingAttributes attrs = new EncodingAttributes();
//        attrs.setFormat("ogg");
//        attrs.setAudioAttributes(audio);
//        Encoder encoder = new Encoder();
//
//        File folder = new File("./temp");
//
//        JsonObject soundsJson = new JsonObject();
//
//        for(String sound : sounds){
//            try {
//                File sub = new File(folder, sound + ".ogg");
//                encoder.encode(new MultimediaObject(new URI("https://realmofthemadgodhrd.appspot.com/sfx/" + sound + ".mp3").toURL()), sub, attrs);
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                IOUtils.copy(new FileInputStream(sub), baos);
//                byte[] byteArr = baos.toByteArray();
//
//                JsonObject soundEvent = new JsonObject();
//                soundsJson.add(sound.replaceAll("/", "."), soundEvent);
//                JsonObject soundJson = new JsonObject();
//                soundJson.addProperty("name", RotCG.MODID + ":" + sound);
//                soundJson.addProperty("stream", false);
//                JsonArray jsonArray = new JsonArray();
//                jsonArray.add(soundJson);
//                soundEvent.add("sounds", jsonArray);
//
//                resources.put(new RotCGResourceLocation("sounds/" + sound + ".ogg"), byteArr);
//            } catch (InputFormatException ignored) {
//            } catch (EncoderException e) {
//                e.printStackTrace();
//            }
//            resources.put(new RotCGResourceLocation("sounds.json"), soundsJson.toString().getBytes());
//        }
//        FileUtils.deleteDirectory(folder);
    }

    @Override
    public InputStream getRootResource(@NotNull String fileName) throws IOException {
        if("pack.png".equals(fileName)){
            BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            return new ByteArrayInputStream(os.toByteArray());
        }
        return null;
    }

    @Override
    public InputStream getResource(PackType type, @NotNull ResourceLocation location) throws IOException {
        return new ByteArrayInputStream(resources.get(location));
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType type, String namespaceIn, String pathIn, Predicate<ResourceLocation> filter) {
        if(type == PackType.CLIENT_RESOURCES) {
            List<ResourceLocation> list = new ArrayList<>(resources.keySet());
            list.removeIf(filter);
            list.removeIf(resourceLocation -> !resourceLocation.toString().startsWith(namespaceIn + ":" + pathIn));
            return list;
        }
        return new ArrayList<>();
    }

    @Override
    public boolean hasResource(PackType type, ResourceLocation location) {
        return resources.containsKey(location) && type == PackType.CLIENT_RESOURCES;
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return Sets.newHashSet(Rotcg.MODID);
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) throws IOException {
        JsonObject obj = GSON.fromJson("""
                {
                    "pack": {
                        "description": "RotCG Built-In Resource Pack containing essential RotMG Assets",
                        "pack_format": 9,
                        "forge:resource_pack_format": 9,
                        "forge:data_pack_format": 10
                    }
                }
                """, JsonObject.class);
        if(obj.has(deserializer.getMetadataSectionName())){
            return deserializer.fromJson(obj.getAsJsonObject(deserializer.getMetadataSectionName()));
        }
        return null;
    }

    @Override
    public String getName() {
        return "RotCG";
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isHidden() {
        return false;
    }

    private byte[] imageToArray(BufferedImage image){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            return imageInByte;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static RotCGResourceLocation textToRl(Texture texture){
        return new RotCGResourceLocation((texture.animated ? "a_" : "") + texture.file.toLowerCase() + "_" + texture.index);
    }

    public static RotCGResourceLocation textToRlFull(Texture texture){
        return new RotCGResourceLocation("textures/" + textToRl(texture).getPath() + ".png");
    }

    public static RotCGResourceLocation animRl(SpriteLocation loc, int action, int direction){
        StringBuilder path = new StringBuilder("textures/a_");
        path.append(loc.spritesheet.toLowerCase()).append("_");
        path.append(loc.index);
        if(action != 0 || direction != 0){
            path.append("_").append(action).append("_").append(direction);
        }
        path.append(".png");
        return new RotCGResourceLocation(path.toString());
    }
}
