package com.edu.crimepredictor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrimeMapActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "map_mode";
    public static final String EXTRA_AREA = "area";
    public static final String EXTRA_CRIME = "crime";
    public static final String EXTRA_RISK_SCORE = "risk_score";
    public static final String EXTRA_RISK_LEVEL = "risk_level";

    private MapView mapView;
    private String mode;
    private String areaName;
    private String crimeType;
    private int riskScore;

    // South African Area Coordinates Lookup
    private static final Map<String, GeoPoint> AREA_COORDINATES = new HashMap<>();
    static {
        // Gauteng
        AREA_COORDINATES.put("tembisa", new GeoPoint(-25.996, 28.226));
        AREA_COORDINATES.put("soweto", new GeoPoint(-26.248, 27.854));
        AREA_COORDINATES.put("alexandra", new GeoPoint(-26.103, 28.093));
        AREA_COORDINATES.put("johannesburg cbd", new GeoPoint(-26.204, 28.047));
        AREA_COORDINATES.put("jhb cbd", new GeoPoint(-26.204, 28.047));
        AREA_COORDINATES.put("pretoria cbd", new GeoPoint(-25.747, 28.188));
        AREA_COORDINATES.put("mamelodi", new GeoPoint(-25.708, 28.375));
        AREA_COORDINATES.put("soshanguve", new GeoPoint(-25.534, 28.106));
        AREA_COORDINATES.put("sandton", new GeoPoint(-26.107, 28.056));
        AREA_COORDINATES.put("centurion", new GeoPoint(-25.860, 28.189));

        // Western Cape
        AREA_COORDINATES.put("khayelitsha", new GeoPoint(-33.957, 18.677));
        AREA_COORDINATES.put("nyanga", new GeoPoint(-33.992, 18.583));
        AREA_COORDINATES.put("mitchells plain", new GeoPoint(-34.048, 18.618));
        AREA_COORDINATES.put("cape town cbd", new GeoPoint(-33.924, 18.424));

        // KwaZulu-Natal
        AREA_COORDINATES.put("umlazi", new GeoPoint(-29.967, 30.883));
        AREA_COORDINATES.put("inanda", new GeoPoint(-29.692, 30.933));
        AREA_COORDINATES.put("durban cbd", new GeoPoint(-29.858, 31.021));
        AREA_COORDINATES.put("pietermaritzburg", new GeoPoint(-29.600, 30.379));

        // Eastern Cape
        AREA_COORDINATES.put("mdantsane", new GeoPoint(-32.917, 27.717));
        AREA_COORDINATES.put("gqeberha north end", new GeoPoint(-33.945, 25.610));
        AREA_COORDINATES.put("gqeberha", new GeoPoint(-33.960, 25.602));
        AREA_COORDINATES.put("mthatha", new GeoPoint(-31.589, 28.784));

        // Free State
        AREA_COORDINATES.put("bloemfontein cbd", new GeoPoint(-29.118, 26.225));
        AREA_COORDINATES.put("mangaung", new GeoPoint(-29.218, 26.283));

        // Limpopo
        AREA_COORDINATES.put("polokwane", new GeoPoint(-23.904, 29.468));
        AREA_COORDINATES.put("thohoyandou", new GeoPoint(-22.948, 30.484));

        // Mpumalanga
        AREA_COORDINATES.put("mbombela", new GeoPoint(-25.475, 30.969));
        AREA_COORDINATES.put("emalahleni", new GeoPoint(-25.872, 29.232));

        // North West
        AREA_COORDINATES.put("rustenburg", new GeoPoint(-25.668, 27.242));
        AREA_COORDINATES.put("mahikeng", new GeoPoint(-25.865, 25.644));

        // Northern Cape
        AREA_COORDINATES.put("kimberley", new GeoPoint(-28.728, 24.749));
        AREA_COORDINATES.put("upington", new GeoPoint(-28.447, 21.256));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // MUST load configuration & user agent before creating/inflating the map view
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_crime_map);

        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = "PREDICTION";

        areaName = getIntent().getStringExtra(EXTRA_AREA);
        if (areaName == null || areaName.trim().isEmpty()) areaName = "Tembisa";

        crimeType = getIntent().getStringExtra(EXTRA_CRIME);
        if (crimeType == null || crimeType.trim().isEmpty()) crimeType = "Assault";

        riskScore = getIntent().getIntExtra(EXTRA_RISK_SCORE, 75);

        TextView title = findViewById(R.id.txtMapTitle);
        TextView description = findViewById(R.id.txtMapDescription);

        if ("ALERTS".equals(mode)) {
            title.setText("CRIME ALERTS HEATMAP");
            description.setText("Incident density zones near " + areaName);
        } else {
            title.setText("CRIME RISK HEATMAP");
            description.setText(crimeType + " risk density in " + areaName);
        }

        Button back = findViewById(R.id.btnMapBack);
        back.setOnClickListener(v -> finish());

        // Set up the map
        mapView = findViewById(R.id.crimeMap);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        GeoPoint centerPoint = getGeoPointForArea(areaName);
        mapView.getController().setZoom(13.8);
        mapView.getController().setCenter(centerPoint);

        // Render Heatmap overlay (Red, Amber, Green density zones around the location)
        renderHeatmapOverlay(centerPoint);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDetach();
    }

    private GeoPoint getGeoPointForArea(String inputArea) {
        if (inputArea == null || inputArea.trim().isEmpty()) {
            return new GeoPoint(-25.996, 28.226); // Default Tembisa
        }
        String key = inputArea.trim().toLowerCase();
        if (AREA_COORDINATES.containsKey(key)) {
            return AREA_COORDINATES.get(key);
        }
        for (Map.Entry<String, GeoPoint> entry : AREA_COORDINATES.entrySet()) {
            if (entry.getKey().contains(key) || key.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return new GeoPoint(-25.996, 28.226); // Default Tembisa fallback
    }

    /**
     * Renders a continuous, multi-layered heatmap overlay using semi-transparent polygons
     * representing Red (High Risk), Amber (Moderate Risk), and Green (Safe) crime density zones.
     */
    private void renderHeatmapOverlay(GeoPoint centerPoint) {
        mapView.getOverlays().clear();

        double lat = centerPoint.getLatitude();
        double lon = centerPoint.getLongitude();

        // Safe / Low Risk Outer Layer (Green Zone - ~3200m radius)
        Polygon greenZone = new Polygon(mapView);
        greenZone.setPoints(Polygon.pointsAsCircle(centerPoint, 3200));
        greenZone.getFillPaint().setColor(0x35388E3C);   // Semi-transparent Green
        greenZone.getOutlinePaint().setColor(0x88388E3C); // Green Border
        greenZone.getOutlinePaint().setStrokeWidth(2.0f);
        greenZone.setTitle("Safe / Lower Risk Zone (" + areaName + ")");
        mapView.getOverlays().add(greenZone);

        // Moderate Risk Middle Layer (Amber Zone - ~2000m radius, slightly offset for natural geography)
        GeoPoint amberCenter = new GeoPoint(lat + 0.003, lon - 0.002);
        Polygon amberZone = new Polygon(mapView);
        amberZone.setPoints(Polygon.pointsAsCircle(amberCenter, 2000));
        amberZone.getFillPaint().setColor(0x45F57C00);   // Semi-transparent Amber
        amberZone.getOutlinePaint().setColor(0x99F57C00); // Amber Border
        amberZone.getOutlinePaint().setStrokeWidth(2.0f);
        amberZone.setTitle("Moderate Risk Zone");
        mapView.getOverlays().add(amberZone);

        // High Risk Hotspot Layer (Red Zone - ~1000m radius around primary crime node)
        GeoPoint redCenter;
        if (riskScore >= 60) {
            redCenter = centerPoint; // Center of area is primary hotspot
        } else {
            redCenter = new GeoPoint(lat - 0.005, lon + 0.004); // Offset hotspot for lower risk areas
        }

        Polygon redHotspot = new Polygon(mapView);
        redHotspot.setPoints(Polygon.pointsAsCircle(redCenter, 1100));
        redHotspot.getFillPaint().setColor(0x55D32F2F);   // Deep Semi-transparent Red
        redHotspot.getOutlinePaint().setColor(0xBBD32F2F); // Red Border
        redHotspot.getOutlinePaint().setStrokeWidth(2.5f);
        redHotspot.setTitle("High Risk Crime Hotspot (" + crimeType + ")");
        mapView.getOverlays().add(redHotspot);

        // Secondary High-Density Hotspot Core (~500m radius)
        Polygon redCore = new Polygon(mapView);
        redCore.setPoints(Polygon.pointsAsCircle(redCenter, 500));
        redCore.getFillPaint().setColor(0x75B71C1C);   // Concentrated Red Core
        redCore.getOutlinePaint().setColor(0xFFB71C1C);
        redCore.getOutlinePaint().setStrokeWidth(2.0f);
        redCore.setTitle("Critical Crime Concentration Core");
        mapView.getOverlays().add(redCore);
        redCore.setTitle("Critical Crime Concentration Core");
        mapView.getOverlays().add(redCore);

        mapView.invalidate();

        Toast.makeText(this, "Heatmap loaded for " + crimeType + " in " + areaName, Toast.LENGTH_SHORT).show();
    }
}
