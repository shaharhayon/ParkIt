package com.parkit.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.parkit.R;
import com.parkit.databinding.FragmentGalleryBinding;

import java.text.SimpleDateFormat;

public class SearchFragment extends Fragment {

    private FragmentGalleryBinding binding;
    SearchView searchView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Widgets

        searchView = binding.SearchView;
        Fragment pinfragment = new PinFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.pinFragmentView, pinfragment).hide(pinfragment).commit();
//        getChildFragmentManager().beginTransaction().hide(pinfragment).commit();

        // Widgets setup

        searchView.setOnQueryTextListener(searchQueryHandler());

        // Map Fragment

        Fragment mapFragment = new SearchMapFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.SearchMapFragmentContainerView, mapFragment).commit();

        return root;
    }

    /**
     * handles sending data to map from searchview
     * @return SearchView.OnQueryTextListener
     */
    private SearchView.OnQueryTextListener searchQueryHandler() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Bundle addr = new Bundle();
                addr.putString("address_bundle", searchView.getQuery().toString());
                getChildFragmentManager().setFragmentResult("address_key", addr);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };
    }

    /**
     * hadnles showing and hiding the Pin view
     * does not change the data
     * @param f
     */
    public void showHidePin(Fragment f){
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        if (f.isHidden()) {
            transaction.show(f);
            Log.d("hidden","Show");
        } else {
            transaction.hide(f);
            Log.d("Shown","Hide");
        }
        transaction.commit();
    }

    /**
     * handles showing and hiding the Pin view.
     * loads data from marker
     * @param f
     * @param marker
     */
    public void showHidePin(Fragment f, Marker marker){
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        DocumentSnapshot doc = (DocumentSnapshot) marker.getTag();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(doc.getString("owner_id"))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy, HH:mm");
                            String dateString = format.format(doc.getTimestamp("expire_time").toDate());
                            String address = doc.getString("address");
                            String addressString = address.replaceFirst(", ", ",\n");
                Bundle mark = new Bundle();
                mark.putString("address_bundle", addressString);
                mark.putString("owner_bundle", task.getResult().getString("fullname"));
                mark.putString("expire_bundle", dateString);
                mark.putString("image_url", doc.getString("image_url"));
                mark.putString("parkingid_bundle", doc.getId());
                mark.putInt("price_bundle", doc.getLong("price").intValue());
                getChildFragmentManager().setFragmentResult("marker_key", mark);

            }
        });
        if (f.isHidden()) {
            transaction.show(f);
            Log.d("hidden","Show");
        } else {
            transaction.hide(f);
            Log.d("Shown","Hide");
        }
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}