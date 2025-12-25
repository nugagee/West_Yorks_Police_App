package com.example.uob_23057989_wypf_app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.uob_23057989_wypf_app.fragments.WelcomeFragment;
//import com.example.uob_23057989_wypf_app.fragments.CrimeListFragment;
import com.example.uob_23057989_wypf_app.fragments.AdminFragment;
import com.example.uob_23057989_wypf_app.fragments.MapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        // Default screen = WelcomeFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new WelcomeFragment())
                    .commit();
        }

//        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
//
//        bottomNav.setOnItemSelectedListener(item -> {
//            Fragment selected = null;
//
//            if (item.getItemId() == R.id.nav_list) {
//                selected = new CrimeListFragment();
//            } else if (item.getItemId() == R.id.nav_map) {
//                selected = new MapFragment();
//            } else if (item.getItemId() == R.id.nav_admin) {
//                selected = new AdminFragment();
//            }
//
//            if (selected != null) {
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fragmentContainer, selected)
//                        .commit();
//            }
//
//            return true;
//        });
    }
}
