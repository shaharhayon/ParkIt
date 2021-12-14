package com.parkit.ui.home;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.Timestamp;
import java.util.Date;
import com.parkit.MainActivity;
import com.parkit.Parking;
import com.parkit.R;
import com.parkit.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private Parking p;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        homeViewModel =
//                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        EditText startdate_box = binding.dateStartBox;
        startdate_box.setInputType(InputType.TYPE_NULL);
        startdate_box.setOnClickListener(datepicker(startdate_box));

        EditText enddate_box = binding.dateEndBox;
        enddate_box.setInputType(InputType.TYPE_NULL);
        enddate_box.setOnClickListener(datepicker(enddate_box));

        p = new Parking();
//        Date date = new Date(year,monthOfYear, dayOfMonth);
//        p.setPublish_time(new Timestamp(date));
        return root;
    }

    private View.OnClickListener datepicker(EditText textbox) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//               DatePicker dp = root.findViewById(R.id.datePicker_start);
//               dp.setVisibility(View.VISIBLE);

                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);

                DatePickerDialog picker = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                textbox.setText(checkDigit(dayOfMonth) + "/" + checkDigit((monthOfYear + 1)) + "/" + year);

                                int hour = cldr.get(Calendar.HOUR);
                                int minute = cldr.get(Calendar.MINUTE);

                                TimePickerDialog picker = new TimePickerDialog(getActivity(),
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hour, int minute) {
//                                                textbox.setText(hour + ":" + minute);
                                                textbox.getText().append(" " + checkDigit(hour) + ":" + checkDigit(minute));
                                            }
                                        }, hour, minute, true);
                                picker.show();


                            }
                        }, year, month, day);
                picker.show();
            }


        };
    }

    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    private Date stringToDate(String date){
        int year = Integer.parseInt(date.substring(6,10));
        int month = Integer.parseInt(date.substring(3,5));
        int day = Integer.parseInt(date.substring(0,2));
        int hour = Integer.parseInt(date.substring(11,13));
        int minute = Integer.parseInt(date.substring(14));
        return new Date(year,month, day, hour, minute);
    }

//    private View.OnClickListener timepicker(EditText textbox) {
//        return new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////               DatePicker dp = root.findViewById(R.id.datePicker_start);
////               dp.setVisibility(View.VISIBLE);
//
//                final Calendar cldr = Calendar.getInstance();
//                int hour = cldr.get(Calendar.HOUR);
//                int minute = cldr.get(Calendar.MINUTE);
//
//                TimePickerDialog picker = new TimePickerDialog(getActivity(),
//                        new TimePickerDialog.OnTimeSetListener() {
//                            @Override
//                            public void onTimeSet(TimePicker view, int hour, int minute) {
//                                textbox.setText(hour + ":" + minute);
//                            }
//                        }, hour, minute, false);
//                picker.show();
//            }
//        };
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}