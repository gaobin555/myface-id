package com.orbbec.keyguard;

import android.content.Context;
import android.view.SurfaceView;

import com.orbbec.constant.Constant;
import com.orbbec.model.User;
import com.orbbec.utils.AppDef;
import com.orbbec.utils.DrawUtil;
import com.orbbec.utils.GlobalDef;
import com.orbbec.utils.SerialPortHelper;
import com.orbbec.utils.UserDataUtil;

import java.util.List;
import java.util.Map;

import mobile.ReadFace.YMFace;

import static dou.utils.HandleUtil.runOnUiThread;


public class ObFacePresenter extends BaseFacePresenter {
    private static final String TAG = "ObFacePresenter";

    private SurfaceView mFaceTrackDrawView;

    private int mVideoViewMarginLeft;
    private int mVideoViewMarginTop;
    private int mVideoViewWidth;
    private int mVideoViewHeitht;


    // 读取用户数
    private Map<Integer, User> userMap;

    private SerialPortHelper serialPortHelper;

    private GlobalDef mDef = new AppDef();

    public ObFacePresenter(Context context) {
        super(context, context.getResources().getDimension(R.dimen.color_width), context.getResources().getDimension(R.dimen.color_height), new AppDef());
        userMap = UserDataUtil.updateDataSource(true);
        if (mContext instanceof RecognitionFaceActivity) {
            serialPortHelper = new SerialPortHelper();
            serialPortHelper.initSerial();
        }
    }

    public void setFaceTrackDrawView(SurfaceView frameSurfaceView) {
        mFaceTrackDrawView = frameSurfaceView;
    }

    /**
     * 是否需要检查距离
     * @return true
     */
    @Override
    public boolean needToMeasureDistance(){
        return true;
    }

    /**
     * 获取用户姓名
     * @param personId 用户ID
     * @return 返回用户姓名
     */
    @Override
    public String getNameFromPersonId(int personId) {
        if (userMap.containsKey(personId)){
            User user = userMap.get(personId);
            return user.getName();
        } else {
            return mContext.getString(R.string.nouser);
        }
    }

    /**
     *  打开闸机门
     */
    @Override
    public boolean openTheGate(int personId) {
        if (userMap.containsKey(personId)) {
            if (mContext instanceof RecognitionFaceActivity) {
                // 到UI线程发送开门命令
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //更新UI
                        serialPortHelper.sendOpenGate(Constant.OPENGATE);
                    }
                });
            }
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isRegistUser(int personId) {
        return userMap.containsKey(personId);
    }

    /**
     * 返回是否需要做活体验证
     * @param identifyPerson  identifyPerson
     * @param nameFromPersonId nameFromPersonId
     * @param happy  happy
     * @return true
     */
    @Override
    public boolean needToCheckLiveness(int identifyPerson, String nameFromPersonId, int happy){

        return true;
    }

    @Override
    public boolean isRegistTask(){
//        if (mContext instanceof RegistFromCamAcitvity) {
//           return true;
//        } else {
//            return false;
//        }
        return mContext instanceof RegistFromCamAcitvity;
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
        if (mContext instanceof RegistFromCamAcitvity) {
            DrawUtil.drawRect(faces, mFaceTrackDrawView, mDataSource.isUVC() && !mDataSource.Deeyea(), scaleBit, mVideoViewMarginLeft, mVideoViewMarginTop, mVideoViewWidth, mDef.getColorWidth());
        } else {
            if (needIdentificationFace && mFaceTrackDrawView != null) {
                DrawUtil.drawAnim(faces, mFaceTrackDrawView, mDataSource.isUVC() && !mDataSource.Deeyea(), scaleBit, mVideoViewMarginLeft, mVideoViewMarginTop, mVideoViewWidth, mDef.getColorWidth(), currentUser, mAge, happystr, livenessStatus, distance);
            }
        }
    }

    /**
     * 视频view相对于画人脸框的位置
     * @param marginLeft  左
     * @param marginTop   上
     * @param viewWidth   宽
     * @param viewHeight  高
     */
    public void setFaceTrackDrawViewMargin(int marginLeft, int marginTop, int viewWidth, int viewHeight){
        mVideoViewMarginLeft = marginLeft;
        mVideoViewMarginTop = marginTop;
        mVideoViewWidth = viewWidth;
        mVideoViewHeitht = viewHeight;
    }
}
