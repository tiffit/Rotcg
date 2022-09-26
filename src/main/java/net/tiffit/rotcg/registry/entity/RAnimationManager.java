package net.tiffit.rotcg.registry.entity;

import net.tiffit.realmnetapi.assets.xml.*;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.map.object.StatType;
import net.tiffit.realmnetapi.net.RealmNetworker;

import java.util.Map;

public class RAnimationManager {

    private final RObject obj;
    private GameObject textureObj;
    private Style currentStyle;
    private int currentStyleId = Style.EMPTY_PRESENTATION;
    private final RotcgEntity entity;
    private Animation current;
    private Animation.AnimationFrame frame;
    private int currentFrame = 0;
    private long currentFrameTime = 0;

    public RAnimationManager(RObject obj, RotcgEntity entity) {
        this.obj = obj;
        this.textureObj = obj.getGameObject();
        if(!textureObj.defaultSkin.isEmpty()){
            if(XMLLoader.ID_TO_OBJECT.containsKey(textureObj.defaultSkin)){
                textureObj = XMLLoader.ID_TO_OBJECT.get(textureObj.defaultSkin);
            }
        }
        this.entity = entity;
        Map<String, Animation> animations = textureObj.animations;
        if(animations.containsKey("")){
            setAnimation(animations.get(""));
        }
    }
    public Texture getTexture(){
        if(currentStyle != null){
            if(currentStyle.steps.size() > 0){
                Style.StyleStep step = currentStyle.steps.get(0);
                if(step instanceof Style.SetAltTextureStep s){
                    if(textureObj.altTextures.containsKey(s.altTextureId)){
                        return textureObj.altTextures.get(s.altTextureId);
                    }
                }
            }
        }
        if(inAnimation()){
            return frame.texture;
        }
        return textureObj.texture.get(0);
    }

    public void update(){
        if(inAnimation()){
            if(current.sync){
                if(System.currentTimeMillis() > currentFrameTime){
                    float totalTime = 0;
                    for (Animation.AnimationFrame frame : current.frames) {
                        totalTime += frame.time;
                    }
                    float currentTime = (RealmNetworker.getTime() / 1000f) % totalTime;
                    for (int i = 0; i < current.frames.size(); i++) {
                        currentTime -= current.frames.get(i).time;
                        if(currentTime <= 0){
                            setFrame(i);
                            break;
                        }
                    }
                }
            }else{
                if((System.currentTimeMillis() - currentFrameTime) / 1000d > frame.time){
                    setFrame((currentFrame + 1) % current.frames.size());
                }
            }
        }
        if(obj.getState().hasStat(StatType.TEXTURE)){
            int newTexId = obj.getState().getStat(StatType.TEXTURE);
            if(newTexId != 0 && (textureObj == null || textureObj.type != newTexId)){
                if(XMLLoader.OBJECTS.containsKey(newTexId)){
                    textureObj = XMLLoader.OBJECTS.get(newTexId);
                }
            }
        }
        if(obj.getState().hasStat(StatType.STYLE_ID_HASH)){
            int id = obj.getState().<Integer>getStat(StatType.STYLE_ID_HASH);
            if(id != currentStyleId){
                currentStyleId = id;
                if(id == Style.EMPTY_PRESENTATION){
                    currentStyle = null;
                    setAnimation(null);
                }else{
                    currentStyle = textureObj.styles.get(id);
                    if(currentStyle != null && currentStyle.steps.size() > 0){
                        Style.StyleStep step = currentStyle.steps.get(0);
                        if(step instanceof Style.SetAnimationStep s){
                            setAnimation(textureObj.animations.get(s.animationId));
                        }
                    }
                }
            }
        }
    }
    public void setAnimation(Animation animation){
        current = animation;
        if(current != null){
            setFrame(0);
        }
    }

    private void setFrame(int index){
        currentFrame = index;
        frame = current.frames.get(index);
        currentFrameTime = System.currentTimeMillis();
    }

    private void setFrameSync(int index){
        currentFrame = index;
        frame = current.frames.get(index);
        currentFrameTime = System.currentTimeMillis() + (int)(frame.time * 1000);
    }

    public boolean inAnimation(){
        return current != null;
    }

}
