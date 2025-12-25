package com.example.uob_23057989_wypf_app.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uob_23057989_wypf_app.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminFragment extends Fragment {

    private FirebaseFirestore db;
    private ProgressBar importLoader;
    private View buttonsContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin, container, false);

        db = FirebaseFirestore.getInstance();

        // Find views
        Button addBtn = v.findViewById(R.id.admin_add);
        Button importBtn = v.findViewById(R.id.btnImportCSV);
        importLoader = v.findViewById(R.id.import_loader);
        buttonsContainer = v.findViewById(R.id.admin_buttons_container);

        // --- Set OnClick Listeners ---

        // 1. Launch the Add Crime dialog
        addBtn.setOnClickListener(view -> {
            CrimeFormDialogFragment addDialog = CrimeFormDialogFragment.newInstance(CrimeFormDialogFragment.ACTION_ADD, null);
            addDialog.show(getParentFragmentManager(), CrimeFormDialogFragment.TAG);
        });

        // 2. Import CSV
        importBtn.setOnClickListener(view -> importCSVInBackground());

        // The old delete button logic is now removed.

        return v;
    }

    // --- The rest of the file (setLoadingState and importCSVInBackground) remains exactly the same ---
    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            importLoader.setVisibility(View.VISIBLE);
            buttonsContainer.setVisibility(View.GONE);
        } else {
            importLoader.setVisibility(View.GONE);
            buttonsContainer.setVisibility(View.VISIBLE);
        }
    }

    // In AdminFragment.java

    /**
     * Executes the CSV import on a background thread to prevent UI freezes.
     */
    // In AdminFragment.java

    /**
     * Executes the CSV import on a background thread to prevent UI freezes.
     */
    private void importCSVInBackground() {
        // Use an ExecutorService for modern background threading
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // Use a Handler to post results back to the main thread
        Handler handler = new Handler(Looper.getMainLooper());

        // Show the loader before starting the task
        setLoadingState(true);

        executor.execute(() -> {
            // --- This code runs in the background ---
            int successfulImports = 0;
            String errorMessage = null;
            try {
                InputStream is = requireContext().getAssets().open("crimeyorkshire.csv");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                int lineCount = 0;
                WriteBatch batch = db.batch(); // Use a batch for efficiency

                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    if (lineCount <= 1) continue; // Skip header

                    // The CSV has 11 columns.
                    String[] cols = line.split(",", -1); // Use -1 limit to include trailing empty strings
                    if (cols.length >= 11) { // Check for at least 11 columns
                        Map<String, Object> crime = new HashMap<>();

                        // Map the correct column index to the correct field name
                        crime.put("CrimeID", cols[0].trim());
                        crime.put("Month", cols[1].trim());
                        crime.put("ReportedBy", cols[2].trim());
                        crime.put("FallsWithin", cols[3].trim());

                        try {
                            crime.put("Longitude", Double.parseDouble(cols[4].trim()));
                            crime.put("Latitude", Double.parseDouble(cols[5].trim()));
                        } catch (NumberFormatException e) {
                            // If coordinates are invalid, skip this row
                            continue;
                        }

                        crime.put("Location", cols[6].trim());
                        crime.put("LsoaCode", cols[7].trim());
                        crime.put("LsoaName", cols[8].trim());
                        crime.put("CrimeType", cols[9].trim());
                        crime.put("LastOutcome", cols[10].trim()); // Correctly map the last outcome

                        // --- vvv THIS IS THE FIX vvv ---
                        // Add a server timestamp. This is essential for sorting by the latest.
                        crime.put("CreatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                        // --- ^^^ THIS IS THE FIX ^^^ ---

                        // Add to batch instead of writing one by one
                        batch.set(db.collection("crimes").document(), crime);
                        successfulImports++;
                    }
                }
                batch.commit(); // Commit all writes at once

            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
            }

            // --- Post the result back to the UI thread ---
            final String finalErrorMessage = errorMessage;
            final int finalSuccessfulImports = successfulImports;

            handler.post(() -> {
                // This code runs on the UI thread
                setLoadingState(false); // Hide the loader
                if (finalErrorMessage != null) {
                    Toast.makeText(getContext(), "Error during import: " + finalErrorMessage, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Successfully imported " + finalSuccessfulImports + " records.", Toast.LENGTH_LONG).show();

                    // Send a result back to the DashboardActivity to trigger a refresh and navigation
                    Bundle result = new Bundle();
                    result.putBoolean("import_success", true);
                    getParentFragmentManager().setFragmentResult("csv_import_result", result);
                }
            });
        });
    }
}

