package com.edu.crimepredictor.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);
}
