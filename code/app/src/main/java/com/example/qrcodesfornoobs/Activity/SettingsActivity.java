package com.example.qrcodesfornoobs.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qrcodesfornoobs.R;


/**
 * The SettingsActivity class represents the activity that displays the application settings to the user.
 * It provides a button to navigate back to the MainActivity.
 */
public class SettingsActivity extends AppCompatActivity {
    Button backButton;
    private Intent mainIntent;


    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mainIntent = new Intent(this, MainActivity.class);
        addListenerOnButtons();
    }

    /**
     * Adds a listener to the back button, which starts the MainActivity when clicked.
     */
    private void addListenerOnButtons() {
        backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(mainIntent);
            }
        });
    }
}