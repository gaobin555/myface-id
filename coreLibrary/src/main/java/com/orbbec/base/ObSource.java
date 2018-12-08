package com.orbbec.base;

import android.view.SurfaceView;



public interface ObSource {

    /**
     * 设置视图
     * @param surfaceView
     */
    void setSurfaceView(SurfaceView surfaceView);

    /**
     * 初始化驱动
     */
    void initDevice();

    /**
     * 开始color
     */
    void startColor();

    /**
     * 开始depth
     */
    void startDepth();

    /**
     * 停止color
     */
    void stopColor();

    /**
     * 停止depth
     */
    void stopDepth();

    /**
     * 释放
     */
    void onRelease();

    /**
     * 判断是否是UVC
     * @return
     */
    boolean isUVC();

    /**
     * 判断是否为朵朵
     * @return
     */
    boolean isDuoDuo();

    /**
     * 判断是否是Deeyea
     * deeyea uvc 640*400
     * <p>
     * RGB 镜像已经翻转过了
     *
     * @return
     */
    boolean Deeyea();

}
