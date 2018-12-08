package com.orbbec.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author zlh
 * @date 2015/8/4
 */
public class FpsMeter {

	private long mPrevTS;
	private float mFPS;
	private long mPassTime;
	private int mN;
	private int mCount;

	DecimalFormat mdt;
	private int mFrameCount;
	private long mLastTime;
	private int mFps;
	
	public FpsMeter() {
		mCount = 1;
		//获得格式化类对象
		mdt = (DecimalFormat) NumberFormat.getInstance();
		mdt.applyPattern("0.00");
	}

	public void mesureFps(){
		++mFrameCount;
		if (mFrameCount == GlobalDef.NUMBER_30) {
			long now = System.nanoTime();
			long diff = now - mLastTime;
			mFps = (int)(1e9 * 30 / diff);
			mFrameCount = 0;
			mLastTime = now;
		}
		
		return ;
	}
	

	public String getFps() {
		return mdt.format(mFps);
	}

	private void reset() {
		mPassTime = 0;
		mCount = 0;
	}
	
}
