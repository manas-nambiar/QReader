package com.example.qrcodesfornoobs;

import android.app.Activity;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.qrcodesfornoobs.Activity.MainActivity;
import com.example.qrcodesfornoobs.Activity.ProfileActivity;


import com.example.qrcodesfornoobs.Models.Creature;
import com.example.qrcodesfornoobs.Models.Player;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.qrcodesfornoobs.Models.Player;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


/**
 * Test class for SearchFragment. Robotium test framework is used.
 */

public class SearchFragmentTest {
    private Solo solo;
    private FirebaseFirestore db;
    private String mockGeoHash1;
    private String mockGeoHash2;

    @Rule
    public ActivityTestRule<MainActivity> rule =
            new ActivityTestRule<>(MainActivity.class, true, false);

    /**
     * Runs before all tests and creates solo instance.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception{
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        Player.LOCAL_USERNAME = "test";
        db = FirebaseFirestore.getInstance();
        mockGeoHash1 = GeoFireUtils.getGeoHashForLocation(new GeoLocation(38.0, -121.0));
        mockGeoHash2 = GeoFireUtils.getGeoHashForLocation(new GeoLocation(36.0, -118.0));
        Player mockPlayer = new Player("test", "123321456654");
        Creature creature1 = new Creature("creature1","1234",25,1, 38.0,-121.0,"MockLocation1",mockGeoHash1,null);
        Creature creature2 = new Creature("creature2","12345",25,1,36.0,-118.0,"MockLocation2",mockGeoHash2,null);
        mockPlayer.addCreature(creature1);
        mockPlayer.addCreature(creature2);

        Map<String, Object> mockGeoHashMap1 = new HashMap<>();
        mockGeoHashMap1.put("geoHash", mockGeoHash1);
        mockGeoHashMap1.put("lat", 38.0);
        mockGeoHashMap1.put("lng", -121.0);

        Map<String, Object> mockGeoHashMap2 = new HashMap<>();
        mockGeoHashMap2.put("geoHash", mockGeoHash2);
        mockGeoHashMap2.put("lat", 36.0);
        mockGeoHashMap2.put("lng", -118.0);

        db.collection("Creatures").document(creature1.getHash()).set(creature1);
        db.collection("Creatures").document(creature2.getHash()).set(creature2);
        db.collection("Players").document("test").set(mockPlayer);
        db.collection("Geohashes").document(mockGeoHash1).set(mockGeoHashMap1);
        db.collection("Geohashes").document(mockGeoHash2).set(mockGeoHashMap2);

        rule.launchActivity(null);
    }

    /**
     * Gets the activity of the fragment.
     * @throws Exception
     */
    @Test
    public void start() throws Exception{
        Activity activity = rule.getActivity();
    }

    @Test
    public void checkOpenFragment(){
        solo.assertCurrentActivity("Wrong activity.", MainActivity.class);
        solo.waitForFragmentById(solo.getView(R.id.search).getId(),2000);
        solo.clickOnView(solo.getView(R.id.search));
    }

    /**
     * US 05.01.01: As a player, I want to search for other players by username.
     */
    @Test
    public void checkSearchUser(){
        checkOpenFragment();
        solo.clickOnView(solo.getView(R.id.radioUser));
        solo.clickOnView(solo.getView(R.id.username_search));
        assertFalse(solo.searchText("test"));
        solo.enterText(0, "test");
        solo.sendKey(Solo.ENTER);
        assertTrue(solo.waitForText("test", 1, 2000));
    }

    /**
     * US 01.07.01: As a player, I want to see other players profiles.
     */
    @Test
    public void checkSelectUserProfile(){
        checkSearchUser();
        solo.clickInRecyclerView(0);
        solo.assertCurrentActivity("Wrong activity.", ProfileActivity.class);
    }

    /**
     * US 02.03.01 As a player, I want to be able to browse QR codes that other players have scanned.
     */
    @Test
    public void checkBrowsePlayerQR(){
        checkSelectUserProfile();
        solo.searchText("creature1");
    }
    @Test
    public void checkBrowseByGeolocation(){
        checkOpenFragment();
        solo.clickOnView(solo.getView(R.id.radioLocation));
        solo.clickOnView(solo.getView(R.id.longitude_search));
        assertFalse(solo.searchText("-121"));
        solo.enterText(0, "-121");
        solo.clickOnView(solo.getView(R.id.latitude_search));
        assertFalse(solo.searchText("38"));
        solo.enterText(1, "38");
        solo.sendKey(Solo.ENTER);
        assertTrue(solo.waitForText("creature1", 1, 2000));

    }
    @After
    public void tearDown() throws Exception{
        db.collection("Players").document("test").delete();
        db.collection("Creatures").document("1234").delete();
        db.collection("Creatures").document("12345").delete();
        db.collection("Geohashes").document(mockGeoHash1).delete();
        db.collection("Geohashes").document(mockGeoHash2).delete();
        solo.finishOpenedActivities();
    }
}
