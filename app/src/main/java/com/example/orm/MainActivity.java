package com.example.orm;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.orm.db.OrmApp;
import com.example.orm.entity.GitHubUser;
import com.example.orm.entity.UserModel;
import com.example.orm.network.RestAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mInfoTextView;
    private ProgressBar progressBar;
    Button btnLoad;
    Button btnSaveAll;
    Button btnSelectAll;
    Button btnDeleteAll;
    RestAPI restAPI;
    List<UserModel> modelList = new ArrayList<>();
    DisposableSingleObserver<Bundle> dso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInfoTextView = findViewById(R.id.tvLoad);
        progressBar = findViewById(R.id.progressBar);
        btnLoad = findViewById(R.id.btnLoad);
        btnSaveAll = findViewById(R.id.btnSaveAll);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        btnDeleteAll = findViewById(R.id.btnDeleteAll);
        btnLoad.setOnClickListener(this);
        btnSaveAll.setOnClickListener(this);
        btnSelectAll.setOnClickListener(this);
        btnDeleteAll.setOnClickListener(this);
    }

    private DisposableSingleObserver<Bundle> CreateObserver() {
        return new DisposableSingleObserver<Bundle>() {
            @Override
            protected void onStart() {
                super.onStart();
                progressBar.setVisibility(View.VISIBLE);
                mInfoTextView.setText("");
            }

            @Override
            public void onSuccess(@NonNull Bundle bundle) {
                progressBar.setVisibility(View.GONE);
                mInfoTextView.append("количество = " + bundle.getInt("count") +
                        "\n милисекунд = " + bundle.getLong("msek"));
            }

            @Override
            public void onError(@NonNull Throwable e) {
                progressBar.setVisibility(View.GONE);
                mInfoTextView.setText("ошибка БД: " + e.getMessage());
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLoad:
                mInfoTextView.setText("");
                Retrofit retrofit = null;
                try {
                    retrofit = new Retrofit.Builder()
                            .baseUrl("https://api.github.com/") // Обратить внимание на слеш в базовом адресе
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    restAPI = retrofit.create(RestAPI.class);
                } catch (Exception io) {
                    mInfoTextView.setText("no retrofit: " + io.getMessage());
                    return;
                }
                // Подготовили вызов на сервер
                Call<List<UserModel>> call = restAPI.loadUsers();
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkinfo = connectivityManager.getActiveNetworkInfo();

                if (networkinfo != null && networkinfo.isConnected()) {
                    // Запускаем
                    try {
                        progressBar.setVisibility(View.VISIBLE);
                        downloadOneUrl(call);
                    } catch (IOException e) {
                        e.printStackTrace();
                        mInfoTextView.setText(e.getMessage());
                    }
                } else {
                    Toast.makeText(this, "Подключите интернет", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnSaveAll:
                Single<Bundle> singleSaveAllRoom = Single.create(new SingleOnSubscribe<Bundle>() {
                    @Override
                    public void subscribe(@NonNull SingleEmitter<Bundle> emitter) throws Exception {
                        String curLogin = "";
                        String curUserID = "";
                        String curAvatarUrl = "";
                        Date first = new Date();
                        List<GitHubUser> roomModelList = new ArrayList<>();
                        for (UserModel curItem : modelList) {
                            GitHubUser gitHubUser = new GitHubUser();
                            curLogin = curItem.getLogin();
                            curUserID = curItem.getId();
                            curAvatarUrl = curItem.getAvatarUrl();
                            gitHubUser.setLogin(curLogin);
                            gitHubUser.setAvatarUrl(curAvatarUrl);
                            gitHubUser.setUserId(curUserID);
                            roomModelList.add(gitHubUser);
                        }
                        OrmApp.getDB().productDao().insertAll(roomModelList);

                        Date second = new Date();
                        Bundle bundle = new Bundle();
                        List<GitHubUser> tempList = OrmApp.get().getDB().productDao().getAll();
                        bundle.putInt("count", tempList.size());
                        bundle.putLong("msek", second.getTime() - first.getTime());
                        emitter.onSuccess(bundle);
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
                singleSaveAllRoom.subscribeWith(CreateObserver());
                break;
            case R.id.btnSelectAll:
                Single<Bundle> singleSelectAllRoom = Single.create(new SingleOnSubscribe<Bundle>() {
                    @Override
                    public void subscribe(@NonNull SingleEmitter<Bundle> emitter) throws Exception {
                        try {
                            Date first = new Date();
                            List<GitHubUser> products = OrmApp.get().getDB().productDao().getAll();
                            Date second = new Date();
                            Bundle bundle = new Bundle();
                            bundle.putInt("count", products.size());
                            bundle.putLong("msek", second.getTime() - first.getTime());
                            emitter.onSuccess(bundle);
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
                singleSelectAllRoom.subscribeWith(CreateObserver());
                break;
            case R.id.btnDeleteAll:
                Single<Bundle> singleDeleteAllRoom = Single.create(new SingleOnSubscribe<Bundle>() {
                    @Override
                    public void subscribe(@NonNull SingleEmitter<Bundle> emitter) throws Exception {
                        try {
                            List<GitHubUser> tempList = OrmApp.get().getDB().productDao().getAll();
                            Date first = new Date();
                            OrmApp.get().getDB().productDao().deleteAll();
                            Date second = new Date();
                            Bundle bundle = new Bundle();
                            bundle.putInt("count", tempList.size());
                            bundle.putLong("msek", second.getTime() - first.getTime());
                            emitter.onSuccess(bundle);
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
                singleDeleteAllRoom.subscribeWith(CreateObserver());
                break;
        }
    }


    private void downloadOneUrl(Call<List<UserModel>> call) throws IOException {
        call.enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful()) {
                    if (response != null) {
                        UserModel curModel = null;
                        mInfoTextView.append("\n Size = " + response.body().size() +
                                "\n-----------------");
                        for (int i = 0; i < response.body().size(); i++) {
                            curModel = response.body().get(i);
                            modelList.add(curModel);
                            mInfoTextView.append(
                                    "\nLogin = " + curModel.getLogin() +
                                            "\nId = " + curModel.getId() +
                                            "\nURI = " + curModel.getAvatarUrl() +
                                            "\n-----------------");
                        }
                    }
                } else {
                    System.out.println("onResponse error: " + response.code());
                    mInfoTextView.setText("onResponse error: " + response.code());
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                System.out.println("onFailure " + t);
                mInfoTextView.setText("onFailure " + t.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
