package com.edu.crimepredictor.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReportDao {
    @Query("SELECT * FROM community_reports ORDER BY timestamp DESC")
    List<ReportEntity> getAllReports();

    @Query("SELECT * FROM community_reports WHERE LOWER(area) = LOWER(:area) ORDER BY timestamp DESC")
    List<ReportEntity> getReportsForArea(String area);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReportEntity report);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ReportEntity> reports);

    @Update
    void update(ReportEntity report);

    @Query("DELETE FROM community_reports WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM community_reports")
    void deleteAll();
}
