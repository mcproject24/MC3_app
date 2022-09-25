package com.example.mc_project_1;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private ImageCapture imageCapture;

    private int counter = 0;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Permissions for sdk greater than api 28
            String[] permissions = new String[3];
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissions[0] = Manifest.permission.CAMERA;
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions[1] = Manifest.permission.READ_EXTERNAL_STORAGE;
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions[2] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            }
            ActivityCompat.requestPermissions(this, permissions, 100);
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        previewView = findViewById(R.id.previewView);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraProvider", "Failed to load");
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onStart() {
        super.onStart();
        Bundle args = new Bundle();

        args.putString("dialog_title", "Do you want to take a picture?");
        args.putString("dialog_msg", "This service allows you to take a picture and upload it to the server");
        args.putString("dialog_num", "one");

        CustomDialogFragment obj = new CustomDialogFragment();
        obj.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        obj.show(fragmentManager, "dialog1");

    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void click(@NonNull View v) {

        if (v.getId() == R.id.cameraButton) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, (counter++));
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

            ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build();
            Log.d("tag", "coming till here");
            imageCapture.takePicture(outputFileOptions, getMainExecutor(),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            // ask if it needs to be sent to the server
                            Toast.makeText(getApplicationContext(), "Photo saved", Toast.LENGTH_SHORT).show();
                            Uri u = outputFileResults.getSavedUri();
                            Intent i = new Intent(MainActivity.this, ServerActivity.class);
                            i.putExtra("image", u);
                            startActivity(i);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException error) {
                            Log.d("tag", error.toString());
                        }
                    }
            );
        }
    }


}