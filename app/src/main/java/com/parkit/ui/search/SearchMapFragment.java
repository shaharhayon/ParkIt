package com.parkit.ui.search;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.parkit.R;

import java.io.IOException;
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
    List<Marker> MarkersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (checkLocationPermissions(container.getContext()) == false) {
            askLocationPermissions();
        }
        geocoder = new Geocoder(getActivity());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        MarkersList = new ArrayList<>();

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                gMap = googleMap;
                gMap.getUiSettings().setCompassEnabled(false);
                relocateMyLocationButton(supportMapFragment.getView());
                if (checkLocationPermissions(container.getContext())) {
                    centerLocation(container.getContext());
                }

                // GoogleMap listeners

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        centerMap(latLng);
                    }
                });
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        ((SearchFragment) getParentFragment()).showHidePin(getParentFragment().getChildFragmentManager().findFragmentById(R.id.pinFragmentView), marker);
                        return false;
                    }
                });
                googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        LatLng center = googleMap.getCameraPosition().target;
                        clearOutOfRangeMarkers();
                        getParkingsAround(center);
                    }
                });
            }
        });


        // Searchbar listener
        /**
         * listener that handles address string recieved from parent fragment
         * @return FragmentResultListener
         */
        getParentFragmentManager().setFragmentResultListener("address_key", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String address = result.getString("address_bundle");
                try {
                    List<Address> locationList = geocoder.getFromLocationName(address, 1); //.get(0);
                    if (!locationList.isEmpty()) {
                        Address location = locationList.get(0);
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        CameraUpdate cameraLocation = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                        gMap.animateCamera(cameraLocation);
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

    /**
     * centers the camera around the user's location
     * @param context
     */
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
                getParkingsAround(latLng);
            }
        }, null);
    }

    /**
     * set a marker on the map
     * adds the marker to the map's markers list
     * @param markerOptions
     * @return
     */
    private Marker setMarker(MarkerOptions markerOptions) {
        Marker m = gMap.addMarker(markerOptions);
        MarkersList.add(m);
        return m;
    }

    /**
     * centers the map around LatLng latLng
     * @param latLng
     */
    private void centerMap(LatLng latLng) {
        gMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    /**
     * relocates My Location button so it wont override map features
     * @param view
     */
    private void relocateMyLocationButton(View view) {
        View locationButton = ((View) view.findViewWithTag("GoogleMapMyLocationButton"));
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                locationButton.getLayoutParams();
        int[] ruleList = layoutParams.getRules();
        for (int i = 0; i < ruleList.length; i ++) {
            layoutParams.removeRule(i);
        }
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(30, 30, 30, 70);
        layoutParams.setMarginStart(30);
        locationButton.setLayoutParams(layoutParams);
    }

    /**
     * calculates current view radius from corner to corner
     * @return
     */
    private double getCurrentViewRadius(){
        LatLng latLng1 = gMap.getProjection().getVisibleRegion().latLngBounds.northeast;
        LatLng latLng2 = gMap.getProjection().getVisibleRegion().latLngBounds.southwest;
        float[] maxDistance = new float[1];
        Location.distanceBetween(latLng1.latitude, latLng1.longitude,latLng2.latitude, latLng2.longitude, maxDistance);
        return maxDistance[0];
    }

    /**
     * updates the map with available parkings aroung LatLng latLng
     * in a dynamic radius according to view distance
     * limit 100 results
     * @param latLng
     */
    private void getParkingsAround(LatLng latLng) {
        double radius = getCurrentViewRadius();
        GeoLocation center = new GeoLocation(latLng.latitude, latLng.longitude);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<GeoQueryBounds> boundsList = GeoFireUtils.getGeoHashQueryBounds(center, radius);
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : boundsList) {
            Query q = db.collection("parking")
                    .orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash)
                    .whereEqualTo("client_id", null)
                    .whereEqualTo("status", true)
                    .limit(100);
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
                        // remove irrelevant data as firebase can only query 1 complex condition
                        if ((distanceMeters <= radius) &&
                                (document.getTimestamp("expire_time").compareTo(Timestamp.now()) > 0) &&
                                (document.getTimestamp("publish_time").compareTo(Timestamp.now()) < 0))
                        {
                            results.add(document);
                        }
                    }
                }
                for (DocumentSnapshot document : results) {
                    GeoPoint geoPoint = document.getGeoPoint("location");
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()));
                    marker.title(document.getString("address"));
                    setMarker(marker).setTag(document);
                }
            }
        });
    }

    /**
     * removes all markers that are out of range, to not overload the map with markers
     */
    public void clearOutOfRangeMarkers(){
        LatLng center = gMap.getCameraPosition().target;
        double radius = getCurrentViewRadius();
        float[] distance = new float[1];
        List<Marker> toRemove = new ArrayList<>();
        for (Marker m : MarkersList){
            Location.distanceBetween(m.getPosition().latitude, m.getPosition().longitude,
                    center.latitude, center.longitude, distance);
            if(distance[0] > radius){
                toRemove.add(m);
                m.remove();
            }
        }
        MarkersList.removeAll(toRemove);
    }

    // Permission methods

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
}