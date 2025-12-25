package com.example.uob_23057989_wypf_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uob_23057989_wypf_app.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Double targetLat;
    private Double targetLng;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Ensure you have a layout file named fragment_map.xml with a <fragment> or FragmentContainerView inside
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if arguments were passed (from clicking a table row)
        if (getArguments() != null) {
            targetLat = getArguments().getDouble("lat", 0.0);
            targetLng = getArguments().getDouble("lng", 0.0);
        }

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Default location (e.g., West Yorkshire)
        LatLng defaultLoc = new LatLng(53.8, -1.5);

        if (targetLat != null && targetLat != 0.0 && targetLng != 0.0) {
            // Plot the specific crime clicked from the table
            LatLng crimeLoc = new LatLng(targetLat, targetLng);
            mMap.addMarker(new MarkerOptions().position(crimeLoc).title("Selected Crime"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(crimeLoc, 15f));
        } else {
            // Just show default view
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 10f));
        }
    }
}
