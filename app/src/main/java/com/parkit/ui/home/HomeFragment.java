package com.parkit.ui.home;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
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

    String currentPhotoPath;

    private Uri outputFileUri;
    private LatLng location;
    private String owner_id;
    private String imageUri;
    private int nextParking;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        homeViewModel =
//                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        owner_id = FirebaseAuth.getInstance().getUid();


        Fragment fragment = new MapFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.fragmentContainerView,fragment).commit();
        getChildFragmentManager().setFragmentResultListener("location", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                location = result.getParcelable("location");
                Log.d("Location",location.toString()); // WE HAVE A RESULT FROM THE MAP
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                        location.toString(), Toast.LENGTH_LONG);
                toast.show();
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
                File f = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        File.separator + "CAM" + File.separator);
                f.mkdirs();
                String fname = owner_id + ".jpg";
                File sdImageMainDirectory = new File(f, fname);
//                outputFileUri = Uri.fromFile(sdImageMainDirectory);
                outputFileUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), getActivity().getApplicationContext().getPackageName() + ".provider", sdImageMainDirectory);

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
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
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);//one can be replaced with any action code

            }
        });

        p = new Parking();

        Button publish_button = binding.publishButton;
        publish_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseFirestore.getInstance().collection("parking").whereEqualTo("owner_id", owner_id)
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            int nextParkings = task.getResult().getDocuments().size() + 1;
                        }
                    }
                });

                p.setLocation(new GeoPoint(location.latitude, location.longitude));
                p.setPublish_time(new Timestamp(stringToDate(startdate_box.getText().toString())));
                p.setExpire_time(new Timestamp(stringToDate(enddate_box.getText().toString())));
//                p.setParking_id(nextParking);
                // owner
                // address




                int id = 0; // parking id
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference imgRef = storageRef.child("images/" + id);
//                img.setDrawingCacheEnabled(true);
//                img.buildDrawingCache();

                imgRef.putFile(outputFileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getActivity(),"Image Uploaded Successfully",Toast.LENGTH_SHORT).show();
                            imageUri = task.getResult().getMetadata().getPath().toString();
//                            Toast.makeText(getActivity(),imageUri,Toast.LENGTH_SHORT).show();
                            p.publish();
                        }
                        else {
                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });





            }
        });
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_IMAGE_CAPTURE:
                    binding.imgParking.setImageURI(outputFileUri);
//            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
//                    data.getData().getPath(),
//                    Toast.LENGTH_LONG);
//            toast.show();
                    break;
                case REQUEST_IMAGE_GALLERY:
                    binding.imgParking.setImageURI(data.getData());
            }
        }
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

    private long hashID(int parking_id){
        final int numParkings;
        FirebaseFirestore.getInstance().collection("parking").whereEqualTo("owner_id", owner_id)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    numParkings = task.getResult().getDocuments().size();
                }
            }
        });

        return 31 * numParkings + owner_id.hashCode();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}