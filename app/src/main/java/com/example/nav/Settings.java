package com.example.nav;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class Settings extends AppCompatActivity {

    static String type="metric";
    static String trans = "driving-traffic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        try {
        ToggleButton toggleType = (ToggleButton) findViewById(R.id.toggleType);
        ToggleButton toggleVehicle = (ToggleButton) findViewById(R.id.toggleVehicle);
        ToggleButton toggleCycle = (ToggleButton) findViewById(R.id.toggleCycle);
        ToggleButton toggleWalking = (ToggleButton) findViewById(R.id.toggleWalking);
        Button btnBack = (Button)findViewById(R.id.btnBack);
        Button btnLogout = (Button)findViewById(R.id.btnLogout);

if(trans.equals("driving-traffic")){
    toggleVehicle.setChecked(true);
}else{
    toggleVehicle.setChecked(false);
}
if(trans.equals("cycling")){
                toggleCycle.setChecked(true);
            }else{
    toggleCycle.setChecked(false);
            }
if(trans.equals("walking")){
                toggleWalking.setChecked(true);
            }else{
    toggleWalking.setChecked(false);
            }
if(type.equals("metric")){
                toggleType.setChecked(true);
            }else{
                toggleType.setChecked(false);
            }
    toggleType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked ) {
                // The toggle is enabled
                type = "metric";
            } else {
                // The toggle is disabled
                type = "imperial";

            }
            vibrate();
        }
    });
    toggleVehicle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                // The toggle is enabled
                trans = "driving-traffic";
                toggleCycle.setChecked(false);
                toggleWalking.setChecked(false);
            } else {
                // The toggle is disabled

            }
            vibrate();
        }
    });
    toggleCycle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked ) {
                // The toggle is enabled
                trans = "cycling";
                toggleVehicle.setChecked(false);
                toggleWalking.setChecked(false);

            } else {
                // The toggle is disabled
            }
            vibrate();
        }
    });
    toggleWalking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked ) {
                // The toggle is enabled
                trans = "walking";
                toggleVehicle.setChecked(false);
                toggleCycle.setChecked(false);
            } else {
                // The toggle is disabled
            }
            vibrate();
        }
    });
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //code
                    vibrate();
                    startActivity(new Intent(Settings.this, MainActivity.class));

                }
            });
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //code
                    vibrate();
                    startActivity(new Intent(Settings.this, Login.class));

                }
            });

        }catch (Exception e){
    Log.d("Settings problem", ""+e);

}

    }

    @SuppressLint("MissingPermission")
    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(100);
        }
    }
    }
