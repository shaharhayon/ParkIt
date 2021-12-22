package com.parkit.ui.gallery;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.parkit.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchMapFragment extends Fragment {

    static final int REQUEST_LOCATION = 100;

    GoogleMap gMap;

    Geocoder geocoder;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (checkLocationPermissions(container.getContext()) == false) {
            askLocationPermissions();
        }
        geocoder = new Geocoder(getActivity());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);


        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                gMap = googleMap;
                relocateMyLocationButton(view);
                if (checkLocationPermissions(container.getContext())) {
                    centerLocation(container.getContext());
                }


                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        centerMap(latLng);

//                        pinView.setVisibility(FragmentContainerView.VISIBLE);
                        setMarker(latLng);
//                        sendResult(latLng);
//                        FragmentManager homeFragment = getParentFragmentManager();
//                        Bundle args = new Bundle();
//                        args.putParcelable("location",latLng);
//                        homeFragment.setFragmentResult("location",args);

//                        homeFragment.setFragmentResult("location",args);
//                        googleMap.clear();
//                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                    }
                });
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        ((GalleryFragment) getParentFragment()).showHidePin(getParentFragment().getChildFragmentManager().findFragmentById(R.id.pinFragmentView), marker);

//                        FirebaseStorage storage = FirebaseStorage.getInstance();
//                        String imageUrl = ((DocumentSnapshot) marker.getTag()).getString("image_url")
//                        StorageReference img_ref = storage.getReference(imageUrl);
//                        final long ONE_MEGABYTE = 1024 * 1024;
//                        img_ref.getBytes(ONE_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
//                            @Override
//                            public void onComplete(@NonNull Task<byte[]> task) {
//
//                            }
//                        });
                        return false;
                    }
                });
            }
        });

        getParentFragmentManager().setFragmentResultListener("address_key", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String address = result.getString("address_bundle");
//                Geocoder geocoder = new Geocoder(getActivity());
                try {
                    List<Address> locationList = geocoder.getFromLocationName(address, 1); //.get(0);
                    if (!locationList.isEmpty()) {
                        Address location = locationList.get(0);
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        CameraUpdate cameraLocation = CameraUpdateFactory.newLatLngZoom(latLng, 18);
                        gMap.moveCamera(cameraLocation);
                        setMarker(latLng);
                        sendResult(latLng);
                    } else {
                        Toast.makeText(getActivity(), "Cant find the specified address", Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
        });

        return view;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED
                        || grantResults[1] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getActivity(),
                            "Location permission denied. \nCurrent location will be disabled.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    centerLocation(getContext());
                    Toast.makeText(getActivity(),
                            "Location permission granted.",
                            Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void centerLocation(Context context) {
        gMap.setMyLocationEnabled(true);
        LocationManager m = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        m.requestSingleUpdate(LocationManager.FUSED_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate centerLocation = CameraUpdateFactory.newLatLngZoom(latLng, 18);
                gMap.moveCamera(centerLocation);
//                setMarker(latLng);
//                sendResult(latLng);
                getParkingsAround(latLng);
            }
        }, null);
    }

    private static boolean checkLocationPermissions(Context context) {
        return ((ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED));
    }

    private void askLocationPermissions() {
        Log.d("PERMISSIONS", "Asking device for location permissions");
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }

    private void setMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(latLng.latitude + " : " + latLng.longitude);
        try {
            String title = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0).getAddressLine(0);
            markerOptions.title(title);
        } catch (IOException e) {
            e.printStackTrace();
        }

        gMap.clear();
        gMap.addMarker(markerOptions);
    }

    private void sendResult(LatLng latLng) {
        FragmentManager homeFragment = getParentFragmentManager();
        Bundle args = new Bundle();
        args.putParcelable("location", latLng);
        homeFragment.setFragmentResult("location", args);
    }

    private void centerMap(LatLng latLng) {
        gMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void relocateMyLocationButton(View view) {
        View locationButton = ((View) view.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                locationButton.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 0, 30, 30);
    }

    private void getParkingsAround(LatLng latLng) {
        double radius = 2 * 1000;
        GeoLocation center = new GeoLocation(latLng.latitude, latLng.longitude);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<GeoQueryBounds> boundsList = GeoFireUtils.getGeoHashQueryBounds(center, radius);
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : boundsList) {
            Query q = db.collection("parking")
                    .orderBy("geohash")
                    .orderBy("expire_time")
                    .startAt(b.startHash)
                    .endAt(b.endHash)
//                    .whereGreaterThanOrEqualTo("expire_time", Timestamp.now())
//                    .whereLessThanOrEqualTo("publish_time", Timestamp.now())
                    .whereEqualTo("client_id", null);
            tasks.add(q.get());
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {
                List<DocumentSnapshot> results = new ArrayList<>();
                for (Task<QuerySnapshot> taskSnapshot : tasks) {
                    QuerySnapshot snapshot = taskSnapshot.getResult();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        GeoPoint geoPoint = document.getGeoPoint("location");
                        GeoLocation docLocation = new GeoLocation(
                                geoPoint.getLatitude(), geoPoint.getLongitude());
                        double distanceMeters = GeoFireUtils.getDistanceBetween(docLocation, center);
                        if (distanceMeters <= radius)
                            results.add(document);
                    }
                }
                for (DocumentSnapshot document : results) {
                    GeoPoint geoPoint = document.getGeoPoint("location");
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()));
                    marker.title(document.getString("address"));

                    gMap.addMarker(marker).setTag(document);
                }
            }
        });
    }
}