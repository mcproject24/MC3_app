package com.example.mc_project_2;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UserClient {

    @Multipart
    @POST("predict")
    Call<ResponseBody> uploadPhoto(
            @Part MultipartBody.Part photo
            );
}
