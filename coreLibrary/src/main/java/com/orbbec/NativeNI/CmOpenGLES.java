package com.orbbec.NativeNI;

import java.nio.ByteBuffer;

/**
 * @author lgp
 */
public class CmOpenGLES {

	static{
		OrbbecUtils.getVersion();
	}
	
	/**
	 * 初始化OpenGL ES 2.0
	 */
	public static native void init(int width, int height);
	
	/**
	 * 释放
	 */
	public static native void release();
	
	/**
	 * 传入宽高
	 * @param width
	 * @param height
	 */
	public static native void changeLayout(int width, int height);
	
	/**
	 * 渲染Yuv数据
	 * @param data
	 */
	public static native void drawNV21Frame(byte[] data, int size);

	/**
	 * 渲染i420数据
	 * @param i420Buffer
	 * @param width
	 * @param height
	 */
	public static native void drawI420Frame(ByteBuffer i420Buffer, int width, int height);

	/**
	 * 旋转角度和翻转变换，这个函数可以在渲染过程中改变
	 * @param rotationAngle
	 * @param isHorizontalFlip
	 */
	public static native void changeRotation(int rotationAngle, boolean isHorizontalFlip);
}
