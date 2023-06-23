package com.example.qrcodesfornoobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.qrcodesfornoobs.Activity.MainActivity;
import com.example.qrcodesfornoobs.Activity.SignInActivity;
import com.example.qrcodesfornoobs.Models.Player;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class SignInActivityTest {
    private Solo solo;
    private FirebaseFirestore db;
    @Rule
    public ActivityTestRule<SignInActivity> rule = new ActivityTestRule<>(SignInActivity.class,true,false);

    /**
     * Sets up the solo variable and clear username cache before each test.
     */
    @Before
    public void setUp() {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        // Clear the SharedPreferences cache
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(SignInActivity.CACHE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        // Launch the SignInActivity
        rule.launchActivity(new Intent());
        db = FirebaseFirestore.getInstance();
        Player mockPlayer = new Player("noSignIn", "123321456654");
        db.collection("Players").document("noSignIn").set(mockPlayer);
    }
    /**
     * Signs into the app as a new user, entry will then be deleted at the end of the test.
     * @throws Exception
     */
    @Test
    public void SuccessfulSignIn() throws Exception{
        Activity activity = rule.getActivity();
        solo.assertCurrentActivity("Wrong Activity",SignInActivity.class);
        solo.enterText((EditText) solo.getView(R.id.username_EditText),"TestReynel");
        solo.clickOnView(solo.getView(R.id.sign_in_button));
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
    }
    /**
     * Signs into the app as a existing user with a different device, should remain on signin page.
     * @throws Exception
     */
    @Test
    public void UnsuccessfulSignIn() throws Exception{
        Activity activity = rule.getActivity();
        solo.assertCurrentActivity("Wrong Activity",SignInActivity.class);
        EditText editText = (EditText) solo.getView(R.id.username_EditText);
        solo.enterText(editText,"noSignIn");
        solo.clickOnView(solo.getView(R.id.sign_in_button));
        solo.waitForText("Username Already Exists!");
        solo.assertCurrentActivity("Wrong Activity", SignInActivity.class);
        solo.clearEditText(editText);
        solo.enterText(editText,"");
        solo.clickOnView(solo.getView(R.id.sign_in_button));
        solo.waitForText("Provide a Username!");
    }
    /**
     * Clears username cache after every test
     */
    @After
    public void cleanup(){
        db = FirebaseFirestore.getInstance();
        db.collection("Players").document("noSignIn").delete();
        db.collection("Players").document("TestReynel").delete();
        solo.finishOpenedActivities();
    }
    String TAG = "IntentTesting";
}

