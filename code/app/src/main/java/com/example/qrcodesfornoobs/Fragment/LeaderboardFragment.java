package com.example.qrcodesfornoobs.Fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qrcodesfornoobs.Adapter.LeaderboardPlayerAdapter;
import com.example.qrcodesfornoobs.Models.Creature;
import com.example.qrcodesfornoobs.Models.Player;
import com.example.qrcodesfornoobs.databinding.FragmentLeaderboardBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LeaderboardFragment} factory method to
 * create an instance of this fragment.
 */
public class LeaderboardFragment extends Fragment {

    FragmentLeaderboardBinding binding;
    LeaderboardPlayerAdapter adapter;
    private ArrayList<Player> playersToDisplay;

    // FIREBASE INITIALIZE
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    final CollectionReference playerCollectionReference = db.collection("Players");
    final String TAG = "tag";
    Query query = playerCollectionReference.orderBy("score", Query.Direction.DESCENDING);

    /**
     * Required empty public constructor for fragment.
     */
    public LeaderboardFragment() {
        // Required empty public constructor
    }

    /**
     * Called to do initial creation of the fragment.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous
     * saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentLeaderboardBinding.inflate(getLayoutInflater());
        playersToDisplay = new ArrayList<>();
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error!= null){
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }
                playersToDisplay.clear();
                for (QueryDocumentSnapshot document : value) {
                    // Extract data from the document
                    Player tempPlayer = document.toObject(Player.class);
                    playersToDisplay.add(tempPlayer);
                }
                loadLeaderBoard();
            }
        });
    }
    /**
     * Called when leaderboard data is ready to be loaded.
     *
     */
    private void loadLeaderBoard() {
        binding.leaderboardRecyclerView.setHasFixedSize(true);
        binding.leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        adapter = new LeaderboardPlayerAdapter(this.getContext(), playersToDisplay); // list is a dummy data, should be replaced with db stuff
        binding.leaderboardRecyclerView.setAdapter(adapter);
        binding.topPlayerScore.setText(String.valueOf(playersToDisplay.get(0).getScore()));
        binding.topPlayerUsername.setText(playersToDisplay.get(0).getUsername());
    }



    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
     * saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return binding.getRoot();
    }
}