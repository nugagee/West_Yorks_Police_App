package com.example.uob_23057989_wypf_app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.uob_23057989_wypf_app.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CrimeFormDialogFragment extends DialogFragment {

    public static final String TAG = "CrimeFormDialog";
    public static final String ACTION_ADD = "ACTION_ADD";
    public static final String ACTION_EDIT = "ACTION_EDIT";
    public static final String ACTION_DELETE = "ACTION_DELETE";

    private String currentAction;
    private Bundle crimeData;

    // View components
    private TextInputEditText etCrimeID, etMonth, etCrimeType, etLastOutcome, etLocation, etReportedBy, etLsoaName, etLatitude, etLongitude;

    public static CrimeFormDialogFragment newInstance(String action, @Nullable Bundle data) {
        CrimeFormDialogFragment fragment = new CrimeFormDialogFragment();
        Bundle args = new Bundle();
        args.putString("action", action);
        if (data != null) {
            args.putBundle("crimeData", data);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentAction = getArguments().getString("action");
            crimeData = getArguments().getBundle("crimeData");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_crime_form, null);

        // Find all the views from the layout
        TextView dialogTitle = view.findViewById(R.id.dialogTitle);
        etCrimeID = view.findViewById(R.id.etCrimeID);
        etMonth = view.findViewById(R.id.etMonth);
        etCrimeType = view.findViewById(R.id.etCrimeType);
        etLastOutcome = view.findViewById(R.id.etLastOutcome);
        etLocation = view.findViewById(R.id.etLocation);
        etReportedBy = view.findViewById(R.id.etReportedBy);
        etLsoaName = view.findViewById(R.id.etLsoaName);
        etLatitude = view.findViewById(R.id.etLatitude);
        etLongitude = view.findViewById(R.id.etLongitude);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // --- Populate fields if data exists (for EDIT mode) ---
        if (ACTION_EDIT.equals(currentAction) && crimeData != null) {
            dialogTitle.setText("Edit Crime");
            etCrimeID.setText(crimeData.getString("CrimeID"));
            etMonth.setText(crimeData.getString("Month"));
            etCrimeType.setText(crimeData.getString("CrimeType"));
            etLastOutcome.setText(crimeData.getString("LastOutcome"));
            etLocation.setText(crimeData.getString("Location"));
            etReportedBy.setText(crimeData.getString("ReportedBy"));
            etLsoaName.setText(crimeData.getString("LsoaName"));
            etLatitude.setText(String.valueOf(crimeData.getDouble("Latitude")));
            etLongitude.setText(String.valueOf(crimeData.getDouble("Longitude")));
        } else {
            dialogTitle.setText("Add New Crime");
        }

        btnSave.setOnClickListener(v -> saveCrime());
        btnCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }

    private void saveCrime() {
        // --- Collect data from all fields ---
        String crimeID = etCrimeID.getText().toString().trim();
        String month = etMonth.getText().toString().trim();
        String crimeType = etCrimeType.getText().toString().trim();
        String lastOutcome = etLastOutcome.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String reportedBy = etReportedBy.getText().toString().trim();
        String lsoaName = etLsoaName.getText().toString().trim();
        String latStr = etLatitude.getText().toString().trim();
        String lngStr = etLongitude.getText().toString().trim();

        if (TextUtils.isEmpty(crimeID) || TextUtils.isEmpty(month) || TextUtils.isEmpty(crimeType)) {
            Toast.makeText(getContext(), "Crime ID, Month, and Crime Type are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Create a map of the data to save ---
        Map<String, Object> crime = new HashMap<>();
        crime.put("CrimeID", crimeID);
        crime.put("Month", month);
        crime.put("CrimeType", crimeType);
        crime.put("LastOutcome", lastOutcome);
        crime.put("Location", location);
        crime.put("ReportedBy", reportedBy);
        crime.put("LsoaName", lsoaName);
        crime.put("CreatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        try {
            crime.put("Latitude", Double.parseDouble(latStr));
            crime.put("Longitude", Double.parseDouble(lngStr));
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid Latitude or Longitude format.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use default values from the original bundle for fields that are not in the edit form
        if (crimeData != null) {
            if (crimeData.getString("FallsWithin") != null) crime.put("FallsWithin", crimeData.getString("FallsWithin"));
            if (crimeData.getString("LsoaCode") != null) crime.put("LsoaCode", crimeData.getString("LsoaCode"));
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // --- Determine whether to add a new document or update an existing one ---
        if (ACTION_EDIT.equals(currentAction) && crimeData != null) {
            String documentId = crimeData.getString("documentId");
            db.collection("crimes").document(documentId).set(crime)
                    .addOnSuccessListener(aVoid -> handleSuccess("Crime updated successfully."))
                    .addOnFailureListener(e -> handleError("Failed to update crime: " + e.getMessage()));
        } else {
            db.collection("crimes").add(crime)
                    .addOnSuccessListener(documentReference -> handleSuccess("Crime added successfully."))
                    .addOnFailureListener(e -> handleError("Failed to add crime: " + e.getMessage()));
        }
    }

    private void handleSuccess(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        // Send a result back to DashboardActivity to trigger a refresh
        Bundle result = new Bundle();
        result.putBoolean("operation_success", true);
        getParentFragmentManager().setFragmentResult("crime_form_result", result);
        dismiss();
    }

    private void handleError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}
