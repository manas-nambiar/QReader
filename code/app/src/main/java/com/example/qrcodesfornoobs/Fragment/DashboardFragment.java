package com.example.qrcodesfornoobs.Fragment;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qrcodesfornoobs.Adapter.CodeSliderAdapter;
import com.example.qrcodesfornoobs.Models.Creature;
import com.example.qrcodesfornoobs.Models.Player;
import com.example.qrcodesfornoobs.Activity.ProfileActivity;
import com.example.qrcodesfornoobs.databinding.FragmentDashboardBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;


import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment} method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {

    FragmentDashboardBinding binding;
    private CodeSliderAdapter codeSliderAdapter;
    private Intent profileIntent;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    final CollectionReference creatureCollectionReference = db.collection("Creatures");
    final CollectionReference playerCollectionReference = db.collection("Players");


    /**
     * Required empty public constructor
     */
    public DashboardFragment() {
        // Required empty public constructor
    }

    /**
     * Called to do initial creation of a fragment.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     * this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentDashboardBinding.inflate(getLayoutInflater());
        binding.usernameTextView.setText(Player.LOCAL_USERNAME);

        profileIntent = new Intent(getActivity(), ProfileActivity.class);
        setUpSliders();
        addListenerOnButtons();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadEstimatedRanking();
    }

    public void loadEstimatedRanking() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        binding.progressBar.setVisibility(View.VISIBLE);

        // get owned creatures
        db.collection("Players").document(Player.LOCAL_USERNAME).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(getContext(), "Cannot connect to db (for owned creatures).", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                return;
            }
            Player player = task.getResult().toObject(Player.class);
            if (player == null){
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to get owned creatures.", Toast.LENGTH_SHORT).show();
                } else if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Failed to get owned creatures.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Both getContext() and getActivity() returned null");
                }
                return;
            }
            ArrayList<String> ownedCreatures = player.getCreatures();
            // if no creatures owned, set rank to N/A
            if (ownedCreatures.isEmpty()) {
                binding.rankTextView.setText("N/A");
                binding.progressBar.setVisibility(View.GONE);
                return;
            }

            // get highest score of owned unique creature
            db.collection("Creatures").whereIn("hash", ownedCreatures).get().addOnCompleteListener(task1 -> {
                if (!task1.isSuccessful()) {
                    Toast.makeText(getContext(), "Cannot connect to db (for your highest score).", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                int ownedUniqueHighestScore = -1;
                for (QueryDocumentSnapshot doc : task1.getResult()) {
                    Creature ownedCreature = doc.toObject(Creature.class);
                    if (ownedCreature.getNumOfScans() > 1)
                        continue;
                    int tempScore = ownedCreature.getScore();
                    if (tempScore > ownedUniqueHighestScore) {
                        ownedUniqueHighestScore = tempScore;
                    }
                }
                // if no unique creatures owned, set rank to N/A
                if (ownedUniqueHighestScore == -1) {
                    binding.rankTextView.setText("N/A");
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }

                final int finalOwnedUniqueHighestScore = ownedUniqueHighestScore;
                // attempt at most 50 times to get the highest "unique" score in db
                db.collection("Creatures").orderBy("score", Query.Direction.DESCENDING).limit(50).get().addOnCompleteListener(task2 -> {
                    if (!task2.isSuccessful()) {
                        Toast.makeText(getContext(), "Cannot connect to db (for highest score in db).", Toast.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.GONE);
                        return;
                    }
                    List<DocumentSnapshot> docs = task2.getResult().getDocuments();
                    int dbHighestScore = docs.get(docs.size()-1).toObject(Creature.class).getScore(); // smallest value fetched
                    for (QueryDocumentSnapshot doc : task2.getResult()) {
                        if (doc.toObject(Creature.class).getNumOfScans() == 1) {
                            dbHighestScore = doc.toObject(Creature.class).getScore(); // highest unique value
                            break;
                        }
                    }
                    // calculate rank
                    if (finalOwnedUniqueHighestScore  >= dbHighestScore) {
                        binding.rankTextView.setText("1");
                        binding.progressBar.setVisibility(View.GONE);
                        return;
                    }
                    /*
                        formula: rank = max(2, # total players * (1- ownedUniqueHighestScore / dbHighestScore))
                    */
                    final int finalDbHighestScore = dbHighestScore;
                    db.collection("Players").count().get(AggregateSource.SERVER).addOnCompleteListener(task3 -> {
                        if (!task3.isSuccessful()) {
                            Toast.makeText(getContext(), "Cannot connect to db (for total players).", Toast.LENGTH_SHORT).show();
                            binding.progressBar.setVisibility(View.GONE);
                            return;
                        }
                        int totalPlayers = (int) task3.getResult().getCount();
                        int rank = (int) Math.max(2.0, totalPlayers * (1 - (double) finalOwnedUniqueHighestScore / finalDbHighestScore));
                        binding.rankTextView.setText("#" + rank);
                        binding.progressBar.setVisibility(View.GONE);
                    });
                });
            });
        });
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as
     * given here.
     * @return Return the View for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return binding.getRoot();

    }

    /**
     * This method is used to set the click listeners on the buttons in the dashboard fragment.
     */
    private void addListenerOnButtons() {
        binding.profileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(profileIntent);
            }
        });
    }


    /**
     * Sets up the sliders for the dashboard view by fetching player's creatures from the Firestore database.
     * It initializes the creature list, listens for changes in the player's document, and updates the slider view
     * and total score accordingly.
     */
    private void setUpSliders() {

        ArrayList<Creature> creatureList = new ArrayList<>(); // Initialize the photoCreatureUrlList
        playerCollectionReference.document(Player.LOCAL_USERNAME).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }
                if (value != null && value.exists()) {
                    Player currentPlayer = value.toObject(Player.class);
                    List<String> playerCreatureList = currentPlayer.getCreatures();
                    if (!playerCreatureList.isEmpty()) {
                        binding.dashboardSliderView.setVisibility(View.VISIBLE);
                        creatureCollectionReference.whereIn("hash", playerCreatureList)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            creatureList.clear();
                                            int totalScore = 0;
                                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                                Creature creature;
                                                creature = doc.toObject(Creature.class);
                                                creatureList.add(creature);
                                                totalScore += creature.getScore();
                                            }
                                            CodeSliderAdapter adapter = new CodeSliderAdapter(getContext(), creatureList);
                                            binding.codeScoreTextView.setText(String.valueOf(totalScore));
                                            binding.dashboardSliderView.setSliderAdapter(adapter);
                                        } else {
                                            Log.d(TAG, "get failed with ", task.getException());
                                        }
                                    }
                                });
                    } else {
                        binding.dashboardSliderView.setVisibility(View.GONE);
                        binding.codeScoreTextView.setText("0");
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }

        });
    }
}
