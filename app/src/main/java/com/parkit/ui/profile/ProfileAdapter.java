package com.parkit.ui.profile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
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
import com.google.android.material.drawable.DrawableUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.parkit.Parking;
import com.parkit.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.Viewholder> {

    private Context context;
    private ArrayList<Parking> parkingArrayList;

    // Constructor
    public ProfileAdapter(Context context, ArrayList<Parking> parkingArrayList) {
        this.context = context;
        this.parkingArrayList = parkingArrayList;
    }

    @NonNull
    @Override
    public ProfileAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_layout, parent, false);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_log_details, parent, false);
        return new ProfileAdapter.Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileAdapter.Viewholder holder, int position) {
        // to set data to textview and imageview of each card layout

        Parking parking = parkingArrayList.get(position);

        holder.parkingAddress.setText(parking.getAddress());

        String owner_id = parking.getOwner_id();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(owner_id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                        String fullname = documentSnapshot.getString("fullname");
                        holder.owner.setText(fullname);;
                    }
                });

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy, HH:mm");
        String start = format.format(parking.getStart_time().toDate());
        String end = format.format(parking.getEnd_time().toDate());

        holder.startTime.setText(start);
        holder.endTime.setText(end);

//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference ref =  storage.getReference(parking.getImage_url());
//        ref.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
//            @Override
//            public void onSuccess(@NonNull StorageMetadata storageMetadata) {
//                long size = storageMetadata.getSizeBytes();
//                ref.getBytes(size).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                    @Override
//                    public void onSuccess(@NonNull byte[] bytes) {
//                        Drawable img = new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(bytes,0, bytes.length));
//                        holder.image.setImageDrawable(img);
//                    }
//                });
//            }
//        });

        holder.owner.getMeasuredHeight();
    }

    @Override
    public int getItemCount() {
        // this method is used for showing number
        // of card items in recycler view.
        return parkingArrayList.size();
    }


    // View holder class for initializing of
    // your views such as TextView and Imageview.
    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView parkingAddress,
                owner,
                startTime,
                endTime;
        private ImageView image;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            parkingAddress = itemView.findViewById(R.id.details_address_data);
            owner = itemView.findViewById(R.id.details_owner_data);
            startTime = itemView.findViewById(R.id.details_start_time_data);
            endTime = itemView.findViewById(R.id.details_end_time_data);
            image = itemView.findViewById(R.id.details_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // show details
        }
    }

/*    private void scaleImage(Bitmap bitMap, int scale){
        int currentBitmapWidth = bitMap.getWidth();
        int currentBitmapHeight = bitMap.getHeight();

        int ivWidth = imageView.getWidth();
        int ivHeight = imageView.getHeight();
        int newWidth = ivWidth;

        newHeight = (int) Math.floor((double) currentBitmapHeight *( (double) new_width / (double) currentBitmapWidth));

        Bitmap newbitMap = Bitmap.createScaledBitmap(bitMap, newWidth, newHeight, true);

        imageView.setImageBitmap(newbitMap)

    }*/
}