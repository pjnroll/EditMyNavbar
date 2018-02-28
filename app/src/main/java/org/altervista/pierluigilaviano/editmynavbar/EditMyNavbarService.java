package org.altervista.pierluigilaviano.editmynavbar;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class EditMyNavbarService extends AccessibilityService {
    private final static String TAG = "EditMyNavbarService";
    private final static String SYSTEMUI_PKG_NAME = "com.android.systemui";

    private WindowManager wm;
    private LinearLayout mLayout;
    private Bitmap bmpFromIntent;
    private boolean visible;
    private int navbarW;
    private int navbarH;

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
        //  Obtained the display metrics
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        //  Set the navbar's dimensions
        navbarW = displayMetrics.widthPixels;
        navbarH = getResources().getDimensionPixelSize(R.dimen.nav_bar_size);

        /*lpNavView.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lpNavView.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        lpNavView.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        lpNavView.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;*/
        int flags = 0;
        flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;


        //  Create the layout parameters and setting the layout
        WindowManager.LayoutParams lpNavView = new WindowManager.LayoutParams(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, flags, -3);
        lpNavView.width = WindowManager.LayoutParams.MATCH_PARENT;
        lpNavView.height = navbarH;
        lpNavView.x = 0;
        lpNavView.y = -navbarH;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(TAG, "OREO");
            lpNavView.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            Log.i(TAG, "Not OREO");
            lpNavView.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }


        lpNavView.gravity = Gravity.BOTTOM;

        mLayout = new LinearLayout(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.view_navbar, mLayout);

        ImageView imgNavbar = mLayout.findViewById(R.id.imgNavbar);

        Bitmap toAdd = getImg();
        imgNavbar.setImageBitmap(toAdd);
        wm.addView(mLayout, lpNavView);

        //  Setting this variable at true, it means that the navbar is visible
        visible = true;
    }

    /**
     * If the Service is started programmatically, this method will be called
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getParcelableExtra("image") != null) {
                bmpFromIntent = intent.getParcelableExtra("image");
            }
        }
        return START_NOT_STICKY;
    }

    /**
     * This method create TWO different bitmap.
     * The first "default" bitmap is made starting from the drawable resource "bg_camo";
     * the second one is made starting from the image previously chosen by the user.
     * If there's any error with the rendering of the second bitmap, the first one will be shown
     * @return the default bitmap or the custom one
     */
    private Bitmap getImg() {
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.bg_camo).copy(Bitmap.Config.ARGB_8888, true);
        Bitmap navbarImage = Bitmap.createBitmap(navbarW, navbarH, Bitmap.Config.ARGB_8888);

        int[] pixels = new int[navbarW * navbarH];

        image.getPixels(pixels, 0, navbarW, 0, 0, navbarW, navbarH);
        navbarImage.setPixels(pixels, 0, navbarW, 0, 0, navbarW, navbarH);

        //  This is not a stupid thing.
        //  If bmpProva is null (i.e. nothing comes with the intent) it shows a default image
        boolean newBitmap = bmpFromIntent != null;

        Log.i(TAG, "New?->" + newBitmap);
        return (bmpFromIntent != null) ? bmpFromIntent : navbarImage;
    }

    /**
     * The service's main core
     * @param accessibilityEvent the event that wakes up EditMyNavbarService
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        CharSequence appPackage = getAppPackage(accessibilityEvent);
        if (appPackage != null) {
            boolean isSystemUI = appPackage.equals(SYSTEMUI_PKG_NAME);
            if (isSystemUI && visible) {
                onInterrupt();
                visible = false;
            } else if (!isSystemUI && !visible) {
                onServiceConnected();
            }
        }
    }

    /**
     * This method returns the package value of the app running in the foreground
     * @param event the event that wakes up EditMyNavbarService
     * @return appPackage is the package value
     */
    private CharSequence getAppPackage(AccessibilityEvent event) {
        PackageManager pm = getPackageManager();
        CharSequence appPackage = null;
        ApplicationInfo appInfo;
        if (event != null && !TextUtils.isEmpty(event.getPackageName())) {
            try {
                appInfo = pm.getApplicationInfo(event.getPackageName().toString(), 0);
                if (appInfo != null) {
                    appPackage = appInfo.packageName;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        return appPackage;
    }

    /**
     * If the service is interrupted, the view is removed
     */
    @Override
    public void onInterrupt() {
        if (mLayout != null && mLayout.getWindowToken() != null) {
            wm.removeView(mLayout);
        }
        //  No edit, it's just a joke
    }
}