package com.parkit;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    String image_url;

    public int getParking_id() {
        return parking_id;
    }

    public Parking(int parking_id, int owner_id, int client_id, GeoPoint location, boolean status,
                   Timestamp publish_time, Timestamp start_time, Timestamp end_time, Timestamp expire_time,
                   String image_url) {
        this.parking_id = parking_id;
        this.owner_id = owner_id;
        this.client_id = client_id;
        this.location = location;
        this.status = status;
        this.publish_time = publish_time;
        this.start_time = start_time;
        this.end_time = end_time;
        this.expire_time = expire_time;
        this.image_url = image_url;
    }

    public Parking(Parking p) {
        this.parking_id = p.parking_id;
        this.owner_id = p.owner_id;
        this.client_id = p.client_id;
        this.location = p.location;
        this.status = p.status;
        this.publish_time = p.publish_time;
        this.start_time = p.start_time;
        this.end_time = p.end_time;
        this.expire_time = p.expire_time;
        this.image_url = image_url;
    }

    public Parking(DocumentSnapshot doc) {
        this.parking_id = Integer.valueOf(doc.getId());
        this.owner_id = doc.getLong("owner_id").intValue();
        this.client_id = doc.getLong("client_id").intValue();
        this.location = doc.getGeoPoint("location");
        this.status = doc.getBoolean("status");
        this.publish_time = doc.getTimestamp("publish_time");
        this.start_time = doc.getTimestamp("start_time");
        this.end_time = doc.getTimestamp("end_time");
        this.expire_time = doc.getTimestamp("expire_time");
        this.image_url = doc.getString("image_url");
    }

    public void setParking_id(int parking_id) {
        this.parking_id = parking_id;
    }

    public int getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(int owner_id) {
        this.owner_id = owner_id;
    }

    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Timestamp getPublish_time() {
        return publish_time;
    }

    public void setPublish_time(Timestamp publish_time) {
        this.publish_time = publish_time;
    }

    public Timestamp getStart_time() {
        return start_time;
    }

    public void setStart_time(Timestamp start_time) {
        this.start_time = start_time;
    }

    public Timestamp getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Timestamp end_time) {
        this.end_time = end_time;
    }

    public Timestamp getExpire_time() {
        return expire_time;
    }

    public void setExpire_time(Timestamp expire_time) {
        this.expire_time = expire_time;
    }

    boolean publish() {
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
        data.put("image_url", image_url);
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

    static List<Parking> getClientParkings(int client_id) {
        List<Parking> result = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference parkings = db.collection("parking");
        Query query = parkings.whereEqualTo("client_id", client_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        result.add(new Parking(document));
                    }
                }
//                else {
//                     error
//                }
            }
        });
        return result;
    }

    static List<Parking> getOwnerParkings(int owner_id) {
        List<Parking> result = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference parkings = db.collection("parking");
        Query query = parkings.whereEqualTo("owner_id", owner_id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        result.add(new Parking(document));
                    }
                }
//                else {
//                     error
//                }
            }
        });
        return result;
    }

}
