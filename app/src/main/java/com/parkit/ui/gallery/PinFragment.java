package com.parkit.ui.gallery;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StreamDownloadTask;
import com.parkit.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PinFragment extends Fragment {

    View view;
    Fragment f;
    GalleryFragment parentFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_pin, container, false);
        f = this;
        parentFragment = (GalleryFragment) getParentFragment();

        // Widgets

        TextView owner = view.findViewById(R.id.textView_owner_data);
        TextView address = view.findViewById(R.id.textView_address_data);
        TextView expiretime = view.findViewById(R.id.textView_expiretime_data);

        ImageView image = view.findViewById(R.id.imageView_parking);

        FloatingActionButton button = view.findViewById(R.id.TakeParkingButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentFragment.showHidePin(f);
            }
        });

        getParentFragmentManager().setFragmentResultListener("marker_key", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String string_address = result.getString("address_bundle");
                String string_owner = result.getString("owner_bundle");
                String string_expiretime = result.getString("expire_bundle");
                String string_image_url = result.getString("image_url");
                String string_parkingid = result.getString("parkingid_bundle");
                owner.setText(string_owner);
                address.setText(string_address);
                expiretime.setText(string_expiretime);

                FirebaseStorage storage = FirebaseStorage.getInstance();
                storage.getReference(string_image_url).getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(@NonNull byte[] bytes) {
                        Drawable img = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytes,0, bytes.length));
                        image.setImageDrawable(img);
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

    }

}
