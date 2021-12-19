package com.parkit.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import com.parkit.R;
import com.parkit.ui.home.HomeFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

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
                if (checkLocationPermissions(container.getContext())) {
                    centerLocation(container.getContext());
                }

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        setMarker(latLng);
                        sendResult(latLng);
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
                        sendResult(marker.getPosition());
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
                    if(!locationList.isEmpty()) {
                        Address location = locationList.get(0);
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        CameraUpdate cameraLocation = CameraUpdateFactory.newLatLngZoom(latLng, 18);
                        gMap.moveCamera(cameraLocation);
                        setMarker(latLng);
                        sendResult(latLng);
                    }
                    else{
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
                setMarker(latLng);
//                sendResult(latLng);
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

    private void sendResult(LatLng latLng){
        FragmentManager homeFragment = getParentFragmentManager();
        Bundle args = new Bundle();
        args.putParcelable("location",latLng);
        homeFragment.setFragmentResult("location",args);
    }
}