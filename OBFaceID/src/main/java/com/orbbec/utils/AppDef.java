package com.orbbec.utils;


public class AppDef extends GlobalDef{

    public int getColorWidth(){
        return 640;
    }

    public int getColorHeight(){
        return 480;
    }

    public int getDepthWidth(){
        return RES_DEFAULT_DEPTH_WIDTH;
    }

    public int getDepthHeight(){
        return RES_DEFAULT_DEPTH_HEIGHT;
    }
}