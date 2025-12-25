package com.example.uob_23057989_wypf_app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.uob_23057989_wypf_app.fragments.AdminFragment;
import com.example.uob_23057989_wypf_app.fragments.CrimeFormDialogFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DashboardActivity extends AppCompatActivity implements OnMapReadyCallback {

    // ... (Your class variables remain the same)
    private static final String TAG = "DashboardActivity";
    private GoogleMap mMap;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TableLayout table;
    private BottomNavigationView bottomNav;
    private ProgressBar loadingIndicator;
    private View adminContainer;
    private View mainContentContainer;
    private MaterialToolbar toolbar;
    private List<DocumentSnapshot> allCrimes = new ArrayList<>();
    private String userRole = "user";


    // In DashboardActivity.java

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // --- View initializations are unchanged ---
        table = findViewById(R.id.crimeTable);
        toolbar = findViewById(R.id.topBarDashboard);
        bottomNav = findViewById(R.id.dashboard_bottom_nav);
        SearchView searchView = findViewById(R.id.searchCrimes);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        adminContainer = findViewById(R.id.dashboard_container);
        mainContentContainer = findViewById(R.id.main_content_container);

        bottomNav.setEnabled(false);
        checkUserRole();

        // --- Toolbar listener is unchanged ---
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(DashboardActivity.this, MainActivity.class));
                finishAffinity();
            }
            return true;
        });

        // --- MapFragment setup is unchanged ---
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // --- BottomNav listener is unchanged ---
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_list) {
                toolbar.setTitle(R.string.dashboard);
                if (mainContentContainer != null) mainContentContainer.setVisibility(View.VISIBLE);
                if (adminContainer != null) adminContainer.setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.nav_admin) {
                if ("admin".equals(userRole)) {
                    toolbar.setTitle("Admin");
                    if (mainContentContainer != null) mainContentContainer.setVisibility(View.GONE);
                    if (adminContainer != null) adminContainer.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.dashboard_container, new AdminFragment())
                            .commit();
                    return true;
                } else {
                    showPermissionDeniedDialog();
                    return false;
                }
            }
            return false;
        });


        // --- vvv FIX IS HERE: COMBINED FRAGMENT RESULT LISTENERS vvv ---
        // This single listener now handles results from MULTIPLE fragments.

        // Listener for when the Add/Edit crime form is successfully submitted.
        getSupportFragmentManager().setFragmentResultListener("crime_form_result", this, (requestKey, bundle) -> {
            boolean success = bundle.getBoolean("operation_success");
            if (success) {
                // A crime was added or edited successfully.
                // 1. Ensure we are on the main dashboard screen.
                bottomNav.setSelectedItemId(R.id.nav_list);
                // 2. Reload all data from Firestore to show the changes.
                loadAllCrimesFirst();
            }
        });

        // Listener for when the CSV import is finished in the AdminFragment.
        getSupportFragmentManager().setFragmentResultListener("csv_import_result", this, (requestKey, bundle) -> {
            boolean success = bundle.getBoolean("import_success");
            if (success) {
                // The import was successful.
                // 1. Switch the view to the main dashboard screen.
                bottomNav.setSelectedItemId(R.id.nav_list);
                // 2. Reload all data from Firestore to show the new data.
                loadAllCrimesFirst();
            }
        });
        // --- ^^^ FIX IS HERE ^^^ ---


        // --- SearchView listener is unchanged ---
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { filterCrimes(query); return true; }
                @Override
                public boolean onQueryTextChange(String newText) { filterCrimes(newText); return true; }
            });
        }
    }


    // ... (checkUserRole, showPermissionDeniedDialog, onMapReady, openMapAtLocation methods remain unchanged)
    private void checkUserRole() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        String userId = currentUser.getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.getString("role") != null) {
                        userRole = documentSnapshot.getString("role");
                        Log.d(TAG, "Role check SUCCESS. User is: " + userRole);
                    } else {
                        Log.w(TAG, "Role check SUCCESS, but user document has no 'role' field or does not exist. Defaulting to 'user'.");
                        userRole = "user";
                    }
                    loadAllCrimesFirst();
                    bottomNav.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Role check FAILED: " + e.getMessage());
                    Toast.makeText(this, "Could not verify user role.", Toast.LENGTH_SHORT).show();
                    loadAllCrimesFirst();
                    bottomNav.setEnabled(true);
                });
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Access Denied")
                .setMessage("You don't have access to this screen.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override public View getInfoWindow(Marker marker) { return null; }
            @Override public View getInfoContents(Marker marker) {
                LinearLayout info = new LinearLayout(DashboardActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);
                TextView title = new TextView(DashboardActivity.this);
                title.setTextColor(Color.BLACK);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());
                TextView snippet = new TextView(DashboardActivity.this);
                snippet.setTextColor(Color.DKGRAY);
                snippet.setText(marker.getSnippet());
                info.addView(title);
                info.addView(snippet);
                return info;
            }
        });
        LatLng defaultLoc = new LatLng(53.8, -1.5);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 10f));
    }

    private void openMapAtLocation(DocumentSnapshot doc) {
        Double lat = doc.getDouble("Latitude");
        Double lng = doc.getDouble("Longitude");
        if (lat == null || lng == null || (lat == 0.0 && lng == 0.0)) {
            Toast.makeText(this, "No location data for this crime", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mMap != null) {
            LatLng loc = new LatLng(lat, lng);
            String type = doc.getString("CrimeType") != null ? doc.getString("CrimeType") : "Unknown";
            String snippetInfo = "Location: " + doc.getString("Location") + "\n" +
                    "Area Name: " + doc.getString("LsoaName") + "\n" +
                    "ID: " + doc.getString("CrimeID") + "\n" +
                    "Month: " + doc.getString("Month") + "\n" +
                    "Outcome: " + doc.getString("LastOutcome");
            mMap.clear();
            Marker marker = mMap.addMarker(new MarkerOptions().position(loc).title(type).snippet(snippetInfo));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f));
            if (marker != null) {
                marker.showInfoWindow();
            }
        }
    }


    private void loadAllCrimesFirst() {
        if (loadingIndicator != null) loadingIndicator.setVisibility(View.VISIBLE);
        db.collection("crimes").orderBy("CreatedAt", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(snaps -> {
                    allCrimes.clear();
                    allCrimes.addAll(snaps.getDocuments());
                    renderTable(allCrimes);
                    if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
                });
    }

    private void deleteCrime(String documentId, String crimeId) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete Crime ID: " + crimeId + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    loadingIndicator.setVisibility(View.VISIBLE);
                    db.collection("crimes").document(documentId).delete()
                            .addOnSuccessListener(aVoid -> {
                                loadingIndicator.setVisibility(View.GONE);
                                Toast.makeText(DashboardActivity.this, "Crime " + crimeId + " deleted successfully.", Toast.LENGTH_SHORT).show();
                                loadAllCrimesFirst();
                            })
                            .addOnFailureListener(e -> {
                                loadingIndicator.setVisibility(View.GONE);
                                Toast.makeText(DashboardActivity.this, "Failed to delete crime: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // In DashboardActivity.java

    private void renderTable(List<DocumentSnapshot> crimeList) {
        if (table == null) return;
        table.removeAllViews();
        addTableHeaders(table); // This will now add the new headers

        boolean isAdmin = "admin".equals(userRole);

        for (DocumentSnapshot doc : crimeList) {
            TableRow row = new TableRow(this);
            row.setBackgroundResource(android.R.drawable.list_selector_background);
            row.setOnClickListener(v -> openMapAtLocation(doc));

            // --- vvv FIX IS HERE vvv ---
            // Render all the new columns from the Firestore document
            addCell(row, doc.getString("CrimeID"));
            addCell(row, doc.getString("Month"));
            addCell(row, doc.getString("CrimeType"));
            addCell(row, doc.getString("LastOutcome")); // Use the correct field "LastOutcome"
            addCell(row, doc.getString("ReportedBy"));
            addCell(row, doc.getString("Location"));
            addCell(row, doc.getString("LsoaName"));
            // --- ^^^ FIX IS HERE ^^^ ---
            addCell(row, String.format("%.4f", doc.getDouble("Latitude") != null ? doc.getDouble("Latitude") : 0.0));
            addCell(row, String.format("%.4f", doc.getDouble("Longitude") != null ? doc.getDouble("Longitude") : 0.0));

            if (isAdmin) {
                LinearLayout actionLayout = new LinearLayout(this);
                actionLayout.setOrientation(LinearLayout.HORIZONTAL);
                actionLayout.setGravity(Gravity.CENTER);

                // EDIT Button
                Button editButton = new Button(this, null, android.R.attr.buttonStyleSmall);
                editButton.setText("Edit");
                editButton.setOnClickListener(v -> {
                    v.cancelPendingInputEvents();
                    // --- vvv FIX IS HERE vvv ---
                    // Create a Bundle and populate it with ALL the data needed for the edit form.
                    Bundle data = new Bundle();

                    // Core identifiers
                    data.putString("documentId", doc.getId()); // Essential for saving the update
                    data.putString("CrimeID", doc.getString("CrimeID"));

                    // Crime details
                    data.putString("Month", doc.getString("Month"));
                    data.putString("CrimeType", doc.getString("CrimeType"));
                    data.putString("LastOutcome", doc.getString("LastOutcome"));

                    // Location details
                    data.putString("Location", doc.getString("Location"));
                    Double lat = doc.getDouble("Latitude");
                    Double lng = doc.getDouble("Longitude");
                    data.putDouble("Latitude", lat != null ? lat : 0.0);
                    data.putDouble("Longitude", lng != null ? lng : 0.0);

                    // Additional CSV data
                    data.putString("ReportedBy", doc.getString("ReportedBy"));
                    data.putString("FallsWithin", doc.getString("FallsWithin"));
                    data.putString("LsoaCode", doc.getString("LsoaCode"));
                    data.putString("LsoaName", doc.getString("LsoaName"));
                    // --- ^^^ FIX IS HERE ^^^ ---

                    // Pass the complete bundle to the dialog fragment
                    CrimeFormDialogFragment dialog = CrimeFormDialogFragment.newInstance(CrimeFormDialogFragment.ACTION_EDIT, data);
                    dialog.show(getSupportFragmentManager(), CrimeFormDialogFragment.TAG);
                });
                actionLayout.addView(editButton);

                // DELETE Button
                Button deleteButton = new Button(this, null, android.R.attr.buttonStyleSmall);
                deleteButton.setText("Delete");
                deleteButton.getBackground().setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_light), PorterDuff.Mode.MULTIPLY);
                deleteButton.setTextColor(Color.WHITE);

                deleteButton.setOnClickListener(v -> {
                    v.cancelPendingInputEvents();
                    deleteCrime(doc.getId(), doc.getString("CrimeID"));
                });
                actionLayout.addView(deleteButton);

                // Add the entire layout (with both buttons) to the row
                row.addView(actionLayout);
            }
            // If the user is NOT an admin, the 'if' block is skipped, and no action buttons are added.
            // --- ^^^ FIX IS HERE ^^^ ---

                table.addView(row);
        }
    }

    private void addTableHeaders(TableLayout table) {
        TableRow row = new TableRow(this);
        row.setBackgroundColor(Color.LTGRAY);

        // --- vvv FIX IS HERE vvv ---
        // Add all the new headers for the columns you want to display
        addCell(row, "Crime ID");
        addCell(row, "Month");
        addCell(row, "Crime Type");
        addCell(row, "Outcome"); // Renamed from "Outcome" to be more accurate
        addCell(row, "Reported By");
        addCell(row, "Location");
        addCell(row, "Area Name"); // LSOA Name
        // --- ^^^ FIX IS HERE ^^^ ---
        addCell(row, "Latitude");  // Add Latitude header
        addCell(row, "Longitude"); // Add Longitude header

        if ("admin".equals(userRole)) {
            addCell(row, "Action");
        }
        table.addView(row);
    }


    private void addCell(TableRow row, String text) {
        TextView tv = new TextView(this);
        tv.setText(text != null ? text : "-");
        tv.setPadding(20, 20, 20, 20);
        tv.setTextSize(14);
        row.addView(tv);
    }

    private void filterCrimes(String query) {
        if (allCrimes.isEmpty()) return;
        String lowerQuery = query.toLowerCase();
        List<DocumentSnapshot> filtered = new ArrayList<>();
        for (DocumentSnapshot c : allCrimes) {
            String type = c.getString("CrimeType");
            String outcome = c.getString("Outcome");
            String id = c.getString("CrimeID");
            if ((type != null && type.toLowerCase().contains(lowerQuery)) ||
                    (outcome != null && outcome.toLowerCase().contains(lowerQuery)) ||
                    (id != null && id.toLowerCase().contains(lowerQuery))) {
                filtered.add(c);
            }
        }
        renderTable(filtered);
    }
}
