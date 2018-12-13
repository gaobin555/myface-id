package com.orbbec.keyguard;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.SurfaceView;

import com.lzh.easythread.EasyThread;
import com.orbbec.base.ObSource;
import com.orbbec.base.OrbbecPresenter;
import com.orbbec.utils.FpsMeter;
import com.orbbec.utils.GlobalDef;
import com.orbbec.utils.LogUtil;
import com.orbbec.utils.OpenNiHelper;
import com.orbbec.view.GlFrameSurface;
import com.orbbec.view.UvcSurfaceView;

import org.openni.Device;
import org.openni.ImageRegistrationMode;
import org.openni.OpenNI;
import org.openni.PixelFormat;
import org.openni.SensorType;
import org.openni.VideoFrameRef;
import org.openni.VideoMode;
import org.openni.VideoStream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;


/**
 * @author cjj
 */
public class ObDataSource implements ObSource {

    private static  boolean IS_CANGLONG =false ;
    private static boolean IS_DEEYEA = false ;
    private static boolean IS_UVC = false;
    private static boolean IS_DUODUO = false;

    private static int mDepthWidth;
    private static int mDepthHeight;
    private static int mColorWidth;
    private static int mColorHeight;

    private static final String TAG = "ObDataSource";
    private static final boolean DEBUG = true;
    /**
     * uvc
     */
    private SurfaceView mSurfaceView;
    private RgbCamera mCamera;
    private byte[] mColorData = null;
    /**
     * astra color
     */
    private VideoStream mColorStream;
    private ByteBuffer mColorBuffer;
    /**
     * astra depth
     */
    private VideoStream mDepthStream;
    private ByteBuffer mDepthBuffer;
    /**
     * base
     */
    private OpenNiHelper mOpenNiHelper;
    private Device mDevice;
    private boolean isUsbPermissionGrant = false;
    private volatile List<VideoStream> mColorStreams = new ArrayList<>();
    private volatile List<VideoStream> mDepthStreams = new ArrayList<>();
    private volatile boolean isExitData = false;
    private Context mContext;
    private OrbbecPresenter mOrbbecPresenter;
    private EasyThread easyThread = null;
    private GlobalDef mDef;

    public ObDataSource(Context context, OrbbecPresenter orbbecPresenter, GlobalDef def) {
        mContext = context;
        mOrbbecPresenter = orbbecPresenter;
        easyThread = EasyThread.Builder.createFixed(4).build();
        mDef = def;
        mDepthWidth = mDef.getDepthWidth();
        mDepthHeight = mDef.getDepthHeight();
        mColorWidth = mDef.getColorWidth();
        mColorHeight = mDef.getColorHeight();

        Log.d(TAG, "ObDataSource: " + easyThread.getExecutor().isShutdown() + ":" + easyThread.getExecutor().isTerminated());
    }

    @Override
    public void setSurfaceView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
    }

    @Override
    public void initDevice() {
        mColorStreams.clear();
        mDepthStreams.clear();
        if (getDevList().isEmpty()) {
            if (mOrbbecPresenter != null) {
                mOrbbecPresenter.onNoDevice();
            }
            return;
        }
        if (mOpenNiHelper == null) {
            mOpenNiHelper = new OpenNiHelper(mContext);
        }
        if (!isUsbPermissionGrant) {
            mOpenNiHelper.requestDeviceOpen(mDeviceOpenListener);
        }
    }

    @Override
    public void startColor() {
        if (IS_UVC) {
            if (mCamera != null) {
                mCamera.startPreview();
                LogUtil.d(TAG + " startColor:IS_UVC mCamera.startPreview()");
            }
        } else {
            if (mColorStream != null) {
                mColorStream.start();
                mColorStreams.add(mColorStream);
                LogUtil.d(TAG + " startColor:!IS_UVC mColorStreams.start()");
            }
        }
    }

    @Override
    public void startDepth() {
        if (mDepthStreams.size() <= 0) {
            mDepthStream.start();
            mDepthStreams.add(mDepthStream);
        }
    }

    @Override
    public void stopColor() {
        if (IS_UVC) {
            mCamera.stopPreview();
            LogUtil.d(TAG + "startColor:IS_UVC mCamera.stopPreview()");
        } else {
            if (mColorStream != null) {
                mColorStreams.remove(mColorStream);
                mColorStream.stop();
                LogUtil.d(TAG + "startColor:!IS_UVC mColorStreams.stop()");
            }
        }
    }

    @Override
    public void stopDepth() {
        if (mDepthStreams != null && mDepthStream != null) {
            mDepthStreams.remove(mDepthStream);
            mDepthStream.stop();
        }
    }

    @Override
    public void onRelease() {
        if (isExitData) {
            return;
        }
        isExitData = true;
        logd("onRelease: ");
        if (mCamera != null) {
            mCamera.closeCamera();
            logd(" : close");
        }
        if (mSurfaceView != null) {
            if (mSurfaceView instanceof UvcSurfaceView) {
                ((UvcSurfaceView) mSurfaceView).removeCamera();
            } else if (mSurfaceView instanceof GlFrameSurface) {
                ((GlFrameSurface) mSurfaceView).removeCamera();
            }
        }

        if (!IS_UVC) {
            stopColor();
        }
        stopDepth();
        try {
            if (mDevice != null && mOpenNiHelper.getUsbDevice() != null) {
                mDevice.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mOpenNiHelper != null) {
            mOpenNiHelper.shutdown();
        }

        mContext = null;
        mOrbbecPresenter = null;

    }

    @Override
    public boolean isUVC() {
        return IS_UVC;
    }

    @Override
    public boolean isDuoDuo() {
        return IS_DUODUO;
    }

    @Override
    public boolean Deeyea() {
        return  IS_DEEYEA;
    }

    public static boolean isUVC(Context context) {
        boolean isUVCproduct = false;
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> iterator = deviceList.values().iterator();
        while (iterator.hasNext()) {
            UsbDevice device = iterator.next();
            int vendorId = device.getVendorId();
            int productId = device.getProductId();
            boolean ob = (vendorId == GlobalDef.VENDORID_0x1D2 && (productId == GlobalDef.PRODUCTID_05FC || productId != GlobalDef.PRODUCTID_0601)) ||
                    (vendorId == GlobalDef.VENDORID_0_x2BC5 && (productId >= GlobalDef.PRODUCTID_0401 && productId <= GlobalDef.PRODUCTID_04FF)) ||
                    (vendorId == GlobalDef.VENDORID_0_x2BC5 && (productId >= GlobalDef.PRODUCTID_0601 && productId <= GlobalDef.PRODUCTID_06FF));
            if (ob) {
                if (productId == GlobalDef.ASTER_PRO || productId == GlobalDef.Deeyea || productId == GlobalDef.P2 || productId == GlobalDef.CANGLONG) {
                    isUVCproduct = true;
                } else if (productId == GlobalDef.DUO_DUO) {
                } else if (productId == GlobalDef.ASTER) {

                }
                continue;
            }
        }
        return isUVCproduct;
    }

    public static boolean isDouDou(Context context) {
        boolean isDuoDuoProduct = false;
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> iterator = deviceList.values().iterator();
        while (iterator.hasNext()) {
            UsbDevice device = iterator.next();
            int vendorId = device.getVendorId();
            int productId = device.getProductId();
            boolean ob = (vendorId == GlobalDef.VENDORID_0x1D2 && (productId == GlobalDef.PRODUCTID_05FC || productId != GlobalDef.PRODUCTID_0601)) ||
                    (vendorId == GlobalDef.VENDORID_0_x2BC5 && (productId >= GlobalDef.PRODUCTID_0401 && productId <= GlobalDef.PRODUCTID_04FF)) ||
                    (vendorId == GlobalDef.VENDORID_0_x2BC5 && (productId >= GlobalDef.PRODUCTID_0601 && productId <= GlobalDef.PRODUCTID_06FF));
            if (ob) {
                if (productId == GlobalDef.ASTER_PRO || productId == GlobalDef.Deeyea || productId == GlobalDef.P2 || productId == GlobalDef.CANGLONG) {
                    IS_UVC = true;
                    //                    || productId == GlobalDef.LUNA_DVT2
                } else if (productId == GlobalDef.DUO_DUO) {
                    isDuoDuoProduct = true;
                } else if (productId == GlobalDef.ASTER) {

                }
                continue;
            }
        }
        return isDuoDuoProduct;
    }
    private HashMap<String, UsbDevice> getDevList() {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> iterator = deviceList.values().iterator();
        while (iterator.hasNext()) {
            UsbDevice device = iterator.next();
            int vendorId = device.getVendorId();
            int productId = device.getProductId();
            Log.d(TAG, "getDevList: " + productId);
            boolean ob = (vendorId == GlobalDef.VENDORID_0x1D2 && (productId == GlobalDef.PRODUCTID_05FC || productId != GlobalDef.PRODUCTID_0601)) ||
                    (vendorId == GlobalDef.VENDORID_0_x2BC5 && (productId >= GlobalDef.PRODUCTID_0401 && productId <= GlobalDef.PRODUCTID_04FF)) ||
                    (vendorId == GlobalDef.VENDORID_0_x2BC5 && (productId >= GlobalDef.PRODUCTID_0601 && productId <= GlobalDef.PRODUCTID_06FF));
            if (ob) {
                if (productId == GlobalDef.ASTER_PRO || productId == GlobalDef.Deeyea || productId == GlobalDef.P2 || productId == GlobalDef.CANGLONG) {
                    IS_UVC = true;
                    if (productId == GlobalDef.Deeyea ) {
                        mDepthWidth = GlobalDef.RES_DUODUO_DEPTH_WIDTH;
                        mDepthHeight = GlobalDef.RES_DUODUO_DEPTH_HEIGHT;
                        IS_DEEYEA = true;
                    }
                    if (productId == GlobalDef.CANGLONG){
                        mDepthWidth = GlobalDef.RES_DUODUO_DEPTH_WIDTH;
                        mDepthHeight = GlobalDef.RES_DUODUO_DEPTH_HEIGHT;
                        IS_CANGLONG = true;
                    }

                    //                    || productId == GlobalDef.LUNA_DVT2
                } else if (productId == GlobalDef.DUO_DUO || productId == GlobalDef.LUNA_DVT2) {
                    //                    IS_DUODUO = true;
                    //                    mDepthWidth = GlobalDef.RES_DUODUO_DEPTH_WIDTH;
                    //                    mDepthHeight = GlobalDef.RES_DUODUO_DEPTH_HEIGHT;
                } else if (productId == GlobalDef.ASTER) {
                }
                continue;
            } else {
                iterator.remove();
            }
        }

        return deviceList;
    }

    private OpenNiHelper.DeviceOpenListener mDeviceOpenListener = new OpenNiHelper.DeviceOpenListener() {
        @Override
        public void onDeviceOpened(UsbDevice device) {

            OpenNI.setLogAndroidOutput(true);
            OpenNI.setLogMinSeverity(0);
            OpenNI.initialize();
            mDevice = Device.open();
            if (IS_UVC) {

                VideoMode colorMode = new VideoMode();
                colorMode.setResolution(mColorWidth, mColorHeight);
                colorMode.setFps(30);
                logd("onDeviceOpened: OBDataDource");
                mCamera = CameraControl.getInstance();
                mCamera.initModeAndCallback(mContext, colorMode, null, mPreviewCallback, mDef);
            } else {
                mColorStream = VideoStream.create(mDevice, SensorType.COLOR);
                VideoMode colorMode = mColorStream.getVideoMode();
                colorMode.setResolution(mColorWidth, mColorHeight);
                // RGB888    YUYV=YUY2   YUV422=UYVY
                colorMode.setPixelFormat(PixelFormat.YUYV);
                mColorStream.setVideoMode(colorMode);
            }
            mDepthStream = VideoStream.create(mDevice, SensorType.DEPTH);
            if (isUVC()&&!Deeyea()) {
                mDepthStream.setMirroringEnabled(false);
            }
            List<VideoMode> mVideoModes = mDepthStream.getSensorInfo().getSupportedVideoModes();
            for (VideoMode mode : mVideoModes) {
                int x = mode.getResolutionX();
                int y = mode.getResolutionY();
                int fps = mode.getFps();
                logd(" support resolution: " + x + " x " + y + " fps: " + fps + ", (" + mode.getPixelFormat() + ")");
                if (x == mDepthWidth && y == mDepthHeight && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                    mDepthStream.setVideoMode(mode);
                    Log.v(TAG, " setmode");
                }
            }
            mDevice.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
            mDevice.setDepthColorSyncEnabled(true);

            initDataThread();
            if (mOrbbecPresenter != null) {
                mOrbbecPresenter.onDeviceOpened();
            }
        }

        @Override
        public void onDeviceOpenFailed(UsbDevice device) {
            if (mOrbbecPresenter != null) {
                mOrbbecPresenter.onDeviceOpenFailed();
            }
            Log.e("downtap", "onDeviceOpenFailed");
        }
    };


    /**
     * 内存
     */
    private void initDataThread() {
        easyThread.execute(new Runnable() {
            @Override
            public void run() {
                while (!isExitData) {
                    // 在深度图关闭只剩下UVC的时候等待一下，因为UVC每秒最多30帧数据
                    if (mColorStreams.size() <= 0 && mDepthStreams.size() <= 0 && mColorData == null) {
                        //                        logd("mColorStreams.size() <= 0 && mDepthStreams.size() <= 0 && mColorData == null");
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (isExitData) {
                        continue;
                    }

                    try {

                        if (mColorStreams.size() > 0) {
                            OpenNI.waitForAnyStream(mColorStreams, GlobalDef.GLOBAL_STREAM_TIMEOUT);
                        }
                        if (mDepthStreams.size() > 0) {
                            OpenNI.waitForAnyStream(mDepthStreams, GlobalDef.GLOBAL_STREAM_TIMEOUT);
                        }
                    } catch (TimeoutException e) {
                        Log.d(TAG, "run: 异常");
                        continue;
                    }
                    if (isExitData) {
                        continue;
                    }

                    if (mOrbbecPresenter != null && mColorData != null) {
                        mOrbbecPresenter.onColorUpdate(mColorData);
                    }

                    if (mColorStream != null && mColorStreams.contains(mColorStream)) {

                        VideoFrameRef videoFrameRef = mColorStream.readFrame();
                        mColorBuffer = videoFrameRef.getData();
                        mOrbbecPresenter.onColorUpdate(mColorBuffer, videoFrameRef.getStrideInBytes());
                        videoFrameRef.release();
                    }
                    if (mDepthStreams.contains(mDepthStream)) {

                        VideoFrameRef videoFrameRef = mDepthStream.readFrame();
                        mDepthBuffer = videoFrameRef.getData();
                        mOrbbecPresenter.onDepthUpdate(mDepthBuffer, videoFrameRef.getWidth(), videoFrameRef.getHeight(), videoFrameRef.getSensorType(), videoFrameRef.getStrideInBytes());
                        videoFrameRef.release();
                    } else {

                    }

                    if (mOrbbecPresenter != null) {
                        mOrbbecPresenter.onFaceTrack();
                    }
                }
            }
        });
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            //            logd("onPreviewFrame: 888888" + data);
            checkCameraFPS();
            synchronized (ObDataSource.this) {
                mColorData = data;
            }
        }
    };

    int times = 0;
    private FpsMeter fpsMeter = new FpsMeter();

    private void checkCameraFPS() {
        if (DEBUG) {
            times++;
            fpsMeter.mesureFps();
            if (times % 30 == 0) {
                Log.i(TAG, "color fps = " + fpsMeter.getFps());
            }
        }
    }

    private void logd(String str) {
        if (DEBUG) {
            Log.d(TAG, str);
        }
    }
}
