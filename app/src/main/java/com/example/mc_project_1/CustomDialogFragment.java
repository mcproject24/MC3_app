package com.example.mc_project_1;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.core.ImageCapture;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.fragment.app.DialogFragment;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CustomDialogFragment extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        String dialog_msg = getArguments().getString("dialog_msg");
        String dialog_title = getArguments().getString("dialog_title");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(dialog_msg)
                .setTitle(dialog_title)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d("check1", "working properly");
                    }
             })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //close the app
                        getActivity().finishAndRemoveTask();
                    }
                });
    return builder.create();
    }

}
