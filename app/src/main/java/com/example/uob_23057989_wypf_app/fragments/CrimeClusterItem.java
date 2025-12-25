package com.example.uob_23057989_wypf_app.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class CrimeClusterItem implements ClusterItem {

    private final LatLng position;
    private final String title;
    private final String snippet;

    // Constructor matching line 66 in MapFragment
    public CrimeClusterItem(Double lat, Double lng, String title, Double snippet) {
        this.position = new LatLng(lat, lng);
        this.title = title;
        // Fix: Convert the Double to a String
        this.snippet = String.valueOf(snippet);
    }


    // Required by ClusterItem interface
    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    // Required by ClusterItem interface
    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    // Required by ClusterItem interface
    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    // Optional: If using newer library versions, you might need getZIndex
    @Nullable
    @Override
    public Float getZIndex() {
        return 0f;
    }
}