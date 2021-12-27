package com.parkit;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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
    String address;
    String geohash;
    String image_url;
    int price;

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

    public boolean getStatus() {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }


    public Parking(){};

    public Parking(String parking_id, String owner_id, String client_id, GeoPoint location, boolean status,
                   Timestamp publish_time, Timestamp start_time, Timestamp end_time, Timestamp expire_time,
                   String address, String geohash, String image_url, int price) {
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
        this.address = address;
        this.geohash = geohash;
        this.price = price;
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
        this.address = p.address;
        this.geohash = p.geohash;
        this.price = p.price;
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
        this.address = doc.getString("address");
        this.geohash = doc.getString("geohash");
        this.image_url = doc.getString("image_url");
        try {
            this.price = doc.getLong("price").intValue();
        }catch (Exception e){
            this.price = 0;
        }
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

    public void publish(View view) {
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
        data.put("address", address);
        data.put("geohash", geohash);
        data.put("image_url", image_url);
        data.put("price", price);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("parking").document().set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void unused) {
                Log.d("UPLOAD", "upload success");
                Snackbar snack = Snackbar.make(view, "Parking successfully published.", Snackbar.LENGTH_INDEFINITE);
                snack.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snack.dismiss();
                    }
                }).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("UPLOAD", "upload failed", e);
                Snackbar snack = Snackbar.make(view, "Problem uploading parking data. \nThe parking has not been published.", Snackbar.LENGTH_INDEFINITE);
                snack.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snack.dismiss();
                    }
                }).show();
            }
        });
    }
}
