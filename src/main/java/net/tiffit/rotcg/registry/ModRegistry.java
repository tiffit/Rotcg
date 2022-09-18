package net.tiffit.rotcg.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.assets.xml.Ground;
import net.tiffit.realmnetapi.assets.xml.XMLLoader;
import net.tiffit.rotcg.KeybindingManager;
import net.tiffit.rotcg.registry.entity.*;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

import static net.tiffit.rotcg.Rotcg.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRegistry {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final HashMap<Integer, RegistryObject<GroundBlock>> R_GROUNDS = new HashMap<>();
    public static final HashMap<Integer, RegistryObject<WallBlock>> R_WALLS = new HashMap<>();
    public static final HashMap<Integer, RegistryObject<EquipmentItem>> R_EQUIPMENT = new HashMap<>();

    public static void register(){
        for (Map.Entry<Integer, Ground> entry : XMLLoader.GROUNDS.entrySet()) {
            RegistryObject<GroundBlock> blockRegistry = BLOCKS.register("ground_" + entry.getKey(), () -> new GroundBlock(entry.getValue()));
            R_GROUNDS.put(entry.getKey(), blockRegistry);
        }

        for (GameObject wall : XMLLoader.getGameObjectsOfClass("Wall", "ConnectedWall", "CaveWall")) {
            RegistryObject<WallBlock> blockRegistry = BLOCKS.register("wall_" + wall.type, () -> new WallBlock(wall));
            R_WALLS.put(wall.type, blockRegistry);
        }

        for (GameObject equipment : XMLLoader.getGameObjectsOfClass("Equipment")) {
            RegistryObject<EquipmentItem> itemRegistry = ITEMS.register("item_" + equipment.type, () -> new EquipmentItem(equipment));
            R_EQUIPMENT.put(equipment.type, itemRegistry);
        }

        PlayerEntity.TYPE = registerEntity(PlayerEntity::new, "player", 1, 1);
        GameObjectEntity.TYPE = registerEntity(GameObjectEntity::new, "generic", 1, 1);
        WallEntity.TYPE = registerEntity(WallEntity::new, "wall", 1, 1);
        MerchantEntity.TYPE = registerEntity(MerchantEntity::new, "merchant", 1, 1);
        CharacterEntity.TYPE = registerEntity(CharacterEntity::new, "character", 1, 1);
        PortalEntity.TYPE = registerEntity(PortalEntity::new, "portal", 1, 1);
        WallOfFameEntity.TYPE = registerEntity(WallOfFameEntity::new, "walloffame", 1, 1);

        ProjectileEntity.TYPE = registerEntity(ProjectileEntity::new, "projectile", 0.1f, 0.1f);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> RegistryObject<EntityType<T>> registerEntity(EntityType.EntityFactory<T> factory, String name, double width, double height){
        final String nameToUse = "go_" + name;
        RegistryObject<EntityType<T>> registry = ENTITIES.register(nameToUse, () -> EntityType.Builder.of(factory, MobCategory.MISC)
                .sized((float) width, (float) height)
                .noSummon()
                .noSave()
                .clientTrackingRange(1000)
                .build(nameToUse));
        return registry;
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event){
        String category = "Realm of the Craft God";
        event.register(KeybindingManager.INTERACT = new KeyMapping("Interact", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, category));
        event.register(KeybindingManager.TOGGLE_ALLY_SHOOT = new KeyMapping("Toggle Ally Shoot", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, category));
        event.register(KeybindingManager.USE_ABILITY = new KeyMapping("Use Ability", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, category));

        event.register(KeybindingManager.MINIMAP_ZOOM_IN = new KeyMapping("Zoom In", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_EQUAL, category));
        event.register(KeybindingManager.MINIMAP_ZOOM_OUT = new KeyMapping("Zoom Out", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_MINUS, category));

    }

}


