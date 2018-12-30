package com.orbbec.keyguard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.orbbec.constant.Constant;
import com.orbbec.utils.SerialPortHelper;


public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView face_regist;
    private EditText edit_input;
    private Button bt_send;

    private SerialPortHelper serialPortHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initView();
        initEvent();
    }

    void initView() {
        face_regist = (TextView) findViewById(R.id.face_regist);
        edit_input = (EditText) findViewById(R.id.ed_input);
        bt_send = (Button) findViewById(R.id.bt_send);
        serialPortHelper = new SerialPortHelper();
        serialPortHelper.initSerial();
    }

    void initEvent() {
        face_regist.setOnClickListener(this);
        edit_input.setText(Constant.OPENGATE);
        bt_send.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.face_regist:
                Intent faceRegistActivityIntent = new Intent(SettingsActivity.this, FaceRegistActivity.class);
                startActivity(faceRegistActivityIntent);
                break;

            case R.id.bt_send:
                serialPortHelper.sendOpenGate(edit_input.getText().toString());
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (serialPortHelper.serialIsOpen()) {
            serialPortHelper.serialClose();
        }
    }
}
