package com.parkit.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.parkit.Parking;
import com.parkit.R;
import com.parkit.databinding.FragmentGalleryBinding;
import com.parkit.databinding.FragmentProfileBinding;
import com.parkit.ui.gallery.GalleryViewModel;
import com.parkit.ui.slideshow.ParkingsAdapter;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ArrayList<Parking> parkingArrayList;
    RecyclerView RV;

    TextView profile_name, profile_email;
    ImageView profile_image;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RV = root.findViewById(R.id.profile_rv);
        parkingArrayList = new ArrayList<>();

        profile_name = binding.myProfileName;
        profile_email = binding.myProfileEmail;
        profile_image = binding.myProfileImage;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        profile_name.setText(user.getDisplayName());
        profile_email.setText(user.getEmail());
        Picasso.get().load(user.getPhotoUrl()).resize(180,180).into(profile_image);


        ProfileAdapter profileAdapter = new ProfileAdapter(getActivity(), parkingArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(RV.getContext(),
                linearLayoutManager.getOrientation());
        RV.addItemDecoration(dividerItemDecoration);

        RV.setLayoutManager(linearLayoutManager);
        RV.setAdapter(profileAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();

        db.collection("log")
                .whereEqualTo("client_id", uid)
                .orderBy("end_time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot logDoc : queryDocumentSnapshots){
                            db.collection("parking").document(logDoc.getString("parking_id"))
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                                    Parking p = new Parking(documentSnapshot);
                                    p.setStart_time(logDoc.getTimestamp("start_time"));
                                    p.setEnd_time(logDoc.getTimestamp("end_time"));
                                    p.setClient_id(logDoc.getString("client_id"));
                                    parkingArrayList.add(p);
                                    profileAdapter.notifyItemInserted(parkingArrayList.size() - 1);
                                }
                            });
                        }
                    }
                });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}