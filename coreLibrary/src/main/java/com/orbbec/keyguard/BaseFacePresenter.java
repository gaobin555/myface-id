package com.orbbec.keyguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.SparseArray;
import android.view.SurfaceView;

import com.lzh.easythread.EasyThread;
import com.orbbec.NativeNI.OrbbecUtils;
import com.orbbec.base.DetectionCallback;
import com.orbbec.base.DeviceCallback;
import com.orbbec.base.FacePresenter;
import com.orbbec.base.IdentifyCallback;
import com.orbbec.base.ObSource;
import com.orbbec.base.OrbbecPresenter;
import com.orbbec.constant.Constant;
import com.orbbec.model.User;
import com.orbbec.utils.DataSource;
import com.orbbec.utils.FpsMeter;
import com.orbbec.utils.GlobalDef;
import com.orbbec.utils.LogUtil;
import com.orbbec.utils.TrackUtil;
import com.orbbec.utils.XmyLog;
import com.orbbec.view.GlFrameSurface;
import org.openni.SensorType;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

import static com.orbbec.keyguard.BaseFacePresenter.Distance.MEASURE_DISTANCE_IS_OK;
import static com.orbbec.keyguard.BaseFacePresenter.Distance.MEASURE_DISTANCE_TOO_CLOSE;
import static com.orbbec.keyguard.BaseFacePresenter.Distance.MEASURE_DISTANCE_TOO_FAR;
import static dou.utils.HandleUtil.runOnUiThread;

/**
 * @author lgp
 *
 */
public abstract class BaseFacePresenter implements OrbbecPresenter, FacePresenter {
    private static final String TAG = "BaseFacePresenter";

    public static final int LIVENESS_STATUS_CHECKETING = 0;
    public static final int LIVENESS_STATUS_CHECK_SUCCESS = 1;
    public static final int LIVENESS_STATUS_CHECK_FAIL = 2;
    public static final int LIVENESS_STATUS_CHECK_INVALID = -1;
    public static final int IDENTIFY_PERSON_CHECK_SUCCESS = 3; // ADD 人脸识别成功

    private static final boolean DEBUG = false;
    private final DataSource mDBSource;
    protected Context mContext;
    private DeviceCallback mDeviceCallback;
    private DetectionCallback mDetectionCallback;
    private IdentifyCallback mIdentifyCallback;
    protected YMFaceTrack mYmFaceTrack;
    private boolean mFaceTrackExit = false;

    private int mColorWidth;
    private int mColorHeight;
    private int mDepthWidth;
    private int mDepthHeight;

    int noFaceCount = 0;

    private Bitmap mColorBitmap;
    /**
     * 用于测距离（人脸）时计算深度像素点的方差，判断是否超出深度测量范围
     */
    private short[] distanceShortArray;
    private short[] variance;
    private boolean enVariance = false;
    /**
     * 最大方差临界值，超过这个值则认为超过测距范围
     */
    private static final int VARIANCE_MAX_THRESHOLD = 1 * 1000;

    /**
     * 测距离（人脸）返回的值
     */
    public enum Distance {
        MEASURE_DISTANCE_TOO_CLOSE,
        MEASURE_DISTANCE_TOO_FAR,
        MEASURE_DISTANCE_IS_OK
    }


    protected List<YMFace> mYMFaceList;
    private volatile boolean isFaceListUpdata = false;
    private Object facesLock = new Object();
    protected boolean needIdentificationFace = false;
    private SparseArray<YMFace> faceArrays = new SparseArray<>();
    private int mMaxDistance = Integer.MAX_VALUE;
    private int mMinDistance = Integer.MIN_VALUE;
    private SurfaceView mSurfaceView;

    private int cropX = -1;
    private int cropY = -1;
    private int cropWidth = -1;
    private int cropHeitht = -1;

    private ByteBuffer temp422toNV21Buffer;
    private ByteBuffer tempI420ColorBuffer;

    private ByteBuffer tempColorBuffer;
    private ByteBuffer tempDepthBuffer;
    private ByteBuffer mDepthBuffer;
    private ByteBuffer mColorBuffer;
    private volatile boolean isBufferready = false;
    private volatile boolean isDepthReady = false;
    private Object colorBufferLock = new Object();
    private Object depthBufferLock = new Object();
    FpsMeter fpsMeter = new FpsMeter();
    FpsMeter fpsMeter1 = new FpsMeter();

    protected ObSource mDataSource;

    /**
     * 视频检测范围，数值是以渲染rgb数据的view的大小为参考的
     */
    private Rect mDetectReferColorView;
    /**
     * 视频检测范围，数值是以Camera分辨率的大小为参考的
     */
    private Rect mDetectReferVideoSize;

    private Rect mDstRect;
    private Rect mSrcRect;

    private float mColorViewWidth;
    private float mColorViewHeight;
    private int identifyPerson = -111;
    protected boolean isSendOpenGate = false;
    boolean isLiveness;
    private int livenessFailCount = 0;
    private int livenessCount = 0;
    private int tooFarOrCloseCount = 0;
    /**
     * 表情
     */
    private static String happystr = "";
    private volatile float mCurrentDistance = -1f;
    private User mCurrentUser;
    private String mCurrentUserName = "";
    private int livenessStatus;
    private EasyThread easyThread = null;
//    private EasyThread easyThread2 = null;


    /**
     * 是否需要检查距离
     *
     * @return
     */
    public abstract boolean needToMeasureDistance();

    /**
     * 返回是否需要做活体验证
     *
     * @param identifyPerson
     * @param nameFromPersonId
     * @param happy
     * @return
     */
    public abstract boolean needToCheckLiveness(int identifyPerson, String nameFromPersonId, int happy);

    /**
     *  获取用户姓名
     */
    public abstract String getNameFromPersonId(int personId);

    /**
     *  开门
     */
    public abstract boolean openTheGate(int personId);

    /**
     * 判断是否需要录入
     * @return
     */
    public abstract boolean isRegistTask();

    /**
     * 检测人脸对边框的间隔，避免半张脸录入的情况
     * 也避免深度图不全判断为太远或者太近或非活体的情况
     *
     * @return
     */
    public abstract int faceOffsetPixel();

    /**
     * 显示RGB数据的fps
     *
     * @param fps
     */
    public abstract void showRgbFpsToUI(String fps);

    /**
     * 显示Depth数据的fps
     *
     * @param fps
     */
    public abstract void showDepthFpsToUI(String fps);

    /**
     * 画人脸跟中框
     *
     * @param faces
     * @param scaleBit
     * @param videoWidth     每帧RGB原始数据源的宽，face的定位坐标是以此为依据的
     * @param currentUser
     * @param livenessStatus
     * @param distance
     */
    public abstract void drawFaceTrack(List<YMFace> faces, boolean toFlip, float scaleBit, int videoWidth, String currentUser,
                                       String mAge, String happystr, int livenessStatus, float distance);


    public BaseFacePresenter(Context context, float colorViewWidth, float colorViewHeight, GlobalDef def) {

        mColorWidth = def.getColorWidth();
        mColorHeight = def.getColorHeight();
        mDepthWidth = def.getDepthWidth();
        mDepthHeight = def.getDepthHeight();
        mContext = context;
        mDstRect = new Rect();
        mSrcRect = new Rect();
        mDetectReferVideoSize = new Rect();
        mColorViewWidth = colorViewWidth;
        mColorViewHeight = colorViewHeight;

        mDBSource = new DataSource(context);
        easyThread = EasyThread.Builder.createFixed(6).build();
//        easyThread2 = EasyThread.Builder.createFixed(6).build();
    }

    @Override
    public void setDetectionCallback(DetectionCallback detectionCallback) {
        mDetectionCallback = detectionCallback;
    }

    @Override
    public void setIdentifyCallback(IdentifyCallback identifyCallback) {
        mIdentifyCallback = identifyCallback;
    }

    /**
     * 识别范围的设置
     *
     * @param rect 这是以渲染视频的view的像素大小为基准的裁切
     */
    @Override
    public void setIdentificationRect(Rect rect) {
        mDetectReferColorView = rect;
        mDetectReferVideoSize.left = (int) (mDetectReferColorView.left * mColorWidth / mColorViewWidth);
        mDetectReferVideoSize.right = (int) (mDetectReferColorView.right * mColorWidth / mColorViewWidth);
        mDetectReferVideoSize.top = (int) (mDetectReferColorView.top * mColorHeight / mColorViewHeight);
        mDetectReferVideoSize.bottom = (int) (mDetectReferColorView.bottom * mColorHeight / mColorViewHeight);

        Rect r = mDetectReferColorView;
        LogUtil.e(TAG+" xxx mDetectReferColorView = " + r + ", (" + r.width() + " * " + r.height() + ")");
        r = mDetectReferVideoSize;
        LogUtil.w(TAG+" xxx mDetectReferVideoSize = " + r + ", (" + r.width() + " * " + r.height() + ")");

        cropX = mDetectReferVideoSize.left;
        cropY = mDetectReferVideoSize.top;
        cropWidth = mDetectReferVideoSize.right - mDetectReferVideoSize.left;
        cropHeitht = mDetectReferVideoSize.bottom - mDetectReferVideoSize.top;

        if (cropWidth >= mColorWidth) {
            cropX = -1;
            cropY = -1;
            cropWidth = -1;
            cropHeitht = -1;
        }
        if (cropWidth % GlobalDef.NUMBER_2 != 0) {
            cropWidth--;
        }
        if (cropHeitht % GlobalDef.NUMBER_2 != 0) {
            cropHeitht--;
        }
    }

    @Override
    public void initFaceTrack() {
        if (mYmFaceTrack == null) {
            mYmFaceTrack = new YMFaceTrack();
            int result = mYmFaceTrack.initTrack(mContext, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640, Constant.FeatureDatabasePath);
//            int result = mYmFaceTrack.initTrack(mContext, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640,
//                    Constant.appid, Constant.appsecret);
            if (result == 0){
                mYmFaceTrack.setRecognitionConfidence(75);
                LogUtil.i(TAG+" initTrack初始化检测器成功 + facedb size: " + mYmFaceTrack.getEnrolledPersonIds().size());
            } else {
                LogUtil.e(TAG + " initTrack初始化检测器失败: " + result);
            }

        }
    }


    @Override
    public void startFaceTrack() {
        mFaceTrackExit = false;
        faceTrackThread();
    }

    private void faceTrackThread() {
        easyThread.execute(new Runnable() {
            @Override
            public void run() {
                while (!mFaceTrackExit) {
                    if (!isBufferready) {
                        sleepTime(20);
                        continue;
                    }
                    if (mFaceTrackExit) {
                        continue;
                    }
                    if (mYmFaceTrack == null) {
                        if (DEBUG) {
                            LogUtil.i(TAG+ " mYmFaceTrack = null");
                        }
                        sleepTime(20);
                        continue;
                    }
                    if (mFaceTrackExit) {
                        continue;
                    }

                    ArrayList<YMFace> ymFaceList;
                    synchronized (facesLock) {

                        while (!isFaceListUpdata || mYMFaceList == null) {
                            try {
                                /** 这里沉睡，直到下一帧数据来到被 {@linkplain BaseFacePresenter#onFaceTrack()} 唤醒 */
                                facesLock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (mFaceTrackExit) {
                                break;
                            }
                        }
                        if (mFaceTrackExit) {
                            continue;
                        }
                        ymFaceList = new ArrayList<>(mYMFaceList);
                        isFaceListUpdata = false;
                    }

                    if (mYMFaceList != null) {
                        synchronized (depthBufferLock) {
                            if (tempDepthBuffer != null && mDepthBuffer != null) {
                                OrbbecUtils.ByteBufferCopy(tempDepthBuffer, mDepthBuffer, getFaceTrackWidth() * getFaceTrackHeight() * 2);
                            }
                        }
                        synchronized (colorBufferLock) {
                            OrbbecUtils.ByteBufferCopy(tempColorBuffer, mColorBuffer, getFaceTrackWidth() * getFaceTrackHeight() * 3 / 2);
                        }
                        mCurrentUser = null;
                        identification(ymFaceList);
                    }
                }
            }
        });
    }

    @Override
    public void stopFaceTrack() {

        LogUtil.e(TAG + " stopFaceTrack() ...");
        if (mFaceTrackExit) {
            LogUtil.e(TAG + "mFaceTrackExit = true");
            return;
        }

        mFaceTrackExit = true;
        synchronized (facesLock) {
            facesLock.notifyAll();  // 让等待面部识别的等待线程跳过等待
        }

        try {
            easyThread.getExecutor().awaitTermination(GlobalDef.PRO_MIX_DISTANCE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        easyThread.getExecutor().shutdownNow();
        if (mYmFaceTrack != null) {
            mYmFaceTrack.onRelease();
        }
    }

    @Override
    public void startIdentification() {
        needIdentificationFace = true;
    }


    private String age;

    @Override
    public void setDeviceCallback(DeviceCallback deviceCallback) {
        mDeviceCallback = deviceCallback;
    }

    @Override
    public void setOBSource(ObSource dataSource) {
        mDataSource = dataSource;
        if (mDataSource != null) {
            mDataSource.setSurfaceView(mSurfaceView);
            mDataSource.initDevice();
        }
    }

    @Override
    public void setDistance(int maxDistance, int minDistance) {
        mMaxDistance = maxDistance;
        mMinDistance = minDistance;
    }

    @Override
    public void onNoDevice() {
        if (mDeviceCallback != null) {
            mDeviceCallback.onNoDevices();
        }
    }

    @Override
    public void onDeviceOpened() {
        if (mDeviceCallback != null) {
            mDeviceCallback.onDeviceOpened();
        }
    }

    @Override
    public void onDeviceOpenFailed() {
        if (mDeviceCallback != null) {
            mDeviceCallback.onDeviceOpenFailed();
        }
    }

    /**
     * UVC 模式采集的nv21数据
     *
     * @param data nv21
     */
    @Override
    public void onColorUpdate(byte[] data) {

        //绘制
        if (mSurfaceView instanceof GlFrameSurface) {
            if (mDataSource.isUVC()&&!mDataSource.Deeyea()) {
                ((GlFrameSurface) mSurfaceView).changeRotation(0, true);
            }
            ((GlFrameSurface) mSurfaceView).updateNV21Data(data);
        }
        if (mDetectReferColorView == null) {
            LogUtil.d(TAG + " UVC mDetectReferColorView == null");
            return;
        }
        fpsMeter.mesureFps();
        showRgbFpsToUI(fpsMeter.getFps());

        if (mColorBuffer == null) {
            /** TODO: 这里得到的getFaceTrackWidth其实还是视频宽度，因为  {@link this#setIdentificationRect(Rect)} 还没有被调用 */

            mColorBuffer = ByteBuffer.allocateDirect(getFaceTrackWidth() * getFaceTrackHeight() * 3 / 2);
            tempColorBuffer = ByteBuffer.allocateDirect(getFaceTrackWidth() * getFaceTrackHeight() * 3 / 2);
        }

        synchronized (mDataSource) {
            if (isCrop()) {
                // 裁切YUV
                handlerImageClip(data, tempColorBuffer);
            } else {
                tempColorBuffer.clear();
                tempColorBuffer.put(data);
            }
            isBufferready = true;
        }
        if (mDetectionCallback != null && mDetectionCallback.needColorBitmap()) {
            if (mColorBitmap == null) {
                mColorBitmap = Bitmap.createBitmap(getRenderFrameWidth(), getRenderFrameHeight(), Bitmap.Config.ARGB_8888);
            }
            OrbbecUtils.NV21ToRGB32(getRenderFrameWidth(), getRenderFrameHeight(), tempColorBuffer.array(), mColorBitmap, true);
            mDetectionCallback.onDrawColor(mColorBitmap);
        }
        isBufferready = true;
    }

    /**
     * 由OpenNI读到
     *
     * @param data          rgb888
     * @param strideInBytes
     */
    @Override
    public void onColorUpdate(ByteBuffer data, int strideInBytes) {
        if (mDetectReferColorView == null) {
            LogUtil.d("onColorUpdate: rgb888 mDetectReferColorView == null");
            return;
        }
        fpsMeter.mesureFps();
        showRgbFpsToUI(fpsMeter.getFps());

        int width = getRenderFrameWidth();
        int height = getRenderFrameHeight();
        if (mColorBuffer == null) {
            /** TODO: 这里得到的getFaceTrackWidth其实还是视频宽度，因为  {@link this#setIdentificationRect(Rect)} 还没有被调用 */

            mColorBuffer = ByteBuffer.allocateDirect(getFaceTrackWidth() * getFaceTrackHeight() * 3 / 2);
            tempColorBuffer = ByteBuffer.allocateDirect(getFaceTrackWidth() * getFaceTrackHeight() * 3 / 2);

            tempI420ColorBuffer = ByteBuffer.allocateDirect(width * height * 3 / 2);
            temp422toNV21Buffer = ByteBuffer.allocateDirect(width * height * 3 / 2);
        }

        OrbbecUtils.CropYUY2toNV21AndI420(data, mColorWidth, mColorHeight, temp422toNV21Buffer, tempI420ColorBuffer, 0, 0, width, height);

        if (mSurfaceView instanceof GlFrameSurface) {
            ((GlFrameSurface) mSurfaceView).updateI420Frame(tempI420ColorBuffer);
        }

        synchronized (mDataSource) {
            if (isCrop()) {
                // 裁切YUV
                handlerImageClip(temp422toNV21Buffer.array(), tempColorBuffer);
            } else {
                tempColorBuffer = temp422toNV21Buffer;
            }
            isBufferready = true;
        }
        if (mDetectionCallback != null && mDetectionCallback.needColorBitmap()) {
            if (mColorBitmap == null) {
                mColorBitmap = Bitmap.createBitmap(getRenderFrameWidth(), getRenderFrameHeight(), Bitmap.Config.ARGB_8888);
            }
            OrbbecUtils.NV21ToRGB32(getRenderFrameWidth(), getRenderFrameHeight(), tempColorBuffer.array(), mColorBitmap, true);
            mDetectionCallback.onDrawColor(mColorBitmap);
        }
    }

    @Override
    public void onDepthUpdate(ByteBuffer data, int width, int height, SensorType sensorType, int strideInBytes) {
        if (mDetectReferColorView == null) {
            return;
        }
        fpsMeter1.mesureFps();
        showDepthFpsToUI(fpsMeter1.getFps());

        if (mDepthBuffer == null) {
            LogUtil.e(TAG + " xxx getFaceTrack = (" + cropX + ", " + cropY + "), " + getFaceTrackWidth() + " * " + getFaceTrackHeight());

            /** TODO: 这里得到的getFaceTrackWidth其实还是视频宽度，因为  {@link this#setIdentificationRect(Rect)} 还没有被调用 */
            mDepthBuffer = ByteBuffer.allocateDirect(getFaceTrackWidth() * getFaceTrackHeight() * 2);
            tempDepthBuffer = ByteBuffer.allocateDirect(getFaceTrackWidth() * getFaceTrackHeight() * 2);
            //字节顺序。
            mDepthBuffer.order(ByteOrder.nativeOrder());
            tempDepthBuffer.order(ByteOrder.nativeOrder());
        }

        synchronized (depthBufferLock) {
            if (isCrop()) {
                //裁剪深度
                handlerDepthClip(data, tempDepthBuffer);
            } else {
                OrbbecUtils.ByteBufferCopy(data, tempDepthBuffer, width * height * 2);
            }
            isDepthReady = true;
        }

        if (mDetectionCallback != null) {
            mDetectionCallback.onDepthUpdate(data, width, height, sensorType, strideInBytes);
        }
    }

    /**
     * 启动人脸跟踪，这一步会在 {@linkplain this#onColorUpdate(byte[])}( 或者 {@linkplain this#onColorUpdate(ByteBuffer, int)} )
     * 和 {@linkplain this#onDepthUpdate(ByteBuffer, int, int, SensorType, int)}
     * 调用后才调用，可用于人脸跟踪的需求
     */
    @Override
    public void onFaceTrack() {

        if (mFaceTrackExit) {
            LogUtil.d(TAG + " mFaceTrackExit");
            return;
        }
        if (!isBufferready) {
            LogUtil.d(TAG + " !isBufferready");
            return;
        }
        if (mYmFaceTrack == null) {
           // if (DEBUG) {
                XmyLog.i("mYmFaceTrack = null");
           // }
            return;
        }
        if (mDetectReferColorView == null) {
            LogUtil.e(TAG + "#setIdentificationRect(Rect) 检测范围未设置");
            return;
        }

        synchronized (facesLock) {

            // gaobin:回掉拍照录入接口
            if (isRegistTask()) {
                mIdentifyCallback.onRegistTrack(tempColorBuffer.array());
            } else {
                /* FixMe: 这步很快，可以跟踪到人脸的位置、关键点、角度、trackid */
                mYMFaceList = mYmFaceTrack.trackMulti(tempColorBuffer.array(), getFaceTrackWidth(), getFaceTrackHeight());
                // gaobin 第二次人脸后此处mYMFaceList.size()为 0
                if (mYMFaceList != null && mYMFaceList.size() > 0) {

                    /* 判断最大人脸是否变更，以清除人脸跟踪框的显示 */
                    checkMaxFaceIndexIsChangeOrNot(mYMFaceList);

                    isFaceListUpdata = true;
                    /** 这里会唤醒沉睡的 {@linkplain this#faceTrackThread()} */
                    facesLock.notifyAll();

                } else {
                    /*  人脸丢失或者没有人脸 */
                    livenessStatus = LIVENESS_STATUS_CHECKETING;
                    livenessCount = 0;
                    livenessFailCount = 0;

                    if (mDetectionCallback != null) {
                        mDetectionCallback.onNoFace();
                        if (noFaceCount < GlobalDef.MAX_FAIL_COUNT) {
                            noFaceCount++;
                        } else {
                            noFaceCount = 0;
                        }
                    }
                    mCurrentUserName = "";
                    age = null;
                    happystr = "";
                    mCurrentDistance = -1f;
                }
            }
        }

        float scanle = (float) 1.3;

        drawFaceTrack(mYMFaceList, (mDataSource.isUVC() ? true : false), scanle, mColorWidth,
                    mCurrentUserName, age, happystr, livenessStatus, mCurrentDistance);
    }

    /**
     * 判断最大人脸是否变更，以清除人脸跟踪框的显示
     *
     * @param ymFaces
     */
    private void checkMaxFaceIndexIsChangeOrNot(List<YMFace> ymFaces) {

        int faceIndex = -1;
        float maxFace = 0;
        for (int i = 0; i < ymFaces.size(); i++) {
            YMFace face = ymFaces.get(i);
            float faceSize = face.getRect()[2] * face.getRect()[3];
            if (faceSize > maxFace) {
                maxFace = faceSize;
                faceIndex = i;
            }
        }

        if (faceIndex >= 0 && faceIndex != lastRecognitionFaceIndex) {
            LogUtil.e(TAG+" onFaceTrack: checkFace... change face");
            mCurrentUserName = "";
            lastRecognitionFaceIndex = faceIndex;
            livenessCount = 0;
            livenessFailCount = 0;
            livenessStatus = LIVENESS_STATUS_CHECKETING;
        }
    }

    @Override
    public void setSurfaceView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
    }

    private void sleepTime(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 裁剪图片
     *
     * @param data
     */
    private void handlerImageClip(byte[] data, ByteBuffer dst) {
        OrbbecUtils.CropNV21(data, data.length, getRenderFrameWidth(), getRenderFrameHeight(), dst, cropX, cropY, cropWidth, cropHeitht);
    }

    /**
     * 裁切深度图
     *
     * @param srcData
     * @param dstBuffer
     */
    private void handlerDepthClip(ByteBuffer srcData, ByteBuffer dstBuffer) {
        int width = mDepthWidth;
        int height = mDepthHeight;
        OrbbecUtils.CropDepth(srcData, width * height * 2, width, height, dstBuffer, cropX, cropY, cropWidth, cropHeitht);
    }

    private List<YMFace> faceList = new ArrayList<>();

    /**
     * 人脸识别的方法
     *
     * @param faces
     */
    private void identification(List<YMFace> faces) {

        if (mDetectReferColorView == null) {
            if (mDetectionCallback != null) {
                mDetectionCallback.onNoFace();
            }
            return;
        }
        if (faceArrays.size() > 0) {
            faceArrays.clear();
        }
        if (faceList != null && !faceList.isEmpty() && faceList.size() > 0) {
            faceList.clear();
        }

        if (faces != null && !faces.isEmpty()) {
            int i = 0;
            for (YMFace face : faces) {
                int offSet = faceOffsetPixel();
                if (offSet > 0) {
                    float[] rect = face.getRect();
                    mSrcRect.set((int) rect[0], (int) rect[1], (int) (rect[0] + rect[2]), (int) (rect[1] + rect[3]));
                    int cx = mSrcRect.centerX() + mDetectReferVideoSize.left;
                    int cy = mSrcRect.centerY() + mDetectReferVideoSize.top;
                    if (mDetectReferVideoSize.contains(cx, cy)) {
                        if (mDetectReferVideoSize.contains(cx - offSet, cy - offSet, cx + offSet, cy - offSet)) {
                            faceArrays.append(i, face);
                            faceList.add(face);
                        } else {
                            if (mDetectionCallback != null) {
                                mDetectionCallback.onNoFace();
                            }
                        }
                    } else {
                        if (mDetectionCallback != null) {
                            mDetectionCallback.onNoFace();
                        }
                    }
                } else {
                    float[] rect = face.getRect();
                    mSrcRect.set((int) rect[0], (int) rect[1], (int) (rect[0] + rect[2]), (int) (rect[1] + rect[3]));
                    faceArrays.append(i, face);
                }
                i++;
            }
            if (faceArrays.size() > 0) {

                if (faceArrays.size() == 1) {
                    recognitionFace(faceArrays.keyAt(0));
                } else {
                    Collections.sort(faceList, comparator);
                    int faceIndex = faceArrays.indexOfValue(faceList.get(0));

//                    if (faceIndex == -1 && mDetectionCallback != null) {
//                        mDetectionCallback.onNoFace();
//                    }
                    YMFace face = faceList.get(0);
                    mSrcRect.set((int) face.getRect()[0], (int) face.getRect()[1], (int) (face.getRect()[0] + face.getRect()[2]), (int) (face.getRect()[1] + face.getRect()[3]));
                    recognitionFace(faceIndex);
                }
            } else {
                if (mDetectionCallback != null) {
                    mDetectionCallback.onNoFace();
                    identifyPerson = -111;
                }
            }
        } else {
            if (mDetectionCallback != null) {
                mDetectionCallback.onNoFace();
                identifyPerson = -111;
            }
        }
    }

    /**
     * 定义人脸比较的方法
     */
    private Comparator<YMFace> comparator = new Comparator<YMFace>() {
        @Override
        public int compare(YMFace o1, YMFace o2) {
            float[] rect = o1.getRect();
            float[] rect1 = o2.getRect();
            float area = rect[2] * rect[3];
            float area1 = rect1[2] * rect1[3];
            return (int) (area1 - area);
        }
    };

    private int lastRecognitionFaceIndex = -1;

    /**
     * 最大人脸识别
     *
     * @param faceIndex
     */
    private void recognitionFace(int faceIndex) {

        if (mDetectionCallback != null) {
            mDetectionCallback.onFoundFace(mDstRect);
        }
        Distance distance = MEASURE_DISTANCE_IS_OK;
        if (needToMeasureDistance() && mDepthBuffer != null) {
            synchronized (depthBufferLock) {
                distance = measureDistance(mSrcRect, mDepthBuffer);
            }
            if (distance == MEASURE_DISTANCE_TOO_CLOSE) {
                updateLivenessStatusThenTooFarOrClose();
                if (mDetectionCallback != null) {
                    mDetectionCallback.onClose();
                }
            } else if (distance == MEASURE_DISTANCE_TOO_FAR) {
                updateLivenessStatusThenTooFarOrClose();
                if (mDetectionCallback != null) {
                    mDetectionCallback.onFar();
                }
            }
            if (distance != MEASURE_DISTANCE_IS_OK) {
                // 此时有脸
//                if (mDetectionCallback != null) {
//                    mDetectionCallback.onNoFace();
//                }
                return;
            }
        }

        //识别年龄性别
        age = String.valueOf(mYmFaceTrack.getAge(faceIndex));
        // 识别表情
        float[] look = mYmFaceTrack.getEmotion(faceIndex);
        int happy = (int) (look[0] * 100);

        if (happy >= GlobalDef.FACE_HAPPEY) {
            happystr = "开心";
        }
        if (happy < GlobalDef.FACE_HAPPEY) {
            happystr = "平静";
        }

        // 单纯为了判断活体检测的显示（人脸跟踪框）
        if (needToCheckLiveness(identifyPerson, mCurrentUserName, happy)) {
//            int[] irLiveness = mYmFaceTrack.livenessDetectInfrared(faceIndex); // 红外活体
            // TODO:需要通过红外识别已存在的人脸
            // gaobin: 旧包和新包不同需要修改
            isLiveness = mYmFaceTrack.ObIsLiveness(mColorBuffer.array(), mDepthBuffer, faceIndex, getFaceTrackWidth(), getFaceTrackHeight());
//            isLiveness = mYmFaceTrack.ObIsLiveness(mDepthBuffer, faceIndex, getFaceTrackWidth(), getFaceTrackHeight());// Gavin:使用新的jar包编译报错
//            LogUtil.i("红外活体irLiveness = " + irLiveness[0] + "  isLiveness = " + isLiveness);
            // 人脸识别
            identifyPerson = mYmFaceTrack.identifyPerson(faceIndex);
            mCurrentUserName = getNameFromPersonId(identifyPerson);
            LogUtil.i("identifyPerson = " + identifyPerson + "  isSendOpenGate = " + isSendOpenGate);
            updateLivenessStatus(isLiveness, identifyPerson);
        }

        // 到UI线程发送开门命令
        if (identifyPerson > 0 && !isSendOpenGate) {
            if(openTheGate(identifyPerson)) {
                isSendOpenGate = true;
                LogUtil.d("runOnUiThread identifyPerson = " + identifyPerson);
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 回调上层显示
                mIdentifyCallback.onLiveness(isLiveness, livenessStatus, identifyPerson, mCurrentUserName, happystr);
            }
        });

        if (faceIndex != lastRecognitionFaceIndex) {
            mCurrentUserName = "";
        }
    }

    private void updateLivenessStatusThenTooFarOrClose() {

        livenessCount = 0;
        livenessFailCount = 0;
        if (tooFarOrCloseCount < GlobalDef.NUMBER_3) {
            tooFarOrCloseCount++;
        } else {
            livenessStatus = LIVENESS_STATUS_CHECK_INVALID;
        }
    }

    /**
     * 判断活体检测的显示（人脸跟踪框）
     *
     * @param isLiveness
     */
    private void updateLivenessStatus(boolean isLiveness, int identifyPerson) {

        tooFarOrCloseCount = 0;
        if (!isLiveness) {
            livenessCount = 0;
            if (livenessFailCount < GlobalDef.NUMBER_3) {
                livenessFailCount++;
            } else {
                livenessStatus = LIVENESS_STATUS_CHECK_FAIL;
            }
        } else {
            livenessFailCount = 0;
            if (livenessCount < GlobalDef.NUMBER_3) {
                livenessCount++;
            } else {
                if (identifyPerson > 0) {
                    livenessStatus = IDENTIFY_PERSON_CHECK_SUCCESS;
                } else {
                    livenessStatus = LIVENESS_STATUS_CHECK_SUCCESS;
                }
            }
        }

        if (identifyPerson > 0) {
            livenessStatus = IDENTIFY_PERSON_CHECK_SUCCESS;
        }
    }


    /**
     * @param rect
     * @param depthBuffer
     * @return 包含方差的距离算法
     */
    private Distance measureDistance(Rect rect, ByteBuffer depthBuffer) {

        if (DEBUG) {
            XmyLog.d(TAG + "measureDistance() called with: rect = [" + rect + "]");
        }

        int rectWidth = 6;
        int rectHeight = 6;
        float sum = 0;
        int validCount = 0;
        int centerX = rect.left + rect.width() / 2;
        int centerY = rect.top + rect.height() / 2;

        ShortBuffer shortBuffer = depthBuffer.asShortBuffer();
        short[] array = new short[shortBuffer.limit()];
        shortBuffer.get(array);

        if (enVariance) {
            if (distanceShortArray == null || distanceShortArray.length < shortBuffer.limit()) {
                distanceShortArray = new short[shortBuffer.limit()];

                // 用于求深度值的方差，若方差大于则表示这个矩形内的深度值有可能是太远或者太近，误差已经非常大
                variance = new short[(rectWidth + 1) * (rectHeight + 1)];
            }
            shortBuffer.get(distanceShortArray);

            int varianceIndex = 0;
            for (int x = centerX - rectWidth; x < centerX + rectWidth; x++) {
                for (int y = centerY - rectHeight; y < centerY + rectHeight; y++) {
                    int index = y * getFaceTrackWidth() + x;
                    if (index >= 0 && index < distanceShortArray.length) {
                        short value = distanceShortArray[index];
                        if (value > 0) {
                            sum += value;
                            validCount++;

                            if (varianceIndex < variance.length) {
                                variance[varianceIndex] = value;
                                varianceIndex++;
                            }
                        }
                        if (value > mMaxDistance) {
                            if (DEBUG) {
                            }
                        }
                    }
                }
            }

            if (sum == 0 || TrackUtil.variance(variance, varianceIndex) > VARIANCE_MAX_THRESHOLD) {
                log("dis, averDistance = -1 sum = 0");
                return MEASURE_DISTANCE_TOO_CLOSE;
            }
        } else {
            for (int x = centerX - rectWidth; x < centerX + rectWidth; x++) {
                for (int y = centerY - rectHeight; y < centerY + rectHeight; y++) {
                    int index = y * getFaceTrackWidth() + x;
                    if (index >= 0 && index < array.length) {
                        short value = array[index];
                        if (value > 0) {
                            sum += value;
                            validCount++;
                        }
                        if (value > mMaxDistance) {
                        }
                    }
                }
            }
        }

        Distance iDis = MEASURE_DISTANCE_IS_OK;
        float averDistance = (validCount == 0 ? 0 : (sum / validCount));

        if (DEBUG) {
            XmyLog.d("sum = " + (int) sum + ", validCount = " + (int) validCount + ", rect.width = " + rect.width() + ", averDistance = " + (int) averDistance);
        }
        if (averDistance < mMinDistance) {
            log("dis, close distance：" + averDistance);
            iDis = MEASURE_DISTANCE_TOO_CLOSE;
        }

        if (averDistance > mMaxDistance) {
            log("dis, far distance：" + averDistance);
            iDis = MEASURE_DISTANCE_TOO_FAR;
        }
        mCurrentDistance = averDistance;
        return iDis;
    }

    private void log(String str) {
        if (DEBUG) {
            LogUtil.i(TAG + " " + str);
        }
    }

    private boolean isCrop() {
        return cropX >= 0;
    }

    private int getRenderFrameWidth() {

        return mDataSource.isDuoDuo() ? GlobalDef.RES_DUODUO_DEPTH_WIDTH : mColorWidth;
    }

    private int getRenderFrameHeight() {

        return mDataSource.isDuoDuo() ? GlobalDef.RES_DUODUO_DEPTH_HEIGHT : mColorHeight;
    }

    private int getFaceTrackWidth() {
        return isCrop() ? cropWidth : (mDataSource.isDuoDuo() ? GlobalDef.RES_DUODUO_DEPTH_WIDTH : mColorWidth);
    }

    private int getFaceTrackHeight() {
        return isCrop() ? cropHeitht : (mDataSource.isDuoDuo() ? GlobalDef.RES_DUODUO_DEPTH_HEIGHT : mColorHeight);
    }
}
