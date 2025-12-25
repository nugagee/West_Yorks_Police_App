package viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

import data.Crime;

public class CrimeViewModel extends ViewModel {
    private final MutableLiveData<List<Crime>> crimes = new MutableLiveData<>();
    public LiveData<List<Crime>> getCrimes(){ return crimes; }
    public void setCrimes(List<Crime> list){ crimes.setValue(list); }

    // 1. Declare the backup list here
    private List<Crime> allCrimesBackup = new ArrayList<>();

    private final MutableLiveData<List<Crime>> crimeList = new MutableLiveData<>();
    private ListenerRegistration registration;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<Crime>> getCrimeList() {
        return crimeList;
    }

    // 🔥 Listen in real-time for changes in the Firestore “crimes” collection
    public void startListening() {
        registration = db.collection("crimes")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("CrimeVM", "Firestore listen failed", error);
                        return;
                    }

                    List<Crime> temp = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Crime crime = doc.toObject(Crime.class);
                            if (crime != null) {
                                crime.setId(doc.getId());
                                temp.add(crime);
                            }
                        }
                    }
                    // 2. Save a copy of the data here
                    allCrimesBackup = new ArrayList<>(temp);
                    crimeList.setValue(temp);  // 👈 RecyclerView auto-updates
                });
    }

    public void stopListening() {
        if (registration != null) registration.remove();
    }

    public void searchCrimes(String query) {
        // 3. Use the backup list here
        if (query == null || query.isEmpty()) {
            crimeList.setValue(allCrimesBackup);
        } else {
            List<Crime> filteredList = new ArrayList<>();
            for (Crime crime : allCrimesBackup) {
                // Ensure your Crime object has a getCrimeType or similar getter
                if (crime.getCrimeType() != null &&
                        crime.getCrimeType().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(crime);
                }
            }
            crimeList.setValue(filteredList);
        }
    }
}

