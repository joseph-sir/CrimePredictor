package com.edu.crimepredictor;

import com.google.gson.annotations.SerializedName;

public class PredictionResponse {

    @SerializedName("prediction")
    private String prediction;

    @SerializedName("risk_score")
    private double riskScore;

    @SerializedName("confidence")
    private double confidence;

    public String getPrediction() { return prediction; }
    public double getRiskScore() { return riskScore; }
    public double getConfidence() { return confidence; }
}