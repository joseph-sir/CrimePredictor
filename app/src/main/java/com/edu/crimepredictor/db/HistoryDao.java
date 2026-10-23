package com.edu.crimepredictor.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryDao {
    @Query("SELECT * FROM prediction_history ORDER BY timestamp DESC")
    List<HistoryEntity> getAllHistory();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HistoryEntity item);

    @Query("DELETE FROM prediction_history")
    void deleteAll();
}
