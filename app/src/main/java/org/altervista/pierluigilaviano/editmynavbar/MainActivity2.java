package org.altervista.pierluigilaviano.editmynavbar;


import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity2 extends AppCompatActivity {
    private final static String TAG = "MainActivity2";

    ImageView imageView;
    Toolbar toolbar;
    File file;
    Uri uri;
    Intent CamIntent,GalIntent,CropIntent;
    final int RequestPermissionCode=1;
    DisplayMetrics displayMetrics;
    int width,height;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        imageView = (ImageView)findViewById(R.id.imageView);

        Button btnChoose = findViewById(R.id.btn_choose_image);
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        /*int permissionCheck = ContextCompat.checkSelfPermission(MainActivity2.this, Manifest.permission.CAMERA);
        if(permissionCheck == PackageManager.PERMISSION_DENIED)
            RequestRuntimePermission();*/
    }

    private void openGallery() {
        GalIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(GalIntent,"Select Image from Gallery"),2);
    }

    /*private void RequestRuntimePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity2.this,Manifest.permission.CAMERA))
            Toast.makeText(this,"CAMERA permission allows us to access CAMERA app",Toast.LENGTH_SHORT).show();
        else
        {
            ActivityCompat.requestPermissions(MainActivity2.this,new String[]{Manifest.permission.CAMERA},RequestPermissionCode);
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult/" + requestCode + "/" + resultCode);
        if(requestCode == 0 && resultCode == RESULT_OK) {
            Log.i(TAG, "requestCode == 0");
            CropImage();
        } else if(requestCode == 2) {
            if(data != null) {
                uri = data.getData();
                CropImage();
            }
        }  else if (requestCode == 1) {
            if(data != null) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = bundle.getParcelable("data");
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private void CropImage() {

        try{
            CropIntent = new Intent("com.android.camera.action.CROP");
            CropIntent.setDataAndType(uri,"image/*");

            CropIntent.putExtra("crop","true");
            CropIntent.putExtra("outputX",180);
            CropIntent.putExtra("outputY",180);
            CropIntent.putExtra("aspectX",3);
            CropIntent.putExtra("aspectY",4);
            CropIntent.putExtra("scaleUpIfNeeded",true);
            CropIntent.putExtra("return-data",true);

            startActivityForResult(CropIntent,1);
        }
        catch (ActivityNotFoundException ex)
        {

        }

    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case RequestPermissionCode:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this,"Permission Canceled",Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }*/
}