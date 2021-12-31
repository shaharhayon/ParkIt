package com.parkit.ui.myparkings;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.parkit.Parking;
import com.parkit.R;

import java.util.ArrayList;

public class ParkingsAdapter extends RecyclerView.Adapter<ParkingsAdapter.Viewholder> {

    private Context context;
    private ArrayList<Parking> parkingArrayList;

    // Constructor
    public ParkingsAdapter(Context context, ArrayList<Parking> parkingArrayList) {
        this.context = context;
        this.parkingArrayList = parkingArrayList;
    }

    @NonNull
    @Override
    public ParkingsAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingsAdapter.Viewholder holder, int position) {
        // to set data to textview and imageview of each card layout
        Parking parking = parkingArrayList.get(position);
        setImage(parking.getImage_url(), holder.image);

        holder.enableSwitch.setChecked(parking.getStatus());
        holder.enableSwitch.setOnCheckedChangeListener(enableDisableListener(parking));
        holder.address.setText(parking.getAddress());
        holder.expiretime.setText(parking.getExpire_time().toDate().toString());
        holder.price.setText(String.valueOf(parking.getPrice()));
/*        holder.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = FirebaseAuth.getInstance().getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("parking").document(parking.getParking_id()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                                Timestamp now = Timestamp.now();
                                Timestamp startTime = documentSnapshot.getTimestamp("start_time");
                                Timestamp endTime = documentSnapshot.getTimestamp("end_time");
                                String client_id = documentSnapshot.getString("client_id");
                                if(client_id != null){
                                    if((startTime != null) && (endTime != null)){
                                        if((now.compareTo(endTime) == -1) && now.compareTo(startTime) == 1){
                                            Toast.makeText(context, "Parking currently in use", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                else {
                                    Toast.makeText(context, "EDIT OPTIONS FOR PARKING ID " + parking.getParking_id(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });*/
    }

    @Override
    public int getItemCount() {
        // this method is used for showing number
        // of card items in recycler view.
        return parkingArrayList.size();
    }

    private void setImage(String url, ImageView image){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference ref =  storage.getReference(url);
        ref.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(@NonNull StorageMetadata storageMetadata) {
                long size = storageMetadata.getSizeBytes();
                ref.getBytes(size).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(@NonNull byte[] bytes) {
                        Drawable img = new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(bytes,0, bytes.length));
                        image.setImageDrawable(img);
                    }
                });
            }
        });
    }


    // View holder class for initializing of
    // your views such as TextView and Imageview.
    public class Viewholder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView address, expiretime, price;
        private SwitchCompat enableSwitch;
        private FloatingActionButton fab;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            enableSwitch = itemView.findViewById(R.id.enable_switch);
            image = itemView.findViewById(R.id.image_card);
            address = itemView.findViewById(R.id.textView_address_data_card);
            expiretime = itemView.findViewById(R.id.textView_expiretime_data_card);
            price = itemView.findViewById(R.id.textView_price_data_card);
            fab = itemView.findViewById(R.id.floatingActionButton);
        }
    }

    /**
     * enable-disable button handler
     * @param parking
     * @return CompoundButton.OnCheckedChangeListener
     */
    private CompoundButton.OnCheckedChangeListener enableDisableListener(Parking parking){
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                if (isChecked) {
                    db.collection("parking").document(parking.getParking_id()).update("status", true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void unused) {
                                    Toast.makeText(context, "Parking enabled", Toast.LENGTH_SHORT);
                                }
                            });
                }
                else {
                    db.collection("parking").document(parking.getParking_id()).update("status", false)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void unused) {
                                    Toast.makeText(context, "Parking disabled", Toast.LENGTH_SHORT);
                                }
                            });
                }
            }
        };
    }
}