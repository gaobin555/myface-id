package com.orbbec.keyguard;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;

import java.util.ArrayList;
import java.util.List;

import mobile.ReadFace.YMFace;

import com.orbbec.constant.Constant;
import com.orbbec.model.User;
import com.orbbec.utils.BitmapUtil;
import com.orbbec.utils.DataSource;
import com.orbbec.utils.GlideImageLoader;
import com.orbbec.base.BaseApplication;
import com.orbbec.utils.LogUtil;

public class RegistFromPicActivity extends NoCameraActivity {

    ImagePicker imagePicker;
    private int IMAGE_PICKER = 10000;
    private Bitmap head;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //setContentView(R.layout.activity_regist_from_pic);
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
        imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(false);  //显示拍照按钮
        imagePicker.setCrop(true);        //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); //是否按矩形区域保存
        imagePicker.setSelectLimit(1);    //选中数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);//保存文件的高度。单位像素
    }

    @Override
    void initView() {

    }

    @Override
    void initEvent() {
        Intent intent = new Intent(this, ImageGridActivity.class);
        startActivityForResult(intent, IMAGE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == IMAGE_PICKER) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                for (int i = 0; i < images.size(); i++) {
                    final Bitmap bitmap = BitmapUtil.decodeScaleImage(images.get(i).path, 1000, 1000);
                    startTrack();
                    final List<YMFace> ymFaces = faceTrack.detectMultiBitmap(bitmap);
                    int faceIndex = 0;
                    int personId = faceTrack.identifyPerson(faceIndex);
                    float[] rect = ymFaces.get(faceIndex).getRect();
                    if (personId > 0) {
                        //已经认识，不不能再添加，可以选择删除之前的重新添加。
                        showShortToast(RegistFromPicActivity.this, "已经认识，不不能再添加");
                        finish();
                    } else {
                        //personId < 0 //还不不认识，可以添加
                        personId = faceTrack.addPerson(faceIndex);
                        if (personId > 0) {
                            //添加成功，此返回值即为数据库对当前⼈人脸的中唯⼀一标识
                            head = Bitmap.createBitmap(bitmap, (int) rect[0], (int) rect[1], (int) rect[2], (int) rect[3], null, true);
                            doEnd(personId);
                        } else {
                            //personId < 0 //添加失败
                            showShortToast(RegistFromPicActivity.this, "添加失败");
                            finish();
                        }
                    }
                }
            } else {
                Toast.makeText(this, "未选择照片", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "未选择照片", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void doEnd(final int personId) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(RegistFromPicActivity.this);
        builder.setCancelable(false);
        final EditText et = new EditText(RegistFromPicActivity.this);
        et.setGravity(Gravity.CENTER);
        et.setHint("输入昵称不能为空");
        et.setHintTextColor(0xffc6c6c6);
        builder.setTitle("提示")
                .setMessage(String.format("人脸录入成功，Face ID =  %1$s 请输入昵称", personId))
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String name = et.getText().toString();
                        if (!TextUtils.isEmpty(name.trim())) {
                        } else {
                            doEnd(personId);
                            return;
                        }
                        User user = new User("" + personId, name, "", "");
                        LogUtil.i("User personId = " + personId + "; name = " + name);
                        //user.setScore(score);
                        DataSource dataSource = new DataSource(BaseApplication.getContext());
                        dataSource.insert(user);
                        BitmapUtil.saveBitmap(head, Constant.ImagePath + personId + ".jpg");

                        final AlertDialog.Builder builder = new AlertDialog.Builder(RegistFromPicActivity.this);
                        builder.setCancelable(false);
                        builder.setMessage("当前录入成功是否继续录入？")
                                .setNegativeButton("是",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent(RegistFromPicActivity.this, ImageGridActivity.class);
                                                startActivityForResult(intent, IMAGE_PICKER);
                                            }
                                        })
                                .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        setResult(102, getIntent());
                                        onBackPressed();
                                    }
                                });
                        builder.create().show();
                    }
                });
        builder.create().show();
    }
}
