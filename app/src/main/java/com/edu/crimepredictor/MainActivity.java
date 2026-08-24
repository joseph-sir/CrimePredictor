package com.edu.crimepredictor;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private AutoCompleteTextView spinnerProvince, spinnerCrimeType, spinnerDay, spinnerMonth;
    private SeekBar seekBarHour;
    private TextView tvHourValue, tvResult, tvRiskScore, tvConfidence, tvError;
    private EditText etCrimeCount;
    private Button btnPredict;
    private ProgressBar progressBar;
    private LinearLayout resultContainer;

    // Data Arrays
    private String[] provinces = {
            "Gauteng", "Western Cape", "KwaZulu-Natal", "Eastern Cape",
            "Limpopo", "Mpumalanga", "North West", "Free State", "Northern Cape"
    };
    private String[] crimeTypes = {
            "Assault", "Robbery", "Burglary", "Theft", "Murder",
            "Sexual Offence", "Drug-related", "Carjacking", "Fraud"
    };
    private String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private String[] months = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupSpinners();
        setupSeekBar();
        setupApi();

        btnPredict.setOnClickListener(v -> performPrediction());
    }

    private void initViews() {
        spinnerProvince = findViewById(R.id.spinnerProvince);
        spinnerCrimeType = findViewById(R.id.spinnerCrimeType);
        spinnerDay = findViewById(R.id.spinnerDay);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        seekBarHour = findViewById(R.id.seekBarHour);
        tvHourValue = findViewById(R.id.tvHourValue);
        etCrimeCount = findViewById(R.id.etCrimeCount);
        btnPredict = findViewById(R.id.btnPredict);
        progressBar = findViewById(R.id.progressBar);
        tvResult = findViewById(R.id.tvResult);
        tvRiskScore = findViewById(R.id.tvRiskScore);
        tvConfidence = findViewById(R.id.tvConfidence);
        tvError = findViewById(R.id.tvError);
        resultContainer = findViewById(R.id.resultContainer);
    }

    private void setupSpinners() {
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, provinces);
        spinnerProvince.setAdapter(provinceAdapter);
        spinnerProvince.setText(provinces[0], false);

        ArrayAdapter<String> crimeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, crimeTypes);
        spinnerCrimeType.setAdapter(crimeAdapter);
        spinnerCrimeType.setText(crimeTypes[0], false);

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, days);
        spinnerDay.setAdapter(dayAdapter);
        spinnerDay.setText(days[0], false);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, months);
        spinnerMonth.setAdapter(monthAdapter);
        spinnerMonth.setText(months[5], false);
    }

    private void setupSeekBar() {
        seekBarHour.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvHourValue.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupApi() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        // CHANGE THIS TO YOUR PC'S IP ADDRESS
        String BASE_URL = "http://192.168.1.100:8000/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void performPrediction() {
        String province = spinnerProvince.getText().toString().trim();
        String crimeType = spinnerCrimeType.getText().toString().trim();
        String crimeCountStr = etCrimeCount.getText().toString().trim();

        if (crimeCountStr.isEmpty()) {
            showError("Please enter a crime count");
            return;
        }

        int hour = seekBarHour.getProgress();
        int dayOfWeek = getDayIndex(spinnerDay.getText().toString());
        int month = getMonthIndex(spinnerMonth.getText().toString());
        int crimeCount = Integer.parseInt(crimeCountStr);

        showLoading(true);
        hideResult();

        PredictionRequest request = new PredictionRequest(
                province, crimeType, hour, dayOfWeek, month, crimeCount
        );

        Call<PredictionResponse> call = apiService.predict(request);
        call.enqueue(new Callback<PredictionResponse>() {
            @Override
            public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    displayResult(response.body());
                } else {
                    showError("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PredictionResponse> call, Throwable t) {
                showLoading(false);
                showError("Connection error: " + t.getMessage());
            }
        });
    }

    private void displayResult(PredictionResponse result) {
        resultContainer.setVisibility(View.VISIBLE);
        boolean isHighRisk = result.getPrediction().equals("High Risk");

        tvResult.setText(result.getPrediction());
        tvResult.setBackgroundResource(
                isHighRisk ? R.drawable.risk_high_bg : R.drawable.risk_low_bg
        );
        tvResult.setTextColor(getResources().getColor(android.R.color.white));

        tvRiskScore.setText(String.format("%.1f%%", result.getRiskScore() * 100));
        tvConfidence.setText(String.format("%.1f%%", result.getConfidence() * 100));

        tvError.setVisibility(View.GONE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnPredict.setEnabled(!show);
        btnPredict.setText(show ? "PREDICTING..." : "PREDICT HOTSPOT RISK");
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        resultContainer.setVisibility(View.GONE);
    }

    private void hideResult() {
        resultContainer.setVisibility(View.GONE);
    }

    private int getDayIndex(String day) {
        for (int i = 0; i < days.length; i++) {
            if (days[i].equalsIgnoreCase(day)) return i;
        }
        return 0;
    }

    private int getMonthIndex(String month) {
        for (int i = 0; i < months.length; i++) {
            if (months[i].equalsIgnoreCase(month)) return i + 1;
        }
        return 6;
    }
}