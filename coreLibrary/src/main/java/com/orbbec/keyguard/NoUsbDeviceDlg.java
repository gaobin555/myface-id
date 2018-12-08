package com.orbbec.keyguard;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.TextView;
import com.orbbec.keyguard.core.R;

/**
 * @author lgp
 */
public class NoUsbDeviceDlg extends Dialog {

	private static final String TAG = "StartLoadingDlg";
    private Activity mContext;

	public NoUsbDeviceDlg(Activity context, String message) {
        super(context, R.style.HalfTransparentDialog);
		setContentView(R.layout.dlg_nousb_message);
		setCanceledOnTouchOutside(false);
		TextView loginStatus = (TextView) findViewById(R.id.message_title);
		if (message != null){
			loginStatus.setText(message);
		}
		mContext = context;

		//设置window背景，默认的背景会有Padding值，不能全屏。当然不一定要是透明，你可以设置其他背景，替换默认的背景即可。
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

		this.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {

			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mContext != null){
			return mContext.onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}

}