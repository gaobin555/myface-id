package com.orbbec.base;


import android.graphics.Bitmap;

/**
 * 注册流程回调
 * @author lgp
 */
public interface RegisterCallback {
    /**
     * 当前步骤条件检测成功
     * @param mainFace
     * @param leftFace
     * @param rightFace
     * @param upFace
     * @param lowFace
     */
    void onTracked(boolean mainFace, boolean leftFace, boolean rightFace, boolean upFace, boolean lowFace);

    /**
     * 当前步骤条件检测失败
     */
    void onTrackFailed();

    /**
     * 全部步骤完成
     *
     * @param identifyPerson 注册成功分配的脸的id
     * @param head
     * @param gender
     */
    void onEndTrack(int identifyPerson, Bitmap head, String gender);

    /**
     * 更新当前步骤
     *
     * @param step 当前注册流程中的第几步
     */
    void onUpdateStep(int step);
}
