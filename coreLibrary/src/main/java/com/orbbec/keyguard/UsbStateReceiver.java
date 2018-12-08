package com.orbbec.keyguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;

/**
 *
 * @author LENOVO
 * @date 2017/10/24
 */

public class UsbStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Log.e("edg","ACT = "+action);
        if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
            Log.e("edg","插入 usb");
        }else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){
            Log.e("edg","拔出 usb");
        }else{

        }
    }
}
