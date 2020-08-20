package com.example.orm.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInit {

    private static final String BASE_URL = "https://api.github.com/";

    private static RestAPI api;

    public static synchronized RestAPI newApiInstance() {
        if (api == null) {
            api = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(RestAPI.class);
        }
        return api;
    }
}
