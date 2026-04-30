package com.noober.backgroudlibrary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;

public class NightModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_night_mode);

        findViewById(R.id.btn_toggle_night).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentMode = AppCompatDelegate.getDefaultNightMode();
                if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                recreate();
            }
        });
    }
}
