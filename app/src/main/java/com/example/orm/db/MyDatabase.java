package com.example.orm.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.orm.entity.GitHubUser;

@Database(entities = {GitHubUser.class}, version = 1)
public abstract class MyDatabase extends RoomDatabase {
    public abstract RoomModelDao productDao();
}

