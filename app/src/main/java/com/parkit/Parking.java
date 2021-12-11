package com.parkit;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.Timestamp;
import com.google.firebase.database.*;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Parking {
    int parking_id;
    int owner_id;
    int client_id;
    GeoPoint location;
    boolean status;
    Timestamp publish_time;
    Timestamp start_time;
    Timestamp end_time;
    Timestamp expire_time;
    //string
    //available/not

    public int getParking_id() {
        return parking_id;
    }

    public Parking(int parking_id, int owner_id, int client_id, GeoPoint location, boolean status,
                   Timestamp publish_time, Timestamp start_time, Timestamp end_time, Timestamp expire_time) {
        this.parking_id = parking_id;
        this.owner_id = owner_id;
        this.client_id = client_id;
        this.location = location;
        this.status = status;
        this.publish_time = publish_time;
        this.start_time = start_time;
        this.end_time = end_time;
        this.expire_time = expire_time;
    }

    public Parking(Parking p){
        this.parking_id = p.parking_id;
        this.owner_id = p.owner_id;
        this.client_id = p.client_id;
        this.location = p.location;
        this.status = p.status;
        this.publish_time = p.publish_time;
        this.start_time = p.start_time;
        this.end_time = p.end_time;
        this.expire_time = p.expire_time;
    }

    public Parking(DocumentSnapshot doc){
        this.parking_id = Integer.valueOf(doc.getId());
        this.owner_id = doc.getLong("owner_id").intValue();
        this.client_id = doc.getLong("client_id").intValue();
        this.location = doc.getGeoPoint("location");
        this.status = doc.getBoolean("status");
        this.publish_time = doc.getTimestamp("publish_time");
        this.start_time = doc.getTimestamp("start_time");
        this.end_time = doc.getTimestamp("end_time");
        this.expire_time = doc.getTimestamp("expire_time");
    }

    public boolean enable(){
        this.status = true;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("parking").document(Integer.toString(parking_id)).update("status", this.status).isSuccessful();
    }

    public boolean disable(){
        this.status = false;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("parking").document(Integer.toString(parking_id)).update("status", this.status).isSuccessful();
    }

    boolean publish(){
        Map<String, Object> data = new HashMap<>();
        data.put("parking_id", parking_id);
        data.put("owner_id", owner_id);
        data.put("client_id", client_id);
        data.put("location", location);
        data.put("status", status);
        data.put("publish_time", publish_time);
        data.put("expire_time", expire_time);
        data.put("start_time", start_time);
        data.put("end_time", end_time);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("parking").document(Integer.toString(parking_id)).set(data).isSuccessful();
    }

    static Parking getParking(int parking_id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Parking p = null;
        p = new Parking(db.collection("parking").document(Integer.toString(parking_id)).get().getResult()); // remove getresult to use listener
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot doc) {
//                        Parking park = new Parking(doc);
//                    }
//                });
        return p;
    }

    public boolean end_parking(){
        Map<String, Object> log = new HashMap<>();
        log.put("parking_id", this.parking_id);
        log.put("client_id", this.client_id);
        log.put("start_time", this.start_time);
        log.put("end_time", this.end_time);

        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
        String start_string = sfd.format(start_time.toDate());
        StringBuilder sb = new StringBuilder();
        sb.append(parking_id);
        sb.append('_');
        sb.append(start_string);
        String DocumentName = sb.toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("log").document(DocumentName).set(log).isSuccessful();
    }

}
