package com.orbbec.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author jjchai
 * @date 2017/8/17
 */

public class Util {

    private static final int OB_VID = 0x2BC5;
    private static final int OB_START_PID = 0x0401;
    private static final int OB_END_PID = 0x0410;


    /**
     * dp转换成px
     */
    public static int dp2px(Context context,float dpValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }

    /**
     * px转换成dp
     */
    public static int px2dp(Context context,float pxValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(pxValue/scale+0.5f);
    }

    /**
     * sp转换成px
     */
    public static int sp2px(Context context,float spValue){
        float fontScale=context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue*fontScale+0.5f);
    }

    /**
     * px转换成sp
     */
    public static int px2sp(Context context, float pxValue){
        float fontScale=context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue/fontScale+0.5f);
    }


    public static String generateName() {
        String name = "" + System.currentTimeMillis();
        int length = name.length();
        if (length > 11) {
            name = name.substring(0, 11);
        } else if (length < 11) {
            for (int i = 0; i < (11 - length); i++) {
                name += "0";
            }
        }
        return name;
    }

    /**
     * 从asset文件夹拷贝文件到data目录
     * @param context
     * @param dataPath
     * @param fileName
     */
    public static void copyFileFromAsset(Context context, String dataPath, String fileName) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = context.getAssets().open("data" + File.separator + fileName);
            String filePath = dataPath + File.separator + fileName;
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            os = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            while (is.read(buffer) != 0) {
                os.write(buffer);
            }
        } catch (Exception e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
    public static boolean isOursDevices(Context ctx) {

        UsbManager manager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> iterator = deviceList.values().iterator();
        while (iterator.hasNext()) {
            UsbDevice usbDevice = iterator.next();
            boolean obDevice = usbDevice.getVendorId() == OB_VID && (usbDevice.getProductId() <= OB_END_PID && usbDevice.getProductId() >= OB_START_PID);
            if (obDevice) {
                return true;
            }
        }
        return false;
    }

    /**
     * 当前时间是否超过约定时间
     *
     * @return
     */
    public static boolean compareTime() {
        try {
            return System.currentTimeMillis() >= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2017-10-1 0:0:0").getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void saveBitmapToBMP(Bitmap bitmap, String filename) {
        if (bitmap == null) {
            return;
        }
        int nBmpWidth = bitmap.getWidth();
        int nBmpHeight = bitmap.getHeight();
        int bufferSize = nBmpHeight * (nBmpWidth * 3 + nBmpWidth % 4);
        try {
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(filename);
            //bmp文件头
            int bfType = 0x4d42;
            long bfSize = 14 + 40 + bufferSize;
            int bfReserved1 = 0;
            int bfReserved2 = 0;
            long bfOffBits = 14 + 40;
            //保存文件头
            writeWord(outputStream, bfType);
            writeDword(outputStream, bfSize);
            writeWord(outputStream, bfReserved1);
            writeWord(outputStream, bfReserved2);
            writeDword(outputStream, bfOffBits);
            //bmp信息头
            long biSize = 40L;
            long biWidth = nBmpWidth;
            long biHeight = nBmpHeight;
            int biPlanes = 1;
            int biBitCount = 24;
            long biCompression = 0L;
            long biSizeImage = 0L;
            long biXpelsPerMeter = 0L;
            long biYPelsPerMeter = 0L;
            long biClrUsed = 0L;
            long biClrImportant = 0L;
            // 保存bmp信息头
            writeDword(outputStream, biSize);
            writeLong(outputStream, biWidth);
            writeLong(outputStream, biHeight);
            writeWord(outputStream, biPlanes);
            writeWord(outputStream, biBitCount);
            writeDword(outputStream, biCompression);
            writeDword(outputStream, biSizeImage);
            writeLong(outputStream, biXpelsPerMeter);
            writeLong(outputStream, biYPelsPerMeter);
            writeDword(outputStream, biClrUsed);
            writeDword(outputStream, biClrImportant);
            // 像素扫描
            byte[] bmpData = new byte[bufferSize];
            int wWidth = (nBmpWidth * 3 + nBmpWidth % 4);
            for (int nCol = 0, nRealCol = nBmpHeight - 1; nCol < nBmpHeight; ++nCol, --nRealCol) {
                for (int wRow = 0, wByteIdex = 0; wRow < nBmpWidth; wRow++, wByteIdex += GlobalDef.NUMBER_3) {
                    int clr = bitmap.getPixel(wRow, nCol);
                    bmpData[nRealCol * wWidth + wByteIdex] = (byte) Color.blue(clr);
                    bmpData[nRealCol * wWidth + wByteIdex + 1] = (byte) Color.green(clr);
                    bmpData[nRealCol * wWidth + wByteIdex + 2] = (byte) Color.red(clr);
                }
            }
            outputStream.write(bmpData);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void writeWord(FileOutputStream stream, int value) throws IOException {
        byte[] b = new byte[2];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        stream.write(b);
    }

    private static void writeDword(FileOutputStream stream, long value) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        b[2] = (byte) (value >> 16 & 0xff);
        b[3] = (byte) (value >> 24 & 0xff);
        stream.write(b);
    }

    private static void writeLong(FileOutputStream stream, long value) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        b[2] = (byte) (value >> 16 & 0xff);
        b[3] = (byte) (value >> 24 & 0xff);
        stream.write(b);
    }

    /**
     * 计算从大的bitmap图片中裁切出一个子图的合法位置（不越界，大于0）
     *
     * @param rect
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static int[] getLegalSubImageRect(float[] rect, int maxWidth, int maxHeight) {
        int[] dstRect = new int[4];
        int x = (int) rect[0];
        int y = (int) rect[1];
        int width = (int) rect[2];
        int height = (int) rect[3];
        if (x < 0) {
            x = 0;
        }
        if (width + x > maxWidth) {
            x = maxWidth - width;
        }
        if (y < 0) {
            y = 0;
        }
        if (height + y > maxHeight) {
            y = maxHeight - height;
        }
        if (width > maxWidth) {
            x = 0;
            width = maxWidth;
        }
        if (height > maxHeight) {
            y = 0;
            height = maxHeight;
        }
        if (width % GlobalDef.NUMBER_2 != GlobalDef.NUMBER_0) {
            width--;
        }
        if (height % GlobalDef.NUMBER_2 != GlobalDef.NUMBER_0) {
            height--;
        }

        dstRect[0] = x;
        dstRect[1] = y;
        dstRect[2] = width;
        dstRect[3] = height;

        return dstRect;
    }
}
