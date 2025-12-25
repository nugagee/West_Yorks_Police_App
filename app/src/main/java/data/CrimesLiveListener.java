package data;


import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.*;
import data.Crime;
import java.util.ArrayList;
import java.util.List;

public class CrimesLiveListener {

    private ListenerRegistration listener;

    public void start(MutableLiveData<List<Crime>> live){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        listener = db.collection("west_yorkshire_crimes")
                .addSnapshotListener((snap, error) -> {
                    if (error != null) return;
                    List<Crime> list = new ArrayList<>();
                    for (DocumentSnapshot d : snap) {
                        Crime c = d.toObject(Crime.class);
                        list.add(c);
                    }
                    live.postValue(list);
                });
    }

    public void stop(){
        if (listener != null) listener.remove();
    }
}

