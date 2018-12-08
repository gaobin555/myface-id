package com.orbbec.base;

/**
 * 识别流程回调
 * @author lgp
 */
public interface IdentifyCallback {

    /**
     * 活体识别
     * @param isLiveness
     * @param identifyPerson
     * @param nameFromPersonId
     * @param happy
     */
    void onLiveness(boolean isLiveness, int identifyPerson, String nameFromPersonId, int happy);

}
