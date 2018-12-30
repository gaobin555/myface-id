package com.orbbec.keyguard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.orbbec.utils.FpsMeter;
import com.orbbec.utils.GlobalDef;
import com.orbbec.view.GlFrameSurface;

import org.openni.VideoMode;

/**
 * 单独用单例的原因，有一点是activity和surfaceView的回调顺序有时候会互相干扰，导致相机被错误的控制
 * 二是用单例可以避免大部分的opencamera失败的问题
 * 三是可以上传和显示分开来控制
 *
 */
public class CameraControl implements PreviewCallback, RgbCamera, Camera.ErrorCallback {

    private static final String TAG = "CameraControl";
    private static final boolean DEBUG = false;

    /** Camera出现错误后会发送此广播 */
    public static final String CAMERA_ERROR_INTENT_ACTION = "com.orbbec.keyguard.CameraControl.Error";

    private int mCameraWidth = 0;
    private int mCameraHeight = 0;

    private static CameraControl instance = null;
    private Camera mCamera;

    private int currentCameraId;
    private SurfaceHolder mHolder;
    /**
     * devices like nexus6
     */
    private boolean isFrontConversed;

    /**
     * 摄像头数据回调的方式
     */
    private int previewMode = PREVIEW_MODE_NULL;
    private static final int PREVIEW_MODE_NULL = 0x00;
    /**
     * hoder回调，有一个SurfaceView直接用来渲染画面
     */
    private static final int PREVIEW_MODE_HODER = 0x01;
    /**
     *
     */
    private static final int PREVIEW_MODE_TEXTURE = 0x02;

    private static final int MAGIC_TEXTURE_ID = 10;
    private SurfaceTexture gSurfaceTexture;
    private byte[] gBuffer;

    private Context mContext;
    private Camera.PreviewCallback mCallback;

    private VideoMode mVideoMode;
    private SurfaceView mSurfaceView;
    private GlFrameSurface mGLSurface;

    public static ByteBuffer nv21DirectScaleBuffer = null;

    private CameraControl() {
        init();
    }

    private void init() {
        isFrontConversed = false;
        if (Camera.getNumberOfCameras() > 1) {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(1, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                if (info.orientation == GlobalDef.NUMBER_90) {
                    isFrontConversed = true;
                }
            }
        } else {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        mCamera = null;
    }

    public static CameraControl getInstance() {
        if (instance == null) {
            synchronized (CameraControl.class) {
                if (instance == null) {
                    instance = new CameraControl();
                }
            }
        }
        return instance;
    }

    private void doInit() {
        openCamera();
        initStreamModes();
        //        configParams(mVideoMode);
        // 使用 Texture 回调数据
        if (mSurfaceView == null) {
            previewMode = PREVIEW_MODE_TEXTURE;
            setPreViewDisplay(null);
            // 使用 Texture 回调数据，并使用GLFrameSurface显示i420数据
        } else if (mSurfaceView instanceof GlFrameSurface) {
            previewMode = PREVIEW_MODE_TEXTURE;
            setPreViewDisplay((GlFrameSurface) mSurfaceView);
        } else {  // 使用Surface显示预览并回调数据
            previewMode = PREVIEW_MODE_HODER;
            setPreViewDisplayHolder(mSurfaceView.getHolder());
        }
    }

    private List<VideoMode> mModes = new ArrayList<>();
    private VideoMode curMode;

    @Override
    public boolean openCamera() {

        try {
            mCamera = Camera.open(0);
        } catch (Exception e) {
            log("Camera open 0 exception");
        }
        if (null == mCamera) {
            try {
                mCamera = Camera.open(1);
            } catch (Exception e) {
                log("Camera open 1 exception");
            }
        }
        if (mCamera != null){
            mCamera.setErrorCallback(this);
        }
        return true;
    }

    @Override
    public boolean closeCamera() {
        log("closeCamera");

        if (mCamera != null) {
            if (mHolder != null) {
                mCamera.setPreviewCallback(null);
            }
            try {
                mCamera.setPreviewTexture(null);
                mCamera.addCallbackBuffer(null);
                mCamera.setPreviewCallbackWithBuffer(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.release();
            mCamera = null;
            log("closeCamera >> release mCamera and set null");
        } else {
            log("closeCamera >> mCamera == null");
        }
        mSurfaceView = null;
        previewMode = PREVIEW_MODE_NULL;
        return true;
    }

    @Override
    public void initModeAndCallback(Context context, VideoMode mode, SurfaceView surfaceView, Camera.PreviewCallback callback, GlobalDef def){
        log("initModeAndCallback ...");
        mContext = context;
        this.mCallback = callback;
        this.mVideoMode = mode;
        this.mSurfaceView = surfaceView;
        this.mCameraWidth = def.getColorWidth();
        this.mCameraHeight = def.getColorHeight();
    }

    private int initStreamModes() {
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        for (int i = 0; i < previewSizes.size(); i++) {
            int w = previewSizes.get(i).width;
            int h = previewSizes.get(i).height;

            VideoMode mode = new VideoMode(w, h, 30, org.openni.PixelFormat.RGB888.toNative());
            log("support  modes  " + mode.toString());
            mModes.add(mode);
        }
        return 0;
    }

    private int configParams(VideoMode mode) {
        boolean support = false;
        for (VideoMode m : mModes) {
            if (m.equals(mode)) {
                support = true;
                curMode = m;
                break;
            }
        }
        if (!support) {
            return -1;
        }
        Camera.Parameters params = mCamera.getParameters();
        // FPS
        List<int[]> fpsRange = params.getSupportedPreviewFpsRange();

        for (int i = 0; i < fpsRange.size(); i++) {
            log("FPS range " + fpsRange.get(i)[0] + " " + fpsRange.get(i)[1] + " " + fpsRange.size());
        }

        params.setPreviewFpsRange(fpsRange.get(0)[0], fpsRange.get(0)[1]);
        params.setPreviewFormat(ImageFormat.NV21);
        params.setPreviewSize(mode.getResolutionX(), mode.getResolutionY());
        try {
            mCamera.setParameters(params);
        } catch (Exception e) {
            log("setParameters exception");
            return -1;
        }
        mCamera.setPreviewCallbackWithBuffer(this);
        log("set Mode ");
        for (int i = 0; i < GlobalDef.NUMBER_4; i++) {
            mCamera.addCallbackBuffer(new byte[mode.getResolutionX() * mode.getResolutionY() * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8]);
        }

        return 0;
    }

    public void setPreViewDisplay(GlFrameSurface glSurface) {
        // holder方式回调数据
        if (this.mHolder != null) {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
            }
            if (mCamera != null) {
                try {
                    mCamera.setPreviewDisplay(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCamera.stopPreview();
            }
        }
        this.mGLSurface = glSurface;
        previewMode = PREVIEW_MODE_TEXTURE;
        initCameraTexturePreview(mCamera);
        log("setPreViewDisplay(GLFrameSurface glSurface, GLFrameRenderer glFRenderer)");
    }

    /**
     * 使用setPreviewDisplay(mHolder)方式回调
     * should call before startPreview()
     */
    public void setPreViewDisplayHolder(SurfaceHolder holder) {
        log("setPreViewDisplay");

        if (previewMode == PREVIEW_MODE_TEXTURE) {
            removeCameraTexturePreview();
        }

        this.mHolder = holder;
        try {
            if (mCamera != null) {
                previewMode = PREVIEW_MODE_HODER;
                mCamera.setPreviewDisplay(mHolder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用 TexturePreview 方式回调数据
     *
     * @param gCamera
     */
    private void initCameraTexturePreview(Camera gCamera) {
        if (gSurfaceTexture == null) {
            gSurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
        }
        try {
            gCamera.setPreviewTexture(gSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Camera.Parameters parameters = gCamera.getParameters();
        List<Size> preSize = parameters.getSupportedPreviewSizes();
        calculateResolution(mCameraWidth, mCameraHeight);
        gBuffer = new byte[mCameraWidth * mCameraHeight * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8];
        gCamera.addCallbackBuffer(gBuffer);
        gCamera.setPreviewCallbackWithBuffer(this);
    }

    private void removeCameraTexturePreview() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.addCallbackBuffer(null);
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
        }
    }

    /**
     * either before startPreview() or later is ok
     */
    public void setDisplayOrientation(int degrees) {
        if (mCamera != null) {
            mCamera.setDisplayOrientation(degrees);
        }
    }

    /**
     * 设置摄像头方向
     */
    public void setPreviewDegree() {
        //        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        //            if (currentCameraId == 1 && isFrontConversed) {
        //                setDisplayOrientation(180);
        //            } else {
        //                setDisplayOrientation(0);
        //            }
        //        } else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        //            if (currentCameraId == 1 && isFrontConversed) {
        //                setDisplayOrientation(270);
        //            } else {
        //                setDisplayOrientation(90);
        //            }
        //        }
    }

    @Override
    public void startPreview() {
        if (mCamera == null) {
            doInit();
        }
        if (mCamera != null) {
            log("startCameraPreview");
            if (previewMode == PREVIEW_MODE_HODER) {
                try {
                    setPreviewDegree();
                    mCamera.startPreview();
                    autoFocus();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

                mCamera.setPreviewCallback(this);
                mCamera.startPreview();
            }
        }
    }

    /**
     * will cause stopWriteUnderlyingData() and reset orientation,too
     */
    @Override
    public void stopPreview() {

        this.mGLSurface = null;
        // holder方式回调数据
        if (this.mHolder != null) {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
            }
            this.mHolder = null;
        }
        previewMode = PREVIEW_MODE_NULL;
    }

    public List<Size> getSupportedPreviewSizes() {
        Parameters parameters = mCamera.getParameters();
        List<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        for (int i = 0; i < supportedPreviewSizes.size(); i++) {
            Size s = supportedPreviewSizes.get(i);
            log("SupportedPreviewSizes.....w=" + s.width + ",h" + s.height);
        }
        return supportedPreviewSizes;
    }

    private void calculateResolution(int width, int height) {

        Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(width, height);
        mCamera.setParameters(parameters);
    }

    public Size getResolution() {
        System.out.println("getResolution()...");

        if (mCamera == null) {
            System.out.println("mCamera == null");
            return null;
        } else {
            System.out.println("mCamera.getParameters() == " + (mCamera.getParameters() == null ? "null" : "not null"));
            System.out.println("mCamera.getParameters().getPreviewSize() == " + (mCamera.getParameters().getPreviewSize() == null ? "null" : "not null"));

            return mCamera.getParameters().getPreviewSize();
        }
    }

    /**
     * 获取系统中摄像头的数量
     *
     * @return
     */
    public int getCameraCount() {
        return Camera.getNumberOfCameras();
    }

    public void autoFocus() {
        if (mCamera != null) {
            String focusMode = mCamera.getParameters().getFocusMode();
            if (focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO)
                    || focusMode.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                mCamera.autoFocus(null);
            }
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        camera.addCallbackBuffer(gBuffer);
        if (mCamera == null || data == null || data.length == 0) {
            log("GameDisplay onPreviewFrame  gCamera==null || data.length == 0");
            return;
        }

        Camera.Size size = camera.getParameters().getPreviewSize();
        if (mGLSurface != null) {
//            if (false) {
//                //T裁切数据...
//                if (nv21DirectScaleBuffer == null) {
//                    nv21DirectScaleBuffer = ByteBuffer.allocateDirect(depthWidth * depthHeight * 3 / 2);
//                }
//                OrbbecUtils.CropScaleNV21AndRendering(data, data.length, size.width, size.height, nv21DirectScaleBuffer, depthWidth, depthHeight);
//            }

            // 渲染
            mGLSurface.update(size.width, size.height);
            mGLSurface.updateNV21Data(data);


            checkCameraFPS();  // 测试用，统计fps
        }
        if (mCallback != null) {
            if (false) {
                mCallback.onPreviewFrame(nv21DirectScaleBuffer.array(), camera);
            }
            mCallback.onPreviewFrame(data, camera);
        }
    }

    public static void writeImageToDisk(byte[] img, String fileName) {
        try {
            File file = new File("/sdcard/" + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fops = new FileOutputStream(file, true);
            fops.write(img);
            fops.flush();
            fops.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将byte数组写入文件
     *
     * @param path
     * @param content
     * @throws IOException
     */
    public void createFile(String path, byte[] content) throws IOException {

        FileOutputStream fos = new FileOutputStream(path);
        fos.write(content);
        fos.close();
    }

    int times = 0;
    private FpsMeter fpsMeter = new FpsMeter();

    private void checkCameraFPS() {
        if (DEBUG) {
            times++;
            fpsMeter.mesureFps();
            if (times % 30 == 0){
                log("color fps = " + fpsMeter.getFps());
            }
        }
    }

    private void log(String str) {
        if (DEBUG) {
            Log.e(TAG, str);
        }
    }

    @Override
    public void onError(int i, Camera camera) {
        Log.e(TAG, "camera error = " + i);
        new Exception().printStackTrace();

        if (mContext != null){
            Intent intent = new Intent();
            intent.setAction(CAMERA_ERROR_INTENT_ACTION);
            mContext.sendBroadcast(intent);
        }
    }

}
