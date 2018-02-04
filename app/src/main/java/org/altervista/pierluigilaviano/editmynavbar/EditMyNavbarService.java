package org.altervista.pierluigilaviano.editmynavbar;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class EditMyNavbarService extends AccessibilityService {
    private final static String TAG = "EditMyNavbarService";

    private WindowManager wm;
    private LinearLayout mLayout;
    ImageView imageView;

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

        lpNavView.width = WindowManager.LayoutParams.MATCH_PARENT;
        lpNavView.height = 145;
        lpNavView.x = 0;
        lpNavView.y = -145;
        lpNavView.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        lpNavView.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        lpNavView.gravity = Gravity.BOTTOM;

        mLayout = new LinearLayout(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.view_navbar, mLayout);

        imageView = mLayout.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.bg_camo);
        wm.addView(mLayout, lpNavView);

        visible = true;
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