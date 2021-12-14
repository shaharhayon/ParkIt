package com.parkit.ui.home;

<<<<<<< Updated upstream
import android.os.Bundle;
=======
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
>>>>>>> Stashed changes
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

<<<<<<< Updated upstream
=======
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import java.util.Date;
import com.parkit.MainActivity;
import com.parkit.Parking;
>>>>>>> Stashed changes
import com.parkit.R;
import com.parkit.databinding.FragmentHomeBinding;
import com.parkit.ui.MapFragment;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

<<<<<<< Updated upstream
        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
=======
        EditText startdate_box = binding.dateStartBox;
        startdate_box.setInputType(InputType.TYPE_NULL);
        startdate_box.setOnClickListener(datepicker(startdate_box));

        EditText enddate_box = binding.dateEndBox;
        enddate_box.setInputType(InputType.TYPE_NULL);
        enddate_box.setOnClickListener(datepicker(enddate_box));

        //Google map
        Fragment fragment = new MapFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.frame_layout,fragment).commit();
        getChildFragmentManager().setFragmentResultListener("location", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                LatLng location = result.getParcelable("location");
                Log.d("Location",location.toString()); // WE HAVE A RESULT FROM THE MAP
            }
        });
        p = new Parking();
//        Date date = new Date(year,monthOfYear, dayOfMonth);
//        p.setPublish_time(new Timestamp(date));
        return root;
    }
    private View.OnClickListener datepicker(EditText textbox) {
        return new View.OnClickListener() {
>>>>>>> Stashed changes
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
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