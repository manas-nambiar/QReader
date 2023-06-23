package com.example.qrcodesfornoobs.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.qrcodesfornoobs.Adapter.CommentAdapter;
import com.example.qrcodesfornoobs.Models.Creature;
import com.example.qrcodesfornoobs.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines the comment UI fragment that shows on click of a QR code item in the profile.
 */
public class CommentFragment extends BottomSheetDialogFragment {
    private DocumentReference creatureRef;
    private DocumentReference playerRef;

    private String creatureHash;
    private String userName;
    private boolean canComment;
    private CommentAdapter commentAdapter;
    private RecyclerView recyclerView;
    private EditText addCommentEditText;
    private Button submitButton;
    private ArrayList<String> commentsList;
    private ArrayList<String> imageList;
    private FirebaseFirestore db;
    CollectionReference creatureCollectionReference;
    CollectionReference playerCollectionReference;

    /**
     * Factory method to create a new instance of a CommentFragment
     * @param creatureHash
     * @param userName
     * @return a new instance of CommentFragment
     */
    public static CommentFragment newInstance(String creatureHash, String userName) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putString("creatureHash", creatureHash);
        args.putString("User", userName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflate the fragment's layout and necessary UI elements.
     * Initialize onClickListener for comment submit button
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return a view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.comment_dialog,container,false);

        initWidgets(view);
        checkCommentPerms();

        // Init functionality for the submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get comment text from EditText field and add it to Firebase and display on recyclerview
                String text = addCommentEditText.getText().toString().trim();
                if(!text.isEmpty()){
                    commentsList.add(userName + " : " + text);
                    commentAdapter.notifyDataSetChanged();
                    addCommentToFirebase(text);
                    addCommentEditText.setText("");
                }
            }
        });

        return view;
    }

    /**
     * Initialize data on fragment creation
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // Get creature hash and player username data from intent
        creatureHash = getArguments().getString("creatureHash");
        userName = getArguments().getString("User");

        // Default behaviour of commenting is false until we determine that the player owns the code
        canComment = false;

        initFirebase();
        commentsList = new ArrayList<>();
        imageList = new ArrayList<>();

        super.onCreate(savedInstanceState);


    }

    /**
     * After successful view creation, initialize other views
     * Initialize snapshotlistener to listen for new comments added in the database for this code
     * Fill textviews in upper half of fragment with QR code data
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView creatureImage = view.findViewById(R.id.creature_img);
        ImageView locationImage = view.findViewById(R.id.location_img);
        TextView creatureName = view.findViewById(R.id.creature_name_txt);
        TextView creatureNumScan = view.findViewById(R.id.creature_num_scanned);
        TextView creaturePoints = view.findViewById(R.id.creature_points_txt);
        RequestOptions options = new RequestOptions().circleCrop();

        creatureRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            // Snapshot listener for Creature document specified in creatureRef
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                creatureRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()){
                                // Obtain creature object from creature document in Firestore db
                                Creature creature = document.toObject(Creature.class);
                                // Load comments from creature object
                                ArrayList<String> commentArray = creature.getComments();


                                if(isAdded()){
                                    // Load creature image into ImageView in fragment
                                    Glide.with(getContext()).load(creature.getPhotoCreatureUrl())
                                            .apply(options)
                                            .into(creatureImage);
                                    if (creature.getPhotoLocationUrl() != null){
                                        // If the creature has a saved location photo, display it as well
                                        locationImage.setVisibility(View.VISIBLE);
                                        Glide.with(getContext()).load(creature.getPhotoLocationUrl())
                                                .apply(options)
                                                .into(locationImage);
                                    }
                                }

                                // Set textview to display creature name and points
                                creatureName.setText(creature.getName());
                                if (creature.getNumOfScans() == 1 ){
                                    creatureNumScan.setText("Scanned by " + creature.getNumOfScans() + " player!");
                                } else {
                                    creatureNumScan.setText("Scanned by " + creature.getNumOfScans() + " players!");
                                }
                                creaturePoints.setText(creature.getScore() + " points");

                                // Refresh comment array
                                commentsList.clear();
                                if (!commentArray.isEmpty()){
                                    commentsList.addAll(commentArray);

                                }

                                commentAdapter.notifyDataSetChanged();


                            } else {
                                Log.d("TAG", "No such document");
                            }
                        } else {
                            Log.d("TAG", "get failed with ", task.getException());
                        }
                    }
                });
            }

        });


    }

    /**
     * Function to initialize necessary Firestore Firebase information
     */
    private void initFirebase(){
        db = FirebaseFirestore.getInstance();
        creatureCollectionReference = db.collection("Creatures");
        playerCollectionReference = db.collection("Players");

        // creatureRef is a reference to the QR code clicked on in the QR code list in profile
        creatureRef = creatureCollectionReference.document(creatureHash);

        // playerRef is a reference to the player that clicked the QR code (the player using the app)
        playerRef = playerCollectionReference.document(userName);

    }

    /**
     * Initialize recyclerview and add comment / submit comment buttons
     * @param view view referencing the comment dialog
     */
    private void initWidgets(View view){
        // Init recyclerview
        recyclerView = view.findViewById(R.id.comment_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        commentAdapter = new CommentAdapter(getContext(),commentsList);
        recyclerView.setAdapter(commentAdapter);

        // Init widgets
        addCommentEditText = view.findViewById(R.id.comment_input_edittext);
        submitButton = view.findViewById(R.id.submit_comment_button);

    }

    /**
     * Function that checks if the user that clicked the QR code (the player using the app) owns the
     * QR code that they clicked on.
     * If the player also has the code in the collection, they are given permission to comment on it.
     * Also sets visibility of edit text field and submit comment button
     */
    private void checkCommentPerms(){
        playerRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    ArrayList<String> playerCreatures = (ArrayList<String>) documentSnapshot.get("creatures");

                    // Check if creature hash is present in the player's creature collection
                    if (playerCreatures.contains(creatureHash)){
                        canComment = true;
                    }
                } else {
                    Log.d("TAG","Player document  does not exist!");
                }

                // Set visibility of edittext and submit button depending on perms
                if (canComment){
                    addCommentEditText.setVisibility(View.VISIBLE);
                    submitButton.setVisibility(View.VISIBLE);
                } else {
                    addCommentEditText.setVisibility(View.GONE);
                    submitButton.setVisibility(View.GONE);
                }

            }
        });
    }

    /**
     * Function that adds user text to the creature's comment array in Firebase
     * @param text The user's comment they wish to add to the creature
     */
    private void addCommentToFirebase(String text){
        creatureRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                List<String> stringArray = (List<String>) documentSnapshot.get("comments");
                stringArray.add(userName + " : " + text);
                creatureRef.update("comments", stringArray)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("TAG","Comment successfully added to array");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("TAG","Comment failed to add to array", e);
                            }
                        });
            }
        });
    }
}
