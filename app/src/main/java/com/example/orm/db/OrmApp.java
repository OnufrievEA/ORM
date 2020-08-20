package com.example.orm.db;

import android.app.Application;

import androidx.room.Room;


public class OrmApp extends Application {

    private static final String DATABASE_NAME = "DATABASE_USER_GIT";
    private static MyDatabase database;
    private static OrmApp INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(getApplicationContext(), MyDatabase.class, DATABASE_NAME).build();
        INSTANCE = this;
    }

    public static MyDatabase getDB() {
        return database;
    }

    public static OrmApp get() {
        return INSTANCE;
    }
}
