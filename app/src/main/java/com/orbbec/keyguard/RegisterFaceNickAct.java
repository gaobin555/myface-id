package com.orbbec.keyguard;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orbbec.model.User;
import com.orbbec.ply.graph.GlView;
import com.orbbec.utils.DataSource;
import com.orbbec.utils.GlobalDef;
import com.orbbec.utils.LogUtil;
import com.orbbec.utils.MyDialogUpdata;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dou.utils.BitmapUtil;
import mobile.ReadFace.YMFaceTrack;

/**
 * @author lgp
 * @date 2017/12/15
 */

public class RegisterFaceNickAct extends Activity implements View.OnClickListener {
    private TextView contextView;
    private RelativeLayout layoutLoading;
    private Bitmap head;
    private String identifyPerson;
    private String age;
    private Button backNickBt;
    private Button finishNickBt;
    private YMFaceTrack mYmFaceTrack;
    private MyDialogUpdata myDialogUpdata;
    private EditText nickEdit;
    private ObFacePresenter mPresenterNick;
    private ImageView imageView;
    private ObFacePresenter obFacePresenter;

    private GlView mFaceGLView;
    private String mPlyPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_nick);
        init();
        Intent intent = getIntent();
        identifyPerson = intent.getStringExtra("persion_id");
        age = intent.getStringExtra("persion_age");
        byte[] bis = intent.getByteArrayExtra("bitmap");

        if (GlobalDef.DISPLAY_3D_FACE) {
            mPlyPath = intent.getStringExtra("plyPath");
            mFaceGLView.setRenderer(mPlyPath);
        }
        head = BitmapFactory.decodeByteArray(bis, 0, bis.length);
        imageView.setImageBitmap(head);
    }

    public void init() {
        mPresenterNick = new ObFacePresenter(this);
        contextView = (TextView) findViewById(R.id.congratulation);
        contextView.setText("恭喜您录入成功！" + "\n" + "请输入昵称");
        layoutLoading = (RelativeLayout) findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);
        backNickBt = (Button) findViewById(R.id.back_nick_bt);
        backNickBt.setOnClickListener(this);
        finishNickBt = (Button) findViewById(R.id.finish_nick_bt);
        finishNickBt.setClickable(false);
        finishNickBt.setOnClickListener(this);
        mYmFaceTrack = new YMFaceTrack();
        nickEdit = (EditText) findViewById(R.id.nick_edit);
        nickEdit.addTextChangedListener(new EditChangedListener());
        nickEdit.setFilters(new InputFilter[]{filter});
        imageView = (ImageView) findViewById(R.id.main_face_bitmap);
        obFacePresenter = new ObFacePresenter(RegisterFaceNickAct.this);
        mFaceGLView = (GlView) findViewById(R.id.gl_showface);
        if (GlobalDef.DISPLAY_3D_FACE) {
            imageView.setVisibility(View.GONE);
            mFaceGLView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            mFaceGLView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_nick_bt:
                showDialog();
                break;
            case R.id.finish_nick_bt:
                String getName = nickEdit.getText().toString().trim();
                //保存图片和 昵称等信息
                if (!TextUtils.isEmpty(getName)) {
                    LogUtil.d("getName"+getName);
                }
                if (!nickEdit.getText().toString().trim().equals(GlobalDef.STRING_NULL)) {
                    saveUserInformation(nickEdit.getText().toString().trim());
                } else {
                    Toast.makeText(RegisterFaceNickAct.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    public void saveUserInformation(String name) {
        User user = new User("" + identifyPerson, name, age, "", getSystemDate());
        DataSource dataSource = new DataSource(RegisterFaceNickAct.this);
        if (!dataSource.checkUserByName(name)) {
            dataSource.insert(user);
            LogUtil.d("lgp"+identifyPerson+":"+name);
            BitmapUtil.saveBitmap(head, getSDPath() + "/" + name + ".jpg");
            Intent intent = new Intent(RegisterFaceNickAct.this, PrctureMangerAct.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(RegisterFaceNickAct.this, "昵称已经存在，换个试试吧。", Toast.LENGTH_SHORT).show();
        }
    }

    public void showDialog() {
        myDialogUpdata = new MyDialogUpdata(RegisterFaceNickAct.this);
        myDialogUpdata.setMessage("您确认要放弃录入吗");
        myDialogUpdata.setYesOnclickListener("确认", new MyDialogUpdata.OnYesOnclickListener() {
            @Override
            public void onYesClick() {
                /*
                1、删除 人脸库id  这里不需要删除，因为人脸的信息保存在表中。陌生人不会显示昵称，昵称从表中获取。注册的时候 会删除persionid
                2、跳转到人脸识别首页  finish页面
                3、要删除人脸id库
                 */

                obFacePresenter.initFaceTrack();
                obFacePresenter.deletePersonID(Integer.parseInt(identifyPerson));


                Intent intent = new Intent(RegisterFaceNickAct.this, RecognitionFaceActivity.class);
                startActivity(intent);
                myDialogUpdata.dismiss();
                finish();
            }
        });
        myDialogUpdata.setNoOnclickListener("取消", new MyDialogUpdata.OnNoOnclickListener() {
            @Override
            public void onNoClick() {
                if (myDialogUpdata.isShowing()) {
                    myDialogUpdata.dismiss();
                }
            }
        });
        myDialogUpdata.show();
    }


    /**
     * @return 系统日期
     */
    public String getSystemDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        String now = sdf.format(new Date());
        return now;
    }

    /**
     * 获得文件夹路径
     *
     * @return path
     */

    public String getSDPath() {

        File sd = Environment.getExternalStorageDirectory();
        String path = sd.getPath() + "/FaceID";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        return path;
    }

    class EditChangedListener implements TextWatcher {
        /**
         * /监听前的文本
         */
        private CharSequence temp = null;

        /**
         * 改变之前的内容
         *
         * @param s
         * @param start
         * @param count
         * @param after
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            finishNickBt.setClickable(false);
            finishNickBt.setBackgroundResource(R.drawable.finish_n);
            temp = s;
        }

        /**
         * 改变之后的内容
         *
         * @param s
         * @param start
         * @param before
         * @param count
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            //  nickEdit.setText("还能输入" + (charMaxNum - s.length()) + "字符");

        }

        /**
         * 改变之后的内容
         *
         * @param s
         */
        @Override
        public void afterTextChanged(Editable s) {
            if (temp.length() == 0) {
                finishNickBt.setClickable(false);
                finishNickBt.setBackgroundResource(R.drawable.finish_n);
            } else {
                finishNickBt.setClickable(true);
                finishNickBt.setBackgroundResource(R.drawable.finish_y);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        obFacePresenter.initFaceTrack();
        obFacePresenter.deletePersonID(Integer.parseInt(identifyPerson));

        return super.onKeyDown(keyCode, event);

    }

    private final int maxLen = 16;
    private InputFilter filter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            int dindex = 0;
            int count = 0;

            while (count <= maxLen && dindex < dest.length()) {
                char c = dest.charAt(dindex++);
                if (c < 128) {
                    count = count + 1;
                } else {
                    count = count + 2;
                }
            }

            if (count > maxLen) {
                return dest.subSequence(0, dindex - 1);
            }

            int sindex = 0;
            while (count <= maxLen && sindex < source.length()) {
                char c = source.charAt(sindex++);
                if (c < 128) {
                    count = count + 1;
                } else {
                    count = count + 2;
                }
            }

            if (count > maxLen) {
                sindex--;
            }

            for (int i = start; i < end; i++) {
                if (!Character.isLetterOrDigit(source.charAt(i)) && !Character.toString(source.charAt(i)).equals(GlobalDef.EQUALS_) && !Character.toString(source.charAt(i)).equals(GlobalDef.EQUALS_2)) {
                    return "";
                }
            }

            return source.subSequence(0, sindex);
        }


    };

}
