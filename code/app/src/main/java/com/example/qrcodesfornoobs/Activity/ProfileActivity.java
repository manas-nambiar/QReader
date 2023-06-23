package com.example.qrcodesfornoobs.Activity;


import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.qrcodesfornoobs.Adapter.ProfileCodeArrayAdapter;
import com.example.qrcodesfornoobs.Fragment.CommentFragment;
import com.example.qrcodesfornoobs.Models.Creature;
import com.example.qrcodesfornoobs.Models.Player;
import com.example.qrcodesfornoobs.Tools.ProfileCreatureScoreComparator;
import com.example.qrcodesfornoobs.Fragment.ProfileEditInfoFragment;
import com.example.qrcodesfornoobs.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

/**
 * The ProfileActivity class displays the profile of a player, including their
 * name, the number of codes they have scanned, and their total score. It also
 * displays a filter bar and allows the player to toggle between a list view and
 * a grid view of their codes.
 */
public class ProfileActivity extends AppCompatActivity implements ProfileCodeArrayAdapter.RecyclerViewInterface {
    private ImageButton editProfileButton;
    private Spinner sortListSpinner;
    private RecyclerView recyclerView;
    private ProfileCodeArrayAdapter codeArrayAdapter;
    private TextView playerName;
    private TextView codeCount;
    private TextView playerScore;
    private TextView contactText;
    private ConstraintLayout filterBar;
    private Intent mainIntent;
    private Player currentPlayer;
    private Intent profileIntent;
    private ArrayList<Creature> creaturesToDisplay;
    private ArrayList<String> playerCreatureList;
    private DocumentReference playerRef;
    private String userToOpen;
    private Intent commentIntent;
    private ProfileCodeArrayAdapter.RecyclerViewInterface rvInterface;

    // FIREBASE INITIALIZE
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    final CollectionReference creatureCollectionReference = db.collection("Creatures");
    final CollectionReference playerCollectionReference = db.collection("Players");
    final String TAG = "tag";

    /**
     * Called when the activity is starting. Initializes the activity by setting the layout, initializing
     * data structures and widgets, and setting up event listeners. Listens for changes to the player's
     * collection on the database and updates the UI with the new data.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        // When we add a new creature we need to update the datalist first
        // From the datalist we will add them into the database
        creaturesToDisplay = new ArrayList<>();
        playerCreatureList = new ArrayList<>();
        mainIntent = new Intent(this, MainActivity.class);

        setProfileUser();

        // Initialize buttons and spinners
        initWidgets();

        // Get reference to player's collection
        playerRef = playerCollectionReference.document(userToOpen);

        rvInterface = new ProfileCodeArrayAdapter.RecyclerViewInterface() {
            @Override
            public void onItemClick(int pos) {
                openCreatureComments(pos);
            }
        };

        playerRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            // Listens for changes to the player's collection on the database
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                playerRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    // Gets data from player collection
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Gets players codes from their creatures array list
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                currentPlayer = document.toObject(Player.class);
                                Player dbPlayer = document.toObject(Player.class);
                                // Fill array with creatures from database
                                String contact = "None";
                                if (dbPlayer.getContact() != null) contact = dbPlayer.getContact();
                                        else contact= "This user hasn't entered any info about themselves";
                                contactText.setText(contact);
                                playerCreatureList = dbPlayer.getCreatures();
                                if (!playerCreatureList.isEmpty()){
                                    // Queries the Creature collection on db for creatures that the player owns
                                    creatureCollectionReference.whereIn("hash",playerCreatureList)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    Log.d(TAG, "task success: " + task.isSuccessful());
                                                    if (task.isSuccessful()){
                                                        // query success
                                                        creaturesToDisplay.clear();
                                                        int totalScore = 0; // Initialize total score variable
                                                        for (QueryDocumentSnapshot doc : task.getResult()){
                                                            // Add creatures that the player owns to the local datalist
                                                            Log.d(TAG, "Doc data: " + doc.getId());
                                                            Creature creature;
                                                            creature = doc.toObject(Creature.class);
                                                            creaturesToDisplay.add(creature);
                                                            totalScore += creature.getScore(); // Add creature score to total score
                                                        }
                                                        codeCount.setText(creaturesToDisplay.size() + " Codes Scanned");
                                                        playerScore.setText(totalScore + " Points");
                                                        sort(sortListSpinner.getSelectedItem().toString());
                                                        codeArrayAdapter.notifyDataSetChanged();
                                                    } else {
                                                        Log.d(TAG, "get failed with ", task.getException());
                                                    }
                                                }
                                            });
                                }
                                else{
                                    //empty list
                                    creaturesToDisplay.clear();
                                    codeCount.setText("0 Codes Scanned");
                                    playerScore.setText("0 Points");
                                    codeArrayAdapter.notifyDataSetChanged();
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });

            }
        });

        addListenerOnButtons(); // Initialize button listeners

        // RECYCLER VIEW  CHANGES 230301
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ProfileActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        codeArrayAdapter = new ProfileCodeArrayAdapter(ProfileActivity.this, creaturesToDisplay, rvInterface);
        recyclerView.setAdapter(codeArrayAdapter);

        if (userToOpen == Player.LOCAL_USERNAME){
            setSwipeToDelete();
        }

    }

    /**
     * Sets swipe to delete functionality for the RecyclerView items.
     * When an item is swiped left, it gets removed from the list and the
     * corresponding data gets deleted from the Firestore database. An undo
     * option is also displayed in a Snackbar, which allows the user to undo
     * the deletion.
     */
    // For RecyclerView Delete
    private void setSwipeToDelete() {
        final String TAG = "Sample";

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                // Get the swiped Creature and delete it from the database
                Creature QR = creaturesToDisplay.get(position);
                db.collection("Players")
                        .document(userToOpen)
                        .update("creatures", FieldValue.arrayRemove(QR.getHash()),
                                "score", FieldValue.increment(QR.getScore()*-1))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting document", e);
                            }
                        });

                // UNDO DELETE: not the same hashmap tho
                Snackbar.make(recyclerView, "Deleted " + QR.getName(), Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    // Undo the delete, re-add the deleted Creature to database
                    @Override
                    public void onClick(View view) {

                        if (QR.getHash().length() > 0) {

                            playerCollectionReference
                                    .document(userToOpen)
                                    .update("creatures",FieldValue.arrayUnion(QR.getHash()),
                                            "score", FieldValue.increment(QR.getScore()))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // These are a method which gets executed when the task is succeeded
                                            Log.d(TAG, "Data has been re-added successfully!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // These are a method which gets executed if there’s any problem
                                            Log.d(TAG, "Data could not be re-added!" + e.toString());
                                        }
                                    });
                        }

                        recyclerView.scrollToPosition(position);
                    }
                }).show();
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 1f;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                setDeleteIcon(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);
    }

    /**
     * Draws a delete icon on the canvas at the specified location. This function
     * is called by the setSwipeToDelete function.
     *
     * @param c the canvas on which to draw the icon
     * @param recyclerView the RecyclerView containing the item that is being swiped
     * @param viewHolder the ViewHolder for the item that is being swiped
     * @param dX the amount of horizontal displacement caused by the swipe
     * @param dY the amount of vertical displacement caused by the swipe
     * @param actionState the state of the swipe action
     * @param isCurrentlyActive true if the swipe is currently active
     */
    private void setDeleteIcon(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        Paint mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        ColorDrawable mBackground = new ColorDrawable();
        int backgroundColor = Color.parseColor("#B80F0A");
        Drawable deleteDrawable = ContextCompat.getDrawable(this, R.drawable.delete_icon);
        int intrinsicWidth = deleteDrawable.getIntrinsicWidth();
        int intrinsicHeight = deleteDrawable.getIntrinsicHeight();

        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();

        boolean isCancelled = dX == 0 && !isCurrentlyActive;

        if (isCancelled) {
            c.drawRect(itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(),
                    (float) itemView.getBottom(), mClearPaint);
            return;
        }

        mBackground.setColor(backgroundColor);
        mBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(),
                itemView.getRight(), itemView.getBottom());
        mBackground.draw(c);

        int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
        int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
        int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
        int deleteIconRight = itemView.getRight() - deleteIconMargin;
        int deleteIconBottom = deleteIconTop + intrinsicHeight;

        deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
        deleteDrawable.draw(c);
    }

    /**
     * Initializes all the UI widgets for the activity.
     */
    private void initWidgets() {
        editProfileButton = findViewById(R.id.edit_profile_button);
        if (!Objects.equals(userToOpen, Player.LOCAL_USERNAME)){
            editProfileButton.setVisibility(View.GONE);
        }

        filterBar = findViewById(R.id.filterbar);
        sortListSpinner = findViewById(R.id.sort_list_spinner);
        recyclerView = findViewById(R.id.recyclerView);
        playerName = findViewById(R.id.profile_playername_textview);
        playerName.setText(userToOpen);
        codeCount = findViewById(R.id.profile_playercodecount_textview);
        playerScore = findViewById(R.id.profile_playerpoints_textview);
        contactText = findViewById(R.id.profile_contact_textview);
        // Initialize spinner data
        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(this,
                R.array.filter_options, R.layout.spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        sortListSpinner.setAdapter(spinAdapter);
    }

    /**
     * Adds click listeners to the UI buttons for the activity.
     */
    private void addListenerOnButtons() {
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Replace 'contact' with Player.getInfo() or something when we have that set up
                DialogFragment editInfoFrag = ProfileEditInfoFragment.newInstance(currentPlayer.getContact());
                editInfoFrag.show(getSupportFragmentManager(),"Edit Contact Info");
            }
        });


        sortListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = sortListSpinner.getItemAtPosition(i).toString();
                sort(selected);
                codeArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * Sorts the creatures to display on the profile page based on the given sort value.
     * If sortValue is "SCORE (ASCENDING)", sorts the creatures in ascending order of score.
     * If sortValue is "SCORE (DESCENDING)", sorts the creatures in descending order of score.
     * Uses a ProfileCreatureScoreComparator to compare the creatures based on score.
     * @param sortValue the value to sort the creatures by
     */
    public void sort(String sortValue){
        if (sortValue.equals("SCORE (ASCENDING)")) {
            creaturesToDisplay.sort(new ProfileCreatureScoreComparator());
        } else if (sortValue.equals("SCORE (DESCENDING)")) {
            creaturesToDisplay.sort(new ProfileCreatureScoreComparator());
            Collections.reverse(creaturesToDisplay);
        }
    }

    /**
     * Sets the user whose profile is being viewed to the value stored in the profileIntent.
     * If profileIntent does not contain an extra for "userToOpen", sets userToOpen to the value of Player.LOCAL_USERNAME.
     * Prints a message to the console indicating which user's profile is being viewed.
     */
    private void setProfileUser(){
        profileIntent = getIntent();
        userToOpen = profileIntent.getStringExtra("userToOpen");
        if (userToOpen == null){
            userToOpen = Player.LOCAL_USERNAME;
        }
        System.out.println("Opening profile of user " + userToOpen);
    }

    private void openCreatureComments(int pos){
        commentIntent = new Intent(this, CommentFragment.class);
        Creature selectedCreature = creaturesToDisplay.get(pos);
        String selectedCreatureHash = selectedCreature.getHash();
        commentIntent.putExtra("CreatureHash",selectedCreatureHash);
        commentIntent.putExtra("User",Player.LOCAL_USERNAME);

        CommentFragment commentFragment = CommentFragment.newInstance(selectedCreatureHash, Player.LOCAL_USERNAME);
        commentFragment.show(getSupportFragmentManager(),"Open Comments");
    }

    @Override
    public void onItemClick(int pos) {

    }
}