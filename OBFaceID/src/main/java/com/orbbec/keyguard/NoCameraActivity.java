package com.orbbec.keyguard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import java.io.File;

import mobile.ReadFace.YMFaceTrack;
import com.orbbec.constant.Constant;

/**
 * 非相机相关Activity,并且要初始化sdk的基类
 */
public abstract class NoCameraActivity extends BaseActivity {

    protected YMFaceTrack faceTrack;
    private final Object lock = new Object();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        synchronized (lock) {
            startTrack();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        synchronized (lock) {
            stopTrack();
        }
    }

    /**
     * 初始化
     */
    public void startTrack() {
        if (faceTrack != null) {
            return;
        }
        faceTrack = new YMFaceTrack();
        //设置人脸检测距离，默认近距离，需要在initTrack之前调用
        faceTrack.setDistanceType(YMFaceTrack.DISTANCE_TYPE_FAR);
        //license激活版本初始化
        //int result = faceTrack.initTrack(this, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640,
        //SenseConfig.appid, SenseConfig.appsecret);

        //普通有效期版本初始化
        File file = new File(Constant.FeatureDatabasePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        int result = faceTrack.initTrack(this, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640, Constant.FeatureDatabasePath);
        //设置人脸识别置信度，设置75，不允许修改
        if (result == 0) {
            //faceTrack.setRecognitionConfidence(75);
            showShortToast(this, "初始化检测器成功");
        } else {
            showShortToast(this, "初始化检测器失败: " + result);
        }
    }

    public void stopTrack() {
        if (faceTrack == null) {
            return;
        }
        faceTrack.onRelease();
        faceTrack = null;
    }
}