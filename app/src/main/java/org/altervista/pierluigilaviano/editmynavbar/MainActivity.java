package org.altervista.pierluigilaviano.editmynavbar;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import eu.janmuller.android.simplecropimage.CropImage;

import static java.lang.Math.abs;

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
    File mFileTemp;

    private int navbarW;
    private int navbarH;
    private int TRANS_HEIGHT;


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

        navbarW = displayMetrics.widthPixels;
        navbarH = getResources().getDimensionPixelSize(R.dimen.nav_bar_size);
        TRANS_HEIGHT = navbarH*5/100;

        imageView = findViewById(R.id.imageView);

        // Seek Settings.ACTION_MANAGE_OVERLAY_PERMISSION - required on Marshmallow & above
        swcActivate = findViewById(R.id.switch1);
        swcActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Lo chiamo");
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

    /*@Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        checkGranted();
    }*/

    private void checkGranted() {
        Log.i(TAG, "swcActivate == " + ((swcActivate == null) ? "null" : "notNull"));
        Log.i(TAG, "granted == " + ((granted) ? "true" : "false"));
        if (swcActivate != null) {
            swcActivate.setChecked(granted);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean canShowOverlays() {
        Log.i(TAG, "------------------------------->Entrato1");
        return Settings.canDrawOverlays(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void allowSystemAlertWindow() {
        Log.i(TAG, "------------------------------->Entrato1");
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
                Log.i(TAG, "Ci siamo?");
                if (canShowOverlays()) {
                    Log.i(TAG, "ATTIVATO!!!");
                    granted = true;
                    tvAllowSystemAlertWindow.setText(getResources()
                            .getString(R.string.text_permission_granted_restart_service));
                } else {
                    Log.i(TAG, "NON ATTIVATO!!!");
                    granted = false;
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                checkGranted();
            } else if (requestCode == GALLERY_REQ_CODE) {
                mFileTemp = new File(getFilesDir(), "temp_photo.png");
                try {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
                    copyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    assert inputStream != null;
                    inputStream.close();

                    startCropImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } /*else if (requestCode == CROP_REQ_CODE) {
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    assert bundle != null;
                    Bitmap bitmap = bundle.getParcelable("data");
                    imageView.setImageBitmap(bitmap);
                    Intent bitmapIntent = new Intent(this, EditMyNavbarService.class);
                    bitmapIntent.putExtra("image", bitmap);
                    startService(bitmapIntent);
                }*/ else if (requestCode == 0x3) {
                String path = data.getStringExtra(CropImage.IMAGE_PATH);
                if (path == null) {
                    return;
                }
                Bitmap bitmap;
                bitmap = BitmapFactory.decodeFile(mFileTemp.getPath());
                Bitmap bitmap2 = setTransparency(bitmap);
                imageView.setImageBitmap(bitmap2);
                Intent bitmapIntent = new Intent(this, EditMyNavbarService.class);
                bitmapIntent.putExtra("image", bitmap2);
                startService(bitmapIntent);
            }
        }
    }

    private Bitmap setTransparency(Bitmap bitmap) {
        Bitmap bitmap2 = bitmap.copy(bitmap.getConfig(), true);
        Canvas canvas = new Canvas(bitmap2);
        RectF upRect = new RectF();
        upRect.set(0, 0, bitmap2.getWidth(), TRANS_HEIGHT);
        Paint alphaUp = new Paint();
        alphaUp.setColor(Color.WHITE);
        alphaUp.setAlpha(215);
        canvas.drawRect(upRect, alphaUp);
        canvas.drawBitmap(bitmap, 0f, 0f, alphaUp);

        Canvas canvasDn = new Canvas(bitmap2);
        RectF dnRect = new RectF();
        dnRect.set(0, TRANS_HEIGHT, bitmap2.getWidth(), navbarH);
        Paint alphaDn = new Paint();
        alphaDn.setColor(Color.WHITE);
        alphaDn.setAlpha(15);
        canvasDn.drawRect(dnRect, alphaDn);
        canvasDn.drawBitmap(bitmap2, 0f, 0f, alphaDn);

        return bitmap2;
        /*bitmap2.setHasAlpha(false);
        int w = bitmap2.getWidth();

        int[] pixels = new int[w * TRANS_HEIGHT];
        Log.i(TAG, "x" + 0 + " width" + navbarW + "bitmapW" + bitmap.getWidth());
        try {
            bitmap.getPixels(pixels, 0, w, 0, 0, w, TRANS_HEIGHT);
        } catch (RuntimeException e) {
            Toast.makeText(this, "The selection is too small", Toast.LENGTH_SHORT).show();
        }
//        int row = 0;
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = adjustAlpha(pixels[i], 0.7f);

            /*if ((i+1) % w == 0)
                row++;
            //Log.i(TAG, "Bi=" + i + ":" + pixels[i]);
            //pixels[i] = pixels[i] - (pixels[i] - (-30));
            //Log.i(TAG, "Ai=" + i + ":" + pixels[i]);
            //Log.i(TAG, "=======================================");
            //pixels[i] = Color.argb(50, Color.valueOf(pixels[i]).red(), Color.valueOf(pixels[i]).green(), Color.valueOf(pixels[i]).blue());
            //pixels[i] = Color.WHITE;
        }

        Log.i(TAG, bitmap.toString());
        bitmap2.setPixels(pixels, 0, w, 0, 0, w, TRANS_HEIGHT);
        experiment(bitmap);
        return bitmap2;*/
    }

    @ColorInt
    public static int adjustAlpha(@ColorInt int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    private void experiment(Bitmap bitmap) {
        Bitmap bitmap2 = bitmap.copy(bitmap.getConfig(), true);
        bitmap2.setPixel(0,0, Color.BLACK);
        Log.i(TAG, "COLOR/BLACK->" + Color.BLACK);
        bitmap2.setPixel(0,0, Color.WHITE);
        Log.i(TAG, "COLOR/WHITE->" + Color.WHITE);
    }

    private void startCropImage() {
        Intent intent = new Intent(this, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, mFileTemp.getPath());
        intent.putExtra(CropImage.SCALE, true);

        intent.putExtra(CropImage.ASPECT_X, navbarW);
        intent.putExtra(CropImage.ASPECT_Y, navbarH);

        startActivityForResult(intent, 0x3);
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[2048];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
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