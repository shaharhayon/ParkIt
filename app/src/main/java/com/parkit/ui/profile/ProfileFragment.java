package com.parkit.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.parkit.Parking;
import com.parkit.R;
import com.parkit.databinding.FragmentProfileBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ArrayList<Parking> parkingArrayList;
    RecyclerView RV;

    TextView profile_name, profile_email;
    ImageView profile_image;

    ProfileAdapter profileAdapter;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RV = root.findViewById(R.id.profile_rv);
        parkingArrayList = new ArrayList<>();

        profile_name = binding.myProfileName;
        profile_email = binding.myProfileEmail;
        profile_image = binding.myProfileImage;

        initProfile();
        initAdapter();
        queryData();

        return root;
    }

    /**
     * init profile picture and text to user data
     */
    private void initProfile(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        profile_name.setText(user.getDisplayName());
        profile_email.setText(user.getEmail());
        Picasso.get().load(user.getPhotoUrl()).resize(180,180).into(profile_image);
    }

    /**
     * init profile adapter for history dynamic list
     */
    private void initAdapter(){
        profileAdapter = new ProfileAdapter(getActivity(), parkingArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(RV.getContext(),
                linearLayoutManager.getOrientation());
        RV.addItemDecoration(dividerItemDecoration);

        RV.setLayoutManager(linearLayoutManager);
        RV.setAdapter(profileAdapter);
    }

    /**
     * query data from firebase to fill history dynamic list
     */
    private void  queryData(){
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}