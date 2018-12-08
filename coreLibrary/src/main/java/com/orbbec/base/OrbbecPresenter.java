package com.orbbec.base;

import android.view.SurfaceView;

import org.openni.SensorType;

import java.nio.ByteBuffer;


/**
 * 数据操作及状态回调接口
 *
 * @author lgp
 */
public interface OrbbecPresenter {

    interface View {
        /**
         * show fps
         *
         * @param fps
         */
        void showRgbFps(String fps);

        /**
         * show  depth fps
         *
         * @param fps
         */
        void showDepthFps(String fps);
    }

    /**
     * 设置启动回调
     *
     * @param deviceCallback
     */
    void setDeviceCallback(DeviceCallback deviceCallback);

    /**
     * 设置数据源
     *
     * @param dataSource
     */
    void setOBSource(ObSource dataSource);

    /**
     * 设置测量距离有效距离
     *
     * @param maxDistance 最远有效距离
     * @param minDistance 最近有效距离
     */
    void setDistance(int maxDistance, int minDistance);

    /**
     * 未检测到设备
     */
    void onNoDevice();

    /**
     * 设备已打开
     */
    void onDeviceOpened();

    /**
     * 设备打开失败
     */
    void onDeviceOpenFailed();

    /**
     * uvc 彩色数据回调
     *
     * @param data nv21
     */
    void onColorUpdate(byte[] data);

    /**
     * astra color 彩色数据回调
     *
     * @param data          rgb888
     * @param strideInBytes
     */
    void onColorUpdate(ByteBuffer data, int strideInBytes);

    /**
     * 深度数据回调
     *
     * @param data
     * @param width
     * @param height
     * @param sensorType
     * @param strideInBytes
     */
    void onDepthUpdate(ByteBuffer data, int width, int height, SensorType sensorType, int strideInBytes);

    /**
     * 启动人脸跟踪，这一步会在 onColorUpdate(...) 和 onDepthUpdate(...) 调用后才调用，可用于人脸跟踪的需求
     */
    void onFaceTrack();

    /**
     * android 相机框架回调使用
     *
     * @param surfaceView
     */
    void setSurfaceView(SurfaceView surfaceView);
}
