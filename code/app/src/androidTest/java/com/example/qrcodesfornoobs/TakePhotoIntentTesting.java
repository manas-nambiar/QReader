package com.example.qrcodesfornoobs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.qrcodesfornoobs.Activity.TakePhotoActivity;
import com.example.qrcodesfornoobs.Models.Creature;
import com.example.qrcodesfornoobs.Models.Player;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;


public class TakePhotoIntentTesting {
    private Solo solo;
    private final static String MOCK_USERNAME = "_ThomasTest_";
    private final static String MOCK_CODE = "###Thomas###";
    private FirebaseFirestore db;
    @Rule
    public ActivityTestRule<TakePhotoActivity> rule = new ActivityTestRule<>(TakePhotoActivity.class,true,false);

    /**
     * Sets up the solo variable and clear username cache before each test.
     */
    @Before
    public void setUp() {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        db = FirebaseFirestore.getInstance();
        Player mockPlayer = new Player(MOCK_USERNAME, "123321456654");
        db.collection("Players").document(MOCK_USERNAME).set(mockPlayer);
        Player.LOCAL_USERNAME = MOCK_USERNAME;
    }
    @Test
    public void addNewQRCodeToAccountTest() { // US 01.01.01
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), TakePhotoActivity.class);
        intent.putExtra("code", MOCK_CODE);
        rule.launchActivity(intent);

        solo.waitForActivity(TakePhotoActivity.class);
        assertTrue(solo.waitForLogMessage("[INTENT TESTING] confirm button binded."));
        solo.clickOnView(solo.getView(R.id.confirm_button));
        assertTrue(solo.waitForLogMessage("[INTENT TESTING] Code added successfully!"));
    }

    @Test
    public void ableToRecordPhotoLocationTest() { // part of US 02.01.01
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), TakePhotoActivity.class);
        intent.putExtra("code", MOCK_CODE);
        rule.launchActivity(intent);
        solo.waitForActivity(TakePhotoActivity.class);
        View cameraButton = solo.getView(R.id.cameraButton);
        assertNotNull(cameraButton);
        // cannot do any further testing since we're using built-in camera lib. Please refer to
        // https://stackoverflow.com/questions/3840034/how-do-i-write-a-solo-robotium-testcase-that-uses-the-builtin-camera-to-take-a-p
        // for more details
    }

    @Test
    public void uniqueVisualRepresentationCodeTest() { // US 02.06.01
        Intent intent1 = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), TakePhotoActivity.class);
        intent1.putExtra("code", MOCK_CODE + System.currentTimeMillis());
        rule.launchActivity(intent1);
        solo.waitForActivity(TakePhotoActivity.class);
        ImageView imageView1 = solo.getImage(0);
        solo.sleep(1000);

        Bitmap bitmap1 = ((BitmapDrawable) imageView1.getDrawable()).getBitmap();

        solo.finishOpenedActivities();
        Intent intent2 = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), TakePhotoActivity.class);
        intent2.putExtra("code", MOCK_CODE + System.currentTimeMillis());
        rule.launchActivity(intent2);
        solo.waitForActivity(TakePhotoActivity.class);
        ImageView imageView2 = solo.getImage(0);
        solo.sleep(1000);
        Bitmap bitmap2 = ((BitmapDrawable) imageView2.getDrawable()).getBitmap();
        assertFalse(bitmap1.sameAs(bitmap2));
    }

    @Test
    public void doNotRecordActualCodeTest() { // US 08.01.01
        CountDownLatch latch = new CountDownLatch(1);
        Intent intent1 = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), TakePhotoActivity.class);
        intent1.putExtra("code", MOCK_CODE);
        rule.launchActivity(intent1);
        solo.sleep(500);
        solo.waitForActivity(TakePhotoActivity.class);
        solo.clickOnView(solo.getView(R.id.confirm_button));
        solo.sleep(2000); // give some time to ensure db is updated
        DocumentReference creatureRef =  db.collection("Creatures").document(new Creature(MOCK_CODE).getHash());
        creatureRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    String hashValue = doc.getString("hash");
                    assertNotEquals(MOCK_CODE, hashValue);
                }
            }
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    @After
    public void tearDown() {
        db.collection("Players").document(MOCK_USERNAME).delete();
        db.collection("Creatures").document(new Creature(MOCK_CODE).getHash()).delete();
        solo.finishOpenedActivities();
    }

}
