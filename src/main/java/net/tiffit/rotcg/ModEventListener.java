package net.tiffit.rotcg;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tiffit.rotcg.pack.RotCGPackFinder;
import net.tiffit.rotcg.registry.ModRegistry;
import net.tiffit.rotcg.registry.entity.*;
import net.tiffit.rotcg.render.entity.*;
import net.tiffit.rotcg.render.model.AnimateGroundBER;
import net.tiffit.rotcg.render.model.GroundBlockModelLoader;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventListener {

    @SubscribeEvent
    public static void loadPack(AddPackFindersEvent event){
        event.addRepositorySource(new RotCGPackFinder());
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(PlayerEntity.TYPE.get(), PlayerEntityRenderer::new);
        e.registerEntityRenderer(GameObjectEntity.TYPE.get(), GameObjectEntityRenderer::new);
        e.registerEntityRenderer(WallEntity.TYPE.get(), WallEntityRenderer::new);
        e.registerEntityRenderer(MerchantEntity.TYPE.get(), MerchantEntityRenderer::new);
        e.registerEntityRenderer(CharacterEntity.TYPE.get(), CharacterEntityRenderer::new);
        e.registerEntityRenderer(PortalEntity.TYPE.get(), PortalEntityRenderer::new);
        e.registerEntityRenderer(WallOfFameEntity.TYPE.get(), WallOfFameEntityRenderer::new);

        e.registerEntityRenderer(ProjectileEntity.TYPE.get(), ProjectileEntityRenderer::new);


        e.registerBlockEntityRenderer(ModRegistry.animateGroundBlockEntity, pContext -> new AnimateGroundBER());
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent e) {
        Supplier<AttributeSupplier> factory = () -> LivingEntity.createLivingAttributes().build();
        e.put(PlayerEntity.TYPE.get(), factory.get());
        e.put(GameObjectEntity.TYPE.get(), factory.get());
        e.put(WallEntity.TYPE.get(), factory.get());
        e.put(MerchantEntity.TYPE.get(), factory.get());
        e.put(CharacterEntity.TYPE.get(), factory.get());
        e.put(PortalEntity.TYPE.get(), factory.get());
        e.put(WallOfFameEntity.TYPE.get(), factory.get());

        e.put(ProjectileEntity.TYPE.get(), factory.get());

    }

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders e) {
        e.register("grounds", new GroundBlockModelLoader());
    }
}
