package com.edu.crimepredictor.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "prediction_history")
public class HistoryEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String area;
    public String crime;
    public int hour;
    public int dayOfWeek;
    public int month;
    public int socioEconomicIndex;
    public int score;
    public String riskLevel;
    public long timestamp;

    public HistoryEntity() {}

    public HistoryEntity(String area, String crime, int hour, int dayOfWeek, int month,
                         int socioEconomicIndex, int score, String riskLevel, long timestamp) {
        this.area = area;
        this.crime = crime;
        this.hour = hour;
        this.dayOfWeek = dayOfWeek;
        this.month = month;
        this.socioEconomicIndex = socioEconomicIndex;
        this.score = score;
        this.riskLevel = riskLevel;
        this.timestamp = timestamp;
    }
}
