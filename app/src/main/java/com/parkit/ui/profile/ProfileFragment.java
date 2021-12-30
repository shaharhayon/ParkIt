package com.parkit.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
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

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ArrayList<Parking> parkingArrayList;
    RecyclerView RV;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RV = root.findViewById(R.id.profile_rv);
        parkingArrayList = new ArrayList<>();

        ProfileAdapter profileAdapter = new ProfileAdapter(getActivity(), parkingArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
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

                        /*List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                        for (DocumentSnapshot logDoc : queryDocumentSnapshots) {
                            Task<DocumentSnapshot> d = db.collection("parking")
                                    .document(logDoc.getString("parking_id"))
                                    .get();
                            tasks.add(d);
                        }

                        Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<Task<?>>> task) {
                                List<DocumentSnapshot> resultLogs = new ArrayList<>();

                                for (Task<DocumentSnapshot> taskSnapshot : tasks) {
                                    DocumentSnapshot snapshot = taskSnapshot.getResult();

                                    Parking p = new Parking(snapshot);
                                    p.setStart_time(logDoc.getTimestamp("start_time"));
                                    p.setEnd_time(logDoc.getTimestamp("end_time"));
                                    p.setClient_id(logDoc.getString("client_id"));
                                    parkingArrayList.add(p);
                                }

                            }
                        });*/

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