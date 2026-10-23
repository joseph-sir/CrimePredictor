package com.edu.crimepredictor.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "community_reports")
public class ReportEntity {
    @PrimaryKey
    @NonNull
    public String id = "";

    public String area;
    public String crime;
    public String desc;
    public String photoUri;
    public long timestamp;
    public String by;
    public String role;
    public String status;
    public int confirmCount;
    public String confirmedByJson;

    public ReportEntity() {}

    public ReportEntity(@NonNull String id, String area, String crime, String desc, String photoUri,
                        long timestamp, String by, String role, String status, int confirmCount,
                        String confirmedByJson) {
        this.id = id;
        this.area = area;
        this.crime = crime;
        this.desc = desc;
        this.photoUri = photoUri;
        this.timestamp = timestamp;
        this.by = by;
        this.role = role;
        this.status = status;
        this.confirmCount = confirmCount;
        this.confirmedByJson = confirmedByJson;
    }
}
