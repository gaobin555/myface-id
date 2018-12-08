package com.orbbec.utils;

import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

/**
 *
 * @author mac
 * @date 16/6/23
 */
public class CountDownAnimation {
    private TextView mTextView;
    private Animation mAnimation;
    private int mStartCount;
    private int mCurrentCount;
    private CountDownListener mListener;

    private Handler mHandler = new Handler();

    private final Runnable mCountDown = new Runnable() {
        @Override
        public void run() {
            if (mCurrentCount > 0) {
                mTextView.setText(mCurrentCount + "");
                mTextView.startAnimation(mAnimation);
                mCurrentCount--;
            } else {
                mTextView.setVisibility(View.GONE);
                if (mListener != null) {
                    mListener.onCountDownEnd(CountDownAnimation.this);
                }
            }
        }
    };

    /**
     * <p>
     * Creates a count down animation in the <var>textView</var>, starting from
     * <var>startCount</var>.
     * </p>
     * <p>
     * By default, the class defines a fade out animation, which uses
     * {@link AlphaAnimation } from 1 to 0.
     * </p>
     *
     * @param textView   The view where the count down will be shown
     * @param startCount The starting count number
     */
    public CountDownAnimation(TextView textView, int startCount) {
        this.mTextView = textView;
        this.mStartCount = startCount;

        mAnimation = new AlphaAnimation(1.0f, 0.0f);
        mAnimation.setDuration(1000);
    }

    /**
     * Starts the count down animation.
     */
    public void start() {
        mHandler.removeCallbacks(mCountDown);

        mTextView.setText(mStartCount + "");
        mTextView.setVisibility(View.VISIBLE);

        mCurrentCount = mStartCount;

        mHandler.post(mCountDown);
        for (int i = 1; i <= mStartCount; i++) {
            mHandler.postDelayed(mCountDown, i * 1000);
        }
    }

    /**
     * Cancels the count down animation.
     */
    public void cancel() {
        mHandler.removeCallbacks(mCountDown);

        mTextView.setText("");
        mTextView.setVisibility(View.GONE);
    }

    /**
     * Sets the animation used during the count down. If the duration of the
     * animation for each number is not set, one second will be defined.
     */
    public void setAnimation(Animation animation) {
        this.mAnimation = animation;
        if (mAnimation.getDuration() == 0) {
            mAnimation.setDuration(1000);
        }
    }

    /**
     * Returns the animation used during the count down.
     */
    public Animation getAnimation() {
        return mAnimation;
    }

    /**
     * Sets a new starting count number for the count down animation.
     *
     * @param startCount The starting count number
     */
    public void setStartCount(int startCount) {
        this.mStartCount = startCount;
    }

    /**
     * Returns the starting count number for the count down animation.
     */
    public int getStartCount() {
        return mStartCount;
    }

    /**
     * Binds a listener to this count down animation. The count down listener is
     * notified of events such as the end of the animation.
     *
     * @param listener The count down listener to be notified
     */
    public void setCountDownListener(CountDownListener listener) {
        mListener = listener;
    }

    /**
     * A count down listener receives notifications from a count down animation.
     * Notifications indicate count down animation related events, such as the
     * end of the animation.
     */
    public static interface CountDownListener {
        /**
         * Notifies the end of the count down animation.
         *
         * @param animation The count down animation which reached its end.
         */
        void onCountDownEnd(CountDownAnimation animation);
    }
}
