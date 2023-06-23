package com.example.qrcodesfornoobs.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.qrcodesfornoobs.Fragment.DashboardFragment;
import com.example.qrcodesfornoobs.Fragment.LeaderboardFragment;
import com.example.qrcodesfornoobs.Fragment.MapFragment;
import com.example.qrcodesfornoobs.Fragment.SearchFragment;
import com.example.qrcodesfornoobs.R;
import com.example.qrcodesfornoobs.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * The main activity of the application, responsible for handling the bottom navigation view and
 * switching between the different fragments in the app. The activity also initializes and handles
 * the camera for scanning QR codes and barcodes.
 */
public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private Fragment dashboardFragment;
    private Fragment searchFragment;
    private IntentIntegrator cameraIntentIntegrator;
    private Fragment leaderboardFragment;
    private Fragment mapFragment;

    /**
     * Called when the activity is starting. Initializes the activity and sets the initial fragment
     * to the dashboard fragment.
     * @param savedInstanceState the saved state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeFragments();

        addListenerOnButtons();
        replaceFragment(dashboardFragment);
    }

    /**
     * Initializes all fragments used in the activity, as well as the camera used for scanning QR
     * codes and barcodes.
     */
    private void initializeFragments() {
        dashboardFragment = new DashboardFragment();
        mapFragment = new MapFragment();
        leaderboardFragment = new LeaderboardFragment();
        searchFragment = new SearchFragment();
        cameraIntentIntegrator = new IntentIntegrator(this);
        cameraIntentIntegrator.setPrompt("Scan a barcode or QR Code");
    }

    /**
     * Adds a listener to the bottom navigation view, which is used to switch between the different
     * fragments in the app.
     */
    private void addListenerOnButtons() {

        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    System.out.println("home");
                    replaceFragment(dashboardFragment);
                } else if (item.getItemId() == R.id.search) {
                    replaceFragment(searchFragment);
                } else if (item.getItemId() == R.id.camera) {
                    cameraIntentIntegrator.initiateScan();
                } else if (item.getItemId() == R.id.leaderboard) {
                    replaceFragment(leaderboardFragment);
                } else if (item.getItemId() == R.id.map) {
                    replaceFragment(mapFragment);
                }
                return true;
            }
        });
    }

    /**
     * Process scanned code info
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) { // if it's the result of the scan
            if (intentResult.getContents() == null) { // if user cancelled
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Intent takePhotoIntent = new Intent(this, TakePhotoActivity.class);
                takePhotoIntent.putExtra("code", intentResult.getContents());
                startActivity(takePhotoIntent);
            }
        }
    }

    /**
     * Replace fragment that is currently shown in frame_layout with a new one
     * @param fragment
     */

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}