package net.tiffit.rotcg;

import net.tiffit.realmnetapi.auth.AccessToken;
import net.tiffit.realmnetapi.auth.RotmgEnv;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class RotcgConfig{
    private static File SaveLocation = new File("./rotcg.json");

    public Map<RotmgEnv, AccessToken> tokens = new HashMap<>();
    public RotmgEnv env = RotmgEnv.PRODUCTION;

    public AccessToken getToken(RotmgEnv env){
        return tokens.getOrDefault(env, null);
    }

    public void save(){
        String json = Rotcg.GSON.toJson(this);
        try {
            FileUtils.write(SaveLocation, json, Charset.defaultCharset(), false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static RotcgConfig load(){
        try {
            return Rotcg.GSON.fromJson(new FileReader(SaveLocation), RotcgConfig.class);
        } catch (FileNotFoundException e) {
            return new RotcgConfig();
        }
    }
}
