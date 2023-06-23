package com.example.qrcodesfornoobs.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qrcodesfornoobs.Activity.ProfileActivity;
import com.example.qrcodesfornoobs.Models.Creature;
import com.example.qrcodesfornoobs.R;
import com.example.qrcodesfornoobs.Adapter.SearchAdapter;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * This class defines the search UI fragment that shows on click of the search icon.
 */
public class SearchFragment extends Fragment implements SearchAdapter.RecyclerViewInterface {
    private RadioGroup radioGroup;
    private int radioSelect = -1;

    private CardView userCard;
    private ConstraintLayout locationCard;
    private SearchView userSearchView;
    private SearchView longitudeSearchView;
    private SearchView latitudeSearchView;
    private RecyclerView recyclerView;
    private TextView endResult;

    // For Firebase
    private SearchAdapter searchAdapter;
    private FirebaseFirestore db;
    private CollectionReference collectionReference;
    private String field;
    private final String longitudeField = "longitude";
    private int lnglatSubmit = -1;
    private final double radiusM = 500 * 1000;
    private GeoLocation center;

    private Intent profileIntent;
    private ArrayList<String> searchList;
    private ArrayList<Creature> creatureList;
    private SearchAdapter.RecyclerViewInterface rvInterface;


    /**
     * Empty public constructor
     */
    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * On create of the fragment, initializes non-view components of the class.
     *
     * @param savedInstanceState Unused. Bundle to be used if carrying information over from parent activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        collectionReference = db.collection("Players");
    }

    /**
     * Initializes view to be used.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        return view;
    }

    /**
     * After successful creation of view, initialize xml components.
     * Contains searching function and click to view player profile function.
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        radioGroup = view.findViewById(R.id.radioGroup);
        userCard = view.findViewById(R.id.user_card);
        userCard.setVisibility(View.INVISIBLE);
        locationCard = view.findViewById(R.id.location_card);
        locationCard.setVisibility(View.INVISIBLE);
        recyclerView = view.findViewById(R.id.recyclerView);
        userSearchView = view.findViewById(R.id.username_search);
        userSearchView.setIconified(false);
        longitudeSearchView = view.findViewById(R.id.longitude_search);
        longitudeSearchView.setIconified(false);
        latitudeSearchView = view.findViewById(R.id.latitude_search);
        latitudeSearchView.setIconified(false);
        endResult = view.findViewById(R.id.text_end_of_result);
        endResult.setVisibility(View.INVISIBLE);


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        rvInterface = new SearchAdapter.RecyclerViewInterface() {
            @Override
            public void onItemClick(int pos) {
                if(field == "username") {   // To disable clicking on a location item
                    launchPlayerProfile(pos);
                }
            }
        };

        radioGroupCheck(db, radioGroup);
        // Username Search
        userSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchList = new ArrayList<>();
                if (query.length() > 0) {
                    submitQuery(query, null);
                    userSearchView.clearFocus();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Location Search
        longitudeSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                creatureList = new ArrayList<>();
                if (query.length() > 0 && latitudeSearchView.getQuery().length() > 0) {
                    lnglatSubmit = 0; // Checks if longitude searchview is submitted instead of latitude
                    submitQuery(query, latitudeSearchView.getQuery().toString());
                    longitudeSearchView.clearFocus();
                    latitudeSearchView.clearFocus();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        latitudeSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                creatureList = new ArrayList<>();
                if (query.length() > 0 && longitudeSearchView.getQuery().length() > 0) {
                    lnglatSubmit = 1;   // Checks if latitude searchview is submitted instead of longitude
                    submitQuery(query, longitudeSearchView.getQuery().toString());
                    longitudeSearchView.clearFocus();
                    latitudeSearchView.clearFocus();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    /**
     * Called when app is closed or when switching fragments/activities.
     * Clears the radio group selection and any SearchView.
     */
    @Override
    public void onPause() {
        super.onPause();
        radioGroup.clearCheck();
        radioSelect = -1;
        lnglatSubmit = -1;
        userSearchView.setQuery("", false);
        longitudeSearchView.setQuery("", false);
        latitudeSearchView.setQuery("", false);
        userSearchView.setVisibility(View.INVISIBLE);
        userCard.setVisibility(View.INVISIBLE);
        longitudeSearchView.setVisibility(View.INVISIBLE);
        latitudeSearchView.setVisibility(View.INVISIBLE);
        endResult.setVisibility(View.INVISIBLE);
        userSearchView.clearFocus();
        longitudeSearchView.clearFocus();
        latitudeSearchView.clearFocus();
        collectionReference = null;
        field = "";
    }

    /**
     * On submit of a nonempty query text, searches database for the "field" depending on which
     * radio button was selected (username / location).
     * <p>
     * Notify the RecyclerView's adapter to display the list of query documents.
     *
     * @param query User inputted string to be used to find documents in a Firebase collection.
     *
     */
    public void submitQuery(String query, String locationQuery) {
        // Username Query
        if (locationQuery == null) {
            collectionReference.orderBy(field).startAt(query.toUpperCase()).endAt(query.toUpperCase() + "\uf8ff").startAt(query).endAt(query + "\uf8ff").limit(10).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    Log.d("Query", doc.getId() + " => " + doc.getData());
                                    searchList.add(doc.getData().get(field).toString());
                                }
                            } else {
                                Log.d("Query", "Error getting documents: ", task.getException());
                            }
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
                            recyclerView.setLayoutManager(layoutManager);

                            SearchAdapter searchAdapter = new SearchAdapter(getContext(), searchList, rvInterface, "Players");
                            recyclerView.setAdapter(searchAdapter);
                        }
                    });
        }
        // Location Geofence
        else{
            double longitude = 999;
            double latitude = 999;
            // Longitude Submit (0)
            if(lnglatSubmit == 0 ){
                longitude = Double.parseDouble(query);
                latitude = Double.parseDouble(locationQuery);
            }
            // Latitude Submit (1)
            else if(lnglatSubmit == 1){
                longitude = Double.parseDouble(locationQuery);
                latitude = Double.parseDouble(query);
            }
            // Check if valid lng lat range
            // lat -90, 90
            // lng -180, 180
            if(latitude < -90f || latitude > 90f || longitude < -180f || longitude > 180f){
                Toast.makeText(getContext(), "Invalid coordinate.", Toast.LENGTH_SHORT).show();
            }

            else {
                center = new GeoLocation(latitude, longitude);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(layoutManager);

                SearchAdapter searchAdapter = new SearchAdapter(getContext(), creatureList, rvInterface, "Creatures");
                recyclerView.setAdapter(searchAdapter);
                Log.d("center", center.toString());
                List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusM);
                final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                for (GeoQueryBounds b : bounds) {
                    Query q = db.collection("Geohashes")
                            .orderBy("geoHash")
                            .startAt(b.startHash)
                            .endAt(b.endHash);

                    tasks.add(q.get());
                }

                // Collect all query results together into single list
                Tasks.whenAllComplete(tasks)
                        .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<Task<?>>> t) {
                                List<DocumentSnapshot> matchingDocs = new ArrayList<>();

                                for (Task<QuerySnapshot> task : tasks) {
                                    QuerySnapshot snap = task.getResult();
                                    for (DocumentSnapshot doc : snap.getDocuments()) {
                                        double lat = doc.getDouble("lat");
                                        double lng = doc.getDouble("lng");

                                        // Filter out few false positives due to Geohash accuracy
                                        GeoLocation docLocation = new GeoLocation(lat, lng);
                                        double distance = GeoFireUtils.getDistanceBetween(docLocation, center);
                                        if (distance <= radiusM) {
                                            matchingDocs.add(doc);
                                        }
                                    }
                                }
                                // matchingDocs contains results
                                // For each result, look into Creature collection and get name to display
                                for (DocumentSnapshot documentSnapshot : matchingDocs) {
//                                    Log.d("equal", documentSnapshot.getData().get("geoHash").toString());
                                    db.collection("Creatures")
                                            .whereEqualTo("geoHash", documentSnapshot.getData().get("geoHash").toString())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            //Log.d("GeoQuery", document.getId() + " => " + document.getData());
                                                            creatureList.add(document.toObject(Creature.class));
                                                        }
                                                    } else {
                                                        Log.d("GeoQuery", "Error getting documents: ", task.getException());
                                                    }
                                                    searchAdapter.notifyDataSetChanged();
                                                }
                                            });
                                }
                            }
                        });
            }
        }
        endResult.setVisibility(View.VISIBLE);
    }

    /**
     * Checks which radio button is selected and changes the Firebase collection path accordingly.
     * Resets RadioGroup and SearchViews.
     *
     * @param db         Firebase database collection.
     * @param radioGroup Contains radio buttons.
     */
    public void radioGroupCheck(FirebaseFirestore db, RadioGroup radioGroup) {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.radioUser:
                        collectionReference = db.collection("Players");
                        field = "username";
                        locationCard.setVisibility(View.INVISIBLE);
                        userCard.setVisibility(View.VISIBLE);
                        userSearchView.setVisibility(View.VISIBLE);
                        latitudeSearchView.setQuery("", false);
                        longitudeSearchView.setQuery("",false);
                        break;
                    case R.id.radioLocation:
                        collectionReference = db.collection("Creatures");
                        lnglatSubmit = -1;
                        field = "latitude";
                        userCard.setVisibility(View.INVISIBLE);
                        locationCard.setVisibility(View.VISIBLE);
                        longitudeSearchView.setVisibility(View.VISIBLE);
                        latitudeSearchView.setVisibility(View.VISIBLE);
                        userSearchView.setQuery("",false);
                        break;
                }
                endResult.setVisibility(View.INVISIBLE);
                // Clear list on switching search by
                searchList = new ArrayList<>();
                SearchAdapter searchAdapter = new SearchAdapter(getContext(), searchList, rvInterface, "");
                recyclerView.setAdapter(searchAdapter);
            }
        });
    }

    /**
     * Callback function for item click events in the RecyclerView.
     *
     * @param pos The position of the clicked item in the RecyclerView.
     */
    @Override
    public void onItemClick(int pos) {

    }

    /**
     * Opens a player's profile on click of a searched user.
     *
     * @param pos Position of selected searched user.
     */
    private void launchPlayerProfile(int pos) {
        profileIntent = new Intent(getActivity(), ProfileActivity.class);
        if (searchList != null) {
            String userToOpen = searchList.get(pos);
            System.out.println(userToOpen);
            profileIntent.putExtra("userToOpen", userToOpen);
            getActivity().startActivity(profileIntent);
        }
    }
}
