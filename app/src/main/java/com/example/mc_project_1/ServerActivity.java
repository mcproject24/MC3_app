package com.example.mc_project_1;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

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

public class ServerActivity extends AppCompatActivity {
    private Uri imageUri;
    private String type;
    private Boolean created = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        Bundle bundle = getIntent().getExtras();
        imageUri = bundle.getParcelable("image");


        Bundle args = new Bundle();
        args.putString("dialog_title", "Do you want to send picture to server?");
        args.putString("dialog_msg", "This service allows you to send the picture and save it to a remote server");


        Spinner spinner = findViewById(R.id.spinner);
        Button submitButton = findViewById(R.id.submitButton);

        CustomDialogFragment obj2 = new CustomDialogFragment();

        obj2.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        obj2.show(fragmentManager, "dialog2");


        //after yes set spinner and retrofit action

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.photo_types, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        submitButton.setOnClickListener(v -> submit());
        submitButton.setVisibility(View.GONE);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!created) {
                    created = true;
                } else {
                    if (position > 0) {
                        type = parent.getItemAtPosition(position).toString();
                        submitButton.setVisibility(View.VISIBLE);
                    } else {
                        submitButton.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    public void submit() {
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl("http://192.168.0.16:5000/").addConverterFactory(GsonConverterFactory.create());

        File f = new File(getRealPathFromURI(this, imageUri));

        RequestBody filePart = RequestBody.create(MediaType.parse(this.getContentResolver().getType(imageUri)), f);
        MultipartBody.Part file = MultipartBody.Part.createFormData("photo", f.getName(), filePart);
        RequestBody descriptionPart = RequestBody.create(MultipartBody.FORM, type);

        Retrofit retrofit = builder.build();
        UserClient client = retrofit.create(UserClient.class);
        Log.d("client", "client configured");
        Call<ResponseBody> call = client.uploadPhoto(descriptionPart, file);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(ServerActivity.this, "Success", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ServerActivity.this, "Data transfer failed", Toast.LENGTH_SHORT).show();
                Log.d("network", t.getMessage());
                finishAndRemoveTask();
            }
        });
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e("path error", "getRealPathFromURI Exception : " + e);
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}