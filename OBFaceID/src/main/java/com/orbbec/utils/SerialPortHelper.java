package com.orbbec.utils;

import android.widget.Toast;

import com.bjw.bean.ComBean;
import com.bjw.utils.FuncUtil;
import com.bjw.utils.SerialHelper;
import com.orbbec.base.BaseApplication;
import com.orbbec.constant.Constant;

import java.io.IOException;

import static dou.utils.HandleUtil.runOnUiThread;

public class SerialPortHelper {

    private SerialHelper serialHelper;

    /**
     *  先要初始化才可以使用其它接口
     */
    public void initSerial() {
        serialHelper = new SerialHelper("/dev/ttyS4", 9600) {
            @Override
            protected void onDataReceived(final ComBean comBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BaseApplication.getContext(), FuncUtil.ByteArrToHex(comBean.bRec), Toast.LENGTH_SHORT).show();
                        LogUtil.i("onDataReceived: " + FuncUtil.ByteArrToHex(comBean.bRec));
                    }
                });
            }
        };
    }

    public boolean setPort (String sPort) {
        return serialHelper.setPort(sPort);
    }

    public String getPort () {
        return serialHelper.getPort();
    }

    public int getBaudRate() {
        return serialHelper.getBaudRate();
    }

    public boolean setBaudRate(int iBaud) {
        return serialHelper.setBaudRate(iBaud);
    }

    public void sendHex(String sHex) {
        serialHelper.sendHex(sHex);
    }

    public void sendTxt(String sTxt) {
        serialHelper.sendTxt(sTxt);
    }


    public void  sendOpenGate(String hex_comm) {
        String sendConm;
        if (serialHelper == null) {
            Toast.makeText(BaseApplication.getContext(), "請先初始化Serial", Toast.LENGTH_SHORT).show();
            LogUtil.v("sendOpenGate():serialHelper == null");
            return;
        }

        if (hex_comm == null || hex_comm.equals("")) {
            sendConm = Constant.OPENGATE;
        } else {
            sendConm = hex_comm;
        }

        if (serialHelper.isOpen()) {
            serialHelper.close();
        }

        serialHelper.setPort("/dev/ttyS4");
        serialHelper.setBaudRate(9600);

        try {
            serialHelper.open();
            serialHelper.sendHex(sendConm);
            Toast.makeText(BaseApplication.getContext(), "Send: " + sendConm, Toast.LENGTH_SHORT).show();
            LogUtil.d("Port: "+ getPort() + "  BaudRate : " + getBaudRate());
            LogUtil.d("\nsendOpenGate() send: " + sendConm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serialOpen() {
        try {
            serialHelper.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean serialIsOpen() {
        if (serialHelper == null) {
            LogUtil.v("serialIsOpen():serialHelper == null");
            return false;
        }
       return serialHelper.isOpen();
    }

    public void serialClose() {
        if (serialHelper == null) {
            LogUtil.v("serialClose():serialHelper == null");
            return;
        }
        serialHelper.close();
    }
}
