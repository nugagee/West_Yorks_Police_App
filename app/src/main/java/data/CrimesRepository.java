package data;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.Map;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;

public class CrimesRepository {
    private final CollectionReference crimesRef;
    public CrimesRepository(){
        crimesRef = FirebaseFirestore.getInstance().collection("west_yorkshire_crimes");
    }
    public Query getAll(){ return crimesRef.orderBy("month"); }
    public Task<DocumentReference> addCrime(Map<String,Object> data){ return crimesRef.add(data); }
    public Task<Void> updateCrime(String id, Map<String,Object> data){ return crimesRef.document(id).set(data, SetOptions.merge()); }
    public Task<Void> deleteCrime(String id){ return crimesRef.document(id).delete(); }
    public Query searchByField(String field, Object value){ return crimesRef.whereEqualTo(field, value); }
}

