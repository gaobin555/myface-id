package com.orbbec.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
public class DetectRectView extends View {

    private Rect mRect;

    /**
     * 显示有效范围到view顶端
     * @param activity
     * @param rect
     */
    public static void showDetectRect(Activity activity, Rect rect){
        DetectRectView view = new DetectRectView(activity);
        view.setRect(rect);
        ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        View decorView = activity.getWindow().getDecorView();
        FrameLayout contentParent = (FrameLayout) decorView.findViewById(android.R.id.content);
        contentParent.addView(view, param);
    }

    public DetectRectView(Context context){
        super(context);
    }

    public DetectRectView(Context context, AttributeSet set){
        super(context, set);
    }

    public void setRect(Rect rect){
        mRect = rect;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mRect == null){
            postInvalidateDelayed(200);
        }else{
            Paint paint = new Paint();
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.parseColor("#88ff00ff"));
            canvas.drawRect(mRect, paint);
        }
    }
}
