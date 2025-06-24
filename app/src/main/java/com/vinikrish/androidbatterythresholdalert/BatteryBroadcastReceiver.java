package com.vinikrish.androidbatterythresholdalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.TextView;
import android.content.SharedPreferences;

public class BatteryBroadcastReceiver extends BroadcastReceiver {

    private TextView statusText;

    public BatteryBroadcastReceiver(TextView statusText) {
        this.statusText = statusText;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

        SharedPreferences prefs = context.getSharedPreferences("ThresholdPrefs", Context.MODE_PRIVATE);
        int min = prefs.getInt("min", 20);
        int max = prefs.getInt("max", 80);

        if (level <= min) {
            statusText.setText("Battery is below minimum!");
        } else if (level >= max) {
            statusText.setText("Battery is above maximum!");
        } else {
            statusText.setText(""); // within range
        }
    }
}
