package com.example.orm.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.orm.entity.GitHubUser;

import java.util.List;

@Dao
public interface RoomModelDao {

    @Query("SELECT * FROM GitHubUser")
    abstract List<GitHubUser> getAll();

    @Query("SELECT * FROM GitHubUser WHERE userId LIKE :userId LIMIT 1")
    GitHubUser findByUserId(String userId);

    @Insert
    void insertAll(List<GitHubUser> item);

    @Update
    void update(GitHubUser item);

    @Delete
    void delete(GitHubUser item);

    @Query("DELETE FROM GitHubUser")
    void deleteAll();

}
