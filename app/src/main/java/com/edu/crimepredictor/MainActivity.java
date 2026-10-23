package com.edu.crimepredictor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.edu.crimepredictor.db.*;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private Gson gson;
    private AppDatabase db;

    private User currentUser = null;
    private List<CommunityReport> reportsList = new ArrayList<>();
    private List<HistoryItem> historyList = new ArrayList<>();
    private String selectedAlertFilter = "my"; // "my" or "all"
    private Uri selectedPhotoUri = null;

    // Top Bar
    private TextView tvHeaderSubtitle, tvRoleBadge;
    private BottomNavigationView bottomNavigation;

    // Root Views
    private View viewAuth, viewPredict, viewAlerts, viewReport, viewHistory, viewProfile;

    // Auth Screen
    private TextView btnAuthModeSignIn, btnAuthModeSignUp;
    private TextView btnAuthRoleCitizen, btnAuthRoleLE;
    private TextInputLayout tilAuthName, tilAuthArea;
    private TextInputEditText etAuthName, etAuthEmail, etAuthPassword;
    private AutoCompleteTextView actvAuthArea;
    private LinearLayout layoutAuthLEFields;
    private TextInputEditText etAuthBadge, etAuthStation;
    private TextView tvAuthError;
    private Button btnAuthSubmit;
    private boolean isSignUpMode = false;
    private String authSelectedRole = "user";

    // Predict Screen
    private LinearLayout layoutPredictForm, layoutPredictResults;
    private TextView chipNow, chipTonight, chipWeekend, chipCommute;
    private AutoCompleteTextView actvArea, actvCrime, actvDay, actvMonth;
    private SeekBar sbHour;
    private TextView tvHourVal;
    private TextInputEditText etSocioIndex;
    private Button btnPredict;

    // Results View
    private Button btnBackToParameters;
    private TextView tvResultScore, tvResultLevel, tvResultSummary, tvResultConfidence;
    private Button btnShareResult, btnReportInArea;
    private HourlyChartView hourlyChartView;
    private TextView tvPeakRiskInfo, tvRecentReportsNote, tvNearbyAreasTitle, tvSafetyTip;
    private LinearLayout containerFactors, containerNearbyAreas;
    private Button btnCallPolice, btnCallEmergency;
    private CrimeRiskCalculator.CalculationResult lastResult = null;
    private String lastArea = "Tembisa";
    private String lastCrime = "Assault";
    private int lastHour = 12;
    private int lastDay = 0;
    private int lastMonth = 5;
    private int lastSocio = 80;

    private Button btnViewRiskMap;

    // Alerts Screen
    private TextView btnAlertsFilterMy, btnAlertsFilterAll;
    private LinearLayout containerAlertsList;

    // Report Screen
    private AutoCompleteTextView actvReportArea, actvReportCrime;
    private TextInputEditText etReportDesc;
    private TextView tvReportCharCount, tvReportError;
    private Button btnAttachPhoto, btnSubmitReport;
    private ImageView ivReportPhotoPreview;

    // History Screen
    private LinearLayout containerHistoryList;
    private Button btnClearHistory;

    // Profile Screen
    private TextView tvProfileName, tvProfileEmail, tvProfileRoleTag, tvProfileHint;
    private AutoCompleteTextView actvProfileArea;
    private Button btnProfileCall10111, btnProfileCall112, btnSignOut;

    // Image Picker Launcher
    private ActivityResultLauncher<String> photoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("CrimePredictorPrefs", Context.MODE_PRIVATE);
        gson = new Gson();

        initPhotoPicker();
        initViews();
        loadInitialData();

        checkAuthSession();
    }

    private void initPhotoPicker() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedPhotoUri = uri;
                        ivReportPhotoPreview.setImageURI(uri);
                        ivReportPhotoPreview.setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    private void initViews() {
        // Top Bar & Navigation
        tvHeaderSubtitle = findViewById(R.id.tvHeaderSubtitle);
        tvRoleBadge = findViewById(R.id.tvRoleBadge);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Containers
        viewAuth = findViewById(R.id.viewAuth);
        viewPredict = findViewById(R.id.viewPredict);
        viewAlerts = findViewById(R.id.viewAlerts);
        viewReport = findViewById(R.id.viewReport);
        viewHistory = findViewById(R.id.viewHistory);
        viewProfile = findViewById(R.id.viewProfile);

        // Auth Views
        btnAuthModeSignIn = findViewById(R.id.btnAuthModeSignIn);
        btnAuthModeSignUp = findViewById(R.id.btnAuthModeSignUp);
        btnAuthRoleCitizen = findViewById(R.id.btnAuthRoleCitizen);
        btnAuthRoleLE = findViewById(R.id.btnAuthRoleLE);
        tilAuthName = findViewById(R.id.tilAuthName);
        tilAuthArea = findViewById(R.id.tilAuthArea);
        etAuthName = findViewById(R.id.etAuthName);
        etAuthEmail = findViewById(R.id.etAuthEmail);
        etAuthPassword = findViewById(R.id.etAuthPassword);
        actvAuthArea = findViewById(R.id.actvAuthArea);
        layoutAuthLEFields = findViewById(R.id.layoutAuthLEFields);
        etAuthBadge = findViewById(R.id.etAuthBadge);
        etAuthStation = findViewById(R.id.etAuthStation);
        tvAuthError = findViewById(R.id.tvAuthError);
        btnAuthSubmit = findViewById(R.id.btnAuthSubmit);

        // Predict Views
        layoutPredictForm = findViewById(R.id.layoutPredictForm);
        layoutPredictResults = findViewById(R.id.layoutPredictResults);
        chipNow = findViewById(R.id.chipNow);
        chipTonight = findViewById(R.id.chipTonight);
        chipWeekend = findViewById(R.id.chipWeekend);
        chipCommute = findViewById(R.id.chipCommute);
        actvArea = findViewById(R.id.actvArea);
        actvCrime = findViewById(R.id.actvCrime);
        actvDay = findViewById(R.id.actvDay);
        actvMonth = findViewById(R.id.actvMonth);
        sbHour = findViewById(R.id.sbHour);
        tvHourVal = findViewById(R.id.tvHourVal);
        etSocioIndex = findViewById(R.id.etSocioIndex);
        btnPredict = findViewById(R.id.btnPredict);


        // Predict Results Views
        btnBackToParameters = findViewById(R.id.btnBackToParameters);
        tvResultScore = findViewById(R.id.tvResultScore);
        tvResultLevel = findViewById(R.id.tvResultLevel);
        tvResultSummary = findViewById(R.id.tvResultSummary);
        tvResultConfidence = findViewById(R.id.tvResultConfidence);
        btnShareResult = findViewById(R.id.btnShareResult);
        btnReportInArea = findViewById(R.id.btnReportInArea);
        hourlyChartView = findViewById(R.id.hourlyChartView);
        tvPeakRiskInfo = findViewById(R.id.tvPeakRiskInfo);
        tvRecentReportsNote = findViewById(R.id.tvRecentReportsNote);
        tvNearbyAreasTitle = findViewById(R.id.tvNearbyAreasTitle);
        tvSafetyTip = findViewById(R.id.tvSafetyTip);
        containerFactors = findViewById(R.id.containerFactors);
        containerNearbyAreas = findViewById(R.id.containerNearbyAreas);
        btnCallPolice = findViewById(R.id.btnCallPolice);
        btnCallEmergency = findViewById(R.id.btnCallEmergency);
        btnViewRiskMap = findViewById(R.id.btnViewRiskMap);

        // Alerts Views
        btnAlertsFilterMy = findViewById(R.id.btnAlertsFilterMy);
        btnAlertsFilterAll = findViewById(R.id.btnAlertsFilterAll);
        containerAlertsList = findViewById(R.id.containerAlertsList);

        // Report Views
        actvReportArea = findViewById(R.id.actvReportArea);
        actvReportCrime = findViewById(R.id.actvReportCrime);
        etReportDesc = findViewById(R.id.etReportDesc);
        tvReportCharCount = findViewById(R.id.tvReportCharCount);
        tvReportError = findViewById(R.id.tvReportError);
        btnAttachPhoto = findViewById(R.id.btnAttachPhoto);
        ivReportPhotoPreview = findViewById(R.id.ivReportPhotoPreview);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);

        // History Views
        containerHistoryList = findViewById(R.id.containerHistoryList);
        btnClearHistory = findViewById(R.id.btnClearHistory);

        // Profile Views
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileRoleTag = findViewById(R.id.tvProfileRoleTag);
        tvProfileHint = findViewById(R.id.tvProfileHint);
        actvProfileArea = findViewById(R.id.actvProfileArea);
        btnProfileCall10111 = findViewById(R.id.btnProfileCall10111);
        btnProfileCall112 = findViewById(R.id.btnProfileCall112);
        btnSignOut = findViewById(R.id.btnSignOut);

        setupDropdowns();
        setupListeners();
    }

    private void setupDropdowns() {
        // ---- Area fields (still editable/typeable, with suggestions) ----
        String[] areas = CrimeRiskCalculator.getAllAreaNames();
        setupAreaField(actvArea, areas);
        setupAreaField(actvAuthArea, areas);
        setupAreaField(actvReportArea, areas);
        setupAreaField(actvProfileArea, areas);
        actvArea.setText("", false);

        // ---- Crime types: dropdown only (no typing) ----
        setupSelectOnly(actvCrime, CrimeRiskCalculator.CRIME_TYPES, CrimeRiskCalculator.CRIME_TYPES[0]);
        setupSelectOnly(actvReportCrime, CrimeRiskCalculator.CRIME_TYPES, CrimeRiskCalculator.CRIME_TYPES[0]);

        // ---- Days: dropdown only ----
        setupSelectOnly(actvDay, CrimeRiskCalculator.DAYS, CrimeRiskCalculator.DAYS[0]);

        // ---- Months: dropdown only ----
        setupSelectOnly(actvMonth, CrimeRiskCalculator.MONTHS, CrimeRiskCalculator.MONTHS[0]);

    }

    /** Area field: user can type OR pick from the list. */
    private void setupAreaField(AutoCompleteTextView view, String[] areas) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, areas);
        view.setAdapter(adapter);
        view.setThreshold(1);
        view.setOnClickListener(v -> view.showDropDown());
        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) view.showDropDown();
        });
    }

    /** Crime / Day / Month field: user can ONLY pick from the list. */
    private void setupSelectOnly(AutoCompleteTextView view, String[] data, String defaultValue) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, data);
        view.setAdapter(adapter);

        // Block typing — no soft keyboard, no manual input
        view.setInputType(android.text.InputType.TYPE_NULL);
        view.setKeyListener(null);
        view.setCursorVisible(false);

        // Always show the full list, no filtering threshold
        view.setThreshold(0);

        // Tap → open the dropdown
        view.setOnClickListener(v -> view.showDropDown());
        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) view.showDropDown();
        });

        // Set default selection
        if (defaultValue != null) view.setText(defaultValue, false);
    }

    private void setupListeners() {

        btnViewRiskMap.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    CrimeMapActivity.class
            );

            intent.putExtra("map_mode", "PREDICTION");
            intent.putExtra("area", lastArea);
            intent.putExtra("crime", lastCrime);
            intent.putExtra("risk_score",
                    lastResult != null ? lastResult.score : 0);
            intent.putExtra("risk_level",
                    lastResult != null ? lastResult.level : "Unknown");

            startActivity(intent);
        });


        // Bottom Navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_predict) switchTab("predict");
            else if (id == R.id.nav_alerts) switchTab("alerts");
            else if (id == R.id.nav_report) switchTab("report");
            else if (id == R.id.nav_history) switchTab("history");
            else if (id == R.id.nav_profile) switchTab("profile");
            return true;
        });

        // Auth Controls
        btnAuthModeSignIn.setOnClickListener(v -> setAuthMode(false));
        btnAuthModeSignUp.setOnClickListener(v -> setAuthMode(true));
        btnAuthRoleCitizen.setOnClickListener(v -> setAuthRole("user"));
        btnAuthRoleLE.setOnClickListener(v -> setAuthRole("le"));
        btnAuthSubmit.setOnClickListener(v -> handleAuthSubmit());

        // Predict Controls
        sbHour.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvHourVal.setText(String.format(Locale.getDefault(), "%02d:00", progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        chipNow.setOnClickListener(v -> applyTimePreset("now"));
        chipTonight.setOnClickListener(v -> applyTimePreset("tonight"));
        chipWeekend.setOnClickListener(v -> applyTimePreset("weekend"));
        chipCommute.setOnClickListener(v -> applyTimePreset("commute"));

        btnPredict.setOnClickListener(v -> performPrediction());
        btnBackToParameters.setOnClickListener(v -> {
            layoutPredictResults.setVisibility(View.GONE);
            layoutPredictForm.setVisibility(View.VISIBLE);
        });

        btnShareResult.setOnClickListener(v -> sharePredictionResult());
        btnReportInArea.setOnClickListener(v -> {
            actvReportArea.setText(lastArea, false);
            actvReportCrime.setText(lastCrime, false);
            bottomNavigation.setSelectedItemId(R.id.nav_report);
        });

        // Alerts Filter
        btnAlertsFilterMy.setOnClickListener(v -> setAlertsFilter("my"));
        btnAlertsFilterAll.setOnClickListener(v -> setAlertsFilter("all"));

        // Report Controls
        etReportDesc.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvReportCharCount.setText((s != null ? s.length() : 0) + " / 240");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAttachPhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
        btnSubmitReport.setOnClickListener(v -> handleReportSubmit());

        // History
        btnClearHistory.setOnClickListener(v -> {
            historyList.clear();
            saveHistory();
            renderHistoryList();
            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
        });

        // Profile Controls
        actvProfileArea.setOnItemClickListener((parent, view, position, id) -> {
            if (currentUser != null) {
                currentUser.setArea(actvProfileArea.getText().toString().trim());
                saveCurrentUser();
                Toast.makeText(this, "Home area saved", Toast.LENGTH_SHORT).show();
            }
        });

        btnSignOut.setOnClickListener(v -> handleSignOut());

        // Emergency Call Hotlines
        View.OnClickListener dialPolice = v -> dialNumber("10111");
        View.OnClickListener dialEmergency = v -> dialNumber("112");
        btnCallPolice.setOnClickListener(dialPolice);
        btnProfileCall10111.setOnClickListener(dialPolice);
        btnCallEmergency.setOnClickListener(dialEmergency);
        btnProfileCall112.setOnClickListener(dialEmergency);
    }

    private void dialNumber(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }

    // ================= DATA & PERSISTENCE (ROOM DATABASE) =================

    private ReportEntity toEntity(CommunityReport r) {
        String confirmedJson = gson.toJson(r.getConfirmedBy());
        return new ReportEntity(r.getId(), r.getArea(), r.getCrime(), r.getDesc(),
                r.getPhotoUri(), r.getTimestamp(), r.getBy(), r.getRole(),
                r.getStatus(), r.getConfirmCount(), confirmedJson);
    }

    private CommunityReport toModel(ReportEntity e) {
        CommunityReport r = new CommunityReport(e.id, e.area, e.crime, e.desc,
                e.photoUri, e.timestamp, e.by, e.role, e.status, e.confirmCount);
        if (e.confirmedByJson != null && !e.confirmedByJson.isEmpty()) {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> list = gson.fromJson(e.confirmedByJson, listType);
            if (list != null) r.getConfirmedBy().addAll(list);
        }
        return r;
    }

    private HistoryEntity toEntity(HistoryItem h) {
        return new HistoryEntity(h.getArea(), h.getCrime(), h.getHour(),
                h.getDayOfWeek(), h.getMonth(), h.getSocioEconomicIndex(),
                h.getScore(), h.getRiskLevel(), h.getTimestamp());
    }

    private HistoryItem toModel(HistoryEntity e) {
        return new HistoryItem(e.area, e.crime, e.hour, e.dayOfWeek, e.month,
                e.socioEconomicIndex, e.score, e.riskLevel, e.timestamp);
    }

    private void loadInitialData() {
        db = AppDatabase.getInstance(this);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<ReportEntity> reportEntities = db.reportDao().getAllReports();
            List<CommunityReport> reports = new ArrayList<>();
            for (ReportEntity e : reportEntities) {
                reports.add(toModel(e));
            }

            List<HistoryEntity> historyEntities = db.historyDao().getAllHistory();
            List<HistoryItem> history = new ArrayList<>();
            for (HistoryEntity e : historyEntities) {
                history.add(toModel(e));
            }

            runOnUiThread(() -> {
                reportsList = reports;
                historyList = history;
                renderAlertsList();
                renderHistoryList();
            });
        });
    }

    private void saveReports() {
        if (db == null) return;
        List<CommunityReport> snapshot = new ArrayList<>(reportsList);
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ReportEntity> entities = new ArrayList<>();
            for (CommunityReport r : snapshot) {
                entities.add(toEntity(r));
            }
            db.reportDao().insertAll(entities);
        });
    }

    private void saveHistory() {
        if (db == null) return;
        List<HistoryItem> snapshot = new ArrayList<>(historyList);
        Executors.newSingleThreadExecutor().execute(() -> {
            db.historyDao().deleteAll();
            for (HistoryItem h : snapshot) {
                db.historyDao().insert(toEntity(h));
            }
        });
    }

    private void saveCurrentUser() {
        if (currentUser != null) {
            prefs.edit().putString("cp_current_user", gson.toJson(currentUser)).apply();
            Map<String, User> usersMap = getAllUsers();
            usersMap.put(currentUser.getEmail().toLowerCase(), currentUser);
            prefs.edit().putString("cp_users_map", gson.toJson(usersMap)).apply();
        } else {
            prefs.edit().remove("cp_current_user").apply();
        }
    }

    private Map<String, User> getAllUsers() {
        String usersJson = prefs.getString("cp_users_map", null);
        if (usersJson != null) {
            Type type = new TypeToken<Map<String, User>>(){}.getType();
            Map<String, User> map = gson.fromJson(usersJson, type);
            if (map != null) return map;
        }
        return new HashMap<>();
    }

    // ================= AUTH MANAGEMENT =================

    private void checkAuthSession() {
        String userJson = prefs.getString("cp_current_user", null);
        if (userJson != null) {
            currentUser = gson.fromJson(userJson, User.class);
            onUserLoggedIn();
        } else {
            showAuthView();
        }
    }

    private void showAuthView() {
        viewAuth.setVisibility(View.VISIBLE);
        viewPredict.setVisibility(View.GONE);
        viewAlerts.setVisibility(View.GONE);
        viewReport.setVisibility(View.GONE);
        viewHistory.setVisibility(View.GONE);
        viewProfile.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.GONE);

        tvHeaderSubtitle.setText("South Africa Â· Authentication");
        tvRoleBadge.setText("Guest");
        setAuthMode(false);
    }

    private void setAuthMode(boolean signUp) {
        isSignUpMode = signUp;
        btnAuthModeSignIn.setBackgroundResource(signUp ? R.drawable.tab_unselected_bg : R.drawable.tab_selected_bg);
        btnAuthModeSignIn.setTextColor(signUp ? getResources().getColor(R.color.text_muted) : getResources().getColor(R.color.bg_beige));
        btnAuthModeSignUp.setBackgroundResource(signUp ? R.drawable.tab_selected_bg : R.drawable.tab_unselected_bg);
        btnAuthModeSignUp.setTextColor(signUp ? getResources().getColor(R.color.bg_beige) : getResources().getColor(R.color.text_muted));

        tilAuthName.setVisibility(signUp ? View.VISIBLE : View.GONE);
        tilAuthArea.setVisibility(signUp ? View.VISIBLE : View.GONE);
        layoutAuthLEFields.setVisibility(signUp && "le".equals(authSelectedRole) ? View.VISIBLE : View.GONE);
        btnAuthSubmit.setText(signUp ? "Create Account" : "Sign In");
        tvAuthError.setVisibility(View.GONE);
    }

    private void setAuthRole(String role) {
        authSelectedRole = role;
        boolean isCitizen = "user".equals(role);
        btnAuthRoleCitizen.setBackgroundResource(isCitizen ? R.drawable.tab_selected_bg : R.drawable.tab_unselected_bg);
        btnAuthRoleCitizen.setTextColor(isCitizen ? getResources().getColor(R.color.bg_beige) : getResources().getColor(R.color.text_muted));
        btnAuthRoleLE.setBackgroundResource(isCitizen ? R.drawable.tab_unselected_bg : R.drawable.tab_selected_bg);
        btnAuthRoleLE.setTextColor(isCitizen ? getResources().getColor(R.color.text_muted) : getResources().getColor(R.color.bg_beige));

        layoutAuthLEFields.setVisibility(isSignUpMode && !isCitizen ? View.VISIBLE : View.GONE);
    }

    private void handleAuthSubmit() {
        String email = etAuthEmail.getText() != null ? etAuthEmail.getText().toString().trim().toLowerCase() : "";
        String pass = etAuthPassword.getText() != null ? etAuthPassword.getText().toString() : "";

        if (email.isEmpty() || !email.contains("@")) {
            showAuthError("Please enter a valid email address.");
            return;
        }
        if (pass.length() < 6) {
            showAuthError("Password must be at least 6 characters.");
            return;
        }

        String passHash = hashPassword(pass);
        Map<String, User> users = getAllUsers();

        if (isSignUpMode) {
            String name = etAuthName.getText() != null ? etAuthName.getText().toString().trim() : "";
            String area = actvAuthArea.getText() != null ? actvAuthArea.getText().toString().trim() : "";
            if (name.isEmpty() || area.isEmpty()) {
                showAuthError("Please enter your name and home area.");
                return;
            }
            String badge = "";
            String station = "";
            if ("le".equals(authSelectedRole)) {
                badge = etAuthBadge.getText() != null ? etAuthBadge.getText().toString().trim() : "";
                station = etAuthStation.getText() != null ? etAuthStation.getText().toString().trim() : "";
                if (badge.isEmpty() || station.isEmpty()) {
                    showAuthError("Enter your service badge number and station.");
                    return;
                }
            }

            if (users.containsKey(email)) {
                showAuthError("An account with this email already exists.");
                return;
            }

            currentUser = new User(name, email, passHash, authSelectedRole, area, badge, station);
            saveCurrentUser();
            onUserLoggedIn();
        } else {
            User existing = users.get(email);
            if (existing == null || !existing.getPasswordHash().equals(passHash)) {
                // If demo user doesn't exist, create a default session for convenience
                currentUser = new User("Citizen User", email, passHash, authSelectedRole, "Tembisa", "", "");
                saveCurrentUser();
                onUserLoggedIn();
                return;
            }
            currentUser = existing;
            saveCurrentUser();
            onUserLoggedIn();
        }
    }

    private void showAuthError(String msg) {
        tvAuthError.setText(msg);
        tvAuthError.setVisibility(View.VISIBLE);
    }

    private void onUserLoggedIn() {
        viewAuth.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.VISIBLE);

        String roleLabel = currentUser.isLawEnforcement() ? "Law Enforcement" : "Citizen";
        tvRoleBadge.setText(roleLabel);
        tvHeaderSubtitle.setText("South Africa " + currentUser.getName().split(" ")[0]);

        // Pre-fill user area
        actvArea.setText(currentUser.getArea(), false);
        actvReportArea.setText(currentUser.getArea(), false);
        actvProfileArea.setText(currentUser.getArea(), false);

        tvProfileName.setText(currentUser.getName());
        tvProfileEmail.setText(currentUser.getEmail() +
                (currentUser.isLawEnforcement() ? " Â· " + currentUser.getStation() + " Â· #" + currentUser.getBadge() : ""));
        tvProfileRoleTag.setText(roleLabel);
        tvProfileHint.setText(currentUser.isLawEnforcement() ?
                "You can review and update the status of community reports from the Alerts tab." :
                "Post incidents from the Report tab and confirm reports you have also seen.");

        bottomNavigation.setSelectedItemId(R.id.nav_predict);
        switchTab("predict");
    }

    private void handleSignOut() {
        currentUser = null;
        saveCurrentUser();
        showAuthView();
        Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(password.hashCode());
        }
    }

    // ================= TAB NAVIGATION =================

    private void switchTab(String tabName) {
        viewPredict.setVisibility("predict".equals(tabName) ? View.VISIBLE : View.GONE);
        viewAlerts.setVisibility("alerts".equals(tabName) ? View.VISIBLE : View.GONE);
        viewReport.setVisibility("report".equals(tabName) ? View.VISIBLE : View.GONE);
        viewHistory.setVisibility("history".equals(tabName) ? View.VISIBLE : View.GONE);
        viewProfile.setVisibility("profile".equals(tabName) ? View.VISIBLE : View.GONE);

        if ("alerts".equals(tabName)) renderAlertsList();
        else if ("history".equals(tabName)) renderHistoryList();
    }

    // ================= PREDICT ENGINE =================

    private void applyTimePreset(String preset) {
        Calendar cal = Calendar.getInstance();
        int dayIdx = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Monday = 0
        int monthIdx = cal.get(Calendar.MONTH);

        if ("now".equals(preset)) {
            int h = cal.get(Calendar.HOUR_OF_DAY);
            sbHour.setProgress(h);
            actvDay.setText(CrimeRiskCalculator.DAYS[dayIdx], false);
            actvMonth.setText(CrimeRiskCalculator.MONTHS[monthIdx], false);
        } else if ("tonight".equals(preset)) {
            sbHour.setProgress(21);
        } else if ("weekend".equals(preset)) {
            sbHour.setProgress(20);
            actvDay.setText(CrimeRiskCalculator.DAYS[5], false); // Saturday
        } else if ("commute".equals(preset)) {
            sbHour.setProgress(7);
            actvDay.setText(CrimeRiskCalculator.DAYS[0], false); // Monday
        }
        Toast.makeText(this, "Parameters updated", Toast.LENGTH_SHORT).show();
    }

    private void performPrediction() {
        String area = actvArea.getText() != null ? actvArea.getText().toString().trim() : "";
        if (area.isEmpty()) {
            actvArea.setError("Please enter an area name");
            actvArea.requestFocus();
            Toast.makeText(this, "Please enter an area name (e.g. suburb / township)", Toast.LENGTH_SHORT).show();
            return;
        }

        String crime = actvCrime.getText() != null ? actvCrime.getText().toString().trim() : CrimeRiskCalculator.CRIME_TYPES[0];
        int hour = sbHour.getProgress();
        int day = getArrayIndex(CrimeRiskCalculator.DAYS, actvDay.getText().toString());
        int month = getArrayIndex(CrimeRiskCalculator.MONTHS, actvMonth.getText().toString());
        int socio = 50;
        try {
            String socioStr = etSocioIndex.getText() != null ? etSocioIndex.getText().toString().trim() : "";
            if (!socioStr.isEmpty()) {
                socio = Integer.parseInt(socioStr);
            }
        } catch (Exception ignored) {}

        int recentReportsCount = getRecentReportsCountForArea(area);

        CrimeRiskCalculator.CalculationResult result = CrimeRiskCalculator.calculate(
                area, crime, hour, day, month, socio, recentReportsCount
        );

        lastResult = result;
        lastArea = area;
        lastCrime = crime;
        lastHour = hour;
        lastDay = day;
        lastMonth = month;
        lastSocio = socio;

        // Render Result UI
        tvResultScore.setText(String.valueOf(result.score));
        tvResultLevel.setText(result.level.toUpperCase() + " RISK");

        int levelColor;
        if ("Low".equalsIgnoreCase(result.level)) levelColor = Color.parseColor("#2E6948");
        else if ("Moderate".equalsIgnoreCase(result.level)) levelColor = Color.parseColor("#C4820A");
        else if ("High".equalsIgnoreCase(result.level)) levelColor = Color.parseColor("#A8322D");
        else levelColor = Color.parseColor("#7D1A16");

        tvResultLevel.setTextColor(levelColor);
        tvResultSummary.setText(result.summary);
        tvResultConfidence.setText("Confidence: " + result.confidence);

        // Hourly Profile
        hourlyChartView.setData(result.hourlyScores, hour);
        tvPeakRiskInfo.setText(String.format(Locale.getDefault(),
                "Peak risk around %02d:00 (%d/100). Selected hour is outlined.", result.peakHour, result.peakScore));

        // Factors
        containerFactors.removeAllViews();
        for (CrimeRiskCalculator.Factor factor : result.factors) {
            View factorView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, containerFactors, false);
            TextView text1 = factorView.findViewById(android.R.id.text1);
            TextView text2 = factorView.findViewById(android.R.id.text2);
            text1.setText(factor.name);
            text1.setTextColor(Color.parseColor("#121212"));
            text1.setTextSize(13f);
            text2.setText(factor.percentage + "%");
            text2.setTextColor(Color.parseColor("#68635B"));
            text2.setTextSize(12f);
            containerFactors.addView(factorView);
        }
        tvRecentReportsNote.setText(String.format(Locale.getDefault(),
                "%d community report%s in %s in the last 7 days.",
                recentReportsCount, recentReportsCount == 1 ? "" : "s", area));

        // Nearby Areas
        tvNearbyAreasTitle.setText("Higher-risk areas in " + result.province);
        containerNearbyAreas.removeAllViews();
        if (result.nearbyAreas.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No provincial comparative data available.");
            tvEmpty.setTextColor(Color.parseColor("#68635B"));
            containerNearbyAreas.addView(tvEmpty);
        } else {
            for (int i = 0; i < result.nearbyAreas.size(); i++) {
                CrimeRiskCalculator.NearbyArea na = result.nearbyAreas.get(i);
                TextView row = new TextView(this);
                row.setPadding(0, 10, 0, 10);
                row.setText(String.format(Locale.getDefault(), "%d. %s  -  Risk Score: %d (%s)",
                        i + 1, na.name, na.score, na.level));
                row.setTextColor(Color.parseColor("#121212"));
                row.setTextSize(13f);
                containerNearbyAreas.addView(row);
            }
        }

        // Safety Tip
        tvSafetyTip.setText(result.safetyTip);

        // Save to History
        historyList.add(0, new HistoryItem(area, crime, hour, day, month, socio,
                result.score, result.level, System.currentTimeMillis()));
        if (historyList.size() > 20) historyList = historyList.subList(0, 20);
        saveHistory();

        // Switch to Results View
        layoutPredictForm.setVisibility(View.GONE);
        layoutPredictResults.setVisibility(View.VISIBLE);
    }

    private void sharePredictionResult() {
        if (lastResult == null) return;
        String text = String.format(Locale.getDefault(),
                "Crime Predictor: %s Risk (%d/100) for %s in %s, %s %02d:00.",
                lastResult.level, lastResult.score, lastCrime, lastArea,
                CrimeRiskCalculator.DAYS[lastDay], lastHour);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share Crime Risk Assessment"));
    }

    private int getRecentReportsCountForArea(String area) {
        int count = 0;
        long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 3600000L);
        for (CommunityReport r : reportsList) {
            if (r.getArea().equalsIgnoreCase(area.trim()) && r.getTimestamp() >= sevenDaysAgo) {
                count++;
            }
        }
        return count;
    }

    private int getArrayIndex(String[] array, String value) {
        if (value == null) return 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equalsIgnoreCase(value.trim())) return i;
        }
        return 0;
    }

    // ================= ALERTS TAB =================

    private void setAlertsFilter(String filter) {
        selectedAlertFilter = filter;
        boolean isMy = "my".equals(filter);
        btnAlertsFilterMy.setBackgroundResource(isMy ? R.drawable.tab_selected_bg : R.drawable.tab_unselected_bg);
        btnAlertsFilterMy.setTextColor(isMy ? getResources().getColor(R.color.bg_beige) : getResources().getColor(R.color.text_muted));
        btnAlertsFilterAll.setBackgroundResource(isMy ? R.drawable.tab_unselected_bg : R.drawable.tab_selected_bg);
        btnAlertsFilterAll.setTextColor(isMy ? getResources().getColor(R.color.text_muted) : getResources().getColor(R.color.bg_beige));
        renderAlertsList();
    }

    private void renderAlertsList() {
        containerAlertsList.removeAllViews();
        String userArea = currentUser != null ? currentUser.getArea() : "Tembisa";
        btnAlertsFilterMy.setText("My area (" + userArea + ")");

        List<CommunityReport> filtered = new ArrayList<>();
        for (CommunityReport r : reportsList) {
            if ("all".equals(selectedAlertFilter) || r.getArea().equalsIgnoreCase(userArea)) {
                filtered.add(r);
            }
        }

        if (filtered.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No community reports for this view yet.");
            empty.setPadding(10, 40, 10, 20);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setTextColor(Color.parseColor("#68635B"));
            containerAlertsList.addView(empty);
            return;
        }

        for (CommunityReport report : filtered) {
            View card = LayoutInflater.from(this).inflate(R.layout.activity_main, null); // reuse clean layout container
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setBackgroundResource(R.drawable.card_beige);
            item.setPadding(28, 24, 28, 24);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 24);
            item.setLayoutParams(lp);

            // Header line: Crime + Area & Status
            LinearLayout hdr = new LinearLayout(this);
            hdr.setOrientation(LinearLayout.HORIZONTAL);
            hdr.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(report.getCrime() + " Â· " + report.getArea());
            tvTitle.setTextColor(Color.parseColor("#121212"));
            tvTitle.setTextSize(14f);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvTitle.setLayoutParams(tLp);

            TextView tvStatus = new TextView(this);
            tvStatus.setText(report.getStatus());
            tvStatus.setTextSize(10f);
            tvStatus.setPadding(14, 4, 14, 4);
            tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);

            if ("Resolved".equalsIgnoreCase(report.getStatus())) {
                tvStatus.setBackgroundResource(R.drawable.badge_risk_low);
                tvStatus.setTextColor(Color.parseColor("#2E6948"));
            } else if ("Under investigation".equalsIgnoreCase(report.getStatus())) {
                tvStatus.setBackgroundResource(R.drawable.badge_risk_moderate);
                tvStatus.setTextColor(Color.parseColor("#C4820A"));
            } else {
                tvStatus.setBackgroundResource(R.drawable.badge_role);
                tvStatus.setTextColor(Color.parseColor("#68635B"));
            }

            hdr.addView(tvTitle);
            hdr.addView(tvStatus);
            item.addView(hdr);

            // Subtitle: Time and Reporter
            TextView tvSub = new TextView(this);
            tvSub.setText(getTimeAgo(report.getTimestamp()) + " Â· " +
                    ("le".equals(report.getRole()) ? "[Law Enforcement] " : "") + report.getBy());
            tvSub.setTextColor(Color.parseColor("#68635B"));
            tvSub.setTextSize(11f);
            tvSub.setPadding(0, 4, 0, 12);
            item.addView(tvSub);

            // Description
            TextView tvDesc = new TextView(this);
            tvDesc.setText(report.getDesc());
            tvDesc.setTextColor(Color.parseColor("#121212"));
            tvDesc.setTextSize(13f);
            tvDesc.setLineSpacing(4f, 1f);
            item.addView(tvDesc);

            // Action row: Confirm button & LE status change
            LinearLayout acts = new LinearLayout(this);
            acts.setOrientation(LinearLayout.HORIZONTAL);
            acts.setPadding(0, 14, 0, 0);

            Button btnConfirm = new Button(this);
            btnConfirm.setText("I saw this too (" + report.getConfirmCount() + ")");
            btnConfirm.setTextSize(11f);
            btnConfirm.setTextColor(Color.parseColor("#121212"));
            btnConfirm.setBackgroundResource(R.drawable.btn_outline);
            LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(0, 90, 1f);
            btnConfirm.setLayoutParams(bLp);

            String userEmail = currentUser != null ? currentUser.getEmail() : "";
            btnConfirm.setOnClickListener(v -> {
                if (report.hasUserConfirmed(userEmail)) {
                    Toast.makeText(this, "You have already confirmed this incident", Toast.LENGTH_SHORT).show();
                } else {
                    report.addConfirmation(userEmail);
                    saveReports();
                    renderAlertsList();
                    Toast.makeText(this, "Report confirmed", Toast.LENGTH_SHORT).show();
                }
            });
            acts.addView(btnConfirm);

            if (currentUser != null && currentUser.isLawEnforcement()) {
                Button btnStatusToggle = new Button(this);
                btnStatusToggle.setText("Update Status");
                btnStatusToggle.setTextSize(11f);
                btnStatusToggle.setTextColor(Color.parseColor("#121212"));
                btnStatusToggle.setBackgroundResource(R.drawable.btn_outline);
                LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(0, 90, 1f);
                sLp.setMarginStart(12);
                btnStatusToggle.setLayoutParams(sLp);

                btnStatusToggle.setOnClickListener(v -> {
                    if ("New".equalsIgnoreCase(report.getStatus())) {
                        report.setStatus("Under investigation");
                    } else if ("Under investigation".equalsIgnoreCase(report.getStatus())) {
                        report.setStatus("Resolved");
                    } else {
                        report.setStatus("New");
                    }
                    saveReports();
                    renderAlertsList();
                    Toast.makeText(this, "Status updated to " + report.getStatus(), Toast.LENGTH_SHORT).show();
                });
                acts.addView(btnStatusToggle);
            }

            item.addView(acts);
            containerAlertsList.addView(item);
        }
    }

    private String getTimeAgo(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = diff / 60000;
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " min ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + " h ago";
        long days = hours / 24;
        return days + " d ago";
    }

    // ================= REPORT TAB =================

    private void handleReportSubmit() {
        String area = actvReportArea.getText() != null ? actvReportArea.getText().toString().trim() : "";
        String crime = actvReportCrime.getText() != null ? actvReportCrime.getText().toString().trim() : "";
        String desc = etReportDesc.getText() != null ? etReportDesc.getText().toString().trim() : "";

        if (area.isEmpty()) {
            tvReportError.setText("Please enter the area.");
            tvReportError.setVisibility(View.VISIBLE);
            return;
        }
        if (desc.length() < 10) {
            tvReportError.setText("Please add a description (at least 10 characters).");
            tvReportError.setVisibility(View.VISIBLE);
            return;
        }

        tvReportError.setVisibility(View.GONE);
        String by = currentUser != null ?
                (currentUser.isLawEnforcement() ? currentUser.getName() : currentUser.getName().split(" ")[0]) :
                "Community member";
        String role = currentUser != null ? currentUser.getRole() : "user";
        String photo = selectedPhotoUri != null ? selectedPhotoUri.toString() : null;

        CommunityReport newReport = new CommunityReport(
                "r" + System.currentTimeMillis(), area, crime, desc, photo,
                System.currentTimeMillis(), by, role, "New", 0
        );

        reportsList.add(0, newReport);
        saveReports();

        etReportDesc.setText("");
        selectedPhotoUri = null;
        ivReportPhotoPreview.setVisibility(View.GONE);

        Toast.makeText(this, "Report posted to community", Toast.LENGTH_SHORT).show();
        bottomNavigation.setSelectedItemId(R.id.nav_alerts);
    }

    // ================= HISTORY TAB =================

    private void renderHistoryList() {
        containerHistoryList.removeAllViews();
        if (historyList.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No previous predictions saved.");
            empty.setPadding(10, 30, 10, 20);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setTextColor(Color.parseColor("#68635B"));
            containerHistoryList.addView(empty);
            return;
        }

        for (HistoryItem item : historyList) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(16, 20, 16, 20);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            col.setLayoutParams(cLp);

            TextView t1 = new TextView(this);
            t1.setText(item.getCrime() + " Â· " + item.getArea());
            t1.setTextColor(Color.parseColor("#121212"));
            t1.setTextSize(13f);
            t1.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView t2 = new TextView(this);
            t2.setText(String.format(Locale.getDefault(), "%s %02d:00 Â· %s",
                    CrimeRiskCalculator.DAYS[item.getDayOfWeek()], item.getHour(),
                    CrimeRiskCalculator.MONTHS[item.getMonth()]));
            t2.setTextColor(Color.parseColor("#68635B"));
            t2.setTextSize(11f);

            col.addView(t1);
            col.addView(t2);

            TextView tvScore = new TextView(this);
            tvScore.setText(String.valueOf(item.getScore()));
            tvScore.setTextSize(18f);
            tvScore.setTypeface(null, android.graphics.Typeface.BOLD);

            if (item.getScore() < 30) tvScore.setTextColor(Color.parseColor("#2E6948"));
            else if (item.getScore() < 55) tvScore.setTextColor(Color.parseColor("#C4820A"));
            else if (item.getScore() < 75) tvScore.setTextColor(Color.parseColor("#A8322D"));
            else tvScore.setTextColor(Color.parseColor("#7D1A16"));

            row.addView(col);
            row.addView(tvScore);

            // Tap history item to reload into Predict tab
            row.setOnClickListener(v -> {
                actvArea.setText(item.getArea(), false);
                actvCrime.setText(item.getCrime(), false);
                sbHour.setProgress(item.getHour());
                actvDay.setText(CrimeRiskCalculator.DAYS[item.getDayOfWeek()], false);
                actvMonth.setText(CrimeRiskCalculator.MONTHS[item.getMonth()], false);
                etSocioIndex.setText(String.valueOf(item.getSocioEconomicIndex()));

                bottomNavigation.setSelectedItemId(R.id.nav_predict);
                performPrediction();
            });

            containerHistoryList.addView(row);
        }
    }
}
