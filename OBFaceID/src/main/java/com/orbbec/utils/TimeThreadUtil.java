package com.orbbec.utils;

import android.os.Handler;
import android.os.Message;

/**
 * Created by admin on 2017/12/19.
 * 实时更新时间子线程
 */


public class TimeThreadUtil extends Thread {


    private static final int CURRENTDATETIME = 1;
    private static TimeThreadUtil timeThreadUtil;
    private OnGetCurrentDateTimeListener listener;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CURRENTDATETIME:
                    if (listener != null) {
                        listener.onGetDateTime();
                    }
                    break;
            }
        }
    };


    public TimeThreadUtil(OnGetCurrentDateTimeListener listener) {
        this.listener = listener;
    }


    @Override
    public void run() {
        super.run();
        do {
            try {
                Thread.sleep(1000);
                Message msg = new Message();
                msg.what = CURRENTDATETIME;
                mHandler.sendMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);


    }
}

