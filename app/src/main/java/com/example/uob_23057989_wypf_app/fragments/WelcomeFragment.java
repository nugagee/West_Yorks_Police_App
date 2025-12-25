package com.example.uob_23057989_wypf_app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // 1. Import the Log class
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uob_23057989_wypf_app.LoginActivity;
import com.example.uob_23057989_wypf_app.R;
import com.example.uob_23057989_wypf_app.RegisterActivity;

import org.jspecify.annotations.NonNull;

public class WelcomeFragment extends Fragment {

    // 2. Define a TAG for logging, to easily filter messages
    private static final String TAG = "WelcomeFragment";

    private Button btnLogin, btnSignUp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 3. Log when the view is being created
        Log.d(TAG, "onCreateView: The fragment's view is being created.");

        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 4. Log when the view has been created and we are about to set up listeners
        Log.d(TAG, "onViewCreated: View has been created. Finding buttons now.");

        btnLogin = view.findViewById(R.id.btnLogin);
        btnSignUp = view.findViewById(R.id.btnSignUp);

        // 5. Check if the buttons were found
        if (btnLogin == null || btnSignUp == null) {
            Log.e(TAG, "onViewCreated: ERROR! One or both buttons were not found in the layout.");
            return; // Stop here if buttons are missing
        } else {
            Log.d(TAG, "onViewCreated: Buttons found successfully. Setting listeners...");
        }

        btnLogin.setOnClickListener(v -> {
            // 6. Log that the Login button was clicked
            Log.d(TAG, "Login button CLICKED. Starting LoginActivity...");
            startActivity(new Intent(getActivity(), LoginActivity.class));
        });

        btnSignUp.setOnClickListener(v -> {
            // 7. Log that the Sign Up button was clicked
            Log.d(TAG, "Sign Up button CLICKED. Starting RegisterActivity...");
            startActivity(new Intent(getActivity(), RegisterActivity.class));
        });
    }
}
