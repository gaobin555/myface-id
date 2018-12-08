package com.orbbec.keyguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;

import com.orbbec.model.User;
import com.orbbec.utils.AppDef;
import com.orbbec.utils.DataSource;
import com.orbbec.utils.DrawUtil;
import com.orbbec.utils.GlobalDef;

import java.io.File;
import java.util.List;

import mobile.ReadFace.YMFace;



public class ObFacePresenter extends BaseFacePresenter {
    private static final String TAG = "ObFacePresenter";

    private SurfaceView mFaceTrackDrawView;

    private int mVideoViewMarginLeft;
    private int mVideoViewMarginTop;
    private int mVideoViewWidth;
    private int mVideoViewHeitht;

    private GlobalDef mDef = new AppDef();

    public ObFacePresenter(Context context) {
        super(context, context.getResources().getDimension(R.dimen.color_width), context.getResources().getDimension(R.dimen.color_height), new AppDef());
    }

    public void setFaceTrackDrawView(SurfaceView frameSurfaceView) {
        mFaceTrackDrawView = frameSurfaceView;
    }



    /**
     * 是否需要检查距离
     * @return
     */
    @Override
    public boolean needToMeasureDistance(){
        return true;
    }

    /**
     * 返回是否需要做活体验证
     * @param identifyPerson
     * @param nameFromPersonId
     * @param happy
     * @return
     */
    @Override
    public boolean needToCheckLiveness(int identifyPerson, String nameFromPersonId, int happy){

        return true;
    }

    @Override
    public int faceOffsetPixel(){

        return GlobalDef.OFFSET_PIXEL;
    }

    @Override
    public void showRgbFpsToUI(String fps) {
        if (mContext instanceof RecognitionFaceActivity) {
            ((RecognitionFaceActivity) mContext).showRgbFps(fpsMeter.getFps());
        }
    }

    @Override
    public void showDepthFpsToUI(String fps) {
        if (mContext instanceof RecognitionFaceActivity) {
            ((RecognitionFaceActivity) mContext).showDepthFps(fpsMeter1.getFps());
        }
    }

    @Override
    public void drawFaceTrack(List<YMFace> faces, boolean toFlip, float scaleBit, int videoWidth, String currentUser,
                              String mAge, String happystr, int livenessStatus, float distance){

        if (needIdentificationFace && mFaceTrackDrawView != null) {
            DrawUtil.drawAnim(faces, mFaceTrackDrawView, mDataSource.isUVC()&&!mDataSource.Deeyea(), scaleBit, mVideoViewMarginLeft, mVideoViewMarginTop, mVideoViewWidth, mDef.getColorWidth(), currentUser, mAge, happystr, livenessStatus, distance);
        }
    }

    /**
     * 视频view相对于画人脸框的位置
     * @param marginLeft
     * @param marginTop
     * @param viewWidth
     * @param viewHeight
     */
    public void setFaceTrackDrawViewMargin(int marginLeft, int marginTop, int viewWidth, int viewHeight){
        mVideoViewMarginLeft = marginLeft;
        mVideoViewMarginTop = marginTop;
        mVideoViewWidth = viewWidth;
        mVideoViewHeitht = viewHeight;
    }

}
