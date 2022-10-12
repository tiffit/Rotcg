package net.tiffit.rotcg.render.hud.info;

public class HUDInfoData {

    private int posY = 0;

    public int getPosY(){
        return posY;
    }

    public void increasePosY(int amount){
        posY += amount;
    }

}
