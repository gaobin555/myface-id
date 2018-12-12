package com.orbbec.keyguard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView face_regist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initView();
        initEvent();
    }

    void initView() {
        face_regist = (TextView) findViewById(R.id.face_regist);
    }

    void initEvent() {
        face_regist.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.face_regist:
                Intent faceRegistActivityIntent = new Intent(SettingsActivity.this, FaceRegistActivity.class);
                startActivity(faceRegistActivityIntent);
                break;

            default:
                break;
        }

    }
}
