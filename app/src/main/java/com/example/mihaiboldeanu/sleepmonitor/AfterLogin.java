package com.example.mihaiboldeanu.sleepmonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AfterLogin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);

        Button buttonTrack = findViewById(R.id.buttonTrack);
        Button buttonUpdate = findViewById(R.id.buttonUpdateNN);
        Button buttonSettings = findViewById(R.id.buttonSettings);
        buttonTrack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view){

                goToMonitor();
            }
        });
        buttonUpdate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view){

                goToUpdate();
            }
        });
        buttonSettings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view){

                goToSettings();
            }
        });
    }

    public void goToMonitor() {

        // Go to login screen
        Intent intent = new Intent(this, Monitoring.class);
        startActivity(intent);
    }
    public void goToUpdate() {

        // Go to login screen
        Intent intent = new Intent(this, UpdateNN.class);
        startActivity(intent);
    }
    public void goToSettings() {

        // Go to login screen
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}
