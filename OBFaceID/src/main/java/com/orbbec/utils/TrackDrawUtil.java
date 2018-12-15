package com.orbbec.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;

import com.orbbec.base.BaseApplication;
import com.orbbec.constant.Constant;
import com.orbbec.model.User;

import java.util.List;
import java.util.Map;

import dou.utils.DisplayUtil;
import mobile.ReadFace.YMFace;


/**
 * Created by qyg on 2018/10/15.
 */
public class TrackDrawUtil {

    private static Paint paint;

    /**
     * 绘制人脸框、关键点、性别&年龄属性
     *
     * @param faces
     * @param outputView
     * @param scale_bit
     * @param cameraId
     * @param fps
     * @param showPoint
     */
    public static void drawFaceTracking(List<YMFace> faces, SurfaceView outputView, float scale_bit, String fps, boolean showPoint) {
        Canvas canvas = outputView.getHolder().lockCanvas();
        if (canvas == null) {
            return;
        }
        if (paint == null) {
            paint = new Paint();
            paint.setAntiAlias(true);
        }
        try {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            int viewW = outputView.getLayoutParams().width;
            int viewH = outputView.getLayoutParams().height;
            if (faces == null || faces.size() == 0) {
                return;
            }
            for (int i = 0; i < faces.size(); i++) {
                YMFace ymFace = faces.get(i);
                float[] rect = ymFace.getRect();
                float x1 = viewW - rect[0] * scale_bit - rect[2] * scale_bit;

                float y1 = rect[1] * scale_bit;
                float rect_width = rect[2] * scale_bit;

                int size = DisplayUtil.dip2px(BaseApplication.getContext(), 2);
                paint.setStrokeWidth(size);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.WHITE);
                RectF rectf = new RectF(x1, y1, x1 + rect_width, y1 + rect_width);
                canvas.drawRect(rectf, paint);
                if (ymFace.getAge() > 0) {//绘制性别、年龄
                    StringBuffer sb = new StringBuffer();
                    sb.append(ymFace.getGender() == 1 ? "M " : "");
                    sb.append(ymFace.getGender() == 0 ? "F " : "");
                    sb.append("/");
                    sb.append(ymFace.getAge() + " ");

                    paint.setStrokeWidth(0);
                    paint.setStyle(Paint.Style.FILL);
                    int fontSize = DisplayUtil.dip2px(BaseApplication.getContext(), 20);
                    paint.setTextSize(fontSize);
                    Rect rect_text = new Rect();
                    paint.getTextBounds(sb.toString(), 0, sb.toString().length(), rect_text);
                    canvas.drawText(sb.toString(), x1, y1 - 40, paint);
                }
                if (showPoint) {//绘制人脸关键点
                    paint.setColor(Color.rgb(57, 138, 243));
                    size = DisplayUtil.dip2px(BaseApplication.getContext(), 2.5f);
                    paint.setStrokeWidth(size);
                    float[] points = ymFace.getLandmarks();
                    for (int j = 0; j < points.length / 2; j++) {
                        float x = viewW - points[j * 2] * scale_bit;
                        float y = points[j * 2 + 1] * scale_bit;
                        if (Constant.specialCameraLeftRightReverse) {//特殊设备，需要额外左右翻转
                            if (x == points[j * 2] * scale_bit) {
                                x = viewW - points[j * 2] * scale_bit;
                            } else {
                                x = points[j * 2] * scale_bit;
                            }
                        }
                        if (Constant.specialCameraTopDownReverse) {//特殊设备，需要额外上下翻转
                            y = viewH - points[j * 2 + 1] * scale_bit;
                        }
                        canvas.drawPoint(x, y, paint);
                    }
                }
            }
            if (!TextUtils.isEmpty(fps)) {//绘制fps
                drawFps(fps, canvas, viewH);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            outputView.getHolder().unlockCanvasAndPost(canvas);
        }
    }

    /**
     * 绘制活体
     *
     * @param faces
     * @param outputView
     * @param scale_bit
     * @param cameraId
     * @param fps
     */
    public static void drawFaceLiveness(List<YMFace> faces, SurfaceView outputView, float scale_bit, String fps) {
        Canvas canvas = outputView.getHolder().lockCanvas();
        if (canvas == null) {
            return;
        }
        if (paint == null) {
            paint = new Paint();
            paint.setAntiAlias(true);
        }
        try {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            int viewW = outputView.getLayoutParams().width;
            int viewH = outputView.getLayoutParams().height;
            if (faces == null || faces.size() == 0) {
                return;
            }
            for (int i = 0; i < faces.size(); i++) {
                YMFace ymFace = faces.get(i);
                float[] rect = ymFace.getRect();
                float x1 = viewW - rect[0] * scale_bit - rect[2] * scale_bit;

                float y1 = rect[1] * scale_bit;

                float rect_width = rect[2] * scale_bit;
                RectF rectf = new RectF(x1, y1, x1 + rect_width, y1 + rect_width);
                int size = DisplayUtil.dip2px(BaseApplication.getContext(), 2);
                paint.setStrokeWidth(size);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.WHITE);
                canvas.drawRect(rectf, paint);

                float[] headposes = ymFace.getHeadpose();
                StringBuffer sb = new StringBuffer();
                if ((Math.abs(headposes[0]) > 20 || Math.abs(headposes[1]) > 20 || Math.abs(headposes[2]) > 20)) {//判断人脸角度
                    sb.append("请正脸面对");
                } else {
                    paint.setColor(Color.RED);
                    if (ymFace.getFaceQuality() < 6) {//判断人脸质量
                        sb.append("人脸质量差 " + ymFace.getFaceQuality());
                    } else {
                        if (ymFace.getLiveness() != -1) { // 活体检测
                            if (ymFace.getLiveness() == 1) {
                                sb.append(ymFace.getTrackId() + " 活体检测通过");
                                paint.setColor(Color.BLUE);
                            } else if (ymFace.getLiveness() == 0) {
                                sb.append(ymFace.getTrackId() + " 活体检测失败");

                            } else {
                                sb.append("");
                            }
                        }
                    }
                }
                paint.setStrokeWidth(0);
                paint.setStyle(Paint.Style.FILL);
                int fontSize = DisplayUtil.dip2px(BaseApplication.getContext(), 20);
                paint.setTextSize(fontSize);
                Rect rect_text = new Rect();
                paint.getTextBounds(sb.toString(), 0, sb.toString().length(), rect_text);
                canvas.drawText(sb.toString(), x1, y1 - 40, paint);
            }
            if (!TextUtils.isEmpty(fps)) {//绘制fps
                drawFps(fps, canvas, viewH);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            outputView.getHolder().unlockCanvasAndPost(canvas);
        }
    }

    /**
     * 绘制人脸识别
     *
     * @param faces
     * @param outputView
     * @param scale_bit
     * @param cameraId
     * @param fps
     * @param userMap
     */
    public static void drawFaceRecognition(List<YMFace> faces, View outputView, float scale_bit, String fps, Map<Integer, User> userMap) {
        Paint paint = new Paint();
        Canvas canvas = ((SurfaceView) outputView).getHolder().lockCanvas();
        if (canvas == null) {
            return;
        }
        if (paint == null) {
            paint = new Paint();
            paint.setAntiAlias(true);
        }
        try {
            int viewH = outputView.getHeight();
            int viewW = outputView.getWidth();
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            if (faces == null || faces.size() == 0) return;
            for (int i = 0; i < faces.size(); i++) {
                YMFace ymFace = faces.get(i);
                float[] rect = ymFace.getRect();
                float x1 = viewW - rect[0] * scale_bit - rect[2] * scale_bit;

                float y1 = rect[1] * scale_bit;

                float rect_width = rect[2] * scale_bit;
                RectF rectf = new RectF(x1, y1, x1 + rect_width, y1 + rect_width);
                int size = DisplayUtil.dip2px(BaseApplication.getContext(), 3);
                paint.setStrokeWidth(size);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(0x44ffffff);
                canvas.drawRect(rectf, paint);
                //draw grid
                int line = 10;
                int per_line = (int) (rect_width / (line + 1));
                int smailSize = DisplayUtil.dip2px(BaseApplication.getContext(), 1.5f);
                paint.setStrokeWidth(smailSize);
                for (int j = 1; j < line + 1; j++) {
                    canvas.drawLine(x1 + per_line * j, y1, x1 + per_line * j, y1 + rect_width, paint);
                    canvas.drawLine(x1, y1 + per_line * j, x1 + rect_width, y1 + per_line * j, paint);
                }
                //draw horn
                paint.setStrokeWidth(size);
                paint.setColor(Color.WHITE);
                float x2 = viewW - rect[0] * scale_bit - rect[2] * scale_bit;

                if (Constant.specialCameraLeftRightReverse) {//特殊设备，需要额外左右翻转
                    if (x2 == viewW - rect[0] * scale_bit - rect[2] * scale_bit) {
                        x2 = rect[0] * scale_bit;//后置摄像头翻转
                    } else {
                        x2 = viewW - rect[0] * scale_bit - rect[2] * scale_bit;
                    }
                }
                float y2 = rect[1] * scale_bit;
                if (Constant.specialCameraTopDownReverse) {//特殊设备，需要额外上下翻转
                    y2 = viewH - rect[1] * scale_bit - rect[3] * scale_bit;
                }

                float length = rect[3] * scale_bit / 5;
                float width = rect[3] * scale_bit;
                float offset = size / 2;
                canvas.drawLine(x2 - offset, y2, x2 + length, y2, paint);
                canvas.drawLine(x2, y2 - offset, x2, y2 + length, paint);

                x2 = x2 + width;
                canvas.drawLine(x2 + offset, y2, x2 - length, y2, paint);
                canvas.drawLine(x2, y2 - offset, x2, y2 + length, paint);

                y2 = y2 + width;
                canvas.drawLine(x2 + offset, y2, x2 - length, y2, paint);
                canvas.drawLine(x2, y2 + offset, x2, y2 - length, paint);

                x2 = x2 - width;
                canvas.drawLine(x2 - offset, y2, x2 + length, y2, paint);
                canvas.drawLine(x2, y2 + offset, x2, y2 - length, paint);

                //绘制识别出的人，注册名称
                int personId = ymFace.getPersonId();
                StringBuilder sb = new StringBuilder();
                if (personId > 0 && userMap.containsKey(personId)) {
                    User user = userMap.get(personId);
                    String name = user.getName();
                    String gender = user.getGender();
                    String age = user.getAge();
                    String score = user.getScore();

                    if (TextUtils.isEmpty(name)) {
                        sb.append("id=").append(personId).append("   ");
                    } else {
                        if (isChinese(name)) {
                            if (name.length() > 4) {
                                name = name.substring(0, 4) + "…";
                            }
                        } else {
                            if (name.length() > 10) {
                                name = name.substring(0, 10) + "…";
                            }
                        }
                        sb.append(name).append("  ");
                    }
                    if (!TextUtils.isEmpty(gender))
                        sb.append(gender).append("/");
                    if (!TextUtils.isEmpty(age))
                        sb.append(age);
                }
                paint.setColor(Color.WHITE);
                paint.setStrokeWidth(0);
                paint.setStyle(Paint.Style.FILL);
                int fontSize = DisplayUtil.dip2px(BaseApplication.getContext(), 20);
                paint.setTextSize(fontSize);
                canvas.drawText(sb.toString(), x1, y1 - 30, paint);
            }
            if (!TextUtils.isEmpty(fps)) {//绘制fps
                drawFps(fps, canvas, viewH);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ((SurfaceView) outputView).getHolder().unlockCanvasAndPost(canvas);
        }
    }

    /**
     * 绘制双目活体(红外人脸框)
     *
     * @param faces
     * @param outputView
     * @param scale_bit
     * @param cameraId
     * @param fps
     * @param userMap
     */
    public static void drawBinocularLiveness(List<YMFace> faces, View outputView, float scale_bit, String fps, Map<Integer, User> userMap) {
        Paint paint = new Paint();
        Canvas canvas = ((SurfaceView) outputView).getHolder().lockCanvas();
        if (canvas == null) {
            return;
        }
        if (paint == null) {
            paint = new Paint();
            paint.setAntiAlias(true);
        }
        try {
            int viewH = outputView.getHeight();
            int viewW = outputView.getWidth();
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            if (faces == null || faces.size() == 0) return;
            for (int i = 0; i < faces.size(); i++) {
                YMFace ymFace = faces.get(i);
                float[] rect = ymFace.getRect();
                float x1 = viewW - rect[0] * scale_bit - rect[2] * scale_bit;
                float y1 = rect[1] * scale_bit;
                if (Constant.specialCameraTopDownReverse) {//特殊设备，需要额外上下翻转
                    y1 = viewH - rect[1] * scale_bit - rect[3] * scale_bit;
                }
                float rect_width = rect[2] * scale_bit;
                RectF rectf = new RectF(x1, y1, x1 + rect_width, y1 + rect_width);
                int size = DisplayUtil.dip2px(BaseApplication.getContext(), 3);
                paint.setStrokeWidth(size);
                paint.setStyle(Paint.Style.STROKE);
                if (ymFace.getLiveness() == 1) {//活体通过
                    paint.setColor(Color.GREEN);
                } else {//
                    paint.setColor(Color.RED);
                }
                canvas.drawRect(rectf, paint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ((SurfaceView) outputView).getHolder().unlockCanvasAndPost(canvas);
        }
    }


    /**
     * 绘制fps
     *
     * @param fps
     * @param canvas
     * @param viewH
     */
    private static void drawFps(String fps, Canvas canvas, int viewH) {
        if (paint == null) {
            paint = new Paint();
            paint.setAntiAlias(true);
        }
        paint.setColor(Color.RED);
        paint.setStrokeWidth(0);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        int sizet = DisplayUtil.sp2px(BaseApplication.getContext(), Constant.backCameraLeftRightReverse ? 28 : 17);
        paint.setTextSize(sizet);
        canvas.drawText(fps, 20, viewH * 3 / 17, paint);
    }

    /**
     * 判断是否有中文
     */
    private static boolean isChinese(char c) {
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

    /**
     * 判断是否有中文
     */
    private static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }
}
