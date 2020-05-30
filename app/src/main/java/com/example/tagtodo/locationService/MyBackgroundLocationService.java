package com.example.tagtodo.locationService;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tagtodo.R;
import com.example.tagtodo.model.Note;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import static android.telephony.AvailableNetworkInfo.PRIORITY_HIGH;

public class MyBackgroundLocationService extends Service {
    private static final String TAG = MyBackgroundLocationService.class.getSimpleName();
    private FusedLocationProviderClient mLocationClient;
    private LocationCallback mLocationCallback;
    LocationResultHelper helper;

    String locBack = "def";
    public static final String CHANNEL_ID = "default-channel";


    FirebaseFirestore fStore;
    FirebaseUser fUser;
    Note note;
    List<Note> noteList;

    private String title;
    private String content;
    private String location;
    private String latitude;
    private String longitude;
    private String noteId;
    public static MediaPlayer mp;


    HashMap<String, Note> noteHashMap;
    StringBuilder s;
    String imageUri;


    public MyBackgroundLocationService() {
    }

    @Override
    public void onCreate() {
        mp=MediaPlayer.create(getApplicationContext(), R.raw.alert);

        fUser               = FirebaseAuth.getInstance().getCurrentUser();
        fStore              = FirebaseFirestore.getInstance();
        noteList            = new ArrayList<>();

        fStore.collection("notes").document(fUser.getUid()).collection("userNotes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    noteHashMap = new HashMap<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        title = (String) document.get("title");
                        content = (String) document.get("content");
                        location = (String) document.get("location");
                        latitude = (String) document.get("latitude");
                        longitude = (String) document.get("longitude");

                        note = new Note(title, content, location, latitude, longitude);
                        noteId = document.getId();
                        imageUri = "Users/" + fUser.getUid() + "/" + noteId + "/Images.jpeg";

                        noteHashMap.put(noteId, note);
                    }
//                    s = new StringBuilder();
//                    for (String key : noteHashMap.keySet()) {
//                        Note value = noteHashMap.get(key);
//                        s.append("\nNoteID = "+key);
//                        s.append("\nTitle = "+ value.getTitle());
//                        s.append("\nContent = "+ value.getContent());
//                        s.append("\nLocation = "+ value.getLocation());
//                        s.append("\nLatitude = "+ value.getLatitude());
//                        s.append("\nLongitude = "+ value.getLongitude());
//                        s.append("\n");
//                    }
                }
            }
        });


//        Toast.makeText(this, "create", Toast.LENGTH_SHORT).show();

        super.onCreate();
        mLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    locBack = locationResult.toString();
                    Log.d(TAG, "onLocationResult: location error");
                    return;
                }

                List<Location> locations = locationResult.getLocations();


                    s = new StringBuilder();
                    for (String key : noteHashMap.keySet()) {
                        Note value = noteHashMap.get(key);
                        if (value.getLongitude() != null && value.getLongitude() != null) {
                            Double latNote = Double.parseDouble(value.getLatitude());
                            Double longNote = Double.parseDouble(value.getLongitude());

                            Double latCurr = locations.get(0).getLatitude();
                            Double longCurr = locations.get(0).getLongitude();
                            Resources res = getResources();

                            if (distanceBetweenPoints(latNote, latCurr, longNote, longCurr) <= res.getInteger(R.integer.distance)) {
                                helper = new LocationResultHelper(getApplicationContext(), locations);
                                helper.showNotification(key, noteHashMap.get(key), imageUri);
                                helper.saveLocationResults();
                                Toast.makeText(getApplicationContext(), "Complete task!" + locations.size(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mp=MediaPlayer.create(getApplicationContext(), R.raw.alert);

        fUser               = FirebaseAuth.getInstance().getCurrentUser();
        fStore              = FirebaseFirestore.getInstance();
        noteList            = new ArrayList<>();


        fStore.collection("notes").document(fUser.getUid()).collection("userNotes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    noteHashMap = new HashMap<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        title = (String) document.get("title");
                        content = (String) document.get("content");
                        location = (String) document.get("location");
                        latitude = (String) document.get("latitude");
                        longitude = (String) document.get("longitude");
                        note = new Note(title, content, location, latitude, longitude);
                        final String imageUri = "Users/" + fUser.getUid() + "/" + noteId + "/Images.jpeg";
                        noteId = document.getId();
                        noteHashMap.put(noteId, note);
                    }
                }
            }
        });

        Toast.makeText(this, "start", Toast.LENGTH_SHORT).show();


        mLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    locBack = locationResult.toString();
                    Log.d(TAG, "onLocationResult: location error");
                    return;
                }

                List<Location> locations = locationResult.getLocations();

                s = new StringBuilder();
                for (String key : noteHashMap.keySet()) {
                    Note value = noteHashMap.get(key);
                    if (value.getLongitude() != null && value.getLongitude() != null) {
                        Double latNote = Double.parseDouble(value.getLatitude());
                        Double longNote = Double.parseDouble(value.getLongitude());

                        Double latCurr = locations.get(0).getLatitude();
                        Double longCurr = locations.get(0).getLongitude();
                        Resources res = getResources();

                        if (distanceBetweenPoints(latNote, latCurr, longNote, longCurr) <= res.getInteger(R.integer.distance)) {
                            helper = new LocationResultHelper(getApplicationContext(), locations);
                            helper.showNotification(key, noteHashMap.get(key), imageUri);
                            helper.saveLocationResults();
                            Toast.makeText(getApplicationContext(), "Complete task!" + locations.size(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        };

        locBack = intent.getStringExtra("location");
        startForeground(1001, getNotification());
        getLocationUpdates();
        return START_STICKY;
    }

    private Notification getNotification() {

        String channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            channel = createChannel();
        else {
            channel = "";
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channel)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("App is using GPS to notify you in case you are near any note" +
                        "-specified location. Don't disable GPS for better results. "))
                .setContentTitle("TAGTODO : Location service")
                ;

        Notification notification = mBuilder
                .setPriority(PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        return notification;

    }


    private void getLocationUpdates() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(2000);

        locationRequest.setFastestInterval(1000);

        locationRequest.setMaxWaitTime(5 * 1000);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }

        mLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        startForeground(1001, getNotification());
//        getLocationUpdates();
        Toast.makeText(this, "Destroyed", Toast.LENGTH_SHORT).show();
//        stopForeground(true);
        stopForeground(false);
//        mLocationClient.removeLocationUpdates(mLocationCallback);
    }



    @NonNull
    @TargetApi(26)
    private synchronized String createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String name = "CUSTOM ";
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            stopSelf();
        }
        return CHANNEL_ID;
    }


    public double distanceBetweenPoints(double lat1, double lat2, double lon1, double lon2)
    {
        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;

        // calculate the result
        return (c * r)*1000 ;
    }
}
