package net.tiffit.rotcg.registry.entity;

import net.tiffit.realmnetapi.assets.xml.Animation;
import net.tiffit.realmnetapi.assets.xml.GameObject;
import net.tiffit.realmnetapi.assets.xml.Texture;
import net.tiffit.realmnetapi.assets.xml.XMLLoader;
import net.tiffit.realmnetapi.map.object.RObject;
import net.tiffit.realmnetapi.net.RealmNetworker;

import java.util.Map;

public class RAnimationManager {

    private final RObject obj;
    private GameObject textureObj;
    private final RotcgEntity entity;
    private Animation current;
    private Animation.AnimationFrame frame;
    private int currentFrame = 0;
    private long currentFrameTime = 0;

    public RAnimationManager(RObject obj, RotcgEntity entity) {
        this.obj = obj;
        this.textureObj = obj.getGameObject();
        if(!textureObj.defaultSkin.isEmpty()){
            textureObj = XMLLoader.ID_TO_OBJECT.get(textureObj.defaultSkin);
        }
        this.entity = entity;
        Map<String, Animation> animations = textureObj.animations;
        if(animations.containsKey("")){
            setAnimation(animations.get(""));
        }
    }
    public Texture getTexture(){
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
    }
    public void setAnimation(Animation animation){
        current = animation;
        setFrame(0);
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
