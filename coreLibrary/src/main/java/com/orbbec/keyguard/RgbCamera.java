package com.orbbec.keyguard;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceView;

import com.orbbec.utils.GlobalDef;

import org.openni.VideoMode;


public interface RgbCamera {

    /**
     * 初始化mode和数据回调
     * @param context
     * @param mode
     * @param surfaceView
     * @param callback
     * @param def
     */
    public void initModeAndCallback(Context context, VideoMode mode, SurfaceView surfaceView, Camera.PreviewCallback callback, GlobalDef def);

    /**
     * 打开相机
     * @return
     */
    public boolean openCamera();

    /**
     * 关闭相机
     * @return
     */
    public boolean closeCamera();

    /**
     *开启预览
     */
    public void startPreview();

    /**
     *停止预览
     */
    public void stopPreview();

}
