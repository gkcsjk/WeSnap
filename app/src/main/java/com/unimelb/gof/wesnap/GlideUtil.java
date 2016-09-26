package com.unimelb.gof.wesnap;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class GlideUtil {
    public static void loadImage(String url, ImageView imageView) {
        Context context = imageView.getContext();
        ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(context, R.color.colorGreyMedium));
        Glide.with(context)
                .load(url)
                .placeholder(cd)
                .crossFade()
                .centerCrop()
                .into(imageView);
    }

    public static void loadProfileIcon(String url, ImageView imageView) {
        Context context = imageView.getContext();
        ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(context, R.color.colorGreyMedium));
        Glide.with(context)
                .load(url)
                .placeholder(cd)
                .dontAnimate()
                .fitCenter()
                .into(imageView);
    }
}

