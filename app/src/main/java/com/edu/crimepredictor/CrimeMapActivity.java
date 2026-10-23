package com.edu.crimepredictor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class CrimeMapActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "map_mode";

    private MapView mapView;
    private String mode;

    // Approximate coordinates for the Tembisa area.
    private final GeoPoint tembisa = new GeoPoint(-25.996, 28.226);

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
        if (mode == null) {
            mode = "PREDICTION";
        }

        TextView title = findViewById(R.id.txtMapTitle);
        TextView description = findViewById(R.id.txtMapDescription);

        if ("ALERTS".equals(mode)) {
            title.setText("CRIME ALERTS");
            description.setText("Reported incidents near Tembisa");
        } else {
            title.setText("CRIME PREDICTOR");
            description.setText("Predicted crime-risk locations");
        }

        Button back = findViewById(R.id.btnMapBack);
        back.setOnClickListener(v -> finish());

        // Set up the map
        mapView = findViewById(R.id.crimeMap);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(tembisa);

        // Add custom markers
        if ("ALERTS".equals(mode)) {
            showSimulatedAlerts();
        } else {
            showPredictedRisk();
        }
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

    private void showPredictedRisk() {
        addMarker(-25.993, 28.220,
                "High predicted risk",
                "High crime hotspot window",
                R.drawable.ic_map_pin_red);
        addMarker(-26.002, 28.232,
                "Moderate predicted risk",
                "Moderate crime risk area",
                R.drawable.ic_map_pin_amber);
        addMarker(-25.987, 28.238,
                "Lower predicted risk",
                "Relatively low risk area",
                R.drawable.ic_map_pin_green);

        Toast.makeText(this, "Displaying risk predictions",
                Toast.LENGTH_SHORT).show();
    }

    private void showSimulatedAlerts() {
        addMarker(-25.994, 28.218,
                "Assault report",
                "Recent community alert",
                R.drawable.ic_map_pin_red);
        addMarker(-26.004, 28.229,
                "Theft report",
                "Recent community alert",
                R.drawable.ic_map_pin_amber);
        addMarker(-25.989, 28.235,
                "Robbery report",
                "Recent community alert",
                R.drawable.ic_map_pin_red);

        Toast.makeText(this, "Displaying crime alerts",
                Toast.LENGTH_SHORT).show();
    }

    private void addMarker(double latitude, double longitude,
                           String title, String details, int iconResId) {
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(latitude, longitude));
        marker.setTitle(title);
        marker.setSnippet(details);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        if (iconResId != 0) {
            marker.setIcon(ContextCompat.getDrawable(this, iconResId));
        }
        mapView.getOverlays().add(marker);
    }
}
