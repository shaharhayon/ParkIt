package com.parkit.ui.slideshow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.parkit.Parking;
import com.parkit.R;
import com.parkit.databinding.FragmentSlideshowBinding;

import java.util.ArrayList;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;

    RecyclerView RV;
    ArrayList<Parking> parkingArrayList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RV = root.findViewById(R.id.recycler_cards);
        parkingArrayList = new ArrayList<>();

        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("parking")
                .orderBy("status", Query.Direction.DESCENDING) // enabled first
                .orderBy("client_id", Query.Direction.DESCENDING) // used first
//                .orderBy("client_id", Query.Direction.ASCENDING) // unused first
                .whereEqualTo("owner_id", uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()){
                            parkingArrayList.add(new Parking(document));
                        }
                        ParkingsAdapter parkingsAdapter = new ParkingsAdapter(getActivity(), parkingArrayList);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                        RV.setLayoutManager(linearLayoutManager);
                        RV.setAdapter(parkingsAdapter);
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