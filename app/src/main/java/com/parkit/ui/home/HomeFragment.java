package com.parkit.ui.home;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Calendar;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.RoundedCorner;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

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

    //    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private Parking p;

    static final int REQUEST_IMAGE_CAPTURE = 0;
    static final int REQUEST_IMAGE_GALLERY = 1;

    private Uri outputFileUri;
    private LatLng location;
    private String owner_id;
//    private String imageUri;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        owner_id = FirebaseAuth.getInstance().getUid();

        // Widgets

        EditText startdate_box = binding.dateStartBox;
        EditText enddate_box = binding.dateEndBox;
        Button camera_button = binding.buttonImgCamera;
        Button gallery_button = binding.buttonImgGallery;
        SearchView address_box = binding.addressBox;
//        Button publish_button = binding.publishButton;
        FloatingActionButton publish_button = binding.publishButton;
        NumberPicker price_picker = binding.pricePicker;

        // Map Fragment

        Fragment fragment = new MapFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, fragment).commit();
        getChildFragmentManager().setFragmentResultListener("location", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                location = result.getParcelable("location");
                Geocoder geocoder = new Geocoder(getActivity());
                try {
                    String address = geocoder.getFromLocation(location.latitude, location.longitude,
                            1).get(0).getAddressLine(0);
                    address_box.setIconified(false);
                    address_box.setQuery(address, false);
                    address_box.clearFocus();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("Location", location.toString()); // WE HAVE A RESULT FROM THE MAP
            }
        });

        // Widgets setup

        address_box.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Bundle addr = new Bundle();
                addr.putString("address_bundle", address_box.getQuery().toString());
                getChildFragmentManager().setFragmentResult("address_key", addr);
//                Toast.makeText(getActivity(), "PRESSED", Toast.LENGTH_SHORT).show();
                address_box.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        startdate_box.setInputType(InputType.TYPE_NULL);
        startdate_box.setOnClickListener(datepicker(startdate_box));

        enddate_box.setInputType(InputType.TYPE_NULL);
        enddate_box.setOnClickListener(datepicker(enddate_box));

        price_picker.setMinValue(0);
        price_picker.setMaxValue(99);
        price_picker.setWrapSelectorWheel(false);


        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File f = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        File.separator + "CAM" + File.separator);
                f.mkdirs();
                String fname = owner_id + ".jpg";
                File sdImageMainDirectory = new File(f, fname);
//                outputFileUri = Uri.fromFile(sdImageMainDirectory);
                outputFileUri = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                        getActivity().getApplicationContext().getPackageName() + ".provider",
                        sdImageMainDirectory);

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        .putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }

            }
        });

        gallery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, REQUEST_IMAGE_GALLERY);//one can be replaced with any action code

            }
        });

        publish_button.setOnClickListener(new View.OnClickListener() {
            /**
             * Generates unique ID for the image and saves it as its URL
             * inserts all the data into new Parking object p
             * calls Parking.publish
             * uploads image to Storage
             * @param v
             */
            @Override
            public void onClick(View v) {
                if (checkAllFields()) {
                    UUID uuid = UUID.randomUUID();
                    // Parking_ID is the name of the document in firestore, auto-generated
                    String StorageImgPath = "images/" + uuid.toString();
                    String geoHash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(location.latitude, location.longitude));

                    p = new Parking();
                    p.setLocation(new GeoPoint(location.latitude, location.longitude));
                    p.setPublish_time(new Timestamp(stringToDate(startdate_box.getText().toString())));
                    p.setExpire_time(new Timestamp(stringToDate(enddate_box.getText().toString())));
                    p.setOwner_id(owner_id);
                    p.setAddress(address_box.getQuery().toString());
                    p.setGeohash(geoHash);
                    p.setImage_url(StorageImgPath);
                    p.setPrice(price_picker.getValue());

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imgRef = storageRef.child(StorageImgPath);

                    imgRef.putFile(outputFileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
//                            Toast.makeText(getActivity(),"Image Uploaded Successfully",Toast.LENGTH_SHORT).show();
//                            imageUri = task.getResult().getMetadata().getPath();
                                Log.d("UPLOAD", "upload image success");
                                p.publish(getView());
//                            Toast.makeText(getActivity(),imageUri,Toast.LENGTH_SHORT).show();
                            } else {
//                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.d("UPLOAD", "upload image failure");
                                Snackbar snack = Snackbar.make(getView(), "Problem uploading image. \nThe parking has not been published.", Snackbar.LENGTH_INDEFINITE);
                                snack.setAction("Dismiss", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        snack.dismiss();
                                    }
                                }).show();

                            }
                        }
                    });
                }
            }
        });

        return root;
    }

    /**
     * Handles results for activities:
     * + Image capture using camera
     * + Image selection from gallery
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            binding.imgParking.setStateDescription("IMAGE_LOADED");
            switch (requestCode) {
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

    /**
     * Returns date and time interactive picker that stores data into EditText textbox
     *
     * @param textbox
     * @return View.OnClickListener
     */
    private View.OnClickListener datepicker(EditText textbox) {
        return new View.OnClickListener() {
            private boolean isStartBox() {
                if (textbox.getId() == R.id.date_start_box)
                    return true;
                return false;
            }

            private boolean isEndBox() {
                if (textbox.getId() == R.id.date_end_box)
                    return true;
                return false;
            }

            private boolean isStartBoxEmpty() {
                return binding.dateStartBox.getText().toString().equals(getString(R.string.start_date));
            }

            private boolean isEndBoxEmpty() {
                return binding.dateEndBox.getText().toString().equals(getString(R.string.end_date));
            }

            @Override
            public void onClick(View v) {
                String previous = textbox.getText().toString();
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
                                                textbox.getText().append(" " + checkDigit(hour) + ":" + checkDigit(minute));
                                                if (!isStartBoxEmpty() && !isEndBoxEmpty()) {
                                                    Date start = stringToDate(binding.dateStartBox.getText().toString());
                                                    Date end = stringToDate(binding.dateEndBox.getText().toString());
                                                    if (start.after(end)) {
                                                        textbox.setText(previous);
                                                        new AlertDialog.Builder(getContext())
                                                                .setTitle("Date and time")
                                                                .setMessage("Start time cannot be after end time.")
                                                                .setNeutralButton(android.R.string.ok, null)
                                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                                .show();
                                                        return;
                                                    }
                                                }
                                            }
                                        }, hour, minute, true);
                                picker.show();
                            }
                        }, year, month, day);
                if (isStartBox()) {
                    picker.getDatePicker().setMinDate(new Date().getTime());
                    if (!isStartBoxEmpty()) {
                        Date maxDate = stringToDate(binding.dateEndBox.getText().toString());
                        picker.getDatePicker().setMaxDate(maxDate.getTime());
                    }
                } else if (isEndBox()) {
                    if (!isStartBoxEmpty()) {
                        Date minDate = stringToDate(binding.dateStartBox.getText().toString());
                        picker.getDatePicker().setMinDate(minDate.getTime());
                    } else {
                        picker.getDatePicker().setMinDate(new Date().getTime());
                    }
                }
                picker.show();
            }
        };
    }

    /**
     * Adds preceding zero to single digit integer
     *
     * @param number
     * @return String
     */
    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    /**
     * Convert text in EditText into Date object,
     * Fix numbers using constant because of how java Date object works
     *
     * @param date
     * @return Date object from String date
     */
    private Date stringToDate(String date) {
        int year = Integer.parseInt(date.substring(6, 10)) - 1900;
        int month = Integer.parseInt(date.substring(3, 5)) - 1;
        int day = Integer.parseInt(date.substring(0, 2));
        int hour = Integer.parseInt(date.substring(11, 13));
        int minute = Integer.parseInt(date.substring(14));
        return new Date(year, month, day, hour, minute);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private boolean checkAllFields() {
        String nullFields = "";
        if (binding.dateStartBox.getText().toString().equals(getString(R.string.start_date)))
            nullFields += "start date\n";
        if (binding.dateEndBox.getText().toString().equals(getString(R.string.end_date)))
            nullFields += "end date\n";
        if (binding.imgParking.getStateDescription() == null)
            nullFields += "image\n";
        if (binding.addressBox.getQuery().toString().isEmpty())
            nullFields += "address\n";
        if (location == null)
            nullFields += "location\n";

        if (nullFields.isEmpty())
            return true;

        String nullFieldsString = nullFields;
        if (nullFields.length() <= 11)
            nullFieldsString += "\n\nThis field is required in order to publish a new parking.";
        else
            nullFieldsString += "\n\nThose fields are required in order to publish a new parking.";


        new AlertDialog.Builder(getContext())
                .setTitle("Missing fields")
                .setMessage(nullFieldsString)
                .setNeutralButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}