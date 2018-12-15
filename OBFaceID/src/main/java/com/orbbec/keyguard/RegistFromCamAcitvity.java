package com.orbbec.keyguard;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;

import android.os.Build;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.orbbec.base.BaseApplication;
import com.orbbec.base.DetectionCallback;
import com.orbbec.base.DeviceCallback;
import com.orbbec.base.IdentifyCallback;
import com.orbbec.base.OrbbecPresenter;
import com.orbbec.constant.Constant;
import com.orbbec.model.User;
import com.orbbec.utils.AppDef;
import com.orbbec.utils.BitmapUtil;
import com.orbbec.utils.DataSource;
import com.orbbec.utils.GlobalDef;
import com.orbbec.utils.LogUtil;
import com.orbbec.utils.OpenNiHelper;
import com.orbbec.utils.TrackDrawUtil;
import com.orbbec.utils.UserDataUtil;
import com.orbbec.utils.XmyLog;
import com.orbbec.view.GlFrameSurface;
import com.orbbec.view.OpenGlView;

import org.openni.SensorType;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import mobile.ReadFace.YMFace;

import static android.os.Build.VERSION_CODES.M;
import static android.view.View.GONE;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;

public class RegistFromCamAcitvity extends AppCompatActivity implements Runnable,
        DeviceCallback, DetectionCallback, IdentifyCallback, OrbbecPresenter.View{

    private static final boolean IS_SHOW_DEPTH_VIEW = false;
    private static final boolean DEBUG = false;
    private static final String TAG = "RegistFromCamAcitvity";
    private GlFrameSurface mGLSurface;
    private ObFacePresenter mPresenter;
    private ObDataSource mObDataSource;
    private Rect rect = new Rect();
    private AppDef mAppDef = new AppDef();
    private SurfaceView drawView;

    private View view_show_image;
    private Button btn_add_face;
    private TextView tv_tips;

    private int addCount = 0;//标志当前添加的人脸图片是第几张.
    private boolean isCorrect = false;//标志人脸是否校验通过，默认为false
    private int personId = -111;
    private boolean isAdd = false;

    private String age, gender, score;
    private Bitmap head = null;

    StringBuffer fps;
    private boolean showFps = false;
    private List<Float> timeList = new ArrayList<>();
    protected boolean stop = false;//表示是否已经开始进行人脸分析

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_from_cam_acitvity);
        initView();
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
//            mPresenter.initRegistFromCamView(view_show_image, btn_add_face, tv_tips);

            mObDataSource = new ObDataSource(this, mPresenter, mAppDef);
            //这个里面就会检测设备vid,pid
            mPresenter.setOBSource(mObDataSource);
            //0表示false 库时间 3个月时长。2017 12 1开始。如果是我们的设备 并且没过期
            //  if (Util.isOursDevices(this.getApplicationContext()) && OrbbecUtils.getVersion() == 0) {
            mPresenter.initFaceTrack();
//            mPresenter.startFaceTrack();
            stop = false;
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
        mGLSurface = (GlFrameSurface) findViewById(R.id.sfv_gl_surface);
        mGLSurface.setFrameWidth(mAppDef.getColorWidth());
        mGLSurface.setFrameHeight(mAppDef.getColorHeight());
        LogUtil.d(TAG+" Width = " + mAppDef.getColorWidth() + "  Height = " + mAppDef.getColorHeight());

        drawView = (SurfaceView) findViewById(R.id.sfv_draw_view);
        drawView.setZOrderOnTop(true);
        drawView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        view_show_image = findViewById(R.id.view_show_image);
        btn_add_face = (Button) findViewById(R.id.btn_add_face);
        btn_add_face.setEnabled(false);
        btn_add_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAdd) {
                    isAdd = true;
                }
            }
        });

        tv_tips = (TextView) findViewById(R.id.tv_tips);
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
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mTxtTip.setText(getString(R.string.no_device));
//                mTxtTip.setVisibility(View.VISIBLE);
//            }
//        });
    }

    @Override
    public void onDeviceOpenFailed() {

    }

    @Override
    public void onDeviceOpened() {
        XmyLog.d("onDeviceOpened() called");
        updateSurfaceView();
        mObDataSource.startDepth();
        mObDataSource.startColor();
    }

    private void updateSurfaceView() {
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


    @Override
    public void onClose() {

    }

    @Override
    public void onFar() {

    }

    @Override
    public void onDepthUpdate(ByteBuffer data, int width, int height, SensorType sensorType, int strideInBytes) {

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
        openInit();


        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Gavin:在onPause釋放
        if (mPresenter != null && mObDataSource != null) {
            mPresenter.stopFaceTrack();
            mObDataSource.onRelease();
            mPresenter = null;
            stop = true;
            releaseOBFacePresenter();
            LogUtil.d("释放");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void run() {
        if (mObDataSource != null) {
            mObDataSource.stopDepth();
        }
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

    /**
     * 开始追踪人脸，并绘制人脸框
     *
     * @param data
     */
    @Override
    public void onRegistTrack(byte[] data) {
        try {
            long time = System.currentTimeMillis();
            final List<YMFace> faces = analyse(data, mAppDef.getColorWidth(), mAppDef.getColorHeight());
            fps = new StringBuffer();
            if (showFps) {
                fps.append("fps = ");
                long now = System.currentTimeMillis();
                float than = now - time;
                timeList.add(than);
                if (timeList.size() >= 20) {
                    float sum = 0;
                    for (int i = 0; i < timeList.size(); i++) {
                        sum += timeList.get(i);
                    }
                    fps.append((int) (1000f * timeList.size() / sum));
                    timeList.remove(0);
                }
            }
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    drawAnim(faces, drawView, mAppDef.getColorWidth()/mGLSurface.getHeight(), fps.toString());
//                }
//            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    protected void drawAnim(List<YMFace> faces, SurfaceView draw_view, float scale_bit, String fps) {
//        TrackDrawUtil.drawFaceTracking(faces, draw_view, scale_bit, fps, false);
//    }

    protected List<YMFace> analyse(final byte[] bytes, int iw, int ih) {
        if (null == mPresenter.mYmFaceTrack) {
            return null;
        }
        final List<YMFace> ymFaces = mPresenter.mYmFaceTrack.trackMulti(bytes, iw, ih);
        final byte[] data = bytes;
        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                if (null != ymFaces && ymFaces.size() > 0) {
                    checkAndRegistFace(ymFaces, data);
                } else {
                    btn_add_face.setEnabled(false);
                    btn_add_face.setBackground(getResources().getDrawable(R.mipmap.add_face_unable));
                }
            }
        });
        return ymFaces;
    }

    /**
     * 校验并且注册人脸
     *
     * @param ymFaces
     * @param bytes
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void checkAndRegistFace(List<YMFace> ymFaces, final byte[] bytes) {

        final float[] rect = ymFaces.get(0).getRect();
        switch (addCount) {
            case 0://正脸
                if (!isCorrect) {
                    isCorrect = check1(ymFaces);
                }
                if (isCorrect) {
                    tv_tips.setText("点击页面底添加部按钮 - 添加正脸");
                    btn_add_face.setEnabled(true);
                    btn_add_face.setBackground(getResources().getDrawable(R.mipmap.add_face_able));
                    if (isAdd) {//点击add face按钮后isAdd=true
                        isCorrect = false;
                        personId = mPresenter.mYmFaceTrack.identifyPerson(0);
                        if (personId == -111) {
                            addFace(bytes, rect);
                        } else {
                            User user = UserDataUtil.getUserById(personId + "");
                            String name = personId + "";
                            if (user != null) name = user.getName();

                            final AlertDialog.Builder builder = new AlertDialog.Builder(RegistFromCamAcitvity.this);
                            builder.setTitle("提示").setCancelable(false);
                            builder.setMessage(String.format("已识别您为 %1$s ，是否更新您的人脸库？", name))
                                    .setPositiveButton("忽略", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            personId = -111;
                                            addCount = 0;
                                        }
                                    })
                                    .setNegativeButton("更新", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            mPresenter.mYmFaceTrack.deletePerson(personId);
                                            DataSource dataSource = new DataSource(BaseApplication.getContext());
                                            String imgPath = BaseApplication.getContext().getCacheDir()
                                                    + "/" + personId + ".jpg";
                                            File imgFile = new File(imgPath);
                                            if (imgFile.exists()) {
                                                imgFile.delete();
                                            }
                                            dataSource.deleteById(personId + "");
                                            mPresenter.mYmFaceTrack.deletePerson(personId);
                                            addFace(bytes, rect);
                                        }
                                    })
                                    .setNeutralButton("我不是他/她", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            addFace(bytes, rect);
                                        }
                                    });
                            builder.create().show();
                        }
                    }
                } else {
                    tv_tips.setText("请正脸面对");
                    btn_add_face.setEnabled(false);
                    btn_add_face.setBackground(getResources().getDrawable(R.mipmap.add_face_unable));
                }
                break;
            case 1://侧脸
                if (!isCorrect) isCorrect = check2(ymFaces);
                if (isCorrect) {
                    tv_tips.setText("点击页面底部按钮 - 添加侧脸数据");
                    btn_add_face.setEnabled(true);
                    btn_add_face.setBackground(getResources().getDrawable(R.mipmap.add_face_able));
                    if (isAdd) {
                        isCorrect = false;
                        addCount++;
                        int i = mPresenter.mYmFaceTrack.updatePerson(personId, 0);
                        saveImageFromCamera(personId, 1, bytes);
                        view_show_image.setBackgroundResource(R.mipmap.header_3);

                    }
                } else {
                    tv_tips.setText("请侧脸20°");
                    btn_add_face.setEnabled(false);
                    btn_add_face.setBackground(getResources().getDrawable(R.mipmap.add_face_unable));
                }
                break;

            case 2://抬头
                if (!isCorrect) isCorrect = check3(ymFaces);
                if (isCorrect) {
                    tv_tips.setText("点击页面底部按钮 - 添加抬头数据");
                    btn_add_face.setEnabled(true);
                    btn_add_face.setBackground(getResources().getDrawable(R.mipmap.add_face_able));
                    if (isAdd) {
                        isCorrect = false;
                        addCount++;
                        int i = mPresenter.mYmFaceTrack.updatePerson(personId, 0);
                        saveImageFromCamera(personId, 1, bytes);
                        view_show_image.setBackgroundResource(R.mipmap.header_4);

                    }
                } else {
                    tv_tips.setText("请抬头20°");
                    btn_add_face.setEnabled(false);
                    btn_add_face.setBackground(getResources().getDrawable(R.mipmap.add_face_unable));
                }
                break;

            case 3://低头
                if (!isCorrect) isCorrect = check4(ymFaces);
                if (isCorrect) {
                    tv_tips.setText("点击页面底部按钮 - 添加低头数据");
                    btn_add_face.setEnabled(true);
                    btn_add_face.setBackground(getResources().getDrawable(R.mipmap.add_face_able));
                    if (isAdd) {
                        isCorrect = false;
                        addCount++;
                        int i = mPresenter.mYmFaceTrack.updatePerson(personId, 0);
                        saveImageFromCamera(personId, 1, bytes);
                        view_show_image.setBackgroundResource(R.mipmap.header_4);

                    }
                } else {
                    tv_tips.setText("请低头20°");
                    btn_add_face.setEnabled(false);
                    btn_add_face.setBackground(getResources().getDrawable(R.mipmap.add_face_unable));
                }
                break;

            case 4:
                doEnd();
                addCount++;
                break;

            default:

                break;
        }
        isAdd = false;
    }

    /**
     * 校验正脸数据
     *
     * @param faces
     * @return
     */
    private boolean check1(List<YMFace> faces) {

        YMFace face = faces.get(0);
        float facialOri[] = face.getHeadpose();

        float x = facialOri[0];
        float y = facialOri[1];
        float z = facialOri[2];

        if (Math.abs(x) <= 15 && Math.abs(y) <= 15 && Math.abs(z) <= 15) {
            return true;
        }
        return false;
    }

    /**
     * 校验侧脸数据
     *
     * @param faces
     * @return
     */
    private boolean check2(List<YMFace> faces) {

        YMFace face = faces.get(0);
        float facialOri[] = face.getHeadpose();
        float z = facialOri[2];
        if (Math.abs(z) >= 15) {
            return true;
        }
        return false;
    }

    /**
     * 校验抬头数据
     *
     * @param faces
     * @return
     */
    private boolean check3(List<YMFace> faces) {

        YMFace face = faces.get(0);
        float facialOri[] = face.getHeadpose();
        float y = facialOri[1];
        if (y <= -10) {
            return true;
        }
        return false;
    }

    /**
     * 校验低头数据
     *
     * @param faces
     * @return
     */
    private boolean check4(List<YMFace> faces) {

        YMFace face = faces.get(0);
        float facialOri[] = face.getHeadpose();
        float y = facialOri[1];

        if (y > -10) {
            return true;
        }
        return false;
    }

    /**
     * 注册人脸
     *
     * @param bytes
     * @param rect
     */
    public void addFace(byte[] bytes, float[] rect) {
        personId = mPresenter.mYmFaceTrack.addPerson(0);//添加人脸
        int gender_score = mPresenter.mYmFaceTrack.getGender(0);
        int gender_confidence = mPresenter.mYmFaceTrack.getGenderConfidence(0);
        gender = " ";
        if (gender_confidence >= 90)
            gender = mPresenter.mYmFaceTrack.getGender(0) == 0 ? "F" : "M";
        score = " ";
        age = String.valueOf(mPresenter.mYmFaceTrack.getAge(0));
        //DLog.d("add Face 1 " + personId + " age :" + age + " gender: " + gender);
        saveImageFromCamera(personId, 0, bytes);
        if (personId > 0) {
            addCount++;//添加人脸成功
            view_show_image.setBackgroundResource(R.mipmap.header_2);
            Bitmap image = BitmapUtil.getBitmapFromYuvByte(bytes, mAppDef.getColorWidth(), mAppDef.getColorHeight());
            //TODO 此处在保存人脸小图
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                Matrix matrix = new Matrix();
                matrix.postRotate(270);
                head = Bitmap.createBitmap(image, mAppDef.getColorWidth() - (int) rect[1] -
                        (int) rect[3], (int) rect[0], (int) rect[3], (int) rect[2], matrix, true);
            } else if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                head = Bitmap.createBitmap(image, (int) rect[0], (int) rect[1], (int) rect[2], (int) rect[3], null, true);
            }
        } else {
            Toast.makeText(this, "添加人脸失败！请重新添加", Toast.LENGTH_SHORT).show();
        }
    }

    public void doEnd() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        final EditText et = new EditText(this);
        et.setGravity(Gravity.CENTER);
        et.setHint("输入昵称不能为空");
        et.setHintTextColor(0xffc6c6c6);
        builder.setTitle("提示")
                .setMessage(String.format("人脸录入成功，Face ID =  %1$s 请输入昵称", personId))
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = et.getText().toString();
                        if (!TextUtils.isEmpty(name.trim())) {
                        } else {
                            doEnd();
                            return;
                        }
                        User user = new User("" + personId, name, age, gender);
                        user.setScore(score);
                        DataSource dataSource = new DataSource(BaseApplication.getContext());
                        dataSource.insert(user);
                        BitmapUtil.saveBitmap(head, Constant.ImagePath + personId + ".jpg");

                        final AlertDialog.Builder builder = new AlertDialog.Builder(RegistFromCamAcitvity.this);
                        builder.setCancelable(false);
                        builder.setMessage("当前录入成功是否继续录入？");
                        builder.setNegativeButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.setPositiveButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                setResult(102, getIntent());
                                onBackPressed();
                            }
                        });
                        builder.create().show();
                    }
                });
        builder.create().show();
    }

    /**
     * 保存图片
     *
     * @param personId
     * @param i
     * @param bytes
     */
    private void saveImageFromCamera(int personId, int i, byte[] bytes) {
    }
}
