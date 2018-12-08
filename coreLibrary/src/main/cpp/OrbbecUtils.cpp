// Created by xlj on 17-3-2.
//

#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <android/log.h>
#include<android/bitmap.h>
#include "Include/OniCTypes.h"
#include "Include/OpenNI.h"
#include "Include/OrbbecVersion.h"

#include "utils.h"
#include "Include/libyuv.h"
#include "opengles/esUtil.h"
#include "head_surface.h"


#ifdef __cplusplus
extern "C" {
#endif

#include "opengles/CameraOpenGLES.h"
//    int printVersion(int version);

#ifdef __cplusplus
}
#endif


#define REGISTER_CLASS "com/orbbec/NativeNI/OrbbecUtils"
#define REGISTER_CM_OPENGLES_CLASS "com/orbbec/NativeNI/CmOpenGLES"


#define LOG_TAG "DepthUtils-Jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG, __VA_ARGS__)

unsigned char *renderingY;
unsigned char *renderingU;
unsigned char *renderingV;

unsigned char *tempScaleYUV = 0;
unsigned char *tempCropNV21 = 0;

typedef struct {
    uint8_t r;
    uint8_t g;
    uint8_t b;
} RGB888Pixel;

typedef unsigned char byte;

int *m_histogram;
enum {
    HISTSIZE = 0xFFFF,
};


int ConventFromDepthToRGBA(short *src, int *dst, int w, int h, int strideInBytes) {

    // Calculate the accumulative histogram (the yellow display...)
    if (m_histogram == NULL) {
        m_histogram = new int[HISTSIZE];
    }
    memset(m_histogram, 0, HISTSIZE * sizeof(int));

    int nNumberOfPoints = 0;
    unsigned int value;
    int Size = w * h;
    for (int i = 0; i < Size; ++i) {
        value = src[i];
        if (value != 0) {
            m_histogram[value]++;
            nNumberOfPoints++;
        }
    }

    int nIndex;
    for (nIndex = 1; nIndex < HISTSIZE; nIndex++) {
        m_histogram[nIndex] += m_histogram[nIndex - 1];
    }

    if (nNumberOfPoints != 0) {
        for (nIndex = 1; nIndex < HISTSIZE; nIndex++) {
            m_histogram[nIndex] = (unsigned int) (256 * (1.0f - ((float) m_histogram[nIndex] /
                                                                 nNumberOfPoints)));
        }
    }

    for (int y = 0; y < h; ++y) {
        uint8_t *rgb = (uint8_t *) (dst + y * w);
        short *pView = src + y * w;
        for (int x = 0; x < w; ++x, rgb += 4, pView++) {
            value = m_histogram[*pView];
            rgb[0] = value;
            rgb[1] = value;
            rgb[2] = 0x00;
            rgb[3] = 0xff;
        }
    }
    return 0;
}

jint ConvertDepthBytesTORGBA(JNIEnv *env, jobject obj, jobject src, jbyteArray dst, jint w, jint h,
                             jint strideInBytes){
    if (src == NULL || dst == NULL) {
        return -1;
    }
    int *dstDate = (int *)env->GetByteArrayElements(dst, 0);
    short *srcBuf = (short *) env->GetDirectBufferAddress(src);
    ConventFromDepthToRGBA(srcBuf, dstDate, w, h, strideInBytes);

    env->ReleaseByteArrayElements(dst, (jbyte *) dstDate, 0);
    return 0;
}


jint ConvertTORGBA(JNIEnv *env, jobject obj, jobject src, jobject dst, jint w, jint h,
                   jint strideInBytes) {

    if (src == NULL || dst == NULL) {
        return -1;
    }
    short *srcBuf = (short *) env->GetDirectBufferAddress(src);

    int *dstBuf = (int *) env->GetDirectBufferAddress(dst);

    ConventFromDepthToRGBA(srcBuf, dstBuf, w, h, strideInBytes);

    return 0;
}


int ConventToRGBA(uint8_t *src, uint8_t *dst, int w, int h, int strideInBytes) {
    for (int y = 0; y < h; ++y) {
        uint8_t *pTexture = dst + (y * w * 4);
        const RGB888Pixel *pData = (const RGB888Pixel *) (src + y * strideInBytes);
        for (int x = 0; x < w; ++x, ++pData, pTexture += 4) {
            uint8_t r = pData->r;
            if (r > 255) {
                r = 255;
            } else if (r < 0) {
                r = 0;
            }
            pTexture[0] = r;
            uint8_t g = pData->g;
            if (g > 255) {
                g = 255;
            } else if (g < 0) {
                g = 0;
            }
            pTexture[1] = g;
            uint8_t b = pData->b;
            if (b > 255) {
                b = 255;
            } else if (b < 0) {
                b = 0;
            }
            pTexture[2] =b;
            pTexture[3] = 255;
        }

    }

    return 0;
}


int IRto888(uint8_t *src, uint8_t *dst, int w, int h, int strideInBytes) {

    memset(dst, 0, w * h * 4);
    for (int y = 0; y < h; ++y) {
        uint8_t *pTexture = dst + (y * w * 4);
        uint16_t *pData = (uint16_t *) (src + y * strideInBytes);

        for (int x = 0; x < w; ++x, ++pData, pTexture += 4) {
            uint8_t srcGray = (uint8_t) ((*pData) >> 2);

            pTexture[0] = srcGray;
            pTexture[1] = srcGray;
            pTexture[2] = srcGray;
            pTexture[3] = 255;
        }
    }

    return 0;
}

int ConventYUVToRGBA(byte *yuv, int *intArr, int width, int height, bool mirror) {
    int rc = 0;
    if (yuv != NULL && intArr != NULL) {
        int rows = 0;
        int *rowptr = intArr;
        int yuvIndex = width;
        byte *RGB24Stream = (byte *) intArr;
        int colorLength = width * height * 2;
        int nIndex = 0;
        for (int i = 0; i < colorLength; i += 4) {
            int Index = i;
            byte Y1 = yuv[Index + 0];
            byte U = (byte) (yuv[Index + 1]);
            byte Y2 = yuv[Index + 2];
            byte V = (byte) (yuv[Index + 3]);

            int R1 = (int) (Y1 + 1.4075 * (V - 128));
            int G1 = (int) (Y1 - 0.3455 * (U - 128) - 0.7169 * (V - 128));
            int B1 = (int) (Y1 + 1.779 * (U - 128));

            int R2 = (int) (Y2 + 1.4075 * (V - 128));
            int G2 = (int) (Y2 - 0.3455 * (U - 128) - 0.7169 * (V - 128));
            int B2 = (int) (Y2 + 1.779 * (U - 128));

            R1 = R1 > 255 ? 255 : R1;
            G1 = G1 > 255 ? 255 : G1;
            B1 = B1 > 255 ? 255 : B1;

            R2 = R2 > 255 ? 255 : R2;
            G2 = G2 > 255 ? 255 : G2;
            B2 = B2 > 255 ? 255 : B2;

            R1 = R1 < 0 ? 0 : R1;
            G1 = G1 < 0 ? 0 : G1;
            B1 = B1 < 0 ? 0 : B1;

            R2 = R2 < 0 ? 0 : R2;
            G2 = G2 < 0 ? 0 : G2;
            B2 = B2 < 0 ? 0 : B2;

            if (mirror) {
                yuvIndex -= 2;
                if (yuvIndex == 0) {
                    rows++;
                    yuvIndex = width;
                    rowptr = intArr + rows * width;
                }
                Index = 0;
                RGB24Stream = (byte *) (rowptr + yuvIndex);

                RGB24Stream[0] = (byte) B2;
                RGB24Stream[1] = (byte) G2;
                RGB24Stream[2] = (byte) R2;
                RGB24Stream[3] = (byte) 0xFF;

                RGB24Stream[4] = (byte) B1;
                RGB24Stream[5] = (byte) G1;
                RGB24Stream[6] = (byte) R1;
                RGB24Stream[7] = (byte) 0xFF;
            } else {

                RGB24Stream[nIndex++] = (byte) B1;
                RGB24Stream[nIndex++] = (byte) G1;
                RGB24Stream[nIndex++] = (byte) R1;
                RGB24Stream[nIndex++] = (byte) 0xFF;

                RGB24Stream[nIndex++] = (byte) B2;
                RGB24Stream[nIndex++] = (byte) G2;
                RGB24Stream[nIndex++] = (byte) R2;
                RGB24Stream[nIndex++] = (byte) 0xFF;
            }
        }
    } else {
        rc = -1;
    }
    return rc;

}

jint  checkVersion(JNIEnv* env, jobject)
{
    return isDlt();
}
jint RGB888TORGBA(JNIEnv *env, jobject obj, jobject src, jobject dst, jint w, jint h,
                  jint strideInBytes) {

    if (src == NULL || dst == NULL) {
        return -1;
    }
    uint8_t *srcBuf = (uint8_t *) env->GetDirectBufferAddress(src);

    uint8_t *dstBuf = (uint8_t *) env->GetDirectBufferAddress(dst);

    ConventToRGBA(srcBuf, dstBuf, w, h, strideInBytes);

    return 0;
}

JNIEXPORT jint JNICALL DrawColor
        (JNIEnv *env, jclass, jobject srcObj, jobject bmpObj, jint w, jint h, jint strideInBytes) {
    AndroidBitmapInfo bmpInfo = {0};
    if (AndroidBitmap_getInfo(env, bmpObj, &bmpInfo) < 0) {
        return -1;
    }
    uint8_t *dataFromBmp = NULL;
    if (AndroidBitmap_lockPixels(env, bmpObj, (void **) &dataFromBmp)) {
        return -1;
    }

    uint8_t *srcBuf = (uint8_t *) env->GetDirectBufferAddress(srcObj);
    if (srcBuf == NULL) {
        return -1;
    }

    int rc = 0;
    rc = ConventToRGBA(srcBuf, dataFromBmp, w, h, strideInBytes);
//    rc = ConventYUVToRGBA(srcBuf, (int*)dataFromBmp, w, h, true);

    AndroidBitmap_unlockPixels(env, bmpObj);

    return rc;
}

JNIEXPORT jint JNICALL DrawIR
        (JNIEnv *env, jclass, jobject srcObj, jobject bmpObj, jint w, jint h, jint strideInBytes) {
    AndroidBitmapInfo bmpInfo = {0};
    if (AndroidBitmap_getInfo(env, bmpObj, &bmpInfo) < 0) {
        return -1;
    }
    uint8_t *dataFromBmp = NULL;
    if (AndroidBitmap_lockPixels(env, bmpObj, (void **) &dataFromBmp)) {
        return -1;
    }

    uint8_t *srcBuf = (uint8_t *) env->GetDirectBufferAddress(srcObj);
    if (srcBuf == NULL) {
        return -1;
    }

    int rc = 0;

    rc = IRto888(srcBuf, dataFromBmp, w, h, strideInBytes);

    AndroidBitmap_unlockPixels(env, bmpObj);

    return rc;
}

JNIEXPORT jint JNICALL DrawDepth
        (JNIEnv *env, jclass, jobject srcObj, jobject bmpObj, jint w, jint h, jint strideInBytes) {
    AndroidBitmapInfo bmpInfo = {0};
    if (AndroidBitmap_getInfo(env, bmpObj, &bmpInfo) < 0) {
        LOGD("AndroidBitmap_getInfo  == -1");
        return -1;
    }
    int *dataFromBmp = NULL;
    if (AndroidBitmap_lockPixels(env, bmpObj, (void **) &dataFromBmp)) {
        LOGD("AndroidBitmap_lockPixels  == -1");
        return -1;
    }

    short *srcBuf = (short *) env->GetDirectBufferAddress(srcObj);
    if (srcBuf == NULL) {
        LOGD("srcBuf  == NULL");
        return -1;
    }

    int rc = 0;
    rc = ConventFromDepthToRGBA(srcBuf, dataFromBmp, w, h, strideInBytes);

    AndroidBitmap_unlockPixels(env, bmpObj);

    return rc;
}

openni::Device *p_Device = NULL;

JNIEXPORT jint JNICALL InitDevice
        (JNIEnv *env, jobject obj, jlong deviceHandle) {

    p_Device = new openni::Device((OniDeviceHandle) deviceHandle);

    return 0;
}

jint setGain(JNIEnv *env, jobject obj, int value) {
    int dataSize = 2;
    int gain = value;
    p_Device->setProperty(openni::OBEXTENSION_ID_IR_GAIN, (uint8_t *) &gain, dataSize);
    return 0;
}

jint setExposure(JNIEnv *env, jobject obj, jshort value) {
    int dataSize = 2;
    int exposure = value;
    p_Device->setProperty(openni::OBEXTENSION_ID_IR_EXP, (uint8_t *) &exposure, dataSize);
    return 0;
}

jint getGain(JNIEnv *env, jobject obj) {
    int dataSize = 2;
    int value = 0;
    p_Device->getProperty(openni::OBEXTENSION_ID_IR_GAIN, (uint8_t *) &value, &dataSize);
    return value;
}

jint getExposure(JNIEnv *env, jobject obj) {
    int dataSize = 2;
    int value = 0;
    p_Device->getProperty(openni::OBEXTENSION_ID_IR_EXP, (uint8_t *) &value, &dataSize);
    return value;
}

void setValue(uint8_t *src, int index, int value) {
    src[index] = (uint8_t) value;
}

jint NV21ToRGB32(JNIEnv *env, jobject obj, jint width, jint height, jbyteArray nv21Data,
                 jobject bitmap, jboolean mirror) {

    AndroidBitmapInfo bitmapInfo;
    int ret;
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return ret;
    }
    int bitmapWidth = bitmapInfo.width;
    int bitmapHeight = bitmapInfo.height;
    if (bitmapWidth != width || bitmapHeight != height) {
        LOGE("Size of RGBBitmap is not %d * %d", width, height);
        return -1;
    }

    if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return -1;
    }

    int *pixels;
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed !  error = %d", ret);
        return ret;
    }
//    uint8_t *dstBuf = (uint8_t *) env->GetDirectBufferAddress(dst);

    jbyte *nv21 = env->GetByteArrayElements(nv21Data, 0);

    int indexU = width * height;
    int indexV = indexU + 1;

    for (int row = 0; row < height; row = row + 2) {
        for (int column = 0; column < width; column = column + 2) {
            int y1 = nv21[row * width + column] & 0xff;
            int y2 = nv21[row * width + column + 1] & 0xff;
            int y3 = nv21[(row + 1) * width + column] & 0xff;
            int y4 = nv21[(row + 1) * width + column + 1] & 0xff;

            int u = nv21[indexU] & 0xff;
            int v = nv21[indexV] & 0xff;

            int r1 = GetR(y1, u, v);
            int g1 = GetG(y1, u, v);
            int b1 = GetB(y1, u, v);

            int r2 = GetR(y2, u, v);
            int g2 = GetG(y2, u, v);
            int b2 = GetB(y2, u, v);

            int r3 = GetR(y3, u, v);
            int g3 = GetG(y3, u, v);
            int b3 = GetB(y3, u, v);

            int r4 = GetR(y4, u, v);
            int g4 = GetG(y4, u, v);
            int b4 = GetB(y4, u, v);

            /* setValue(dstBuf, row * width * 3 + column * 3 + 0, r1);
             setValue(dstBuf, row * width * 3 + column * 3 + 1, g1);
             setValue(dstBuf, row * width * 3 + column * 3 + 2, b1);

             setValue(dstBuf, row * width * 3 + (column + 1) * 3 + 0, r2);
             setValue(dstBuf, row * width * 3 + (column + 1) * 3 + 1, g2);
             setValue(dstBuf, row * width * 3 + (column + 1) * 3 + 2, b2);

             setValue(dstBuf, (row + 1) * width * 3 + column * 3 + 0, r3);
             setValue(dstBuf, (row + 1) * width * 3 + column * 3 + 1, g3);
             setValue(dstBuf, (row + 1) * width * 3 + column * 3 + 2, b3);

             setValue(dstBuf, (row + 1) * width * 3 + (column + 1) * 3 + 0, r4);
             setValue(dstBuf, (row + 1) * width * 3 + (column + 1) * 3 + 1, g4);
             setValue(dstBuf, (row + 1) * width * 3 + (column + 1) * 3 + 2, b4);*/
            int pixel1, pixel2;
            if (mirror) {
                pixel1 = width - column;
                pixel2 = width - column - 1;
            } else {
                pixel1 = column;
                pixel2 = column + 1;
            }
            pixels[row * width + pixel1] = 0xff000000 | r1 << 16
                                           | g1 << 8
                                           | b1;
            pixels[row * width + pixel2] = 0xff000000 | r2 << 16
                                           | g2 << 8
                                           | b2;
            pixels[(row + 1) * width + pixel1] = 0xff000000 | r3 << 16
                                                 | g3 << 8
                                                 | b3;
            pixels[(row + 1) * width + pixel2] = 0xff000000 | r4 << 16
                                                 | g4 << 8
                                                 | b4;
            indexU += 2;
            indexV += 2;
        }
    }
    env->ReleaseByteArrayElements(nv21Data, nv21, 0);

    return 0;
}

jint
CropRGB888(JNIEnv *env, jobject obj, jobject srcData, jint width, jint height, jobject dstBuffer, jint cropX, jint cropY, jint dstWidth, jint dstHeight){
    uint8_t *src = (uint8_t *) (*env).GetDirectBufferAddress(srcData);
    uint8_t *dst = (uint8_t *) (*env).GetDirectBufferAddress(dstBuffer);
    if(cropX < 0 || cropY<0 || dstWidth<0 || dstHeight <0){
        LOGE("CropDepth Illegal parameters!! it must be greater than zero");
        return -1;
    }
    if(width %2 != 0 || height % 2 != 0 || dstWidth %2 != 0 || dstHeight % 2 != 0){
        LOGE("CropDepth Illegal parameters!! dstWidth and dstHeight must be a double number");
        return -1;
    }
    if(cropX + dstWidth > width || cropY + dstHeight > height){
        LOGE("CropDepth Illegal parameters!! Cropped size is wrong");
        return -1;
    }
    for (int row = 0; row < dstHeight; ++row) {
        RGB888Pixel *dstTexture = (RGB888Pixel *)dst + (row * dstWidth);
        const RGB888Pixel *srcTexture = (const RGB888Pixel *)src + (row * width) + (cropY * width);
        for (int col = 0; col < dstWidth; col++) {
            (dstTexture+col)->r = (srcTexture+col + cropX)->r;
            (dstTexture+col)->g = (srcTexture+col + cropX)->g;
            (dstTexture+col)->b = (srcTexture+col + cropX)->b;
        }
    }
    return 0;
}

jint BMPToBUF(JNIEnv *env, jobject obj, jint width, jint height, jobject buffer,
              jobject bitmap) {

    AndroidBitmapInfo bitmapInfo;
    int ret;
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return ret;
    }
    int bitmapWidth = bitmapInfo.width;
    int bitmapHeight = bitmapInfo.height;
    if (bitmapWidth != width || bitmapHeight != height) {
        LOGE("Size of RGBBitmap is not %d * %d", width, height);
        return -1;
    }

    if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return -1;
    }

    int *pixels;
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed !  error = %d", ret);
        return ret;
    }
    uint8_t *dstBuf = (uint8_t *) env->GetDirectBufferAddress(buffer);

    int index = 0;
    for (int row = 0; row < height; row = row + 1) {
        for (int column = 0; column < width; column = column + 1) {
            dstBuf[index + 0] = pixels[row * width + column];
            dstBuf[index + 1] = pixels[row * width + column];
            dstBuf[index + 2] = pixels[row * width + column];
            index += 3;
        }
    }


    return 0;
}

jint
CropDepth(JNIEnv *env, jobject obj, jobject srcData, jint dataLength, jint width, jint height, jobject dstBuffer, jint cropX, jint cropY, jint dstWidth, jint dstHeight) {

    if (srcData == NULL || dstBuffer == NULL) {
        return -1;
    }

    short *src = (short *) (*env).GetDirectBufferAddress(srcData);
    short *dst = (short *) (*env).GetDirectBufferAddress(dstBuffer);
    if(cropX < 0 || cropY<0 || dstWidth<0 || dstHeight <0){
        LOGE("CropDepth Illegal parameters!! it must be greater than zero");
        return -1;
    }
    if(width %2 != 0 || height % 2 != 0 || dstWidth %2 != 0 || dstHeight % 2 != 0){
        LOGE("CropDepth Illegal parameters!! dstWidth and dstHeight must be a double number");
        return -1;
    }
    if(cropX + dstWidth > width || cropY + dstHeight > height){
        LOGE("CropDepth Illegal parameters!! Cropped size is wrong");
        return -1;
    }

    for (int row = 0; row < dstHeight; ++row) {
        for (int col = 0; col < dstWidth; ++col) {
            dst[row*dstWidth+col] = src[(cropY+row)*width+(cropX+col)];
        }
    }
    return 0;
}

jint NV21ToI420(JNIEnv *env, jobject obj, jbyteArray srcData, jint dataLength, jint width, jint height, jobject yBuffer, jobject uBuffer, jobject vBuffer){
    jbyte *src = env->GetByteArrayElements(srcData, NULL);
    uint8 *yDst = (unsigned char *) (*env).GetDirectBufferAddress(yBuffer);
    uint8 *uDst = (unsigned char *) (*env).GetDirectBufferAddress(uBuffer);
    uint8 *vDst = (unsigned char *) (*env).GetDirectBufferAddress(vBuffer);

    libyuv::ConvertToI420((const uint8 *) src, dataLength,
                          yDst, width,
                          uDst, width/2,
                          vDst, width/2,
                          0, 0,
                          width, height,
                          width, height,
                          libyuv::RotationMode::kRotate0,
                          FOURCC('N', 'V', '2', '1'));

    env->ReleaseByteArrayElements(srcData, src, 0);
    return 0;
}

jint ByteBufferCopy(JNIEnv *env, jobject obj, jobject srcData,  jobject dstData, jint dataLength){

    unsigned char *src = (unsigned char *) (*env).GetDirectBufferAddress(srcData);
    unsigned char *dst = (unsigned char *) (*env).GetDirectBufferAddress(dstData);
    memcpy(dst, src, dataLength * sizeof(unsigned char));
    return 0;
}

jint
CropScaleNV21AndRendering(JNIEnv *env, jobject obj, jbyteArray srcData, jint dataLength, jint width, jint height, jobject dstBuffer, jint dstWidth, jint dstHeight){

    if(width < height){
        LOGE("CropScaleNV21AndRendering, width < height, this func Unable to work !!!");
        return -1;
    }

    if(dstWidth > width || dstHeight > height || srcData == NULL){
        LOGE("CropScaleNV21AndRendering parmes ERROR !!!");
        return -1;
    }

//    Instance *instance = getInstance();
    if (getInstance() == 0){
        LOGE("CropScaleNV21AndRendering() It must be executed after openglInit() !!!");
        return -1;
    }

    int tempHeight = getInstance()->pHeight;
    int tempWidth = tempHeight * dstWidth / dstHeight;

    if (tempScaleYUV == 0){
        LOGE("tempScaleYUV == NULL !!!!!");
        int yuvSize = sizeof(char) * dstWidth * dstHeight * 3 / 2;
        tempScaleYUV = (unsigned char *) (char *)malloc(yuvSize);
    }
    if (renderingY == 0){
        int ySize = sizeof(char) * tempWidth * tempHeight;
        renderingY = (unsigned char *) (char *)malloc(ySize);
        renderingU = (unsigned char *) (char *)malloc(ySize/4);
        renderingV = (unsigned char *) (char *)malloc(ySize/4);
    }

    jbyte *src = env->GetByteArrayElements(srcData, NULL);
    uint8 *dst = (unsigned char *) (*env).GetDirectBufferAddress(dstBuffer);

//    LOGE("instance.data.size = %d*%d, tempSize = %d*%d", instance->pWidth, instance->pHeight, tempWidth, tempHeight);

    libyuv::ConvertToI420((const uint8 *) src, dataLength,
                          renderingY, tempWidth,
                          renderingU, tempWidth / 2,
                          renderingV, tempWidth / 2,
                          (width-tempWidth)/2, (height - tempHeight)/2,
                          width, height,
                          tempWidth, tempHeight,
                          libyuv::RotationMode::kRotate0,
                          FOURCC('N', 'V', '2', '1'));

    libyuv::I420Scale(renderingY, tempWidth,
                      renderingU, tempWidth / 2,
                      renderingV, tempWidth / 2,
                      tempWidth, tempHeight,
                      tempScaleYUV, dstWidth,
                      tempScaleYUV + dstWidth*dstHeight, dstWidth/2,
                      tempScaleYUV + dstWidth*dstHeight * 5 / 4, dstWidth/2,
                      dstWidth, dstHeight,
                      libyuv::kFilterBilinear);  // libyuv::FilterModeEnum.kFilterNone
    /*
     *kFilterNone > 4~7ms 偶尔22ms
     *kFilterLinear 5~9
     * kFilterBilinear
     */

    libyuv::I420ToNV21(tempScaleYUV, dstWidth,
                       tempScaleYUV + dstWidth*dstHeight, dstWidth / 2,
                       tempScaleYUV + dstWidth*dstHeight*5/4, dstWidth / 2,
                       dst, dstWidth,
                       dst + dstWidth*dstHeight, dstWidth,
                       dstWidth, dstHeight);

    // Opengl渲染
    int renderingYSize = tempWidth * tempHeight;
    CmOpenGLES_drawI420Frame(renderingY, renderingYSize, renderingU, renderingYSize/4, renderingV, renderingYSize/4);

    env->ReleaseByteArrayElements(srcData, src, 0);
    return 0;
}

int frameIndex = 0;

jint
CropYUY2toNV21AndI420(JNIEnv *env, jobject, jobject srcYUYVBuffer, jint width, jint height, jobject dsNv21tBuffer, jobject dstI420Buffer, jint cropX, jint cropY, jint dstWidth, jint dstHeight){

    uint8 *srcYUYV = (uint8 *)env->GetDirectBufferAddress(srcYUYVBuffer);
    uint8 *dstI420 = (uint8 *)env->GetDirectBufferAddress(dstI420Buffer);
    uint8 *dstNv21 = (uint8 *)env->GetDirectBufferAddress(dsNv21tBuffer);

    libyuv::ConvertToI420((const uint8 *) srcYUYV, width * height * 2,
                          dstI420, dstWidth,
                          dstI420 + dstWidth*dstHeight, dstWidth / 2,
                          dstI420 + dstWidth*dstHeight*5/4, dstWidth / 2,
                          cropX, cropY,
                          width, height,
                          dstWidth, dstHeight,
                          libyuv::RotationMode::kRotate0,
                          libyuv::FOURCC_YUY2);  // FOURCC('Y', 'U', 'Y', 'V')   YUY2

    libyuv::I420ToNV21(dstI420, dstWidth,
                       dstI420 + dstWidth*dstHeight, dstWidth / 2,
                       dstI420 + dstWidth*dstHeight*5/4, dstWidth / 2,
                       dstNv21, dstWidth,
                       dstNv21 + dstWidth*dstHeight, dstWidth,
                       dstWidth, dstHeight);
    return 0;
}

jint
CropNV21(JNIEnv *env, jobject, jbyteArray srcData, jint dataLength, jint width, jint height, jobject dstBuffer, jint cropX, jint cropY, jint dstWidth, jint dstHeight) {

    jbyte *src = env->GetByteArrayElements(srcData, NULL);
    uint8 *dst = (unsigned char *) (*env).GetDirectBufferAddress(dstBuffer);

    if (tempCropNV21 == NULL){
        int yuvSize = sizeof(char) * dstWidth * dstHeight * 3 / 2;
        tempCropNV21 = (unsigned char *) (char *)malloc(yuvSize);
    }

    libyuv::ConvertToI420((const uint8 *) src, dataLength,
                          tempCropNV21, dstWidth,
                          tempCropNV21 + dstWidth*dstHeight, dstWidth / 2,
                          tempCropNV21 + dstWidth*dstHeight*5/4, dstWidth / 2,
                          cropX, cropY,
                          width, height,
                          dstWidth, dstHeight,
                          libyuv::RotationMode::kRotate0,
                          FOURCC('N', 'V', '2', '1'));

    libyuv::I420ToNV21(tempCropNV21, dstWidth,
                       tempCropNV21 + dstWidth*dstHeight, dstWidth / 2,
                       tempCropNV21 + dstWidth*dstHeight*5/4, dstWidth / 2,
                       dst, dstWidth,
                       dst + dstWidth*dstHeight, dstWidth,
                       dstWidth, dstHeight);

    env->ReleaseByteArrayElements(srcData, src, 0);
    return 0;
}

jint rgbaWriteToFile(JNIEnv *env, jobject obj, jbyteArray srcData, jint imageWidth, jint imageHeight, jint fileNameIndex)
{

    jbyte *image = env->GetByteArrayElements(srcData, NULL);

    unsigned char header[54] = {
            0x42, 0x4d, 0, 0, 0, 0, 0, 0, 0, 0,
            54, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 32, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0
    };

    long file_size = (long)imageWidth * (long)imageHeight * 4 + 54;
    header[2] = (unsigned char)(file_size &0x000000ff);
    header[3] = (file_size >> 8) & 0x000000ff;
    header[4] = (file_size >> 16) & 0x000000ff;
    header[5] = (file_size >> 24) & 0x000000ff;

    long width = imageWidth;
    header[18] = width & 0x000000ff;
    header[19] = (width >> 8) &0x000000ff;
    header[20] = (width >> 16) &0x000000ff;
    header[21] = (width >> 24) &0x000000ff;

    long height = imageHeight;
    header[22] = height &0x000000ff;
    header[23] = (height >> 8) &0x000000ff;
    header[24] = (height >> 16) &0x000000ff;
    header[25] = (height >> 24) &0x000000ff;

    char fname_bmp[128];
    sprintf(fname_bmp, "/sdcard/test/%d.bmp", fileNameIndex);

    FILE *fp;
    if (!(fp = fopen(fname_bmp, "wb")))
        return -1;

    fwrite(header, sizeof(unsigned char), 54, fp);
    fwrite(image, sizeof(unsigned char), (size_t)(long)imageWidth * imageHeight * 4, fp);

    fclose(fp);

    env->ReleaseByteArrayElements(srcData, image, 0);
    return 0;
}

void
openglInit(JNIEnv *env, jobject obj, jint width, jint height){

    LOGD("opengl****  openglInit(%d, %d)", width, height);

    CmOpenGLES_init(width, height, RotationAngle::Angle_0, false);
}

void
openglRelease(JNIEnv *env, jobject obj){

    LOGD("opengl****  openglRelease");
    CmOpenGLES_release();
    if(tempScaleYUV != 0){
        free(tempScaleYUV);
        tempScaleYUV = 0;
    }
    if (renderingY != 0){
        free(renderingY);
        free(renderingU);
        free(renderingV);
        renderingY = 0;
        renderingU = 0;
        renderingV = 0;
    }
    LOGD("opengl****  openglRelease OK !!!");
}

void
openglChangeLayout(JNIEnv *env, jobject obj, jint width, jint height){

    LOGD("opengl****  openglChangeLayout(%d, %d)", width, height);
    CmOpenGLES_changeLayout(width, height);
}

void
openglChangeRotation(JNIEnv *env, jobject obj, jint rotationAngle, jboolean isHorizontalFlip){

    printf("opengl****  openglChangeRotation, rotationAngle = %d", rotationAngle);

    int is_horizontal_flip = (isHorizontalFlip == JNI_TRUE ? 1 : 0);
    RotationAngle rotation_angle = Angle_0;
    if (rotationAngle == 90){
        rotation_angle = Angle_90;
    }else if (rotationAngle == 180){
        rotation_angle = Angle_180;
    }else if (rotationAngle == 270){
        rotation_angle = Angle_270;
    }

    CmOpenGLES_changeRotation(rotation_angle, is_horizontal_flip);
}


void
openglDrawNV21Frame(JNIEnv *env, jobject obj, jbyteArray nv12Data, jint length){

//    LOGD("opengl****  openglDrawFrame");
    if (nv12Data == NULL){
        LOGE("opengl****  openglDrawFrame, nv12Data == NULL");
        return;
    }
    jbyte *nv12yuv = env->GetByteArrayElements(nv12Data, 0);

    CmOpenGLES_drawNV21Frame( nv12yuv, length);

    env->ReleaseByteArrayElements(nv12Data, nv12yuv, 0);
}

void
openglDrawI420Frame(JNIEnv *env, jobject obj, jobject srcObj, jint width, jint height){
    uint8_t *i420Buf = (uint8_t *) env->GetDirectBufferAddress(srcObj);
    int size = width * height;
    CmOpenGLES_drawI420Frame(i420Buf, size, (i420Buf + size), size/4, (i420Buf + size * 5 / 4), size/4);
}

HeadSurface *headSurface = NULL;

jboolean InitHeadSurface(JNIEnv *env, jobject thiz, jstring dataPath) {
    if (dataPath == NULL) {
        LOGE("dataPath is null!");
        return JNI_FALSE;
    }

    const char *str = env->GetStringUTFChars(dataPath, JNI_FALSE);
    headSurface = new HeadSurface(str);
    env->ReleaseStringUTFChars(dataPath, str);

    return JNI_TRUE;
}

jboolean
DepthToMesh(JNIEnv *env, jobject thiz, jstring depth_png, jintArray face_roi, jstring mesh_out) {
    // face_roi -> Rect
    if (headSurface == NULL) {
        LOGE("HeadSurface is not initialized!");
        return JNI_FALSE;
    }

    int length = env->GetArrayLength(face_roi);
    if (length != 4) {
        LOGE("face rect is error!");
        return JNI_FALSE;
    }

    const char *jDepthPng = env->GetStringUTFChars(depth_png, JNI_FALSE);

    jint *jFaceRoi = env->GetIntArrayElements(face_roi, JNI_FALSE);
    Rect rFace(jFaceRoi[0], jFaceRoi[1], jFaceRoi[2], jFaceRoi[3]);

    const char *jMeshOut = env->GetStringUTFChars(mesh_out, JNI_FALSE);

    headSurface->depth_to_mesh(jDepthPng, rFace, jMeshOut);

    // release
    env->ReleaseStringUTFChars(mesh_out, jMeshOut);
    env->ReleaseIntArrayElements(face_roi, jFaceRoi, 0);
    env->ReleaseStringUTFChars(depth_png, jDepthPng);

    return JNI_TRUE;
}

jboolean DepthToMeshAutoFace(JNIEnv *env, jobject thiz, jstring depth_png, jstring color_bmp,
                             jstring mesh_out) {
    if (headSurface == NULL) {
        LOGE("HeadSurface is not initialized");
        return JNI_FALSE;
    }

    const char *jDepthPng = env->GetStringUTFChars(depth_png, JNI_FALSE);

    const char *jColorBmp = env->GetStringUTFChars(color_bmp, JNI_FALSE);

    const char *jMeshOut = env->GetStringUTFChars(mesh_out, JNI_FALSE);

    headSurface->depth_to_mesh_auto_face(jDepthPng, jColorBmp, jMeshOut);

    // release
    env->ReleaseStringUTFChars(mesh_out, jMeshOut);
    env->ReleaseStringUTFChars(color_bmp, jColorBmp);
    env->ReleaseStringUTFChars(depth_png, jDepthPng);

    return JNI_TRUE;
}

jboolean RegisterToBaseMesh(JNIEnv *env, jobject thiz, jstring fn_scanmesh, jstring fn_outmesh,
                            jboolean befemale) {
    if (headSurface == NULL) {
        LOGE("HeadSurface is not initialized");
        return JNI_FALSE;
    }

    const char *jFnScanmesh = env->GetStringUTFChars(fn_scanmesh, JNI_FALSE);

    const char *jFnOutmesh = env->GetStringUTFChars(fn_outmesh, JNI_FALSE);

    headSurface->register_to_basemesh(jFnScanmesh, jFnOutmesh, befemale);

    // release
    env->ReleaseStringUTFChars(fn_scanmesh, jFnScanmesh);
    env->ReleaseStringUTFChars(fn_outmesh, jFnOutmesh);

    return JNI_TRUE;
}

JNINativeMethod jniMethods[] = {
        {"ConvertTORGBA", "(Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;III)I",     (void *) &ConvertTORGBA},
        {"ConvertTORGBA", "([BLjava/nio/ByteBuffer;III)I",     (void *) &ConvertDepthBytesTORGBA},
        {"NV21ToRGB32",   "(II[BLandroid/graphics/Bitmap;Z)I",                    (void *) &NV21ToRGB32},
        {"BMPToBUF",      "(IILjava/nio/ByteBuffer;Landroid/graphics/Bitmap;)I",  (void *) &BMPToBUF},
        {"RGB888TORGBA",  "(Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;III)I",     (void *) &RGB888TORGBA},
        {"CropRGB888",      "(Ljava/nio/ByteBuffer;IILjava/nio/ByteBuffer;IIII)I",  (void *)&CropRGB888},
        {"DrawDepth",     "(Ljava/nio/ByteBuffer;Landroid/graphics/Bitmap;III)I", (void *) &DrawDepth},
        {"DrawColor",     "(Ljava/nio/ByteBuffer;Landroid/graphics/Bitmap;III)I", (void *) &DrawColor},
        {"DrawIR",        "(Ljava/nio/ByteBuffer;Landroid/graphics/Bitmap;III)I", (void *) &DrawIR},

        {"InitDevice",    "(J)I",                                                 (void *) &InitDevice},
        {"setGain",       "(I)I",                                                 (void *) &setGain},
        {"setExposure",   "(S)I",                                                 (void *) &setExposure},
        {"getGain",       "()I",                                                  (void *) &getGain},
        {"getExposure",   "()I",                                                  (void *) &getExposure},
        {"getVersion",   "()I",                                                  (void *) &checkVersion},
        {"CropNV21",      "([BIIILjava/nio/ByteBuffer;IIII)I",  (void *)&CropNV21},
//        jint CropYUYVtoNV21AndI420(JNIEnv *env, jobject, jbyteArray srcYUYVdata, jint width, jint height, jobject dsNv21tBuffer, jobject dstI420Buffer, jint cropX, jint cropY, jint dstWidth, jint dstHeight){

        {"CropYUY2toNV21AndI420",      "(Ljava/nio/ByteBuffer;IILjava/nio/ByteBuffer;Ljava/nio/ByteBuffer;IIII)I",  (void *)&CropYUY2toNV21AndI420},
        {"CropDepth",      "(Ljava/nio/ByteBuffer;IIILjava/nio/ByteBuffer;IIII)I",  (void *)&CropDepth},
        {"rgbaWriteToFile",      "([BIII)I",  (void *)&rgbaWriteToFile},
        {"CropScaleNV21AndRendering",      "([BIIILjava/nio/ByteBuffer;II)I",  (void *)&CropScaleNV21AndRendering},
        {"NV21ToI420",      "([BIIILjava/nio/ByteBuffer;Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)I",  (void *)&NV21ToI420},
        {"ByteBufferCopy",      "(Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;I)I",  (void *)&ByteBufferCopy},
        {"initHeadSurface",     "(Ljava/lang/String;)Z",                                     (void *) InitHeadSurface},
        {"depthToMesh",         "(Ljava/lang/String;[ILjava/lang/String;)Z",                 (void *) DepthToMesh},
        {"depthToMeshAutoFace", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z", (void *) DepthToMeshAutoFace},
        {"registerToBaseMesh",  "(Ljava/lang/String;Ljava/lang/String;Z)Z",                  (void *) RegisterToBaseMesh}
};

JNINativeMethod jniCOpenglesMethods[] = {
        {"init", "(II)V",     (void *) &openglInit},
        {"release",   "()V",                    (void *) &openglRelease},
        {"changeLayout",   "(II)V",                    (void *) &openglChangeLayout},
        {"drawNV21Frame",   "([BI)V",                    (void *) &openglDrawNV21Frame},
        {"changeRotation",   "(IZ)V",                    (void *) &openglChangeRotation},
        {"drawI420Frame",   "(Ljava/nio/ByteBuffer;II)V",                    (void *) &openglDrawI420Frame}
};

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    jclass gCallbackClass = NULL;

    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }



    jclass clz = env->FindClass(REGISTER_CLASS);
    gCallbackClass = (jclass) env->NewGlobalRef(clz);
    env->RegisterNatives(clz, jniMethods, sizeof(jniMethods) / sizeof(JNINativeMethod));
    env->DeleteLocalRef(clz);

    jclass cm_opengles_clz = env->FindClass(REGISTER_CM_OPENGLES_CLASS);
//    gCallbackClass = (jclass) env->NewGlobalRef(clz);
    env->RegisterNatives(cm_opengles_clz, jniCOpenglesMethods, sizeof(jniCOpenglesMethods) / sizeof(JNINativeMethod));
    env->DeleteLocalRef(cm_opengles_clz);

    LOGD("DepthUtils JNI_OnLoad");
    return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (p_Device != NULL) {
        delete p_Device;
        p_Device = NULL;
    }


    return;
}