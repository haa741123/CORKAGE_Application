package com.example.test;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class VisionController {
    private static final String BASE_URL = "https://vision.googleapis.com/v1/";
    private static final String API_KEY = "YOUR_API_KEY"; // 여기에 API 키를 입력합니다

    public static VisionAPI create() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(VisionAPI.class);
    }

    public interface VisionAPI {
        @Headers("Content-Type: application/json")
        @POST("images:annotate?key=" + API_KEY)
        Call<VisionAPIResponse> detectText(@Body VisionAPIRequest request);
    }
}