package com.orbbec.base;

import android.graphics.Rect;

/**
 * Created by jjchai on 2017/8/9.
 */

/**
 * 人脸操作接口
 *
 * @author lgp
 */
public interface FacePresenter {

    /**
     * 设置检测流程回调
     *
     * @param detectionCallback
     */
    void setDetectionCallback(DetectionCallback detectionCallback);

    /**
     * 设置识别流程回调
     *
     * @param identifyCallback
     */
    void setIdentifyCallback(IdentifyCallback identifyCallback);

    /**
     * 人脸框位置
     *
     * @param rect  识别范围的裁切，这是以渲染视频的view的像素大小为基准的裁切
     */
    void setIdentificationRect(Rect rect);

    /**
     * 初始化人脸检测
     */
    void initFaceTrack();

    /**
     * 开始人脸检测
     */
    void startFaceTrack();

    /**
     * 结束人脸检测
     */
    void stopFaceTrack();

    /**
     * 启动人脸识别检测
     */
    void startIdentification();





}
