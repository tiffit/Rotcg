package net.tiffit.rotcg.util;

public class MoveSpeedUtil {

    public static double bpsToMoveSpeed(double bps){
        double mcSpeed = bps / 4.3478f;
        return 0.11f * mcSpeed;
    }

}
