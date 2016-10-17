package com.unimelb.gof.wesnap.util;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.unimelb.gof.wesnap.R;

/**
 * GlideUtil
 * Helpers to load images into views
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class GlideUtil {
    public static void loadImage(String url, ImageView imageView) {
        Context context = imageView.getContext();
        ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(
                context, R.color.colorTransparentBlack));
        Glide.with(context)
                .load(url)
                .placeholder(cd)
                .crossFade()
                .centerCrop()
                .into(imageView);
    }

    public static void loadProfileIcon(String url, ImageView imageView) {
        Context context = imageView.getContext();
        ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(
                context, R.color.colorTransparentWhite));
        Glide.with(context)
                .load(url)
                .placeholder(cd)
                .dontAnimate()
                .fitCenter()
                .into(imageView);
    }

    public static void loadPhoto(String url, ImageView imageView) {
        Context context = imageView.getContext();
        Glide.with(context)
                .load(url)
                .into(imageView);
    }
}

