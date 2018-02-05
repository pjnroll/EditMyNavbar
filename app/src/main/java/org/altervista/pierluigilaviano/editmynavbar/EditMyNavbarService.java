package org.altervista.pierluigilaviano.editmynavbar;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class EditMyNavbarService extends AccessibilityService {
    private final static String TAG = "EditMyNavbarService";

    private WindowManager wm;
    private LinearLayout mLayout;
    private ImageView imageView;
    private DisplayMetrics displayMetrics;


    private boolean visible;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        info.flags = AccessibilityServiceInfo.DEFAULT;
        setServiceInfo(info);

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        setUp();
    }

    private void setUp() {
        WindowManager.LayoutParams lpNavView = new WindowManager.LayoutParams();

        displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int navbarHeight = getResources().getDimensionPixelSize(R.dimen.nav_bar_size);

        lpNavView.width = WindowManager.LayoutParams.MATCH_PARENT;
        Log.i(TAG, "navbardim " + navbarHeight + "x" + lpNavView.width);
        lpNavView.height = navbarHeight;
        lpNavView.x = 0;
        lpNavView.y = -navbarHeight;
        lpNavView.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        lpNavView.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        lpNavView.gravity = Gravity.BOTTOM;

        mLayout = new LinearLayout(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.view_navbar, mLayout);

        imageView = mLayout.findViewById(R.id.imageView);
        //imageView.setImageResource(R.drawable.debug);
        Bitmap toAdd = getImg(displayMetrics.widthPixels, navbarHeight);
        imageView.setImageBitmap(toAdd);
        wm.addView(mLayout, lpNavView);

        visible = true;
    }

    private Bitmap getImg(int width, int height) {
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.bg_camo).copy(Bitmap.Config.ARGB_8888, true);
        image.setDensity(displayMetrics.densityDpi);
        Bitmap navbarImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        navbarImage.setDensity(displayMetrics.densityDpi);

        int minW = getDrawable(R.drawable.bg_camo).getMinimumWidth();
        int minH = getDrawable(R.drawable.bg_camo).getMinimumHeight();
        int minH = dImage.getMinimumHeight();
        int intW = getDrawable(R.drawable.bg_camo).getIntrinsicWidth();
        int intH = getDrawable(R.drawable.bg_camo).getIntrinsicHeight();
        Log.i(TAG, "Min" + minW + "*" + minH);
        Log.i(TAG, "Int" + intW + "*" + intH);
        Log.i(TAG, "DIMENSIONEBITMAP->" + image.getWidth() + "*" + image.getHeight());
        int[] pixels = new int[width*height];

        image.getPixels(pixels, 0, width, 0, 0, width, height);
        navbarImage.setPixels(pixels, 0, width, 0, 0, width, height);
        Log.i(TAG, "DIMENSION " + width + " * " + height);
        //int n_1440 = 1440;  //width
        //int n_145 = 145;    //height
        //int diff = width - height;  //diff

        /*for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                navbarImage.setPixel(x, y, image.getPixel(x, y));
            }
        }*/
/*
        int px = width-1;
        for (int x = width-1; x >= 0; x--) {
            int py = height-1;
            for (int y = width-1; y > diff; y--) {
                Log.i(TAG, "PUNTO->" + x + "," + y);
                Log.i(TAG, "LO METTE IN->" + px + "," + py);
                navbarImage.setPixel(px, py, image.getPixel(x, y));
                py--;
            }
            px--;
        }*/

        return navbarImage;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        CharSequence appName = getAppName(accessibilityEvent);
        if (appName != null) {
            Log.i(TAG, "Entrato in " + appName);
            Log.i(TAG, "VALORI->" + appName + " " + visible);
            if (appName.equals("UI sistema") && visible) {
                onInterrupt();
                visible = false;
            } else if (!appName.equals("UI sistema") && !visible) {
                onServiceConnected();
            }
        }
    }

    private CharSequence getAppName(AccessibilityEvent event) {
        CharSequence appName = null;
        PackageManager pm = getPackageManager();

        if (event != null && !TextUtils.isEmpty(event.getPackageName())) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(event.getPackageName().toString(), 0);
                if (appInfo != null) {
                    appName = pm.getApplicationLabel(appInfo);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        return appName;
    }

    @Override
    public void onInterrupt() {
        if (mLayout != null && mLayout.getWindowToken() != null) {
            wm.removeView(mLayout);
        }
    }
}