package com.orbbec.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orbbec.app.ExApplication;

import java.io.File;



/**
 * 图片加载工具类
 * Created by mac on 16/9/30.
 */

public class GlideUtil {
    public static void load(int resId, ImageView view) {
        Glide.with(ExApplication.getContext()).load(resId).placeholder(resId).dontAnimate().into(view);
    }

    public static void load(Context context, ImageView v, String path) {
        Glide.with(context).load(new File(path))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(v);
    }
}
