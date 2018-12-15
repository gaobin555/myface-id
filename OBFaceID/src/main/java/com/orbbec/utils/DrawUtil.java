package com.orbbec.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import com.orbbec.base.BaseApplication;
import com.orbbec.keyguard.R;
import com.orbbec.model.User;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dou.utils.DisplayUtil;
import mobile.ReadFace.YMFace;
import static com.orbbec.base.BaseApplication.getAppContext;
import static com.orbbec.keyguard.BaseFacePresenter.IDENTIFY_PERSON_CHECK_SUCCESS;
import static com.orbbec.keyguard.BaseFacePresenter.LIVENESS_STATUS_CHECK_FAIL;
import static com.orbbec.keyguard.BaseFacePresenter.LIVENESS_STATUS_CHECK_INVALID;
import static com.orbbec.keyguard.BaseFacePresenter.LIVENESS_STATUS_CHECK_SUCCESS;


/**
 * @author mac
 * @date 16/8/15
 */
public class DrawUtil {

    private static final String TAG = "DrawUtil";
    private static final boolean DEGUB = false;


    private static boolean isClearDrawed = false;

    static int times = 0;
    static long start;
    static long totalTime;

    private static FaceRect[] faceRectArray = new FaceRect[10];
    private static int strokeWidth;

    /**
     * @param faces
     * @param renderView
     * @param scaleBit     画人脸框的缩放值
     * @param marginLeft   视频view相对于父view的左边距
     * @param marginTop    视频view相对于父view的顶边距
     * @param viewWidth    渲染RGB视频数据的view的跨度
     * @param videoWidth   每帧RGB原始数据源的宽，face的定位坐标是以此为依据的
     * @param currentUser
     * @param livenessStatus
     * @param distance
     */
    public static void drawAnim(List<YMFace> faces, View renderView, boolean toFlip, float scaleBit,
                                int marginLeft, int marginTop, int viewWidth, int videoWidth, String currentUser,
                                String mAge, String happystr, int livenessStatus, float distance) {

        // 当人脸数量为0时清除绘图（仅清一次）
        if (faces == null || faces.size() <= 0) {
            if (!isClearDrawed) {
                Canvas canvas = ((SurfaceView) renderView).getHolder().lockCanvas();
                if (canvas != null) {
                    try {
                        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        ((SurfaceView) renderView).getHolder().unlockCanvasAndPost(canvas);
                    }
                }
            }
            isClearDrawed = true;
            return;
        }
        isClearDrawed = false;

        if (faces.size() > faceRectArray.length) {
            faceRectArray = new FaceRect[faces.size()];
        }

        log("drawAnim: " + renderView.getWidth() + ":" + marginLeft + ";" + marginTop + ":" + viewWidth);
        Paint paint = new Paint();
        Canvas canvas = ((SurfaceView) renderView).getHolder().lockCanvas();
        if (canvas != null) {
            try {

                int viewH = renderView.getHeight();
                int viewW = renderView.getWidth();
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                if (faces == null || faces.size() == 0) {
                    return;
                }
                int maxIndex = 0;

                float maxFaceValue = 0;
                for (int i = 0; i < faces.size(); i++) {
                    if (faces.get(i).getRect()[2] > maxFaceValue) {
                        maxFaceValue = faces.get(i).getRect()[2];
                        maxIndex = i;
                    }
                }

                if (strokeWidth <= 0) {
                    strokeWidth = DisplayUtil.dip2px(getAppContext(), 3);
                }
                for (int i = 0; i < faces.size(); i++) {

                    YMFace ymFace = faces.get(i);
                    paint.setARGB(255, 4, 195, 124);
                    paint.setStrokeWidth(strokeWidth);
                    paint.setStyle(Paint.Style.STROKE);
                    float[] rect = ymFace.getRect();
                    float faceDX = (float) (rect[0] - (scaleBit - 1) * rect[2] / 2.0);
                    float faceDY = (float) (rect[1] - (scaleBit - 1) * rect[3] / 2.0);
                    float faceDWidth = rect[2] * scaleBit;
                    float faceDHeight = rect[3] * scaleBit;

                    float rectWidth = faceDWidth * viewWidth / videoWidth;
                    float rectHeight = faceDHeight * viewWidth / videoWidth;

                    //画外部的框
                    float rectX = marginLeft + faceDX * viewWidth / videoWidth;
                    if (toFlip) {
                        float dx1 = videoWidth - faceDX - faceDWidth;
                        rectX = marginLeft + dx1 * viewWidth / videoWidth;
                    }
                    float rectY = marginTop + faceDY * viewWidth / videoWidth;

                    faceRectArray[i] = new FaceRect(rectX, rectY, rectWidth, rectHeight);

                    //  网格外 图框
                    RectF rectf = new RectF(rectX, rectY, rectX + rectWidth, rectY + rectWidth);
                    canvas.drawRect(rectf, paint);
                    log("drawRect :  (" + (640 - rect[0] - rect[2]) + " , " + rect[1] + ") " + rect[2] + " * " + rect[3]);
                    //  draw grid
                    int line = 10;
                    int smailSize = DisplayUtil.dip2px(getAppContext(), 1.5f);
                    paint.setStrokeWidth(smailSize);


                    paint.setStrokeWidth(strokeWidth);
                    paint.setColor(Color.WHITE);
                    //                    注意前置后置摄像头问题

                    float length = faceDHeight / 5;
                    float width = rectWidth;
                    float heng = strokeWidth / 2;
                    canvas.drawLine(rectX - heng, rectY, rectX + length, rectY, paint);
                    canvas.drawLine(rectX, rectY - heng, rectX, rectY + length, paint);

                    rectX = rectX + width;
                    canvas.drawLine(rectX + heng, rectY, rectX - length, rectY, paint);
                    canvas.drawLine(rectX, rectY - heng, rectX, rectY + length, paint);

                    rectY = rectY + width;
                    canvas.drawLine(rectX + heng, rectY, rectX - length, rectY, paint);
                    canvas.drawLine(rectX, rectY + heng, rectX, rectY - length, paint);

                    rectX = rectX - width;
                    canvas.drawLine(rectX - heng, rectY, rectX + length, rectY, paint);
                    canvas.drawLine(rectX, rectY + heng, rectX, rectY - length, paint);

                    //todo  在这里判断一下最大脸设置  画下字  活体检测失败还没有处理。
                    if (i == maxIndex) {

                        paint.setColor(Color.WHITE);
                        paint.setStrokeWidth(0);
                        paint.setStyle(Paint.Style.FILL);
                        int fontSize = DisplayUtil.dip2px(getAppContext(), 20);
                        paint.setTextSize(fontSize);
                        Paint.FontMetrics fm = paint.getFontMetrics();
                        int textHeight = (int) (Math.ceil(fm.descent - fm.ascent) + 2);
                        FaceRect maxFaceRect = faceRectArray[maxIndex];
                        float faceMaxX = maxFaceRect.x;
                        float faceMaxY = maxFaceRect.y;
                        float faceMaxWidth = maxFaceRect.width;
                        float faceMaxHeight = maxFaceRect.height;

                        log("drawAnim: currentUser" + currentUser);

                        float rowSpacing = 1;
                        float startX = faceMaxX + faceMaxWidth + 20;
                        float startY = faceMaxY + (faceMaxHeight - (rowSpacing + textHeight) * (distance > 0.1 ? 3 : 2)) / 2 + rowSpacing + strokeWidth;
                        startY += 4;

                        Bitmap bm = BitmapFactory.decodeResource(getAppContext().getResources(), R.drawable.canvas_bg);
                        canvas.drawBitmap(bm, startX - 10, startY - (rowSpacing + textHeight) + 4, paint);


                        if (currentUser != null) {
                            canvas.drawText(currentUser, startX, startY, paint);
                        } else {
                            canvas.drawText(currentUser, startX, startY, paint);
                        }

                        if (mAge != null) {
                            canvas.drawText(mAge + "岁", startX, startY + (rowSpacing + textHeight), paint);
                        }

                        if (happystr != null) {
                            canvas.drawText(happystr, startX, startY + 2 * (rowSpacing + textHeight), paint);
                        }

                        if (distance > 0.1) {
                            canvas.drawText(String.format("%.2f", distance / 1000) + "米", startX, startY + 3 * (rowSpacing + textHeight), paint);
                        }
                        maxIndex = 0;

                        String checktStatus = null;
                        if (livenessStatus == LIVENESS_STATUS_CHECK_SUCCESS) {
                            checktStatus = getAppContext().getString(R.string.liveness_check_success);
                            paint.setColor(Color.GREEN);
                        } else if (livenessStatus == LIVENESS_STATUS_CHECK_FAIL) {
                            checktStatus = getAppContext().getString(R.string.liveness_check_fail);
                            paint.setColor(Color.RED);
                        } else if (livenessStatus == LIVENESS_STATUS_CHECK_INVALID) {
                            checktStatus = getAppContext().getString(R.string.liveness_check_invalid);
                            paint.setColor(Color.WHITE);
                        } else if (livenessStatus == IDENTIFY_PERSON_CHECK_SUCCESS) {
                            checktStatus = getAppContext().getString(R.string.identify_success);
                            paint.setColor(Color.GREEN);
                        }
                        else {
                            //                            checktStatus = "";  // 检测中
                        }
                        if (checktStatus != null) {
                            Rect strRect = new Rect();
                            paint.getTextBounds(checktStatus, 0, 1, strRect);
                            float strwid = paint.measureText(checktStatus);
                            float strhei = strRect.height();

                            float strX = faceMaxX + faceMaxWidth / 2 - strwid / 2;
                            float strY = faceMaxY + faceMaxHeight + strhei + 4;

                            canvas.drawText(checktStatus, strX, strY, paint);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ((SurfaceView) renderView).getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }

    public static void drawRect(List<YMFace> faces, View renderView, boolean toFlip, float scaleBit,
                                int marginLeft, int marginTop, int viewWidth, int videoWidth) {

        // 当人脸数量为0时清除绘图（仅清一次）
        if (faces == null || faces.size() <= 0) {
            if (!isClearDrawed) {
                Canvas canvas = ((SurfaceView) renderView).getHolder().lockCanvas();
                if (canvas != null) {
                    try {
                        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        ((SurfaceView) renderView).getHolder().unlockCanvasAndPost(canvas);
                    }
                }
            }
            isClearDrawed = true;
            return;
        }
        isClearDrawed = false;

        if (faces.size() > faceRectArray.length) {
            faceRectArray = new FaceRect[faces.size()];
        }

        log("drawAnim: " + renderView.getWidth() + ":" + marginLeft + ";" + marginTop + ":" + viewWidth);
        Paint paint = new Paint();
        Canvas canvas = ((SurfaceView) renderView).getHolder().lockCanvas();
        if (canvas != null) {
            try {

                int viewH = renderView.getHeight();
                int viewW = renderView.getWidth();
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                if (faces == null || faces.size() == 0) {
                    return;
                }
                int maxIndex = 0;

                float maxFaceValue = 0;
                for (int i = 0; i < faces.size(); i++) {
                    if (faces.get(i).getRect()[2] > maxFaceValue) {
                        maxFaceValue = faces.get(i).getRect()[2];
                        maxIndex = i;
                    }
                }

                if (strokeWidth <= 0) {
                    strokeWidth = DisplayUtil.dip2px(getAppContext(), 3);
                }
                for (int i = 0; i < faces.size(); i++) {

                    YMFace ymFace = faces.get(i);
                    paint.setARGB(255, 4, 195, 124);
                    paint.setStrokeWidth(strokeWidth);
                    paint.setStyle(Paint.Style.STROKE);
                    float[] rect = ymFace.getRect();
                    float faceDX = (float) (rect[0] - (scaleBit - 1) * rect[2] / 2.0);
                    float faceDY = (float) (rect[1] - (scaleBit - 1) * rect[3] / 2.0);
                    float faceDWidth = rect[2] * scaleBit;
                    float faceDHeight = rect[3] * scaleBit;

                    float rectWidth = faceDWidth * viewWidth / videoWidth;
                    float rectHeight = faceDHeight * viewWidth / videoWidth;

                    //画外部的框
                    float rectX = marginLeft + faceDX * viewWidth / videoWidth;
                    if (toFlip) {
                        float dx1 = videoWidth - faceDX - faceDWidth;
                        rectX = marginLeft + dx1 * viewWidth / videoWidth;
                    }
                    float rectY = marginTop + faceDY * viewWidth / videoWidth;

                    faceRectArray[i] = new FaceRect(rectX, rectY, rectWidth, rectHeight);

                    //  网格外 图框
                    RectF rectf = new RectF(rectX, rectY, rectX + rectWidth, rectY + rectWidth);
                    canvas.drawRect(rectf, paint);
                    log("drawRect :  (" + (640 - rect[0] - rect[2]) + " , " + rect[1] + ") " + rect[2] + " * " + rect[3]);
                    //  draw grid
                    int line = 10;
                    int smailSize = DisplayUtil.dip2px(getAppContext(), 1.5f);
                    paint.setStrokeWidth(smailSize);


                    paint.setStrokeWidth(strokeWidth);
                    paint.setColor(Color.WHITE);
                    //                    注意前置后置摄像头问题

                    float length = faceDHeight / 5;
                    float width = rectWidth;
                    float heng = strokeWidth / 2;
                    canvas.drawLine(rectX - heng, rectY, rectX + length, rectY, paint);
                    canvas.drawLine(rectX, rectY - heng, rectX, rectY + length, paint);

                    rectX = rectX + width;
                    canvas.drawLine(rectX + heng, rectY, rectX - length, rectY, paint);
                    canvas.drawLine(rectX, rectY - heng, rectX, rectY + length, paint);

                    rectY = rectY + width;
                    canvas.drawLine(rectX + heng, rectY, rectX - length, rectY, paint);
                    canvas.drawLine(rectX, rectY + heng, rectX, rectY - length, paint);

                    rectX = rectX - width;
                    canvas.drawLine(rectX - heng, rectY, rectX + length, rectY, paint);
                    canvas.drawLine(rectX, rectY + heng, rectX, rectY - length, paint);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ((SurfaceView) renderView).getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * 判断是否有中文
     *
     * @param c
     * @return
     */
    private static final boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    public static final boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    private static void log(String str) {
        if (DEGUB) {
            Log.e(TAG, str);
        }
    }

    static class FaceRect {
        float x;
        float y;
        float width;
        float height;

        public FaceRect(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public static float pxToDp(float px) {
        return px / getDensityScalar();
    }

    private static float getDensityScalar() {
        return BaseApplication.getAppContext().getResources().getDisplayMetrics().density;
    }

}
