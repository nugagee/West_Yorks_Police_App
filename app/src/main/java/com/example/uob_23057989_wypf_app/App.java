package com.example.uob_23057989_wypf_app;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class App extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}

