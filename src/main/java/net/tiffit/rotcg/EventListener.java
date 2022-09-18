package net.tiffit.rotcg;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
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
import net.tiffit.realmnetapi.net.ConnectionAddress;
import net.tiffit.realmnetapi.net.RealmNetworker;
import net.tiffit.realmnetapi.net.packet.in.AoePacketIn;
import net.tiffit.realmnetapi.net.packet.in.ReconnectPacketIn;
import net.tiffit.realmnetapi.net.packet.in.ShowEffectPacketIn;
import net.tiffit.realmnetapi.net.packet.in.TextPacketIn;
import net.tiffit.realmnetapi.util.LangLoader;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.registry.GroundBlock;
import net.tiffit.rotcg.registry.ModRegistry;
import net.tiffit.rotcg.registry.entity.ProjectileEntityContainer;
import net.tiffit.rotcg.registry.entity.RotcgEntity;
import net.tiffit.rotcg.registry.entity.RotcgEntityContainer;
import net.tiffit.rotcg.render.effect.AoeEffect;
import net.tiffit.rotcg.render.effect.RotMGEffect;
import net.tiffit.rotcg.render.hud.map.Minimap;
import net.tiffit.rotcg.rna.McPlayerPosTracker;
import net.tiffit.rotcg.screen.MenuScreen;
import net.tiffit.rotcg.util.ObjectEntityTypeMapping;
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
        if(e.getNewScreen() instanceof TitleScreen){
            e.setNewScreen(new MenuScreen());
        }
    }

    @SubscribeEvent
    public static void onLogIn(PlayerEvent.PlayerLoggedInEvent e) {
        if(Rotcg.DEV_WORLD)return;
        EventHandler.clearListeners();
        Rotcg.SERVER_PLAYER = (ServerPlayer) e.getEntity();
        Rotcg.SERVER_PLAYER.setGameMode(GameType.ADVENTURE);
        Level level = Rotcg.SERVER_PLAYER.getLevel();
        GameRules gr = level.getGameRules();
        gr.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        gr.getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
        gr.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, level.getServer());
        gr.getRule(GameRules.RULE_DOBLOCKDROPS).set(false, level.getServer());
        gr.getRule(GameRules.RULE_MOBGRIEFING).set(false, level.getServer());
        gr.getRule(GameRules.RULE_DOFIRETICK).set(false, level.getServer());

        Hooks.PlayerPosTracker = () -> new McPlayerPosTracker(e.getEntity());

        EventHandler.addListener(TileAddEvent.class, tileAddEvent -> {
            if(Rotcg.MAP == null){
                Rotcg.MAP = new Minimap(Rotcg.ACTIVE_CONNECTION.map);
            }
            Rotcg.MAP.setTiles(tileAddEvent.newTiles());
            tileAddEvent.newTiles().forEach((vec2i, ground) ->
                    level.setBlock(new BlockPos(vec2i.x(), 64, vec2i.y()), ModRegistry.R_GROUNDS.get(ground.type).get().defaultBlockState(),11));
        });

        EventHandler.addListener(ReconnectEvent.class, reconnectEvent -> {
            Rotcg.ACTIVE_CONNECTION.disconnect();
            ReconnectPacketIn packet = reconnectEvent.packet();
            String address = packet.host.isEmpty() ? Rotcg.ADDRESS.address() : packet.host;
            Rotcg.ADDRESS = new ConnectionAddress(address, 2050, packet.key, packet.keyTime, packet.gameId);
            TickExecutor.addRender(() -> {
                Minecraft mc = Minecraft.getInstance();
                mc.level.disconnect();
                mc.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
                MenuScreen.connect(mc);
            });
        });

        EventHandler.addListener(PlayerDataEvent.class, playerDataEvent -> {
            updateInventory = true;
        });

        EventHandler.addListener(ChatEvent.class, chatEvent -> {
            String sendText = "";
            TextPacketIn packet = chatEvent.packet();
            int numStars = packet.numStars;
            String name = packet.name;
            String text = packet.text;
            if (numStars >= 0) {
                ChatFormatting starColor = numStars <= 14 ? ChatFormatting.BLUE :
                        numStars <= 29 ? ChatFormatting.DARK_BLUE :
                                numStars <= 44 ? ChatFormatting.DARK_RED :
                                        numStars <= 59 ? ChatFormatting.GOLD :
                                                numStars <= 74 ? ChatFormatting.YELLOW : ChatFormatting.WHITE;
                sendText = starColor + "\u2b50 ";
            }
            if(packet.recipient.equals("*Guild*")){
                sendText += ChatFormatting.GREEN + "<" + name + "> " + text;
            }else if(name.equals("*Error*")) {
                sendText += ChatFormatting.DARK_RED + text;
            }else if(!packet.recipient.isEmpty() && !name.isEmpty()){
                String suffix = text;
                if(packet.objectId == Rotcg.ACTIVE_CONNECTION.map.getObjectId()){
                    suffix = "To: <" + packet.recipient + "> " + suffix;
                }else{
                    suffix = "<" + packet.name + "> " + suffix;
                }
                sendText += ChatFormatting.AQUA + suffix;
            } else {
                ChatFormatting nameColor = packet.isSupporter ? ChatFormatting.DARK_PURPLE : ChatFormatting.DARK_GREEN;
                if (name.startsWith("#")) nameColor = ChatFormatting.GOLD;
                String nameText = name.isEmpty() ? "" : nameColor + "<" + (name.startsWith("#") ? name.substring(1) : name) + "> ";
                if (numStars < 0) text = LangLoader.format(text);
                sendText += nameText + ChatFormatting.RESET + text;

                if (name.trim().isEmpty())
                    sendText = ChatFormatting.YELLOW + ChatFormatting.stripFormatting(sendText);
            }
            MutableComponent comp = Component.literal(sendText);
            if(!name.isEmpty()){
                comp.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ":/ignore " + name)));
            }
            Rotcg.SERVER_PLAYER.sendSystemMessage(comp);
        });


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
                RotcgEntityContainer container = new RotcgEntityContainer(rObject);
                return container;
            }else{
                Rotcg.LOGGER.warn("Unknown object-class " + go.goClass + "; id: " + go.id);
            }
            return new IObjectListener.EmptyObjectListener(rObject);
        };

        EventHandler.addListener(ShowEffectEvent.class, showEffectEvent -> {
            ShowEffectPacketIn packet = showEffectEvent.packet();
            RotMGEffect.VisualEffect effect = RotMGEffect.VisualEffect.byId(packet.effectType);
            if (effect.effectClass != null) {
                try {
                    RotMGEffect reffect = effect.effectClass.getConstructor(int.class, Vec2f.class, Vec2f.class, int.class, double.class)
                            .newInstance(packet.targetObjectId, packet.start, packet.end, packet.color, packet.duration);
                    TickExecutor.trackEffect(reffect);
                } catch (ReflectiveOperationException ex) {
                    ex.printStackTrace();
                }
            }
        });

        EventHandler.addListener(AoeEvent.class, aoeEvent -> {
            AoePacketIn packet = aoeEvent.packet();
            AoeEffect effect = new AoeEffect(0, packet.pos, new Vec2f(packet.radius, 0), packet.color, packet.duration);
            TickExecutor.trackEffect(effect);
        });

        Hooks.ProjectileListener = ProjectileEntityContainer::new;

        System.out.println("Connecting to " + Rotcg.ADDRESS);
        RealmNetworker networker = new RealmNetworker(Rotcg.ADDRESS);
        networker.connect(Rotcg.TOKEN);
        Rotcg.ACTIVE_CONNECTION = networker;
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
                player.teleportTo(player.getX(), 65, player.getZ());
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
                    double mcSpeed = bps / 4.3478f;
                    player.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.1 * mcSpeed);
                }
                if(updateInventory && e.side == LogicalSide.SERVER){ //Held Item
                    Inventory inv = player.getInventory();
                    ResourceLocation itemRl = new ResourceLocation(Rotcg.MODID, "item_" + Rotcg.ACTIVE_CONNECTION.map.getSelfState().<Integer>getStat(StatType.INVENTORY_0));
                    ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(itemRl));
                    if(!stack.isEmpty() && inv.getSelected().isEmpty()) {
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
            Rotcg.ACTIVE_CONNECTION.controller.sendChatMessage(text);
        }
    }

}
