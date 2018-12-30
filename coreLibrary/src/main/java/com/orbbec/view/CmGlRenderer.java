package com.orbbec.view;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLSurfaceView.Renderer;

import com.orbbec.NativeNI.CmOpenGLES;
import java.nio.ByteBuffer;

/**
 * @author lgp
 */
public class CmGlRenderer implements Renderer {

    private GlFrameSurface mSurface;
    private byte[] mData;
    private ByteBuffer i420Buffer;

    private void logd(String msg){
//        Log.i("CmGlRenderer", msg);
    }

    public CmGlRenderer(GlFrameSurface surface){
        super();
        this.mSurface = surface;
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        logd("opengl**** CmGlRenderer :: onSurfaceCreated");
        CmOpenGLES.init(mSurface.getFrameWidth(), mSurface.getFrameHeight());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        logd("opengl**** CmGlRenderer :: onSurfaceChanged");

        CmOpenGLES.changeLayout(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mData != null) {
            CmOpenGLES.drawNV21Frame(mData, mData.length);
        }else if (i420Buffer != null){
            CmOpenGLES.drawI420Frame(i420Buffer, mSurface.getFrameWidth(), mSurface.getFrameHeight());
        }
    }

    public void updateNV21Data( byte[] data){
        this.mData = data;
    }

    public void drawI420Frame(ByteBuffer i420Buffer){
        this.i420Buffer = i420Buffer;
    }

    public void changeRotation(int rotationAngle, boolean isHorizontalFlip){
        CmOpenGLES.changeRotation(rotationAngle, isHorizontalFlip);
    }

    /**
     * this method will be called from native code, it happens when the video is about to play or
     * the video size changes.
     */
    public void update(int w, int h) {

    }

    public void release(){
        logd("opengl**** CmGlRenderer :: release");
        CmOpenGLES.release();
    }
}