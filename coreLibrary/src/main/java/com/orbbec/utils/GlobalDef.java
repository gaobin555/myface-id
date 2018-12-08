package com.orbbec.utils;

/**
 * @author zlh
 */
public class GlobalDef {

    public static final boolean FPS_ON = false;
    public static String PACKAGE_NAME;

    /**
     * 默认RGB摄像头分辨率
     */
    protected static final int RES_DEFAULT_COLOR_WIDTH = 640;
    protected static final int RES_DEFAULT_COLOR_HEIGHT = 480;

    /**
     * 默认深度图分辨率
     */
    protected static final int RES_DEFAULT_DEPTH_WIDTH = 640;
    protected static final int RES_DEFAULT_DEPTH_HEIGHT = 480;

    public int getColorWidth() {
        return RES_DEFAULT_COLOR_WIDTH;
    }

    public int getColorHeight() {
        return RES_DEFAULT_COLOR_HEIGHT;
    }

    public int getDepthWidth() {
        return RES_DEFAULT_DEPTH_WIDTH;
    }

    public int getDepthHeight() {
        return RES_DEFAULT_DEPTH_HEIGHT;
    }

    /**
     * FixMe：关于朵朵分辨率跟RGB分辨率不同的问题的备注：
     * 1. 当RGB分辨率为640*480，depth为640*400的时候，RGB和depth是左上角对齐的，RGB的底部80个像素点是没有对应的depth数据的
     * 2. 所以会将RGB的底部80像素裁掉
     */
    public static final int RES_DUODUO_DEPTH_WIDTH = 640;
    public static final int RES_DUODUO_DEPTH_HEIGHT = 400;

    /**
     * 检测人脸区域的偏移量
     */
    public static final int OFFSET_PIXEL = 50;
    public static final int RES_FPS = 30;
    public static int UNREGISTERED = -111;
    public static int FACE_HAPPEY = 40;

    public static final int NOT_FOUND_CAMERA = -1;
    public static final int OPEN_CAMERA_FAILE = -2;

    public static final boolean SAVE_RECOGNITION = false;

    /**
     * 最远检测距离 单位：mm
     */
    public static final int MAX_DISTANCE = 1500;
    public static final int ASTRO_MIX_DISTANCE = 350;
    /**
     * 最近距离 单位：mm
     */
    public static final int PRO_MIX_DISTANCE = 500;
    /**
     * 单位：ms
     */
    public static final int GLOBAL_STREAM_TIMEOUT = 2000;
    public static final int GLOBAL_DELAY = 2000;
    public static final int GLOBAL_THREAD_SLEEP = 50;

    public static final int GLOBAL_ANIMAL_ROTATE_DURATION = 8000;
    public static final int GLOBAL_ANIMAL_TRANSLATE_DURATION = 3000;
    public static final float GLOBAL_DEFAULT_BALANCE = 9999.00f;
    public static final float GLOBAL_INIT_BALANCE = -9999.00f;

    /**
     * 活体检测最大连续失败次数 3次
     */
    public static int MAX_FAIL_COUNT = 3;

    /**
     * 连续正脸次数
     */
    public static int MAX_Andy_COUNT = 5;

    /**
     * 连续注册失败次数
     */
    public static int MAX_REGISTER_FAIL_COUNT = 5;
    public static boolean PLAY_ANIMATION = true;
    public static boolean MIRROR_UVC = true;

    public static boolean DISPLAY_3D_FACE = false;

    /**
     * 没有检测到人脸多久后关闭深度流
     */
    public static final long NO_FACE_STOP_DEPTH_IN_TIME = 2 * 60 * 1000;

    /**
     * PictureMangerAct 标识
     */
    public static final String MARK_PICTUREMANGERACT = "PictureMangerAct";

    public static final String EQUALS_ = "_";
    public static final String EQUALS_2 = "-";
    public static final String STRING_NULL = "";

    public static final String VERTEX = "vertex";
    public static final String FACE = "face";
    public static final String JPG = "jpg";
    public static final String PNG = "png";
    public static final String DATA = "DATA";

    public static final int NUMBER_90 = 90;

    public static final int NUMBER_60 = 60;
    public static final int NUMBER_0 = 0;
    public static final int NUMBER_1 = 1;
    public static final int NUMBER_2 = 2;
    public static final int NUMBER_3 = 3;
    public static final int NUMBER_4 = 4;
    public static final int NUMBER_5 = 5;
    public static final int NUMBER_10 = 10;
    public static final int NUMBER_18 = 18;
    public static final int NUMBER_20 = 20;
    public static final int NUMBER_25 = 25;
    public static final int NUMBER_30 = 30;
    public static final int NUMBER_40 = 40;
    public static final int NUMBER_80 = 80;

    public static final int NUMBER_111 = -111;
    public static final double NUMBER_DB = 0.0001;
    public static final float NUMBER_OF = 0f;

    public static final String DATE_STYLE_MM = "yyyy-MM-dd HH:mm";
    public static final String DATE_STYLE_DD = "yyyy-MM-dd";


    public static final int Deeyea = 0x060b;
    public static final int P2 = 0x0609;

    public static final int ASTER_PRO = 0x0403;
    public static final int DUO_DUO = 0x0402;
    public static final int ASTER = 0x0401;
    public static final int CANGLONG = 0x0608;
    /**
     * lunna产品深度640*400 彩色640*480 和朵朵一致。产品id 0x0404
     * 深度有效距离是0.25~1.0米
     */
    public static final int LUNA_DVT2 = 0x0404;
    /**
     * 产品 ID
     * vendorId
     * productId
     * 整理各种设备 id 区分类别。
     * <p>
     * VENDORID = 0x1D27 未知设备(早期奥比或者其他家设备)
     * VENDORID = 0x2BC5 奥比摄像头id
     * <p>
     * productId 04 Mx400
     * 05 RGB 松翰uvc
     * 06 Mx6000
     */
    public static final int VENDORID_0x1D2 = 0x1D27;
    public static final int VENDORID_0_x2BC5 = 0x2BC5;

    public static final int PRODUCTID_0401 = 0x0401;
    public static final int PRODUCTID_04FF = 0x04FF;
    public static final int PRODUCTID_0501 = 0x0501;
    public static final int PRODUCTID_05FF = 0x05FF;
    public static final int PRODUCTID_0601 = 0x0601;
    public static final int PRODUCTID_06FF = 0x06FF;

    public static final int PRODUCTID_05FC = 0x05FC;


}