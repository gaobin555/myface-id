package com.orbbec.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.orbbec.keyguard.RgbCamera;

import java.nio.ByteBuffer;

/**
 * @author lgp
 */
public class GlFrameSurface extends GLSurfaceView{

    private CmGlRenderer mGLFRenderer;

    private static final String TAG = "GLFrameSurface";
    private static final boolean DEGUG = false;

    private int mFrameWidth;
    private int mFrameHeight;

    public GlFrameSurface(Context context) {
        super(context);
        init(context);
    }

    public GlFrameSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        super.setEGLContextClientVersion(2);

        mGLFRenderer = new CmGlRenderer(this);
        setRenderer(mGLFRenderer);
        log("init");
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        log("onAttachedToWindow()");
    }

    public void setFrameWidth(int width){
        mFrameWidth = width;
    }

    public void setFrameHeight(int height){
        mFrameHeight = height;
    }

    public int getFrameWidth(){
        return mFrameWidth;
    }

    public int getFrameHeight(){
        return mFrameHeight;
    }

    public void updateNV21Data( byte[] data){

        if (mGLFRenderer != null){
            mGLFRenderer.updateNV21Data(data);
            requestRender();
            log("updateNV21Data(byte[])");
        }else{
            log("updateNV21Data(byte[]) but mGLFRenderer == null");
        }
    }

    public void updateI420Frame(ByteBuffer i420Buffer){
        if (mGLFRenderer != null){
            mGLFRenderer.drawI420Frame(i420Buffer);
            requestRender();
        }
    }

    public void changeRotation(int rotationAngle, boolean isHorizontalFlip){
        if (mGLFRenderer != null){
            mGLFRenderer.changeRotation(rotationAngle, isHorizontalFlip);
        }
    }

    public void update(int w, int h){
        if (mGLFRenderer != null){
            mGLFRenderer.update(w, h);
        }
    }

    public void setCamera(RgbCamera camera) {
        if (mGLFRenderer != null){
//            mGLFRenderer.setCamera(camera);
        }
    }

    public void removeCamera(){
        if (mGLFRenderer != null){
//            mGLFRenderer.setCamera(null);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        log("onDetachedFromWindow()...  release...");

        // TODO：这里的释放会产生问题，
//        if (mGLFRenderer != null){
//            mGLFRenderer.release();
//            mGLFRenderer = null;
//        }
    }

    private void log(String str){
        if (DEGUG){
            Log.w(TAG, str);
        }
    }
}
