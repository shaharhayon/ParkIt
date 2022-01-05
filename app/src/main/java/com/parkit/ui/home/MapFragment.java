package com.parkit.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parkit.R;

import java.io.IOException;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
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

        getParentFragmentManager().setFragmentResultListener("address_key", this, recieveAddressHandler());

        return view;
    }

    /**
     * permissions result handler
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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

    /**
     * centers the camera around the user's location
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("MissingPermission")
    private void centerLocation(Context context) {
        gMap.setMyLocationEnabled(true);
        LocationManager m = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        m.requestSingleUpdate(LocationManager.FUSED_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate centerLocation = CameraUpdateFactory.newLatLngZoom(latLng, 18);
                gMap.animateCamera(centerLocation);
//                setMarker(latLng);
//                sendResult(latLng);
            }
        }, null);
    }

    /**
     * checks if the required permissions are approved
     * @param context
     * @return true if permissions granted, else returns false
     */
    private static boolean checkLocationPermissions(Context context) {
        return ((ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED));
    }

    /**
     * open permission request dialog
     */
    private void askLocationPermissions() {
        Log.d("PERMISSIONS", "Asking device for location permissions");
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }

    /**
     * sets marker on the map, at position latLng
     * @param latLng
     */
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

    /**
     * sends LatLng object to parent fragment
     * @param latLng
     */
    private void sendResult(LatLng latLng){
        FragmentManager homeFragment = getParentFragmentManager();
        Bundle args = new Bundle();
        args.putParcelable("location",latLng);
        homeFragment.setFragmentResult("location",args);
    }

    /**
     * listener that handles address string recieved from parent fragment
     * @return FragmentResultListener
     */
    private FragmentResultListener recieveAddressHandler() {
        return new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String address = result.getString("address_bundle");
                try {
                    List<Address> locationList = geocoder.getFromLocationName(address, 1);
                    if(!locationList.isEmpty()) {
                        Address location = locationList.get(0);
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        CameraUpdate cameraLocation = CameraUpdateFactory.newLatLngZoom(latLng, 18);
                        gMap.animateCamera(cameraLocation);
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
        };
    }
}