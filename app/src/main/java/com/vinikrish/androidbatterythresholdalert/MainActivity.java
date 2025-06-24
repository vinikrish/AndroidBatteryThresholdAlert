// MainActivity.java
package com.vinikrish.androidbatterythresholdalert;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {

    private EditText editMin, editMax;
    private TextView txtStatus;
    private SharedPreferences sharedPreferences;
    private Handler handler = new Handler();
    private static final int PERMISSION_REQUEST_CODE = 1;

    private final Runnable batteryCheckRunnable = new Runnable() {
        @Override
        public void run() {
            int level = getBatteryLevel(MainActivity.this);
            txtStatus.setText("Current Battery Level: " + level + "%");
            handler.postDelayed(this, 60000); // every 60 sec
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // View bindings
        editMin = findViewById(R.id.editMin);
        editMax = findViewById(R.id.editMax);
        txtStatus = findViewById(R.id.txtStatus);
        Button btnSave = findViewById(R.id.btnSave);
        AdView adView = findViewById(R.id.adView);

        MobileAds.initialize(this, initializationStatus -> {});
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        sharedPreferences = getSharedPreferences("ThresholdPrefs", MODE_PRIVATE);
        String minStr = sharedPreferences.getString("min", "1");
        String maxStr = sharedPreferences.getString("max", "100");
        editMin.setText(minStr);
        editMax.setText(maxStr);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        }

        btnSave.setOnClickListener(view -> {
            String newMinStr = editMin.getText().toString().trim();
            String newMaxStr = editMax.getText().toString().trim();

            if (newMinStr.isEmpty() || newMaxStr.isEmpty()) {
                Toast.makeText(this, "Please enter both thresholds", Toast.LENGTH_SHORT).show();
                return;
            }

            int newMin;
            int newMax;
            try {
                newMin = Integer.parseInt(newMinStr);
                newMax = Integer.parseInt(newMaxStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newMin < 1 || newMax > 100 || newMin >= newMax) {
                Toast.makeText(this, "Enter valid thresholds (1-100, Min < Max)", Toast.LENGTH_LONG).show();
                return;
            }

            sharedPreferences.edit().putString("min", newMinStr).putString("max", newMaxStr).apply();
            Toast.makeText(this, "Thresholds saved", Toast.LENGTH_SHORT).show();

            handler.postDelayed(() -> {
                int level = getBatteryLevel(this);
                txtStatus.setText("Current Battery Level: " + level + "%");
            }, 1000);
        });

        handler.post(batteryCheckRunnable);
    }

    private int getBatteryLevel(Context context) {
        BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        return bm != null ? bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) : -1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
