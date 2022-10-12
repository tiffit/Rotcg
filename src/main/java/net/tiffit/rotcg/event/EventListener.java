package net.tiffit.rotcg.event;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.tiffit.realmnetapi.api.Hooks;
import net.tiffit.realmnetapi.api.IObjectListener;
import net.tiffit.realmnetapi.api.IShootDecider;
import net.tiffit.realmnetapi.api.event.*;
import net.tiffit.realmnetapi.assets.ConditionEffect;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.assets.xml.Ground;
import net.tiffit.realmnetapi.map.RMap;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.rotcg.Constants;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.registry.GroundBlock;
import net.tiffit.rotcg.registry.entity.ProjectileEntityContainer;
import net.tiffit.rotcg.registry.entity.RotcgEntity;
import net.tiffit.rotcg.registry.entity.RotcgEntityContainer;
import net.tiffit.rotcg.rna.McPlayerPosTracker;
import net.tiffit.rotcg.screen.MenuScreen;
import net.tiffit.rotcg.screen.slot.RInventoryScreen;
import net.tiffit.rotcg.util.MoveSpeedUtil;
import net.tiffit.rotcg.util.ObjectEntityTypeMapping;
import net.tiffit.rotcg.util.RotCGResourceLocation;
import net.tiffit.rotcg.util.TickExecutor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventListener {

    public static boolean updateInventory = false;
    public static boolean shouldShoot = false;

    @SubscribeEvent
    public static void onGuiOpen(ScreenEvent.Opening e) {
        if(Rotcg.DEV_WORLD)return;
        Screen screen = e.getNewScreen();
        if(screen instanceof TitleScreen){
            e.setNewScreen(new MenuScreen());
        }else if(screen instanceof InventoryScreen){
            e.setNewScreen(new RInventoryScreen());
        }
    }

    @SubscribeEvent
    public static void onLogIn(PlayerEvent.PlayerLoggedInEvent e) {
        if(Rotcg.DEV_WORLD)return;
        EventHandler.clearListeners();
        Rotcg.SERVER_PLAYER = (ServerPlayer) e.getEntity();
        Rotcg.SERVER_PLAYER.setGameMode(GameType.ADVENTURE);
        ServerLevel level = Rotcg.SERVER_PLAYER.getLevel();
        GameRules gr = level.getGameRules();
        level.setDayTime(12000);
        gr.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        gr.getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
        gr.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, level.getServer());
        gr.getRule(GameRules.RULE_DOBLOCKDROPS).set(false, level.getServer());
        gr.getRule(GameRules.RULE_MOBGRIEFING).set(false, level.getServer());
        gr.getRule(GameRules.RULE_DOFIRETICK).set(false, level.getServer());

        Hooks.PlayerPosTracker = () -> new McPlayerPosTracker(e.getEntity());

        EventHandler.addListener(TileAddEvent.class, tileAddEvent -> TileAddEventHandler.handle(tileAddEvent, level));
        EventHandler.addListener(ReconnectEvent.class, ReconnectEventHandler::handle);
        EventHandler.addListener(PlayerDataEvent.class, playerDataEvent -> updateInventory = true);
        EventHandler.addListener(ChatEvent.class, ChatEventHandler::handle);
        EventHandler.addListener(ShowEffectEvent.class, ShowEffectEventHandler::handle);
        EventHandler.addListener(AoeEvent.class, AoeEventHandler::handle);
        EventHandler.addListener(EnemyHitEvent.class, SoundEventHandler::handleEnemyHit);
        EventHandler.addListener(PlayerShootEvent.class, SoundEventHandler::handlePlayerShoot);
        EventHandler.addListener(DamageEvent.class, SoundEventHandler::handleDamage);
        EventHandler.addListener(DeathEvent.class, DeathEventHandler::handle);

        Hooks.ShootDecider = new IShootDecider() {
            @Override
            public boolean shouldShoot() {
                return shouldShoot && Minecraft.getInstance().screen == null;
            }

            @Override
            public float getAngleRads(GameObject go, float arcGap) {
                float rotYaw = Minecraft.getInstance().player.getYHeadRot();
                if(rotYaw < 0)rotYaw += 360;
                if(rotYaw > 360)rotYaw -= 360;
                rotYaw += 90;
                return (float) Math.toRadians(rotYaw) - (go.numProjectiles * arcGap)/2;
            }
        };

        Hooks.ObjectListener = rObject -> {
            GameObject go = rObject.getGameObject();
            if(ObjectEntityTypeMapping.MAP.containsKey(go.goClass)){
                return new RotcgEntityContainer(rObject);
            }
            Rotcg.LOGGER.warn("Unknown object-class " + go.goClass + "; id: " + go.id);
            return new IObjectListener.EmptyObjectListener(rObject);
        };

        Hooks.ProjectileListener = ProjectileEntityContainer::new;

        Rotcg.LOGGER.info("Connecting to " + Rotcg.ADDRESS);
        RealmNetworker networker = new RealmNetworker(Rotcg.ADDRESS);
        networker.connect(Rotcg.TOKEN);
        Rotcg.ACTIVE_CONNECTION = networker;

        TickExecutor.addClient(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.getWindow().setTitle("Rotcg - " + Rotcg.SERVER.name());
            mc.getSoundManager().play(new SimpleSoundInstance(new RotCGResourceLocation("other_warp_done"), SoundSource.AMBIENT, 0.3f, 1, SoundEventHandler.src,
                    false, 0, SoundInstance.Attenuation.LINEAR, Rotcg.SERVER_PLAYER.getX(), Rotcg.SERVER_PLAYER.getY(), Rotcg.SERVER_PLAYER.getZ(), false));
        });
    }

    @SubscribeEvent
    public static void onLogOut(PlayerEvent.PlayerLoggedOutEvent e) {
        if(Rotcg.DEV_WORLD)return;
        Rotcg.ACTIVE_CONNECTION.disconnect();
        Rotcg.MAP = null;
        TickExecutor.clear();
        EventHandler.clearListeners();
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent e) {
        if(Rotcg.DEV_WORLD)return;
        RealmNetworker networker = Rotcg.ACTIVE_CONNECTION;
        if(networker == null || !networker.connected){
            return;
        }
        Player player = e.player;
        { //hitbox
            float width = 0.95f / 2.0F;
            float height = 1;
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            player.setBoundingBox(new AABB(x - width, y, z - width, x + width, y + height, z + width));
            player.setSprinting(false);
        }
        if(e.phase == TickEvent.Phase.START){
            if(player.getY() < 10){ //Fix falling player
                player.teleportTo(player.getX(), 67, player.getZ());
            }
            RMap map = networker.map;
            if(map != null && map.getSelfState() != null){
                { //Speed
                    double bps = Constants.MIN_MOVE_SPEED + map.getSelfState().getSpeed() / 75f * (Constants.MAX_MOVE_SPEED - Constants.MIN_MOVE_SPEED) / 2;
                    if(map.getSelfState().hasEffect(ConditionEffect.PARALYZED) || map.getSelfState().hasEffect(ConditionEffect.PETRIFIED)) {
                        bps = 0;
                    }else if(map.getSelfState().hasEffect(ConditionEffect.SLOWED)){
                        bps = Constants.MIN_MOVE_SPEED;
                    }else if(map.getSelfState().hasEffect(ConditionEffect.SPEEDY) || map.getSelfState().hasEffect(ConditionEffect.NINJA_SPEEDY)){
                        bps *= 1.5;
                    }
                    bps *= 1000;
                    Block belowBlock = player.getLevel().getBlockState(new BlockPos(player.getX(), 64, player.getZ())).getBlock();
                    if (belowBlock instanceof GroundBlock groundBlock) {
                        Ground ground = groundBlock.ground;
                        bps *= ground.speed;
                    }
                    AttributeInstance attrib = player.getAttribute(Attributes.MOVEMENT_SPEED);
                    attrib.setBaseValue(MoveSpeedUtil.bpsToMoveSpeed(bps));
                    attrib.removeModifiers();
                }
                if(updateInventory && e.side == LogicalSide.SERVER){ //Held Item
                    Inventory inv = player.getInventory();
                    ResourceLocation itemRl = new ResourceLocation(Rotcg.MODID, "item_" + Rotcg.ACTIVE_CONNECTION.map.getSelfState().<Integer>getStat(StatType.INVENTORY_0));
                    ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(itemRl));
                    if(!stack.isEmpty()) {
                        inv.setItem(0, stack);
                    }
                    updateInventory = false;
                }
                player.getInventory().selected = 0;
            }
        }
    }


    @SubscribeEvent
    public static void debugScreen(CustomizeGuiOverlayEvent.DebugText e) {
        if(Rotcg.DEV_WORLD)return;
        List<String> list = e.getRight();
        if(list.isEmpty())return;
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.getCameraEntity();
        HitResult result = entity.pick(20.0D, 0.0F, false);
        if(result.getType() == HitResult.Type.BLOCK){
            BlockPos blockpos = ((BlockHitResult)result).getBlockPos();
            BlockState blockstate = mc.level.getBlockState(blockpos);
            if(blockstate.getBlock() instanceof GroundBlock groundBlock){
                Ground ground = groundBlock.ground;
                list.add("");
                list.add(ChatFormatting.UNDERLINE + "Targeted Ground");
                list.add("Type: " + ground.type + " (0x" + Integer.toHexString(ground.type) + ")");
                list.add("Id: " + ground.id);
                list.add("Num Textures: " + ground.textures.size());
                list.add("Speed: " + ground.speed);
                if(ground.nowalk){
                    list.add("No Walk");
                }
                if(ground.sink){
                    list.add("Sink");
                }
            }
        }
        if(mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY){
            EntityHitResult entHit = (EntityHitResult) mc.hitResult;
            if(entHit.getEntity() instanceof RotcgEntity ent){
                RObject obj = ent.getReference();
                list.add("");
                if(obj == null){
                    list.add(ChatFormatting.UNDERLINE + "Targeted GameObject: null");
                }else{
                    GameObject go = obj.getGameObject();
                    list.add(ChatFormatting.UNDERLINE + "Targeted GameObject");
                    list.add("Type: " + go.type + " (0x" + Integer.toHexString(go.type) + ")");
                    list.add("Id: " + go.id + " (" + go.displayid + ")");
                    list.add("Class: " + go.goClass);
                }
            }
        }
    }

    @SubscribeEvent
    public static void listenForMouse(InputEvent.MouseButton.Pre e) {
        if(Rotcg.DEV_WORLD)return;
        if(e.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT){
            shouldShoot = e.getAction() == GLFW.GLFW_PRESS;
        }
    }

    @SubscribeEvent
    public static void resetFov(ComputeFovModifierEvent e) {
        e.setNewFovModifier(1);
    }

    @SubscribeEvent
    public static void playerChat(ClientChatEvent e) {
        if(Rotcg.DEV_WORLD)return;
        if(e.getMessage().startsWith(":")) {
            String text = e.getMessage().substring(":".length());
            e.setCanceled(true);
            if(text.startsWith("/tp") || text.startsWith("/teleport")){
                String[] args = text.split(" ");
                if(args.length == 2){
                    String ign = args[1];
                    RealmNetworker net = Rotcg.ACTIVE_CONNECTION;
                    RObject obj = net.map.getEntityList().get(object -> object.getState().hasStat(StatType.NAME) && object.getState().<String>getStat(StatType.NAME).equals(ign));
                    if(obj != null){
                        net.controller.teleport(obj);
                    }else{
                        Minecraft.getInstance().gui.setOverlayMessage(Component.literal(ChatFormatting.RED + "Player does not exist!"), true);
                    }
                }
            }else{
                Rotcg.ACTIVE_CONNECTION.controller.sendChatMessage(text);
            }
        }
    }

}
