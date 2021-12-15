package com.parkit;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
//import com.google.firebase.database.*;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Parking {
    String parking_id;
    String owner_id;
    String client_id;
    GeoPoint location;
    boolean status;
    Timestamp publish_time;
    Timestamp start_time;
    Timestamp end_time;
    Timestamp expire_time;
    //string
    //available/not
    String image_url;

    public String getParking_id() {
        return parking_id;
    }

    public void setParking_id(String parking_id) {
        this.parking_id = parking_id;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
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

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Parking(){};

    public Parking(String parking_id, String owner_id, String client_id, GeoPoint location, boolean status,
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
        this.image_url = p.image_url;
    }

    public Parking(DocumentSnapshot doc) {
        this.parking_id = doc.getId();
        this.owner_id = doc.getString("owner_id");
        this.client_id = doc.getString("client_id");
        this.location = doc.getGeoPoint("location");
        this.status = Objects.requireNonNull(doc.getBoolean("status"));
        this.publish_time = doc.getTimestamp("publish_time");
        this.start_time = doc.getTimestamp("start_time");
        this.end_time = doc.getTimestamp("end_time");
        this.expire_time = doc.getTimestamp("expire_time");
        this.image_url = doc.getString("image_url");
    }

    public boolean enable(){
        this.status = true;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("parking").document(parking_id).update("status", this.status).isSuccessful();
    }

    public boolean disable(){
        this.status = false;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("parking").document(parking_id).update("status", this.status).isSuccessful();
    }

    public void publish() {
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
        db.collection("parking").document().set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void unused) {
                Log.d("UPLOAD", "upload success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("UPLOAD", "upload failed", e);
            }
        });
    }

//    public interface MyCallback {
//        DocumentSnapshot onCallback(DocumentSnapshot doc);
//    }
//
//    public void getParking(String parking_id, MyCallback callback) {
//        Parking p = new Parking();
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("parking").document(parking_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                callback.onCallback(task.getResult());p=new Parking(task.getResult());
//            }
//        });DocumentSnapshot d = getdoc(new MyCallback() {
//            @Override
//            public DocumentSnapshot onCallback(DocumentSnapshot doc) {
//                return doc;
//            }
//        }, "abc");
//    }
//
//    private DocumentSnapshot getdoc(MyCallback callback, String str){
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("parking").document(parking_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                callback.onCallback(task.getResult());
//            }
//        });
//    }
//
//    private void loadFromDocument(DocumentSnapshot doc){
//        this.parking_id = doc.getId();
//        this.owner_id = doc.getString("owner_id");
//        this.client_id = doc.getString("client_id");
//        this.location = doc.getGeoPoint("location");
//        this.status = Objects.requireNonNull(doc.getBoolean("status"));
//        this.publish_time = doc.getTimestamp("publish_time");
//        this.start_time = doc.getTimestamp("start_time");
//        this.end_time = doc.getTimestamp("end_time");
//        this.expire_time = doc.getTimestamp("expire_time");
//        this.image_url = doc.getString("image_url");
//    }

    public boolean end_parking(){
        Map<String, Object> log = new HashMap<>();
        log.put("parking_id", this.parking_id);
        log.put("client_id", this.client_id);
        log.put("start_time", this.start_time);
        log.put("end_time", this.end_time);

        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss", Locale.US);
        String start_string = sfd.format(start_time.toDate());
        String DocumentName = String.valueOf(parking_id) + '_' + start_string;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("log").document(DocumentName).set(log).isSuccessful();
    }

    static List<Parking> getClientParkings(int client_id) {
        List<Parking> result = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference parkings = db.collection("parking");
        Query query = parkings.whereEqualTo("client_id", client_id);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    result.add(new Parking(document));
                }
            }
//                else {
//                     error
//                }
        });
        return result;
    }

    static List<Parking> getOwnerParkings(int owner_id) {
        List<Parking> result = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference parkings = db.collection("parking");
        Query query = parkings.whereEqualTo("owner_id", owner_id);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    result.add(new Parking(document));
                }
            }
//                else {
//                     error
//                }
        });
        return result;
    }

}
