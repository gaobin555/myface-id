package com.orbbec.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.view.SurfaceView;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import dou.utils.DLog;
import dou.utils.DisplayUtil;
import dou.utils.StringUtils;
import mobile.ReadFace.YMFace;

/**
 *
 * @author mac
 * @date 16/6/23
 */
public class TrackUtil {

    private static final int COUNT = 20;
    private static List<Integer> happy_list = new ArrayList<>();

    public static boolean isHappy(int score) {
        happy_list.add(score);
        if (happy_list.size() <= COUNT) {
            return false;
        }

        int count = 0;
        for (int i = 0; i < happy_list.size(); i++) {
            if (happy_list.get(i) == 1) {
                count++;
            }
        }
        happy_list.remove(0);
        return count > 15;
    }

    /**
     *  储存每张面部表情的集合
     */

    private static List<Integer> emo_list = new ArrayList<>();

    public static void addFace(YMFace face) {
        if (face == null) {
            return;
        }
        emo_list.add(getMaxFromArr(face.getEmotions()));
        if (emo_list.size() > COUNT) {
            emo_list.remove(0);
        }
    }

    private static int getMaxFromArr(float[] arr) {
        int position = 0;
        float max = 0;
        for (int j = 0; j < arr.length; j++) {
            if (max <= arr[j]) {
                max = arr[j];
                position = j;
            }
        }
        return position;
    }


    public static boolean isSmile() {//微笑拍照
        if (emo_list.size() <= GlobalDef.NUMBER_18) {
            return false;
        }
        return countPosition(emo_list) == 0;
    }


    private static int countPosition(List<Integer> integerList) {

        Map<Integer, Integer> map = new HashMap(100);
        for (int i = 0; i < integerList.size(); i++) {
            int position = integerList.get(i);
            Integer count = map.get(position);
            map.put(position, (count == null) ? 1 : count + 1);
        }

        int max = 0;
        int position = 0;

        Iterator<Integer> iter = map.keySet().iterator();

        while (iter.hasNext()) {
            int key = iter.next();
            int value = map.get(key);
            if (max <= value) {
                position = key;
                max = value;
            }
        }
        return position;
    }

    public static void cleanFace() {
        emo_list.clear();
        happy_list.clear();
    }

    public static void startCountDownAnimation(CountDownAnimation countDownAnimation) {
        // Customizable animation
        // Use a set of animations
        Animation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
                0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        countDownAnimation.setAnimation(animationSet);
        countDownAnimation.start();
    }

//    static int colorffList[] = {
//            0xff66CC66,
//            0xffFFCC00,
//            0xff0099CC,
//            0xffFF6699,
//            0xffFF6600,
//            0xffCC0099,
//            0xff0000FF,
//            0xffFF0000
//    };
//    static int colorList[] = {
//            0x4466CC66,
//            0x44FFCC00,
//            0x440099CC,
//            0x44FF6699,
//            0x44FF6600,
//            0x44CC0099,
//            0x440000FF,
//            0x44FF0000
//    };

    static int[] colorffList = {
            0xffffffff
    };
    static int[] colorList = {
            0xffffffff
    };


    public static void drawAnim(List<YMFace> faces, SurfaceView outputView, float scaleBit, int cameraId, boolean isYU) {
        drawAnim(faces, outputView, scaleBit, cameraId, null, false, isYU);
    }

    public static void drawAnim(YMFace face, SurfaceView outputView, float scaleBit, int cameraId, boolean isYU) {
        List<YMFace> faces = new ArrayList<>();
        faces.add(face);
        drawAnim(faces, outputView, scaleBit, cameraId, null, false, isYU);
    }

    static String happystr = "";

    public static void drawAnim(List<YMFace> faces, SurfaceView outputView, float scaleBit, int cameraId, String fps, boolean showPoint, boolean isYU) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Canvas canvas = outputView.getHolder().lockCanvas();

        if (canvas == null) {
            return;
        }
        try {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            int viewW = outputView.getLayoutParams().width;
            int viewH = outputView.getLayoutParams().height;
            if (faces == null || faces.size() == 0) {
                return;
            }
            for (int i = 0; i < faces.size(); i++) {

                int size = DisplayUtil.dip2px(outputView.getContext(), 2);
                paint.setStrokeWidth(size);
                paint.setStyle(Paint.Style.STROKE);
                YMFace ymFace = faces.get(i);

                float[] rect = ymFace.getRect();

                float x1 = viewW - rect[0] * scaleBit - rect[2] * scaleBit;
                if (cameraId == (isYU ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK)) {
                    x1 = rect[0] * scaleBit;
                }
                float y1 = rect[1] * scaleBit;
                float rectWidth = rect[2] * scaleBit;
                paint.setColor(colorList[ymFace.getTrackId() % colorList.length]);
                //draw rect
                RectF rectf = new RectF(x1, y1, x1 + rectWidth, y1 + rectWidth);
                canvas.drawRect(rectf, paint);

                //draw grid

                int line = 10;
                int smailSize = DisplayUtil.dip2px(outputView.getContext(), 1.5f);
                paint.setStrokeWidth(smailSize);
                if (showPoint) {
                    paint.setColor(Color.rgb(57, 138, 243));
                    size = DisplayUtil.dip2px(outputView.getContext(), 2.5f);
                    paint.setStrokeWidth(size);
                    float[] points = ymFace.getLandmarks();
                    for (int j = 0; j < points.length / GlobalDef.NUMBER_2; j++) {
                        float x = viewW - points[j * 2] * scaleBit;
                        if (cameraId == (isYU ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK)) {
                            x = points[j * 2] * scaleBit;
                        }
                        float y = points[j * 2 + 1] * scaleBit;
                        canvas.drawPoint(x, y, paint);
                    }
                }

                if (ymFace.getAge() > 0) {
                    x1 = viewW - rect[0] * scaleBit - rect[2] * scaleBit;
                    if (cameraId == (isYU ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK)) {
                        x1 = rect[0] * scaleBit;
                    }
                    y1 = rect[1] * scaleBit;
                    StringBuffer sb = new StringBuffer();

                    sb.append(ymFace.getGender() == 1 ? "M" : "");
                    sb.append(ymFace.getGender() == 0 ? "F" : "");
                    sb.append("/");
                    sb.append(ymFace.getAge());
                    paint.setColor(colorffList[ymFace.getTrackId() % colorffList.length]);
                    paint.setStrokeWidth(0);
                    paint.setStyle(Paint.Style.FILL);
                    int fontSize = DisplayUtil.dip2px(outputView.getContext(), 20);
                    paint.setTextSize(fontSize);
                    Rect rectText = new Rect();
                    paint.getTextBounds(sb.toString(), 0, sb.toString().length(), rectText);
                    canvas.drawText(sb.toString(), x1, y1 - 40, paint);
                }
            }

            if (!StringUtils.isEmpty(fps)) {
                paint.setColor(Color.RED);
                paint.setStrokeWidth(0);
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.FILL);

                int sizet = DisplayUtil.sp2px(outputView.getContext(), isYU ? 28 : 17);
                paint.setTextSize(sizet);
                canvas.drawText(fps, 20, viewH * 3 / 17, paint);
                DLog.d(fps);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            outputView.getHolder().unlockCanvasAndPost(canvas);
        }
    }


    public static int computingAge(int age) {
        int ran = new Random().nextInt(3) + 3;
        age = (age / ran) * ran;
        return age;
    }

    public static void displayView(Context context, ImageView v, String path) {
        Glide.with(context).load(new File(path)).into(v);
    }

    private static List<Float> limitArr = new ArrayList<>();

    private static int preX, preY;

    public static boolean isTouchable(int limiX, int limiY) {

        int mutix = preX - limiX;
        int mutiy = preY - limiY;
        limitArr.add((float) Math.sqrt(mutix * mutix + mutiy * mutiy));

        preX = limiX;
        preY = limiY;
        if (limitArr.size() > GlobalDef.NUMBER_5) {
            List<Float> temp = new ArrayList<>();
            temp.addAll(limitArr);
            Collections.sort(temp);
            int limit = (int) Math.abs(temp.get(temp.size() - 2) + temp.get(temp.size() - 1));
            temp.clear();
            limitArr.remove(0);
            if (limit < GlobalDef.NUMBER_40) {
                return true;
            }
        }
        return false;
    }

//    public static void checkStoredFace(Context context) {
//        List<User> users = DrawUtil.updateDataSource();
//        YMFaceTrack faceTrack = new YMFaceTrack();
//        faceTrack.initTrack(context, 0, 0);
//        List<Integer> enrolledPersonIds = faceTrack.getEnrolledPersonIds();
//        List<Integer> invalidIds = new ArrayList<>();
//        if (enrolledPersonIds == null) {
//            return;
//        }
//
//        for (int id : enrolledPersonIds) {
//            boolean isExist = false;
//            for (User user : users) {
//                if (Integer.valueOf(user.getPersonId()) == id) {
//                    isExist = true;
//                }
//            }
//            if (!isExist) {
//                invalidIds.add(id);
//                Log.d("checkStoredFace", " invalid id = " + id);
//            }
//        }
//
//        for (int id : invalidIds) {
//            int result = faceTrack.deletePerson(id);
//            Log.d("checkStoredFace", " delete invalid id = " + id + " ; result = " + result);
//        }
//    }

    /**
     * 求方差s^2=[(x1-x)^2 +...(xn-x)^2]/n
     * @param array
     * @return
     */
    public static double variance(short[] array, int length) {
        double sum=0;
        //求和
        for(int i=0;i<length;i++){
            sum+=array[i];

        }
        //求平均值
        double dAve=sum/length;
        double dVar=0;
        //求方差
        for(int i=0;i<length;i++){
            dVar+=(array[i]-dAve)*(array[i]-dAve);
        }
        return dVar/length;
    }

}
