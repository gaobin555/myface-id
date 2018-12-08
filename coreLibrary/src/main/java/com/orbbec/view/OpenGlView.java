package com.orbbec.view;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

import com.lzh.easythread.EasyThread;
import com.orbbec.NativeNI.OrbbecUtils;

import org.openni.SensorType;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * @author xlj
 * @date 17-1-16
 */

public class OpenGlView extends GLSurfaceView {

    private static final String TAG = "OpenNIView";
    private static final boolean DEBUG = false;
    private boolean drawViewRunning = false;
    private volatile boolean hasFrameDate = false;
    private Object frameDateLock = new Object();

    protected int mSurfaceWidth = 0;
    protected int mSurfaceHeight = 0;
    protected ByteBuffer mTexture;
    protected int mTextureId = 0;

    private long mNativePtr = 0;

    private int mCurrFrameWidth = 0;
    private int mCurrFrameHeight = 0;
    private int mStrideInBytes;
    private SensorType mSensorType;
    private ByteBuffer mTempBuffer;
    private ByteBuffer mByteBuffer;

    private int mBaseColor = Color.WHITE;

    private int mVersionInt;
    private EasyThread easyThread;

    public OpenGlView(Context context) {
        super(context);
        init();
    }

    public OpenGlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        log("init");

        mVersionInt = Build.VERSION.SDK_INT;
        setRenderer(new Renderer() {

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig c) {
                /* Disable these capabilities. */
                final int[] gCapbilitiesToDisable = {
                        GLES10.GL_FOG,
                        GLES10.GL_LIGHTING,
                        GLES10.GL_CULL_FACE,
                        GLES10.GL_ALPHA_TEST,
                        GLES10.GL_BLEND,
                        GLES10.GL_COLOR_LOGIC_OP,
                        GLES10.GL_DITHER,
                        GLES10.GL_STENCIL_TEST,
                        GLES10.GL_DEPTH_TEST,
                        GLES10.GL_COLOR_MATERIAL,
                };

                for (int capability : gCapbilitiesToDisable) {
                    GLES10.glDisable(capability);
                }

                GLES10.glEnable(GLES10.GL_TEXTURE_2D);

                int[] ids = new int[1];
                GLES10.glGenTextures(1, ids, 0);
                mTextureId = ids[0];
                GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, mTextureId);

                GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MIN_FILTER, GLES10.GL_LINEAR);
                GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MAG_FILTER, GLES10.GL_LINEAR);
                GLES10.glShadeModel(GLES10.GL_FLAT);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int w, int h) {
                synchronized (OpenGlView.this) {
                    mSurfaceWidth = w;
                    mSurfaceHeight = h;
                }
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                synchronized (OpenGlView.this) {
                    onDrawGL();
                }
            }
        });

        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void finalize() throws Throwable {
        if (mNativePtr != 0) {
            mNativePtr = 0;
        }
        super.finalize();
    }

    /**
     * Requests update of the view with an OpenNI frame.
     *
     * @param data
     * @param width
     * @param height
     * @param sensorType
     * @param strideInBytes
     */
    synchronized public void update(ByteBuffer data, int width, int height, SensorType sensorType, int strideInBytes) {

        mCurrFrameWidth = width;
        mCurrFrameHeight = height;
        mSensorType = sensorType;
        mStrideInBytes = strideInBytes;

        if (mTexture == null) {
            mTexture = ByteBuffer.allocateDirect(mCurrFrameWidth * mCurrFrameHeight * 4);
            log("mTexture: " + mTexture.limit());
        }

        synchronized (frameDateLock) {
            mTempBuffer = data;
            hasFrameDate = true;
            frameDateLock.notifyAll();
        }
    }

    protected void onDrawGL() {
        if (mTexture == null || mSurfaceWidth == 0 || mSurfaceHeight == 0) {
            return;
        }

        GLES10.glEnable(GLES10.GL_BLEND);
        GLES10.glBlendFunc(GLES10.GL_SRC_ALPHA, GLES10.GL_ONE_MINUS_SRC_ALPHA);
        int red = Color.red(mBaseColor);
        int green = Color.green(mBaseColor);
        int blue = Color.blue(mBaseColor);
        int alpha = Color.alpha(mBaseColor);
        GLES10.glColor4f(red / 255.f, green / 255.f, blue / 255.f, alpha / 255.f);

        GLES10.glEnable(GLES10.GL_TEXTURE_2D);

        GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, mTextureId);
        int[] rect = {0, mCurrFrameHeight, mCurrFrameWidth, -mCurrFrameHeight};
        GLES11.glTexParameteriv(GLES10.GL_TEXTURE_2D, GLES11Ext.GL_TEXTURE_CROP_RECT_OES, rect, 0);

        GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT);

        GLES10.glTexImage2D(GLES10.GL_TEXTURE_2D, 0, GLES10.GL_RGBA, mCurrFrameWidth, mCurrFrameHeight, 0, GLES10.GL_RGBA,
                GLES10.GL_UNSIGNED_BYTE, mTexture);
        GLES11Ext.glDrawTexiOES(0, 0, 0, mSurfaceWidth, mSurfaceHeight);


        GLES10.glDisable(GLES10.GL_TEXTURE_2D);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        log("onAttachedToWindow ...");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        log("onDetachedFromWindow");

    }

    @Override
    public void onResume() {
        super.onResume();
        log("onResume");
        drawViewRunning = true;
        initDrawThread();

    }

    @Override
    public void onPause() {
        super.onPause();
        log("onPause");
        drawViewRunning = false;
        synchronized (frameDateLock) {
            frameDateLock.notifyAll();
        }
    }

    private void initDrawThread() {
        easyThread = EasyThread.Builder.createFixed(6).build();
        easyThread.execute(new Runnable() {
            @Override
            public void run() {
                while (drawViewRunning) {
                    synchronized (frameDateLock) {
                        while (!hasFrameDate) {
                            try {
                                frameDateLock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!drawViewRunning) {
                                break;
                            }
                        }
                    }
                    if (!drawViewRunning) {
                        continue;
                    }

                    if (mByteBuffer == null) {
                        mByteBuffer = ByteBuffer.allocateDirect(mCurrFrameWidth * mCurrFrameHeight * 2);
                        mByteBuffer.order(LITTLE_ENDIAN);
                    }
                    synchronized (frameDateLock) {
                        OrbbecUtils.ByteBufferCopy(mTempBuffer, mByteBuffer, mCurrFrameWidth * mCurrFrameHeight * 2);
                        hasFrameDate = false;
                    }

                    switch (mSensorType) {
                        case DEPTH:
                            OrbbecUtils.ConvertTORGBA(mByteBuffer, mTexture, mCurrFrameWidth, mCurrFrameHeight, mStrideInBytes);
                            break;
                        case COLOR:
                            Log.e(TAG, "RGB888TORGBA 有问题...");
                            break;
                        case IR:
                            break;
                        default:
                            break;
                    }
                    requestRender();
                }
                log("绘图线程结束...");
            }
        });

    }

    private void log(String str) {
        if (DEBUG) {
            Log.e(TAG, str);
        }
    }
}
