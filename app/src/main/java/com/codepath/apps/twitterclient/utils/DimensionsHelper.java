package com.codepath.apps.twitterclient.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DimensionsHelper {

  public static Bitmap scaleToFitWidth(Bitmap b, int width)
  {
    float factor = width / (float) b.getWidth();
    return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
  }

  // Scale and maintain aspect ratio given a desired height
  // BitmapScaler.scaleToFitHeight(bitmap, 100);
  public static Bitmap scaleToFitHeight(Bitmap b, int height, boolean recycle)
  {
    float factor = height / (float) b.getHeight();
    Bitmap updated = Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
    if (recycle) {
      b.recycle();
    }
    return updated;
  }

  // DimensionsHelper.getDisplayWidth(context) => (display width in pixels)
  public static int getDisplayWidth(Context context) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    return displayMetrics.widthPixels;
  }

  // DimensionsHelper.getDisplayHeight(context) => (display height in pixels)
  public static int getDisplayHeight(Context context) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    return displayMetrics.heightPixels;
  }

  // DimensionsHelper.convertDpToPixel(25f, context) => (25dp converted to pixels)
  public static float convertDpToPixel(float dp, Context context){
    Resources r = context.getResources();
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
  }

  // DimensionsHelper.convertPixelsToDp(25f, context) => (25px converted to dp)
  public static float convertPixelsToDp(float px, Context context){
    Resources r = context.getResources();
    DisplayMetrics metrics = r.getDisplayMetrics();
    float dp = px / (metrics.densityDpi / 160f);
    return dp;
  }

  public static Bitmap cropToFitWidth(Bitmap source, int width, boolean recycle) {
    int sourceWidth = source.getWidth();
    if (sourceWidth > width) {
      int offset = (sourceWidth - width) / 2;
      Bitmap updated = Bitmap.createBitmap(source, offset, 0, width, source.getHeight());
      source.recycle();
      return updated;
    }
    return source;
  }
}
