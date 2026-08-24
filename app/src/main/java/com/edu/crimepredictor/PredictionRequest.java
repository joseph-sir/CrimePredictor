package com.edu.crimepredictor;

import com.google.gson.annotations.SerializedName;

public class PredictionRequest {

    @SerializedName("province")
    private String province;

    @SerializedName("crime_type")
    private String crimeType;

    @SerializedName("hour")
    private int hour;

    @SerializedName("day_of_week")
    private int dayOfWeek;

    @SerializedName("month")
    private int month;

    @SerializedName("crime_count_30d")
    private int crimeCount;

    public PredictionRequest(String province, String crimeType, int hour,
                             int dayOfWeek, int month, int crimeCount) {
        this.province = province;
        this.crimeType = crimeType;
        this.hour = hour;
        this.dayOfWeek = dayOfWeek;
        this.month = month;
        this.crimeCount = crimeCount;
    }

    // Getters (optional – Retrofit can use fields directly)
    public String getProvince() { return province; }
    public String getCrimeType() { return crimeType; }
    public int getHour() { return hour; }
    public int getDayOfWeek() { return dayOfWeek; }
    public int getMonth() { return month; }
    public int getCrimeCount() { return crimeCount; }
}