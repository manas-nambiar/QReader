package com.example.qrcodesfornoobs;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.PointF;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.widget.ScrollView;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.qrcodesfornoobs.Activity.MainActivity;
import com.example.qrcodesfornoobs.Activity.ProfileActivity;
import com.example.qrcodesfornoobs.Activity.SettingsActivity;
import com.example.qrcodesfornoobs.Activity.SignInActivity;
import com.example.qrcodesfornoobs.Activity.TakePhotoActivity;
import com.example.qrcodesfornoobs.Fragment.DashboardFragment;
import com.example.qrcodesfornoobs.Fragment.MapFragment;
import com.example.qrcodesfornoobs.Models.Creature;
import com.example.qrcodesfornoobs.Models.Player;
import com.google.firebase.firestore.FirebaseFirestore;
import com.robotium.solo.Solo;
import com.smarteist.autoimageslider.SliderView;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

import junit.framework.AssertionFailedError;

public class DashboardFragmentTest {

        private Solo solo;
        private final String TAG = "TAG POST ROBOTIUM";
        private final static String MOCK_USERNAME = "test";
        private FirebaseFirestore db;


        @Rule
        public ActivityTestRule<MainActivity> rule =
                new ActivityTestRule<>(MainActivity.class, true, false);

        @Before
        public void setUp() throws Exception {
                solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
                db = FirebaseFirestore.getInstance();
                Player mockPlayer = new Player(MOCK_USERNAME, "123321456654");
                db.collection("Players").document(MOCK_USERNAME).set(mockPlayer);
                Player.LOCAL_USERNAME = MOCK_USERNAME;
        }
        @Test
        public void testHomePage() throws Exception{
                Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),MainActivity.class);
                rule.launchActivity(intent);
                solo.assertCurrentActivity("Not in Dashboard", MainActivity.class);
                assertTrue(solo.waitForText(Player.LOCAL_USERNAME, 1, 2000));
                Fragment fragment = solo.getCurrentActivity().getFragmentManager().getFragments().get(0);
                Log.d(TAG, fragment.getTag());
        }

        @Test
        public void testProfileButton() throws Exception{
                Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),MainActivity.class);
                rule.launchActivity(intent);
                solo.assertCurrentActivity("Not in Dashboard", MainActivity.class);
                solo.clickOnView(solo.getView(R.id.profile_imageButton));

                solo.assertCurrentActivity("Not in Profile", ProfileActivity.class);
        }

//        @Test
//        public void testPhotoButton() throws Exception{
//                solo.assertCurrentActivity("Not in Dashboard", MainActivity.class);
//                solo.clickOnView(solo.getView(R.id.camera));
//                assertTrue(solo.waitForText("Scan a barcode or QR Code"));
//        }
        //could not perform this test since its using an outside library.
        // https://stackoverflow.com/questions/3840034/how-do-i-write-a-solo-robotium-testcase-that-uses-the-builtin-camera-to-take-a-p

        @Test
        public void  testSearchButton() throws Exception{
                Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),MainActivity.class);
                rule.launchActivity(intent);
                solo.assertCurrentActivity("Not in Dashboard", MainActivity.class);
                solo.clickOnView(solo.getView(R.id.search));

                assertTrue(solo.waitForText("SEARCH", 1, 2000));
        }

        @Test
        public void  testLeaderboardFragment() throws Exception {
                Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),MainActivity.class);
                rule.launchActivity(intent);
                solo.assertCurrentActivity("Not in Dashboard", MainActivity.class);
                solo.clickOnView(solo.getView(R.id.leaderboard));

                assertTrue(solo.waitForText("Rank", 1, 2000));
        }

        @Test
        public void  testMapFragment() throws Exception{
                Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),MainActivity.class);
                rule.launchActivity(intent);
                solo.assertCurrentActivity("Not in Dashboard", MainActivity.class);
                solo.clickOnView(solo.getView(R.id.map));

        }

        @After
        public void tearDown() throws Exception{
                db.collection("Players").document(MOCK_USERNAME).delete();
                solo.finishOpenedActivities();
        }
}