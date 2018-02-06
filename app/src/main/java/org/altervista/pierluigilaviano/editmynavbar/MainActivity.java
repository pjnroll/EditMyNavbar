package org.altervista.pierluigilaviano.editmynavbar;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    private final int OVERLAY_PERMISSION_REQ_CODE = 1234;
    private final int GALLERY_REQ_CODE = 425;
    private final int CROP_REQ_CODE = 2767;

    private ImageView imageView;
    private Switch swcActivate;
    private TextView tvAllowSystemAlertWindow;

    WindowManager wm;
    DisplayMetrics displayMetrics;

    private Uri uri;

    private boolean granted;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        imageView = findViewById(R.id.imageView);

        // Seek Settings.ACTION_MANAGE_OVERLAY_PERMISSION - required on Marshmallow & above
        swcActivate = findViewById(R.id.switch1);
        swcActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowSystemAlertWindow();
            }
        });

        tvAllowSystemAlertWindow = (TextView) findViewById(R.id.tv_allow_system_alert_window);

        // Takes the user to the Accessibility Settings activity.
        // Here, you can enable/disable SublimeNavBar service
        Button bStartStopService = findViewById(R.id.btn_start_stop_service);
        bStartStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });

        Button btnChooseImage = findViewById(R.id.btn_choose_image);
        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(galIntent, "Select an image from the gallery"), GALLERY_REQ_CODE);
            }
        });

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 63342);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (swcActivate != null) {
            swcActivate.setChecked(granted);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean canShowOverlays() {
        return Settings.canDrawOverlays(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void allowSystemAlertWindow() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (canShowOverlays()) {
                granted = true;
                tvAllowSystemAlertWindow.setText(getResources()
                        .getString(R.string.text_permission_granted_restart_service));
            } else {
                granted = false;
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == GALLERY_REQ_CODE) {
            if (data != null) {
                uri = data.getData();
                cropImage();
            }
        } else if (requestCode == CROP_REQ_CODE) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                assert bundle != null;
                Bitmap bitmap = bundle.getParcelable("data");
                imageView.setImageBitmap(bitmap);
                Intent bitmapIntent = new Intent(this, EditMyNavbarService.class);
                bitmapIntent.putExtra("image", bitmap);
                startService(bitmapIntent);
            }
        }
    }

    /**
     * Here the magic happens
     */
    private void cropImage() {
        try {
            Intent cropIntent;
            cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(uri, "image/*");

            int navbarW = displayMetrics.widthPixels;
            int navbarH = getResources().getDimensionPixelSize(R.dimen.nav_bar_size);

            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("outputX", displayMetrics.widthPixels);
            cropIntent.putExtra("outputY", getResources().getDimensionPixelSize(R.dimen.nav_bar_size));
            cropIntent.putExtra("aspectX", navbarW);
            cropIntent.putExtra("aspectY", navbarH);
            cropIntent.putExtra("scaleUpIfNeeded", true);
            cropIntent.putExtra("return-data", true);

            startActivityForResult(cropIntent, CROP_REQ_CODE);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "L'applicazione non supporta le modifiche. Sceglierne un'altra", Toast.LENGTH_SHORT).show();
        }
    }
}