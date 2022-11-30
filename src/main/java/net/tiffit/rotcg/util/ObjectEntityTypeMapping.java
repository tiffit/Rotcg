package net.tiffit.rotcg.util;

import net.minecraft.world.entity.EntityType;
import net.tiffit.rotcg.Constants;
import net.tiffit.rotcg.registry.entity.*;

import java.util.HashMap;
import java.util.function.Supplier;

public class ObjectEntityTypeMapping {

    public static HashMap<String, Supplier<EntityType<? extends RotcgEntity>>> MAP = new HashMap<>();

    static {
        addMapping(() -> WallEntity.TYPE.get(), "Wall", "ConnectedWall", "CaveWall");
        addMapping(() -> MerchantEntity.TYPE.get(), "Merchant");
        addMapping(() -> PortalEntity.TYPE.get(), Constants.CLASSES_PORTAL);
        addMapping(() -> PlayerEntity.TYPE.get(), "Player");
        addMapping(() -> GameObjectEntity.TYPE.get(), "GameObject", "ClosedVaultChest", "ClosedGiftChest",
                "NameChanger", "CharacterChanger", "SeasonalitySwitcher", "MoneyChanger", "Gravestone",
                "GuildBoard", "GuildRegister", "GuildChronicle", "Pet", "ReskinVendor", "MysteryBoxGround", "YardUpgrader", "PetUpgrader",
                "Container", "Summon", "Blacksmith Inv Object", "VaultContainer", "VaultGiftContainer", "PremiumVaultContainer", "Blacksmith Magic Anvil", "Blacksmith");
        addMapping(() -> CharacterEntity.TYPE.get(), "Character");
        addMapping(() -> WallOfFameEntity.TYPE.get(), "WallOfFame");
    }


    private static void addMapping(Supplier<EntityType<? extends RotcgEntity>> provider, String... classNames){
        for(String className : classNames){
            MAP.put(className, provider);
        }
    }

}
