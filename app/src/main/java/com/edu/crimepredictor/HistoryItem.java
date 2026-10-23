package com.edu.crimepredictor;

import java.io.Serializable;

public class HistoryItem implements Serializable {
    private String area;
    private String crime;
    private int hour;
    private int dayOfWeek;
    private int month;
    private int socioEconomicIndex;
    private int score;
    private String riskLevel;
    private long timestamp;

    public HistoryItem() {}

    public HistoryItem(String area, String crime, int hour, int dayOfWeek, int month,
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

    public String getArea() { return area; }
    public String getCrime() { return crime; }
    public int getHour() { return hour; }
    public int getDayOfWeek() { return dayOfWeek; }
    public int getMonth() { return month; }
    public int getSocioEconomicIndex() { return socioEconomicIndex; }
    public int getScore() { return score; }
    public String getRiskLevel() { return riskLevel; }
    public long getTimestamp() { return timestamp; }
}
