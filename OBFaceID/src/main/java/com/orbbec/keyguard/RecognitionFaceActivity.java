package com.orbbec.keyguard;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.orbbec.base.BaseApplication;
import com.orbbec.base.DetectionCallback;
import com.orbbec.base.DeviceCallback;
import com.orbbec.base.IdentifyCallback;
import com.orbbec.base.OrbbecPresenter;
import com.orbbec.utils.AppDef;
import com.orbbec.utils.GlobalDef;
import com.orbbec.utils.LogUtil;
import com.orbbec.utils.OpenNiHelper;
import com.orbbec.utils.XmyLog;
import com.orbbec.view.GlFrameSurface;
import com.orbbec.view.OpenGlView;

import org.openni.SensorType;

import java.nio.ByteBuffer;

import static android.os.Build.VERSION_CODES.M;
import static android.view.View.GONE;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;

/**
 * 该Demo 作为参考示例，如有问题请联系:springlovvol@163.com
 *
 */
public class RecognitionFaceActivity extends AppCompatActivity implements Runnable,
        DeviceCallback, DetectionCallback, IdentifyCallback, OrbbecPresenter.View {

    private static final boolean IS_SHOW_DEPTH_VIEW = false;
    private static final boolean DEBUG = false;
    private static final String TAG = "RecognitionFaceActivity";
    private long lastBackTime = 0;
    private long backTimeOut = GlobalDef.GLOBAL_DELAY;
    private long resumeTime = 0;
    private String mActionUsbPermission;
    private NoUsbDeviceDlg mNoUsbDeviceDlg;
    private GlFrameSurface mGLSurface;
    private ObFacePresenter mPresenter;
    private ObDataSource mObDataSource;
    private Rect rect = new Rect();
    private TextView mTxtTip;
    private AppDef mAppDef = new AppDef();
    private OpenGlView mDepthView;
    private SurfaceView drawView;
    private Button settingsButton;
    private Button registButton;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition_face);
        initView();

//        if (Build.VERSION.SDK_INT >= M) {
//            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                    | ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
//                    != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
//            } else if (checkPermission()) {
//                openInit();
//            }
//        } else {
//            openInit();
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openInit();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(RecognitionFaceActivity.this);
            builder.setTitle("权限");
            builder.setMessage("手动授权");
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setPositiveButton("去手动授权", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    System.exit(0);
                    finish();
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }

    /**
     * 初始化
     */
    private void openInit() {
        if (OpenNiHelper.hasObUsbDevice(getApplicationContext())) {
            mPresenter = new ObFacePresenter(this);
            //设备回调
            mPresenter.setDeviceCallback(this);
            //人脸距离
            mPresenter.setDetectionCallback(this);
            //识别回调
            mPresenter.setIdentifyCallback(this);
            mPresenter.startIdentification();
            mPresenter.setDistance(GlobalDef.MAX_DISTANCE, GlobalDef.PRO_MIX_DISTANCE);
            mPresenter.setSurfaceView(mGLSurface);
            mPresenter.setFaceTrackDrawView(drawView);

            mObDataSource = new ObDataSource(this, mPresenter, mAppDef);
            //这个里面就会检测设备vid,pid
            mPresenter.setOBSource(mObDataSource);
            //0表示false 库时间 3个月时长。2017 12 1开始。如果是我们的设备 并且没过期
          //  if (Util.isOursDevices(this.getApplicationContext()) && OrbbecUtils.getVersion() == 0) {
                mPresenter.initFaceTrack();
                mPresenter.startFaceTrack();
         //   }
        }
    }

    /**
     * 释放
     */
    private void releaseOBFacePresenter() {
        if (mPresenter != null) {
            //设备回调
            mPresenter.setDeviceCallback(null);
            //人脸距离
            mPresenter.setDetectionCallback(null);
            //识别回调
            mPresenter.setIdentifyCallback(null);
            mPresenter.setSurfaceView(null);
            mPresenter.setFaceTrackDrawView(null);
            mPresenter.setOBSource(null);
        }
    }

    private void initView() {
        mGLSurface = (GlFrameSurface) findViewById(R.id.gl_surface);
        if (ObDataSource.isDouDou(this)) {
            LogUtil.d(TAG+"ObDataSource.isDouDou");
            mGLSurface.setFrameWidth(GlobalDef.RES_DUODUO_DEPTH_WIDTH);
            mGLSurface.setFrameHeight(GlobalDef.RES_DUODUO_DEPTH_HEIGHT);
        } else {
            mGLSurface.setFrameWidth(mAppDef.getColorWidth());
            mGLSurface.setFrameHeight(mAppDef.getColorHeight());
            LogUtil.d(TAG+" Width = " + mAppDef.getColorWidth() + "  Height = " + mAppDef.getColorHeight());
        }
        if (ObDataSource.isUVC(this)) {
            LogUtil.d(TAG+"ObDataSource.isUVC");
            mGLSurface.changeRotation(0, true);
        }
        drawView = (SurfaceView) findViewById(R.id.pointView);
        drawView.setZOrderOnTop(true);
        drawView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mTxtTip = (TextView) findViewById(R.id.txt_tip);
        mDepthView = (OpenGlView) findViewById(R.id.depthview);
        if (!IS_SHOW_DEPTH_VIEW) {
            mDepthView.setVisibility(View.GONE);
        }
        // settings
        settingsButton = (Button)  findViewById(R.id.SettingsButton);
        settingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognitionFaceActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        // 开门指令
        registButton = (Button) findViewById(R.id.RegistButton);
        registButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mPresenter.openTheGate();
                Intent intent = new Intent(RecognitionFaceActivity.this, FaceRegistActivity.class);
                startActivity(intent);
            }
        });
    }


    /**
     * 未检测到人脸时
     */
    @Override
    public void onNoFace() {


    }

    /**
     * @param rect 人脸区域的位置及范围
     */
    @Override
    public void onFoundFace(Rect rect) {
        mObDataSource.startDepth();
    }

    @Override
    public void onNoDevices() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTxtTip.setText(getString(R.string.no_device));
                mTxtTip.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDeviceOpenFailed() {
        XmyLog.d("onDeviceOpenFailed() called");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTxtTip.setText(getString(R.string.open_fail_device));
                mTxtTip.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDeviceOpened() {
        XmyLog.d("onDeviceOpened() called");
        updateSurfaceView();
        mObDataSource.startDepth();
        mObDataSource.startColor();
    }

    private void updateSurfaceView() {
        if (mObDataSource.isUVC() && !GlobalDef.MIRROR_UVC) {
            ViewGroup.LayoutParams layoutParams = mGLSurface.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new ViewGroup.LayoutParams(getResources().getDimensionPixelSize(R.dimen.color_width),
                        getResources().getDimensionPixelSize(R.dimen.color_height));
            } else {

                layoutParams.width = getResources().getDimensionPixelSize(R.dimen.color_width);
                layoutParams.height = getResources().getDimensionPixelSize(R.dimen.color_height);
            }
            mGLSurface.setLayoutParams(layoutParams);
            mGLSurface.post(new Runnable() {
                @Override
                public void run() {
                    int w = mGLSurface.getWidth();
                    int h = mGLSurface.getHeight();
                    rect.set(0, 0, w, h);
                    mPresenter.setIdentificationRect(rect);
                    mPresenter.setFaceTrackDrawViewMargin(mGLSurface.getLeft(), mGLSurface.getTop(), w, h);
                }
            });
            mGLSurface.setVisibility(View.GONE);
        } else {
            mGLSurface.post(new Runnable() {
                @Override
                public void run() {
                    int w = mGLSurface.getWidth();
                    int h = mGLSurface.getHeight();
                    rect.set(0, 0, w, h);
                    mPresenter.setIdentificationRect(rect);
                    mPresenter.setFaceTrackDrawViewMargin(mGLSurface.getLeft(), mGLSurface.getTop(), mGLSurface.getWidth(), mGLSurface.getHeight());
                }
            });
        }
    }


    @Override
    public void onClose() {

    }

    @Override
    public void onFar() {

    }

    @Override
    public void onDepthUpdate(ByteBuffer data, int width, int height, SensorType sensorType, int strideInBytes) {
        if (mDepthView != null && data != null && IS_SHOW_DEPTH_VIEW) {
            Log.d(TAG, "onDepthUpdate: "+data.toString());
            mDepthView.update(data, width, height, sensorType, strideInBytes);
        }
    }


    /**
     * @param isLiveness       活体检测是否通过
     * @param identifyPerson   当前活体检测的脸的id
     * @param nameFromPersonId 当前活体检测的脸的昵称
     * @param happy            表情值
     */
    @Override
    public void onLiveness(boolean isLiveness, final int identifyPerson, final String nameFromPersonId, final int happy) {

    }

    @Override
    public void onRegistTrack(byte[] data){

    }

    @Override
    public boolean needColorBitmap() {
        return false;
    }

    @Override
    public void onDrawColor(Bitmap colorBitmap) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        // Gavin：移到onResume初始化
        if (Build.VERSION.SDK_INT >= M) {
            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    | ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
            } else if (checkPermission()) {
                openInit();
            }
        } else {
            openInit();
        }

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);
        if (mDepthView != null && IS_SHOW_DEPTH_VIEW) {
            mDepthView.onResume();
        }

        resumeTime = System.currentTimeMillis();
        registerUsbReceiver();
        if (!OpenNiHelper.hasObUsbDevice(getApplicationContext())) {
            showNoUsbDialog();
//            Toast.makeText(this, "请插入USB摄像头,否则无法使用", Toast.LENGTH_SHORT).show();
        } else {
            if (mObDataSource.isDuoDuo()) {
                Log.e(TAG, "drawView.setY ...");

                ViewGroup.LayoutParams layout = mGLSurface.getLayoutParams();
                int w = layout.width;
                int h = layout.height;
                int toHeight = w * GlobalDef.RES_DUODUO_DEPTH_HEIGHT / GlobalDef.RES_DUODUO_DEPTH_WIDTH;
                layout.height = toHeight;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterUsbReceiver();
        if (mDepthView != null && IS_SHOW_DEPTH_VIEW) {
            mDepthView.onPause();
        }

        // Gavin:在onPause釋放
        if (mPresenter != null && mObDataSource != null) {
            mPresenter.stopFaceTrack();
            mObDataSource.onRelease();
            mPresenter = null;
            releaseOBFacePresenter();
            LogUtil.d("释放");
        }
   }


    @Override
    public void run() {
        if (mObDataSource != null) {
            log("关闭深度流...");
            mObDataSource.stopDepth();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        long currentTime = System.currentTimeMillis();

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (currentTime - lastBackTime < backTimeOut) {
                Log.d(TAG, "onKeyDown: ");

                if (mPresenter != null && mObDataSource != null) {
                    mPresenter.stopFaceTrack();
                    mObDataSource.onRelease();
                    mPresenter = null;
                    LogUtil.d("释放");
                }
                BaseApplication.appExit(this);
            } else {
                lastBackTime = currentTime;
                Toast.makeText(RecognitionFaceActivity.this, getString(R.string.sign_out), Toast.LENGTH_SHORT).show();
            }
            return true;

        }
        return false;

    }

    private String rgbFps, depthFps;

    @Override
    public void showRgbFps(final String fps) {
        rgbFps = fps;
    }

    @Override
    public void showDepthFps(final String fps) {
        depthFps = fps;
    }

    private boolean checkPermission() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private void showNoUsbDialog() {
        if (mNoUsbDeviceDlg == null) {
            mNoUsbDeviceDlg = new NoUsbDeviceDlg(this, null);
        }
        if (!mNoUsbDeviceDlg.isShowing()) {
            mNoUsbDeviceDlg.show();
        }
        if (mDepthView != null && IS_SHOW_DEPTH_VIEW) {
            mDepthView.setVisibility(GONE);
        }
    }


    private void registerUsbReceiver() {
        mActionUsbPermission = getPackageName() + ".USB_PERMISSION";
        IntentFilter filter = new IntentFilter(mActionUsbPermission);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);
    }

    private void unregisterUsbReceiver() {
        if (mActionUsbPermission != null) {
            unregisterReceiver(mUsbReceiver);
        }
        mActionUsbPermission = null;
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                if (mObDataSource == null && OpenNiHelper.hasObUsbDevice(getApplicationContext())) {
                    BaseApplication.getAppContext().uncaughtException(null, null);
                }
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                //                if (mPresenter != null && mObDataSource != null && !OpenNiHelper.hasObUsbDevice(getApplicationContext
                if (mPresenter != null && mObDataSource != null && !OpenNiHelper.hasObUsbDevice(getApplicationContext())) {
                    showNoUsbDialog();
                    mPresenter.stopFaceTrack();
                    mObDataSource.onRelease();
                    releaseOBFacePresenter();
                    mPresenter = null;
                    mObDataSource = null;
                    Log.d(TAG, "拔出 usb, 释放");
                }
            }
        }
    };

    private void log(String str) {
        if (DEBUG) {
            Log.i(TAG, str);
        }
    }
}