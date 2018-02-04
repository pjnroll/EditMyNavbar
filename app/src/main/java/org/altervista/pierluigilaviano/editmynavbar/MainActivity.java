package org.altervista.pierluigilaviano.editmynavbar;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final int OVERLAY_PERMISSION_REQ_CODE = 1234;

    private Button bAllowSystemAlertWindow;
    private TextView tvAllowSystemAlertWindow;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout llAllowSystemAlertWindow = (LinearLayout) findViewById(R.id.ll_allow_system_alert_window);

        // Seek Settings.ACTION_MANAGE_OVERLAY_PERMISSION - required on Marshmallow & above
        bAllowSystemAlertWindow = (Button) findViewById(R.id.b_allow_system_alert_window);
        bAllowSystemAlertWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowSystemAlertWindow();
            }
        });

        tvAllowSystemAlertWindow = (TextView) findViewById(R.id.tv_allow_system_alert_window);

        // Takes the user to the Accessibility Settings activity.
        // Here, you can enable/disable SublimeNavBar service
        Button bStartStopService = (Button) findViewById(R.id.b_start_stop_service);
        bStartStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });

        // We only need permission if we're on Marshmallow & above.
        // Hide the Button if we're on a lower API level, or if the
        // permission has already been granted.
        if (Utils.IS_AT_LEAST_MARSHMALLOW) {
            if (!canShowOverlays()) {
                llAllowSystemAlertWindow.setVisibility(View.VISIBLE);
            }
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
            if (!canShowOverlays()) {
                // `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` was not granted...
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            } else {
                bAllowSystemAlertWindow.setVisibility(View.GONE);
                tvAllowSystemAlertWindow.setText(getResources()
                        .getString(R.string.text_permission_granted_restart_service));
            }
        }
    }
}