package com.orbbec.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.orbbec.keyguard.RgbCamera;

/**
 * UVC 表示是Astra Pro系列。
 * @author lgp
 */
public class UvcSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "UVCSurfaceView";

    private RgbCamera mCamera;

    private boolean isCreated;


    public UvcSurfaceView(Context context) {
        super(context);
        init();
    }

    public UvcSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UvcSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    public void setCamera(RgbCamera camera) {

        mCamera = camera;
        if (isCreated && mCamera != null) {
            mCamera.startPreview();
        }
    }

    public void removeCamera(){
        this.mCamera = null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, " surfaceCreated");
        if (mCamera != null) {
            mCamera.startPreview();
        }
        isCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
