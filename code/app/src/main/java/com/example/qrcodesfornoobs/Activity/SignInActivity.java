package com.example.qrcodesfornoobs.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.example.qrcodesfornoobs.Models.Player;
import com.example.qrcodesfornoobs.databinding.ActivitySigninBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity responsible for handling user sign-in and authentication.
 */
public class SignInActivity extends AppCompatActivity {
    public static final String CACHE_NAME = "SignInCache";
    ActivitySigninBinding binding;
    private Intent mainIntent;


    /**
     * Called when the activity is first created. Initializes the UI components and sets up
     * listeners on the sign-in button.
     * @param savedInstanceState the saved instance state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainIntent = new Intent(this, MainActivity.class);
        if (isLoggedInBefore()) {
            Player.LOCAL_USERNAME = getSharedPreferences(SignInActivity.CACHE_NAME, MODE_PRIVATE).getString("username", "");
            startActivity(mainIntent);
            finish();
            return;
        }
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        addListenerOnButtons();

        binding.usernameEditText.requestFocus();
    }

    /**
     * Adds listeners to the sign-in button to attempt to log in the user when clicked.
     */
    private void addListenerOnButtons() {
        binding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userInput = binding.usernameEditText.getText().toString().trim();
                if (userInput.isEmpty()){
                    binding.usernameEditText.setError("Provide a Username!");
                    return;
                }
                attemptToLogin(userInput);

            }
        });
    }

    /**
     * Attempts to log in the user with the given username by checking if the user exists in
     * the database and comparing the device ID with the ID in the db.
     * @param username the username of the user attempting to log in.
     */
    private void attemptToLogin(String username) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference playerRef = db.collection("Players").document(username);
        String deviceID = Settings.System.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Player localPlayer = new Player(username, deviceID);
        playerRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Player dbPlayer = document.toObject(Player.class);
                        if (localPlayer.equals(dbPlayer)) {
                            login(localPlayer,false);
                            Toast.makeText(getBaseContext(), "Welcome back!", Toast.LENGTH_SHORT).show();
                        } else {
                            binding.usernameEditText.setError("Username Already Exists!");
                        }
                    } else { // login as new user
                        login(localPlayer, true);
                        Toast.makeText(getBaseContext(), "Welcome!", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                Toast.makeText(getBaseContext(), "Cannot connect to server!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Logs in the user and saves login information to the cache.
     * @param localPlayer the Player object representing the logged in user.
     * @param isNewUser a flag indicating whether the user is new or not.
     */
    private void login(Player localPlayer, boolean isNewUser) {
        SharedPreferences sharedPreferences = getSharedPreferences(SignInActivity.CACHE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", localPlayer.getUsername());
        editor.apply();
        Player.LOCAL_USERNAME = localPlayer.getUsername();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (isNewUser) {
            db.collection("Players")
                    .document(localPlayer.getUsername())
                    .set(localPlayer);
        }

        startActivity(mainIntent);
    }

    /**
     * Checks if the user has previously logged in by retrieving the cached username from SharedPreferences.
     *
     * @return true if the cached username is not empty, indicating that the user has previously logged in; false otherwise.
     */
    private boolean isLoggedInBefore() {
        SharedPreferences sharedPreferences = getSharedPreferences(SignInActivity.CACHE_NAME, MODE_PRIVATE);
        return !sharedPreferences.getString("username", "").isEmpty();
    }

}