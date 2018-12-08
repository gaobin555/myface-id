package com.orbbec.base;

import android.graphics.Bitmap;
import android.graphics.Rect;

import org.openni.SensorType;
import org.openni.VideoStream;

import java.nio.ByteBuffer;

/**
 * Created by jjc on 2017/9/7.
 */

/**
 * 检测流程回调
 *
 * @author lgp
 */
public interface DetectionCallback {
    /**
     * 返回当前帧
     *
     * @param colorBitmap UVC非镜像 bitmap为null，该流程直接绘制到SurfaceView
     */
    void onDrawColor(Bitmap colorBitmap);

    /**
     * 是否需要每针的ColorBitmap图片
     * 返回false则 {@linkplain DetectionCallback.onDrawColor(Bitmap)}}不会有回调数据
     *
     * @return
     */
    boolean needColorBitmap();

    /**
     * 未检测到人脸
     */
    void onNoFace();

    /**
     * 检测到人脸
     *
     * @param rect 人脸区域的位置及范围
     */
    void onFoundFace(Rect rect);

    /**
     * 超出设备识别范围，距离设备太近
     */
    void onClose();

    /**
     * 超出设备识别范围，距离设备太远
     */
    void onFar();


    //    void onDepthUpdate(VideoStream videoStream);

    /**
     * 深度数据更新
     *
     * @param data
     * @param width
     * @param height
     * @param sensorType
     * @param strideInBytes
     */
    void onDepthUpdate(ByteBuffer data, int width, int height, SensorType sensorType, int strideInBytes);

}
