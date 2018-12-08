package com.orbbec.NativeNI;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

/**
 *
 * @author xlj
 * @date 17-3-2
 */

public class OrbbecUtils {

    static {
        System.loadLibrary("yuv");
        System.loadLibrary("OrbbecUtils");
    }

    public native static int ByteBufferCopy(ByteBuffer src, ByteBuffer dst, int length);

    public native static int NV21ToI420(byte[] nv21Data, int dataLenght, int width, int height, ByteBuffer yByteBuffer, ByteBuffer uByteBuffer, ByteBuffer vByteBuffer);

    public native static int CropDepth(ByteBuffer depthSrcData, int dataLenght, int width, int height, ByteBuffer depthDstBuffer, int cropX, int cropY, int dstWidth, int dstHeight);

    public native static int CropRGB888(ByteBuffer depthSrcData, int width, int height, ByteBuffer depthDstBuffer, int cropX, int cropY, int dstWidth, int dstHeight);

    public native static int CropNV21(byte[] nv21SrcData, int dataLenght, int width, int height, ByteBuffer nv21DstBuffer, int cropX, int cropY, int dstWidth, int dstHeight);

    public native static int CropYUY2toNV21AndI420(ByteBuffer yuyvSrcBuffer, int width, int height, ByteBuffer dsNv21tBuffer, ByteBuffer dstI420Buffer, int cropX, int cropY, int dstWidth, int dstHeight);

    public native static int ConvertTORGBA(ByteBuffer src, ByteBuffer dst, int w, int h, int strideInBytes);

    public native static int ConvertTORGBA(byte[] src, ByteBuffer dst, int w, int h, int strideInBytes);

    public native static int rgbaWriteToFile(byte[] rgbaData, int imageWidth, int imageHeight, int fileNameIndex);

    public native static int RGB888TORGBA(ByteBuffer src, ByteBuffer dst, int w, int h, int strideInBytes);

    public native static int NV21ToRGB32(int width, int height, byte[] nv21Data, Bitmap dst, boolean mirror);

    public native static int BMPToBUF(int width, int height, ByteBuffer byteBuffer, Bitmap dst);

    public native static int DrawDepth(ByteBuffer buf, Bitmap bitmap, int w, int h, int strideInBytes);

    /**
     * 输入大分辨率的srcData数据，裁切成 dstWidth & dstHeight 长宽比的大小后由C层渲染，
     * 然后裁切一个dstWidth * dstHeight的小分辨率数据到dstData，供后人脸识别等用途使用
     * 该方法还有点问题 2018/1/5
     */
    public native static int CropScaleNV21AndRendering(byte[] srcData, int dataLength, int width, int height, ByteBuffer dstData, int dstWidth, int dstHeight);

    public native static int DrawColor(ByteBuffer buf, Bitmap bitmap, int w, int h, int strideInBytes);

    public native static int DrawIR(ByteBuffer buf, Bitmap bitmap, int w, int h, int strideInBytes);

    public native static int InitDevice(long device);

    public native static int setGain(int value);

    public native static int setExposure(short value);

    public native static int getGain();

    public native static int getExposure();

    public native static int getVersion();

    /**
     * 初始化
     * @param dataPath data文件所在的目录
     * @return
     */
    public native static boolean initHeadSurface(String dataPath);

    /**
     * 使用shape_predictor_68_face_landmarks.dat做处理
     * @param depth_png 深度图的路径
     * @param face_roi 人脸rect数据
     * @param mesh_out 输出ply文件的路径
     * @return
     */
    public native static boolean depthToMesh(String depth_png, int[] face_roi, String mesh_out);

    /**
     * 使用shape_predictor_68_face_landmarks.dat做处理
     * @param depth_png 深度图的路径
     * @param color_bmp 同帧的彩色图路径
     * @param mesh_out 输出ply文件的路径
     * @return
     */
    public native static boolean depthToMeshAutoFace(String depth_png, String color_bmp, String mesh_out);

    /**
     * 使用base.obj做处理
     * @param fn_scanMesh  depthToMesh*接口输出的ply文件
     * @param fn_outMesh 优化后的ply文件路径
     * @param beFemale 暂时没有用到
     * @return
     */
    public native static boolean registerToBaseMesh(String fn_scanMesh, String fn_outMesh, boolean beFemale);
}
