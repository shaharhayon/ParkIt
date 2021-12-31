package com.parkit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.Time;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.parkit.databinding.ActivityMainBinding;
import com.parkit.databinding.ActivitySignInBinding;

import org.w3c.dom.Text;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    ConstraintLayout currentParkingLayout;
    ImageButton stop_button;
    TextView time_passed;

    CountDownTimer timer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        setSupportActionBar(binding.appBarMain.toolbar);
//        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
////                        .setAction("Action", null).show();
//
//                signOut();
//
//            }
//        });
        binding.appBarMain.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_sign_out:
                        signOut();
                        break;
                }
                return false;
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_profile, R.id.nav_profile)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

    }

    public void currentParkingHandler(){
        currentParkingLayout = findViewById(R.id.currentParkingLayout);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();
        db.collection("parking").whereEqualTo("client_id", uid).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            TextView text = currentParkingLayout.findViewById(R.id.text_current_parking);
                            text.setText(doc.getString("address"));
                            currentParkingLayout.setVisibility(View.VISIBLE);
                            stop_button = findViewById(R.id.button_stop);
                            time_passed = findViewById(R.id.time_passed);
                            stop_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    releaseParking(doc);
                                }
                            });

                            Timestamp end = doc.getTimestamp("end_time");
                            Timestamp start = doc.getTimestamp("start_time");
                            Timestamp now = Timestamp.now();
                            long totalSeconds = end.getSeconds()- start.getSeconds();
                            ProgressBar p = findViewById(R.id.parking_progress_bar);

                            p.setMax((int) totalSeconds);
                            int startTime = (int) (now.getSeconds()-start.getSeconds());
                            p.setProgress(startTime);
                            time_passed.setText(secondsToString(startTime));
                            if(timer != null){
                                timer.cancel();
                                timer = null;
                            }
                            timer = new CountDownTimer((end.getSeconds()- now.getSeconds())*1000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    int secondsUntilFinished = (int) (millisUntilFinished/1000);
                                    int value = (int)(totalSeconds-secondsUntilFinished);
                                    p.setProgress((int) (value*100/totalSeconds) + startTime, false);
                                    time_passed.setText(secondsToString(startTime + value));
                                }

                                @Override
                                public void onFinish() {
                                    forceReleaseParking(doc);
                                }
                            };
                            timer.start();
                        }
                        else{
                            currentParkingLayout.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    private String secondsToString(long seconds){
        return String.format("%02d:%02d:%02d", seconds / 3600, (seconds / 60) % 60, seconds % 60);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void openSignIn(){
        Intent intent = new Intent(getApplicationContext(),SIgnInActivity.class);
        startActivity(intent);
    }

    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("15408172144-vj4dgebr6m46ar68r76eogmg04qp22ga.apps.googleusercontent.com")
                .requestEmail()
                .build();
        GoogleSignIn.getClient(getApplication(), gso).signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                openSignIn();
            }
        });
    }

    private void forceReleaseParking(DocumentSnapshot doc){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("parking").document(doc.getId())
                .update("client_id", null,
                        "start_time", null,
                        "end_time", null)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void unused) {
                        ParkingReleaseHandler(doc);
                        currentParkingLayout.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void releaseParking(DocumentSnapshot doc){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Release parking")
                .setMessage("Are you sure you want to release this parking?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("parking").document(doc.getId())
                                .update("client_id", null,
                                        "start_time", null,
                                        "end_time", null)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(@NonNull Void unused) {
                                        ParkingReleaseHandler(doc);
                                        currentParkingLayout.setVisibility(View.INVISIBLE);
                                    }
                                });
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void ParkingReleaseHandler(DocumentSnapshot doc){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("parking_id", doc.getId());
        data.put("client_id", uid);
        data.put("start_time", doc.getTimestamp("start_time"));
        data.put("end_time", Timestamp.now());
        db.collection("log").document().set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void unused) {
                Log.d("DATABASE", "added entry to log:" + data.toString());
                reduceTokens(doc);
            }
        });
    }

    private void reduceTokens(DocumentSnapshot doc){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                int previousTokens = documentSnapshot.getLong("tokens").intValue();
                long startTime = doc.getTimestamp("start_time").getSeconds();
                long endTime = Timestamp.now().getSeconds();
                int price = doc.getLong("price").intValue();
                int toReduceTokens = (int) ((price * (endTime - startTime)) / 3600);
                int currentTokens = previousTokens-toReduceTokens;
                db.collection("users").document(uid).update("tokens", currentTokens)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void unused) {
                                Log.d("TOKENS", "reduced " + toReduceTokens + " tokens. Current token count: "
                                        + (previousTokens-toReduceTokens));
                                Snackbar snack = Snackbar.make(binding.drawerLayout, "Parking released \nCurrent tokens:" + currentTokens,
                                        Snackbar.LENGTH_INDEFINITE);
                                snack.setAction("Dismiss", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        snack.dismiss();
                                    }
                                }).show();
                            }
                        });
            }
        });
    }
}