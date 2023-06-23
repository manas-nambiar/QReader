package com.example.qrcodesfornoobs.Fragment;

import static java.lang.Thread.sleep;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qrcodesfornoobs.Models.Creature;
import com.example.qrcodesfornoobs.R;
import com.example.qrcodesfornoobs.databinding.FragmentMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class MapFragment extends Fragment {

    int range = 5;
    boolean userFound;
    GoogleMap mMap;
    FragmentMapBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ActivityResultLauncher<String> permissionCheckLauncher;
    final CollectionReference creatureReference = db.collection("Creatures");

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Required empty public constructor for fragment.
     */
    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment is created. Gets the arguments passed in and inflates the layout.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userFound = false;
        binding = FragmentMapBinding.inflate(getLayoutInflater());
        initiatePermissionCheckLauncher();
        showMap();
    }

    public void initiatePermissionCheckLauncher() {
        permissionCheckLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    followPlayer();
                } else {
                    notifyLocationNotGiven();
                }
            });
    }

    /**
     * Method that displays the map can calls methods that rely on the map to be loaded
     */
    public void showMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {

                // Initialize map
                MapsInitializer.initialize(getActivity());
                mMap = googleMap;
                // Implementation from
                // https://stackoverflow.com/questions/31021000/android-google-maps-v2-remove-default-markers/49090477#49090477
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));
                requestPermissions();
            }
        });
    }

    /**
     * Sets the map to follow player, and also call function to display markers around player
     * Does nothing if permissions have not been granted.
     */
    public void followPlayer() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        displayNearbyMarkers();
    }

    /**
     * Requests for location permissions in order to display user location and display nearby creatures.
     */
    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            followPlayer();
        } else {
            // The registered ActivityResultCallback gets the result of this request.
            permissionCheckLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
    /**
     * Method call to make a listener that displays Creatures around the player within a certain range.
     * On the first call, the listener will also center the camera on the player.
     */
    public void displayNearbyMarkers() {

        mMap.setOnMyLocationChangeListener( location -> {

            // One time call to set camera to player's location
            if (!userFound) {
                centerCamera(location);
                userFound = true;
            }

            // A circular radius is too much for me. I'm just making a square
            creatureReference
                .whereLessThanOrEqualTo("longitude", location.getLongitude() + range)
                .whereGreaterThanOrEqualTo("longitude", location.getLongitude() - range)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        mMap.clear();

                        for (DocumentSnapshot doc: task.getResult()){
                            // All cases have a longitude, it is assumed they also have a latitude
                            String field = "latitude";
                            double creatureLatitude = doc.getDouble(field);

                            // Based on this Stack discussion, querying outside the db is a valid option:
                            // https://stackoverflow.com/questions/26700924/query-based-on-multiple-where-clauses-in-firebase
                            if (creatureLatitude >= location.getLatitude() - range &&
                                creatureLatitude <= location.getLatitude() + range) {
                                Creature creature = doc.toObject(Creature.class);
                                LatLng marker = new LatLng(creature.getLatitude(), creature.getLongitude());
                                mMap.addMarker(new MarkerOptions()
                                        .position(marker)
                                        .title(creature.getScore() + " pts")
                                        .icon(bitmapDescriptorFromVector(getContext(), R.drawable.round_qr_code_2_30)));
                            }
                        }
                    }
            });
        });

    }
    /**
     * convert vector drawable to bitmap
     * @param context context
     * @param vectorResId vector drawable resource id
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) { // https://stackoverflow.com/questions/42365658/custom-marker-in-google-maps-in-android-with-vector-asset-icon
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Called when the fragment's view is created. Returns the root view for the fragment.
     * @param inflater The layout inflater for inflating the layout.
     * @param container The container view for the fragment.
     * @param savedInstanceState The saved instance state bundle.
     * @return The root view for the fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return binding.getRoot();
//        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    /**
     * Method to center the view of the map to a target location.
     */
    public void centerCamera(Location location) {

        LatLng target = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 15f));
    }

    /**
     * Create a dialogue that informs user that the map requires permissions to
     * function as intended.
     */
    public void notifyLocationNotGiven() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setMessage("Location Permissions were not found. Please enable them to find nearby creatures.")
                .setPositiveButton("Ok", null)
                .create()
                .show();
    }
}