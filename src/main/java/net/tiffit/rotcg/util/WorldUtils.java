package net.tiffit.rotcg.util;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.tiffit.realmnetapi.map.RMap;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.util.math.Vec2f;
import net.tiffit.rotcg.Rotcg;
import net.tiffit.rotcg.registry.entity.RotcgEntity;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class WorldUtils {

    public static void clearExistingWorlds(){
        Minecraft mc = Minecraft.getInstance();
        File saves = new File(mc.gameDirectory, "saves");
        for(File file : Objects.requireNonNull(saves.listFiles())){
            clearWorld(file);
        }
    }

    public static void clearWorld(File file){
        if(file.isDirectory() && file.getName().startsWith("rotcg_")) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File createEmptyCopy() throws IOException {
        Minecraft mc = Minecraft.getInstance();
        File templateFolder = new File(mc.gameDirectory, "Template World");
        File worldFolder = new File(mc.gameDirectory, "saves/rotcg_" + System.currentTimeMillis());
        FileUtils.copyDirectory(templateFolder, worldFolder);
        return worldFolder;
    }
}
