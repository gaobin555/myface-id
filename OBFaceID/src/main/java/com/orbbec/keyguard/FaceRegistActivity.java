package com.orbbec.keyguard;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.orbbec.adapter.OnItemClickListener;
import com.orbbec.adapter.UserListAdapter;
import com.orbbec.base.BaseApplication;
import com.orbbec.model.User;
import com.orbbec.utils.DataSource;
import com.orbbec.utils.LogUtil;
import com.orbbec.utils.UserDataUtil;

import java.util.ArrayList;

public class FaceRegistActivity extends NoCameraActivity implements View.OnClickListener, OnItemClickListener {

    private static final String TAG = "FaceRegistActivity";

    private RecyclerView rcv_head;
    private UserListAdapter adapter = null;
    private ArrayList<User> userList = null;

    private LinearLayout ll_insert_cam;
//    private LinearLayout ll_insert_vid;
    private LinearLayout ll_insert_pic;
    private LinearLayout ll_insert_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_face_regist);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    void initData() {
        userList = UserDataUtil.updateDataSource();
        LogUtil.i(TAG + " initData userList length = " + userList.size());
        adapter = new UserListAdapter(userList);
    }

    @Override
    void initView() {
        ll_insert_cam = (LinearLayout) findViewById(R.id.ll_insert_cam);
//        ll_insert_vid = (LinearLayout) findViewById(R.id.ll_insert_vid);
        ll_insert_pic = (LinearLayout) findViewById(R.id.ll_insert_pic);
        ll_insert_delete = (LinearLayout) findViewById(R.id.ll_insert_delete);
        rcv_head = (RecyclerView) findViewById(R.id.rcv_head);
        rcv_head.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rcv_head.setAdapter(adapter);
    }

    @Override
    void initEvent() {
        ll_insert_cam.setOnClickListener(this);
//        ll_insert_vid.setOnClickListener(this);
        ll_insert_pic.setOnClickListener(this);
        ll_insert_delete.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_insert_cam://拍照录入
                Intent registFromCamActivityIntent = new Intent(FaceRegistActivity.this, RegistFromCamAcitvity.class);
                startActivityForResult(registFromCamActivityIntent, 10000);
                break;

//            case R.id.ll_insert_vid://视频录入
//                Intent registFromVidActivityIntent = new Intent(FaceRegistActivity.this, RegistFromVidAcitvity.class);
//                startActivityForResult(registFromVidActivityIntent, 10000);
//                break;

            case R.id.ll_insert_pic://照片录入
                Intent registFromPicActivityIntent = new Intent(FaceRegistActivity.this, RegistFromPicActivity.class);
                startActivityForResult(registFromPicActivityIntent, 10000);
                break;

            case R.id.ll_insert_delete://全部删除
                if (0 == UserDataUtil.getUserCount()) {
                    showShortToast(FaceRegistActivity.this, "当前无录入人脸");
                    return;
                }
                final AlertDialog.Builder builder = new AlertDialog.Builder(FaceRegistActivity.this);
                builder.setCancelable(false);
                builder.setMessage("确定删除所有头像？")
                        .setNegativeButton("否",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                UserDataUtil.clearDb();
                                userList = UserDataUtil.updateDataSource();
                                adapter.updateData(userList);
                                faceTrack.resetAlbum();
                                DataSource dataSource = new DataSource(BaseApplication.getContext());
                                dataSource.deleteAllUser();
                            }
                        });
                builder.create().show();

                break;

            default:

                break;
        }
    }

    /**
     * recycleview的点击事件
     *
     * @param position 点击的位置
     */
    @Override
    public void onClick(int position) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10000) {
            //刷新一次
            long time = System.currentTimeMillis();
            userList = UserDataUtil.updateDataSource();
            LogUtil.i(TAG + " onActivityResult userList length = " + userList.size());
            adapter.updateData(userList);
        }
    }
}
