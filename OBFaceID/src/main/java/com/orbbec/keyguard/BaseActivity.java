package com.orbbec.keyguard;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;



/**
 * 所有Activity基类
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

//    private Handler mWorkerHandler;
//    private long mWorkerThreadID = -1;


    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (mWorkerHandler == null) {
//            mWorkerHandler = HandlerThreadHandler.createHandler(TAG);
//            mWorkerThreadID = mWorkerHandler.getLooper().getThread().getId();
//        }
        initData();
        initView();
        initEvent();

    }

    abstract void initData();

    abstract void initView();

    abstract void initEvent();

    /**
     * 显示ShortToast
     *
     * @param context
     * @param content
     */
    void showShortToast(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示LongToast
     *
     * @param context
     * @param content
     */
    void showLongToast(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_LONG).show();
    }

    /**
     * 显示进度对话框不可手动取消
     */
    protected void showProgressDialog(Context context, String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMessage(message);
        }
        mProgressDialog.show();
    }

    /**
     * 显示进度对话框可手动取消
     */
    protected void showCancelableProgressDialog(Context context, String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setCanceledOnTouchOutside(true);
            mProgressDialog.setMessage(message);
        }
        mProgressDialog.show();
    }

    /**
     * 取消显示进度对话框
     */
    protected void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    /**
     * ワーカースレッド上で指定したRunnableを実行する
     * 未実行の同じRunnableがあればキャンセルされる(後から指定した方のみ実行される)
     *
     * @param task
     * @param delayMillis
     */
//    protected final synchronized void queueEvent(final Runnable task, final long delayMillis) {
//        if ((task == null) || (mWorkerHandler == null)) return;
//        try {
//            mWorkerHandler.removeCallbacks(task);
//            if (delayMillis > 0) {
//                mWorkerHandler.postDelayed(task, delayMillis);
//            } else if (mWorkerThreadID == Thread.currentThread().getId()) {
//                task.run();
//            } else {
//                mWorkerHandler.post(task);
//            }
//        } catch (final Exception e) {
//            // ignore
//        }
//    }
}
