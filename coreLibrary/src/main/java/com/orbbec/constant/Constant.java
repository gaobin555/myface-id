package com.orbbec.constant;

import android.os.Environment;

import java.io.File;

public class Constant {
    //注册头像的存储路径
    public static final String ImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/faceid_recognition_pic/";
    public static final String FeatureDatabasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/faceid_recognition_db";
    public static final String UserDatabasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/faceid_user_db/";

    public  static String PORT = "ttyS4";//串口号
    public  static  int BAUDRATE = 9600;//波特率
    public  static  String OPENGATE = "AA00010200000800000000000000000B"; // 開門指令

    static {
        File file = new File(Constant.UserDatabasePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(Constant.FeatureDatabasePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    //后置摄像头绘制左右翻转
    public static final boolean backCameraLeftRightReverse = false;
    //特殊设备摄像头绘制左右翻转
    public static boolean specialCameraLeftRightReverse = false;
    //特殊设备摄像头绘制上下翻转
    public static boolean specialCameraTopDownReverse = false;
    //设置非全屏
    public static boolean specialPreviewSize = false;
    //设置特殊识别角度
    public static boolean specialAngle = false;
    //设置是否是双目摄像头
    public static boolean binocularLiveness = false;

    public static final String SPECIAL_CAMERA_LEFT_RIGHT_REVERSE = "SPECIALCAMERALEFTRIGHTREVERSE";
    public static final String SPECIAL_CAMERA_TOP_DOWN_REVERSE = "SPECIALCAMERATOPDOWNREVERSE";
    public static final String SPECIAL_PREVIEW_SIZE = "SPECIALPREVIEWSIZE";
    public static final String SPECIAL_PREVIEW_SIZE_SCALBIT = "SPECIALPREVIEWSIZESCALBIT";
    public static final String SPECIAL_ANGLE = "SPECIALANGLE";
    public static final String SPECIAL_ANGLE_ANGLE = "SPECIALANGLEANGLE";
}
