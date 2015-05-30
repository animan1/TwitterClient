package com.codepath.apps;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class TargetLinearLayout extends LinearLayout implements Target {
  public TargetLinearLayout(Context context) {
    super(context);
  }

  public TargetLinearLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public TargetLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public TargetLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
    setBackground(new BitmapDrawable(getResources(), bitmap));
  }

  @Override
  public void onBitmapFailed(Drawable errorDrawable) {}

  @Override
  public void onPrepareLoad(Drawable placeHolderDrawable) {}
}
