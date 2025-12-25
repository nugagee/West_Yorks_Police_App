package com.example.uob_23057989_wypf_app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jspecify.annotations.NonNull;

import java.util.Collections;

import viewmodel.CrimeViewModel;

public class CrimeListFragment extends Fragment {

    private CrimeViewModel viewModel;
    private CrimeAdapter adapter;

    private EditText inputSearch;
    private RecyclerView recyclerView;
    private ProgressBar loading;
    private TextView emptyState;

    // 1. Remove the initialization here. Just declare the variable.
    private TableLayout table;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_crime_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState); // Always good practice to call super

        inputSearch = view.findViewById(R.id.inputSearch);
        recyclerView = view.findViewById(R.id.recyclerCrimes);
        loading = view.findViewById(R.id.loadingIndicator);
        emptyState = view.findViewById(R.id.emptyState);

        // 2. Initialize the table here (if it exists in fragment_crime_list.xml)
        // If 'crimeTable' is actually in the Activity layout and not this Fragment's layout,
        // you should probably remove this entirely from the Fragment.
        table = view.findViewById(R.id.crimeTable);

        // Pass an empty list and a lambda/listener implementation
        adapter = new CrimeAdapter(Collections.emptyList(), crime -> {
            // Ensure getContext() is not null before using it
            if (getContext() != null) {
                Toast.makeText(getContext(), "Clicked: " + crime.getCrimeType(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(CrimeViewModel.class);

        observeData();
        setupSearch();
    }

    private void observeData() {
        // Use getViewLifecycleOwner() for Fragment LiveData observation
        viewModel.getCrimeList().observe(getViewLifecycleOwner(), crimes -> {
            // Check if loading view exists before using
            if (loading != null) loading.setVisibility(View.GONE);

            if (crimes == null || crimes.isEmpty()) {
                if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                adapter.setData(Collections.emptyList());
            } else {
                if (emptyState != null) emptyState.setVisibility(View.GONE);
                adapter.setData(crimes);
            }
        });

        // Ensure startListening is called to actually fetch data
        viewModel.startListening();
    }

    private void setupSearch() {
        if (inputSearch == null) return;

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchCrimes(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}
