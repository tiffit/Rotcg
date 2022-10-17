package net.tiffit.rotcg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.tiffit.realmnetapi.RealmNetApi;
import net.tiffit.realmnetapi.assets.OBJModel;
import net.tiffit.realmnetapi.assets.spritesheet.SheetReference;
import net.tiffit.realmnetapi.assets.spritesheet.Spritesheet;
import net.tiffit.realmnetapi.assets.unity.UAsset;
import net.tiffit.realmnetapi.assets.unity.UMeshParser;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.assets.xml.Ground;
import net.tiffit.realmnetapi.assets.xml.XMLLoader;
import net.tiffit.realmnetapi.auth.AccessToken;
import net.tiffit.realmnetapi.auth.RealmAuth;
import net.tiffit.realmnetapi.auth.data.ServerInfo;
import net.tiffit.realmnetapi.net.ConnectionAddress;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.realmnetapi.util.LangLoader;
import net.tiffit.rotcg.registry.ModRegistry;
import net.tiffit.rotcg.render.hud.map.Minimap;
import net.tiffit.rotcg.util.WorldUtils;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Mod(Rotcg.MODID)
public class Rotcg {
    public static final boolean DEV_WORLD = false;
    public static final String MODID = "rotcg";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static Gson GSON = new GsonBuilder().setLenient().create();

    public static RealmNetworker ACTIVE_CONNECTION;
    public static ServerPlayer SERVER_PLAYER;
    public static RotcgConfig CONFIG;
    public static Minimap MAP;

    public static AccessToken TOKEN;
    public static ConnectionAddress ADDRESS;
    public static ServerInfo SERVER;

    public Rotcg() {
        WorldUtils.clearExistingWorlds();
        try {
            CONFIG = GSON.fromJson(new FileReader("./rotcg.json"), RotcgConfig.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            Spritesheet.LoadSpriteSheets(new FileInputStream("./assets/sprites/spritesheet.json"));
            SheetReference.Init("./assets/sprites/");
        }catch (Exception ex){
            ex.printStackTrace();
        }
        File xmlCache = new File("./cache/xmlObjects");
        if(!xmlCache.exists()){
            XMLLoader.loadAllXml();
            try {
                try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(xmlCache))) {
                    oo.writeObject(XMLLoader.OBJECTS);
                    oo.writeObject(XMLLoader.GROUNDS);
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }else{
            try {
                try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(xmlCache))) {
                    XMLLoader.OBJECTS = (HashMap<Integer, GameObject>) oi.readObject();
                    XMLLoader.GROUNDS = (HashMap<Integer, Ground>) oi.readObject();
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        XMLLoader.buildIdMap();

        if(!new File("./assets/models").exists()){
            generateModels();
        }
        OBJModel.LoadModels(new File("./assets/models"));
        try {
            LangLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ModRegistry.register();
        ModRegistry.BLOCKS.register(modEventBus);
        ModRegistry.ITEMS.register(modEventBus);
        ModRegistry.ENTITIES.register(modEventBus);
        ModRegistry.BLOCK_ENTITY_TYPE.register(modEventBus);

        switch (RealmNetApi.ENV){
            case PRODUCTION -> TOKEN = RealmAuth.authenticate(CONFIG.prodUsername, CONFIG.prodPassword);
            case TESTING -> TOKEN = RealmAuth.authenticate(CONFIG.testingUsername, CONFIG.testingPassword);
        }
        TOKEN = RealmAuth.authenticate(Rotcg.TOKEN);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private static void generateModels(){
        Set<String> uniqueModels = new HashSet<>();
        for (GameObject value : XMLLoader.OBJECTS.values()) {
            if(value.model != null && !value.model.isEmpty()){
                uniqueModels.add(value.model);
            }
        }

        File output = new File("./assets/models");
        output.mkdir();
        HashMap<String, UAsset> meshMap = new HashMap<>();
        File meshes = new File("./assets/temp/Mesh");
        for (File file : meshes.listFiles((dir, name) -> name.endsWith(".meta"))) {
            UAsset meshAsset = UAsset.load(file.toPath());
            meshMap.put(meshAsset.getValue("guid"),
                    UAsset.load(new File(meshes, file.getName().replace(".meta", "")).toPath()));
        }
        for (String uniqueModel : uniqueModels) {
            File f = new File("./assets/temp/GameObject/" + uniqueModel + ".prefab");
            if(f.exists()){
                UAsset asset = UAsset.load(f.toPath());
                String guid = asset.getValue("MeshFilter/m_Mesh/guid");
                if(meshMap.containsKey(guid)) {
                    UAsset meshAsset = meshMap.get(guid);
                    try {
                        String objText = UMeshParser.parse(uniqueModel, UMeshParser.meshHasNormals(meshAsset),
                                meshAsset.getValue("Mesh/m_VertexData/_typelessdata"),
                                meshAsset.getValue("Mesh/m_IndexBuffer"));
                        LOGGER.info("Generated model for " + uniqueModel);
                        Files.writeString(new File(output, uniqueModel + ".obj").toPath(), objText);
                    }catch (Exception ex){
                        LOGGER.error("Unable to load model for " + uniqueModel);
                    }
                }
            }
        }
    }
}
