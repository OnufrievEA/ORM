package com.example.orm.network;

import com.example.orm.entity.UserModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RestAPI {
    @GET("users")
    Call<List<UserModel>> loadUsers();
}
