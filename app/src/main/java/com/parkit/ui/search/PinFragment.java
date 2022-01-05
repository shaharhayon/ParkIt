package com.parkit.ui.search;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.parkit.MainActivity;
import com.parkit.R;

public class PinFragment extends Fragment {

    View view;
    Fragment f;
    SearchFragment parentFragment;

    TextView owner;
    TextView address;
    TextView expiretime;
    TextView price;
    ImageView image;
    View popupView;

    String string_address;
    String string_owner;
    String string_expiretime;
    String string_image_url;
    String string_parkingid;
    Integer integer_price;
    Integer user_tokens;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_pin, container, false);
        f = this;
        parentFragment = (SearchFragment) getParentFragment();

        // Widgets

        owner = view.findViewById(R.id.textView_owner_data);
        address = view.findViewById(R.id.textView_address_data);
        expiretime = view.findViewById(R.id.textView_expiretime_data);
        price = view.findViewById(R.id.textView_price_data);
        image = view.findViewById(R.id.imageView_parking);
        popupView = inflater.inflate(R.layout.claim_popup, null);

        ImageButton button_close = view.findViewById(R.id.button_pin_close);

        FloatingActionButton fab = view.findViewById(R.id.TakeParkingButton);

        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentFragment.showHidePin(f);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptTakeParking();
            }
        });

        getParentFragmentManager().setFragmentResultListener("marker_key", this, receiveDataHandler());

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
//        super.onHiddenChanged(hidden);
        if (hidden) {
            clear();
        }
    }

    private void clear() {
        owner.setText(null);
        address.setText(null);
        expiretime.setText(null);
        price.setText(null);
//        image.setImageDrawable(ResourcesCompat
//                .getDrawable(Resources.getSystem(), R.drawable.ic_menu_gallery, null));
        image.setImageResource(R.drawable.ic_menu_gallery);

        string_address = null;
        string_owner = null;
        string_expiretime = null;
        string_image_url = null;
        string_parkingid = null;
        integer_price = null;
        user_tokens = null;
    }

    /**
     * listener that handles data from parent fragment
     *
     * @return FragmentResultListener
     */
    private FragmentResultListener receiveDataHandler() {
        return new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                string_address = result.getString("address_bundle");
                string_owner = result.getString("owner_bundle");
                string_expiretime = result.getString("expire_bundle");
                string_image_url = result.getString("image_url");
                string_parkingid = result.getString("parkingid_bundle");
                integer_price = result.getInt("price_bundle");
                owner.setText(string_owner);
                address.setText(string_address);
                expiretime.setText(string_expiretime);
                price.setText(String.valueOf(integer_price));

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference ref = storage.getReference(string_image_url);
                ref.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(@NonNull StorageMetadata storageMetadata) {
                        long size = storageMetadata.getSizeBytes();
                        ref.getBytes(size).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(@NonNull byte[] bytes) {
                                Drawable img = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                image.setImageDrawable(img);
                            }
                        });
                    }
                });
            }
        };
    }

    /**
     * query Firebase Firestore to make sure the parking is still available
     */
    private void attemptTakeParking() {
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                user_tokens = documentSnapshot.getLong("tokens").intValue();
                if ((user_tokens <= 0)
                        && (integer_price != 0)) {
                    showErrorToast("Can't rent this parking, \nyou have 0 tokens");
                } else {
                    db.collection("parking").whereEqualTo("client_id", uid).get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                                    if (queryDocumentSnapshots.isEmpty()) {
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle("Rent parking")
                                                .setMessage("Are you sure you want to rent this parking?")
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        takeParking();
                                                    }
                                                })
                                                .setNegativeButton(android.R.string.cancel, null)
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .show();
                                    } else {
                                        showErrorToast("You're already renting a parking.");
                                    }
                                }
                            });
                }
            }
        });
    }

    /**
     * query Firebase Firestore to start using the parking
     */
    private void takeParking() {
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("parking").document(string_parkingid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.getString("client_id") == null) {
                            double max_time_tokens_in_seconds = 3600 * (user_tokens.doubleValue() / integer_price.doubleValue());
                            double max_time_expire_in_seconds =
                                    documentSnapshot.getTimestamp("expire_time").getSeconds() - Timestamp.now().getSeconds();
                            Timestamp expireTimestamp;
                            if (max_time_tokens_in_seconds < max_time_expire_in_seconds) {
                                expireTimestamp = new Timestamp((long) (Timestamp.now().getSeconds() + max_time_tokens_in_seconds), 0);
                            } else {
                                expireTimestamp = documentSnapshot.getTimestamp("expire_time");
                            }
                            db.collection("parking").document(documentSnapshot.getId())
                                    .update("client_id", uid,
                                            "start_time", Timestamp.now(),
                                            "end_time", expireTimestamp)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(@NonNull Void unused) {
                                            // add end time to popup
                                            showSuccessPopup();
                                        }
                                    });
                        } else {
                            showErrorToast("parking already taken!");
                        }
                    }
                });
    }

    private void showSuccessPopup() {
        Toast toast = new Toast(getActivity().getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(popupView);
        toast.show();
        ((MainActivity) getActivity()).currentParkingHandler();
    }

    private void showErrorToast(String text) {
        Toast toast = new Toast(getActivity().getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setText(text);
        toast.show();
    }

}
