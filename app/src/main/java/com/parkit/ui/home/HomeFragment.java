package com.parkit.ui.home;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.Timestamp;

import java.io.File;
import java.util.Date;

import com.google.type.LatLng;
import com.parkit.MainActivity;
import com.parkit.Parking;
import com.parkit.R;
import com.parkit.databinding.FragmentHomeBinding;
import com.parkit.ui.MapFragment;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private Parking p;

    static final int REQUEST_IMAGE_CAPTURE = 0;
    static final int REQUEST_IMAGE_GALLERY = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        homeViewModel =
//                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Fragment fragment = new MapFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.fragmentContainerView,fragment).commit();
        getChildFragmentManager().setFragmentResultListener("location", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                LatLng location = result.getParcelable("location");
                Log.d("Location",location.toString()); // WE HAVE A RESULT FROM THE MAP
            }
        });


        EditText startdate_box = binding.dateStartBox;
        startdate_box.setInputType(InputType.TYPE_NULL);
        startdate_box.setOnClickListener(datepicker(startdate_box));

        EditText enddate_box = binding.dateEndBox;
        enddate_box.setInputType(InputType.TYPE_NULL);
        enddate_box.setOnClickListener(datepicker(enddate_box));

        ImageView img = binding.imgParking;
//        img.setImageURI();

        Button camera_button = binding.buttonImgCamera;
        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                File f = new File(android.os.Environment.getExternalStorageDirectory(),
//                        "temp_img.jpg");
//                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, f);
//                startActivityForResult(takePicture, 0);//zero can be replaced with any action code (called requestCode)

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }

            }
        });

        Button gallery_button = binding.buttonImgGallery;
        gallery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);//one can be replaced with any action code

            }
        });
        p = new Parking();
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            binding.imgParking.setImageBitmap(imageBitmap);
//            android.os.Environment.getExternalStorageDirectory()
        }
    }


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(resultCode == RESULT_OK){
//            if(requestCode == 0){
//                File f = new File(android.os.Environment.getExternalStorageDirectory().toString());
//                for (File temp : f.listFiles()) {
//                    if (temp.getName().equals("temp_img.jpg")) {
//                        f = temp;
//                        break;
//                    }
//                }
//                try{
//                    Bitmap bitmap;
//                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
//                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), bitmapOptions);
//                    binding.imgParking.setImageBitmap(bitmap);
//                }
//                catch (Exception e){
//                    Log.d("IMAGE_CAM", e.getMessage());
//                }
//            }
//        }
//    }


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