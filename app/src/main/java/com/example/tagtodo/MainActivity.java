package com.example.tagtodo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tagtodo.authentication.Login;
import com.example.tagtodo.authentication.Register;
import com.example.tagtodo.locationService.LocationResultHelper;
import com.example.tagtodo.locationService.MyBackgroundLocationService;
import com.example.tagtodo.model.Note;
import com.example.tagtodo.note.AddNote;
import com.example.tagtodo.note.EditNote;
import com.example.tagtodo.note.NoteDetails;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {                                                        /* this implement
                                                                                                                    is done to handle click
                                                                                                                    on nav drawer
                                                                                                                    And the second one for
                                                                                                                    background location*/

    //  declaring variables
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    Toolbar toolbar;
    RecyclerView noteList;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter<Note,NoteViewHolder> noteAdapter;      //  Takes model class and viewholder
    FirebaseUser fuser;
    FirebaseAuth fAuth;
    FirebaseStorage mFirebaseStorage;
    StorageReference storagereference;
    private static final String CHANNEL_ID = "Channel";


    public static final int PERMISSION_REQUEST_CODE = 9001;
    private static final int PLAY_SERVICES_ERROR_CODE = 9002;
    public static final int GPS_REQUEST_CODE = 9003;
    private boolean mLocationPermissionGranted;



    public static final String TAG = "MyTag";
    private FusedLocationProviderClient mLocationClient;
    private LocationCallback mLocationCallback;
    private String backLoc;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  assigning resources to variables
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fAuth = FirebaseAuth.getInstance();
        fuser = fAuth.getCurrentUser();

        fStore   = FirebaseFirestore.getInstance();

        mFirebaseStorage= FirebaseStorage.getInstance();
//        //use this storage reference to upload the image
        storagereference = mFirebaseStorage.getReference();


        createNotificationChannel();


        //  Background location service

        initPermission();

//
//        mLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//
//                if (locationResult == null) {
//                    Log.d(TAG, "onLocationResult: location error");
//                    return;
//                }
//
//                List<Location> locations = locationResult.getLocations();
//
//                LocationResultHelper helper = new LocationResultHelper(MainActivity.this, locations);
//
//                helper.showNotification();
//
//                helper.saveLocationResults();
//
//                Toast.makeText(MainActivity.this, "Location received: " + locations.size(), Toast.LENGTH_SHORT).show();
//
//                backLoc = helper.getLocationResultText();
//                Toast.makeText(MainActivity.this, backLoc, Toast.LENGTH_SHORT).show();
//
//            }
//        };




        //  Query data from firebase in the order of title in descending direction (only the notes are queried which are created by user)
        // Query notes -> userID -> userNotes (so that one user can access only his notes)
        Query query = fStore.collection("notes").document(fuser.getUid()).collection("userNotes").orderBy("title", Query.Direction.DESCENDING);


        //  Executing the query using FirestoreRecyclerOptions (they take a model of class(model->Note.class))
        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query,Note.class)         //   Setting query and giving model class
                .build();                           //     calling build method queries data from Firestore and stores in allNotes


        //  adapter taking allNotes
        noteAdapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {

            //  Bind data to the view created by onCreateViewHolder
            @Override
            protected void onBindViewHolder(@NonNull final NoteViewHolder noteViewHolder, final int i, @NonNull final Note note) {
                noteViewHolder.noteTitle.setText(note.getTitle());
                noteViewHolder.noteContent.setText(note.getContent());

                final String docID = noteAdapter.getSnapshots().getSnapshot(i).getId(); //  Getting id of note
                final String imageUri = "Users/" + fuser.getUid() + "/" + docID + "/Images.jpeg";



                noteViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //when an item in holder is clicked, open the note (NoteDetails activity)
                        Intent i = new Intent(v.getContext(), NoteDetails.class);
                        i.putExtra("title", note.getTitle());          //Send title data(with key title) from adapter to NoteDetails when note number-> position is clicked
                        i.putExtra("content", note.getContent());      //Send content data(with key content) from adapter to NoteDetails when note number-> position is clicked
                        i.putExtra("noteID",docID);                    //Send note ID which can be useful for updation
                        i.putExtra("location",note.getLocation());     //Send location data
                        i.putExtra("latitude",note.getLatitude());     //Send latitude
                        i.putExtra("longitude",note.getLongitude());   //Send longitude
                        i.putExtra("imageUri",imageUri);                //Image uri
                        i.putExtra("alarm",note.getAlarm());
                        v.getContext().startActivity(i);
                        overridePendingTransition(R.anim.slide_up,R.anim.slide_down);

                    }
                });

                //  Image view click listener for the three dots options menu in each card to get a popup menu

                ImageView noteThreeDot = noteViewHolder.view.findViewById(R.id.noteThreeDot);
                noteThreeDot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        PopupMenu popupMenu = new PopupMenu(v.getContext(),v);  //  Creating a popup menu
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            popupMenu.setGravity(Gravity.END);      //Setting that popupmenu comes directly below the note
                        }
                        //  Add edit button
                        popupMenu.getMenu().add("Edit").setIcon(R.drawable.ic_edit_white_24dp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                //  Same code as edit floating button on NoteDetails
                                //  We also need the data to the activity about title and content of note we need to edit
                                Intent i = new Intent(v.getContext(), EditNote.class);
                                i.putExtra("title",note.getTitle());
                                i.putExtra("content",note.getContent());
                                i.putExtra("noteID",docID);
                                i.putExtra("location",note.getLocation());
                                i.putExtra("latitude",note.getLatitude());
                                i.putExtra("longitude",note.getLongitude());
                                i.putExtra("imageUri",imageUri);
                                i.putExtra("alarm",note.getAlarm());

                                startActivity(i);
                                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);

                                return false;
                            }
                        });

                        //  Add delete button
                        popupMenu.getMenu().add("Delete").setIcon(R.drawable.ic_delete_sweep_white_24dp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                //  We need a doc reference with docID to access the required note
                                DocumentReference docRef = fStore.collection("notes").document(fuser.getUid()).collection("userNotes").document(docID);
                                //Delete note image
                                String loc = docRef.getId();
                                StorageReference fileref = storagereference.child("Users/" + fuser.getUid() + "/" + loc + "/Images.jpeg");
                                fileref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Timber.d("Deleted note image from firebase");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Timber.d("Failed to delete note image");
                                    }
                                });
                                //  Now delete note
                                //  Check  if deletion was successful
                                docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Error deleting note", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });

                //  Same code when note is long pressed
                noteViewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        PopupMenu popupMenu = new PopupMenu(v.getContext(),v);  //  Creating a popup menu
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            popupMenu.setGravity(Gravity.END);      //Setting that popupmenu comes directly below the note
                        }
                        //  Add edit button
                        popupMenu.getMenu().add("Edit").setIcon(R.drawable.ic_edit_white_24dp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                //  Same code as edit floating button on NoteDetails
                                //  We also need the data to the activity about title and content of note we need to edit
                                Intent i = new Intent(v.getContext(), EditNote.class);
                                i.putExtra("title",note.getTitle());
                                i.putExtra("content",note.getContent());
                                i.putExtra("noteID",docID);
                                i.putExtra("location",note.getLocation());
                                i.putExtra("latitude",note.getLatitude());
                                i.putExtra("longitude",note.getLongitude());
                                i.putExtra("imageUri",imageUri);
                                i.putExtra("alarm",note.getAlarm());

                                startActivity(i);
                                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);

                                return false;
                            }
                        });

                        //  Add delete button
                        popupMenu.getMenu().add("Delete").setIcon(R.drawable.ic_delete_sweep_white_24dp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                //  We need a doc reference with docID to access the required note
                                DocumentReference docRef = fStore.collection("notes").document(fuser.getUid()).collection("userNotes").document(docID);
                                //Delete note image
                                String loc = docRef.getId();
                                StorageReference fileref = storagereference.child("Users/" + fuser.getUid() + "/" + loc + "/Images.jpeg");
                                fileref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Timber.d("Deleted note image from firebase");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Timber.d("Failed to delete note image");
                                    }
                                });
                                //  Delete note now
                                //  Check  if deletion was successful
                                docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Error deleting note", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });
                        popupMenu.show();
                        return true;
                    }
                });


            }

            //  Used to create view for recycler view
            //  It will inflate note_view_layout.xml into the view for the Recycler from the parent(giving context) with parameters->
            //      1.xml file for the layout
            //      2.parent(where we want to display this)
            //      3.false(as we do not want to attach it to root)
            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout,parent,false);
                return new NoteViewHolder(view);
            }
        };



        noteList = findViewById(R.id.noteList);

        drawerLayout = findViewById(R.id.drawer);
        nav_view     = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);   //set onNavigationItemSelectedListener to nav_view to listen to this activity



        //  Creating object for action bar drawer toggle with parameters ->
        //      1.current context
        //      2.drawer layout which we want to toggle when toggle button is clicked
        //      3.location where we need to show the toggle sign ( in toolbar)
        //      4.Toggle open and close string resource (it specifies whether navigation bar is open or close.
        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);

        //  Setting drawer listener for drawer layout to toggle
        drawerLayout.addDrawerListener(toggle);

        //  Enable the hamburger sign in toolbar
        toggle.setDrawerIndicatorEnabled(true);

        //  Sync state to action bar toggle(inform action bar toggle whether navigation bar is open or closed)
        toggle.syncState();


        //  set layout of note list and passing the note adapter into it
        noteList.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteList.setAdapter(noteAdapter);



        // Setting username and email in Navigation drawer
            //  index 0 as we have 1 header only
        View headerView = nav_view.getHeaderView(0);
        final TextView username_nav = headerView.findViewById(R.id.usernameNav);
        final TextView userEmail_nav = headerView.findViewById(R.id.emailNav);
;
        if(!fuser.isAnonymous()){
            DocumentReference documentReference = fStore.collection("users").document(fuser.getUid());
            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    username_nav.setText(documentSnapshot.getString("fName"));
                    userEmail_nav.setText(documentSnapshot.getString("email"));
                }
            });
        }



        // Handling click on floating add note button in main activity
        FloatingActionButton fab = findViewById(R.id.addNoteFloat);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), AddNote.class));
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);

            }
        });

    }
//  Method implemented from implements NavigationView.OnNavigationItemSelectedListener for handling clicks in nav drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        //  Close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        //   check which icon clicked in nav drawer
        switch (menuItem.getItemId()){

            case R.id.addNote:      //  If user clicks on add note, redirect to AddNote activity
                startActivity(new Intent(this,AddNote.class));
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);

                break;

            case R.id.logout_nav:      //  If user clicks logout
                checkuser();
                break;

            case R.id.syncNote:     //  Sync only if user is anonymous ->take him to register activity
                if(fuser.isAnonymous()){
                    startActivity(new Intent(this, Login.class));
                    overridePendingTransition(R.anim.slide_up,R.anim.slide_down);

                }else{
                    Toast.makeText(this, "You are already logged in!", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.userprofile:
                startActivity(new Intent(this,UserProfile.class));
                break;

            default:
                Toast.makeText(this,"Coming soon !",Toast.LENGTH_SHORT).show();         //default case if nowhere to go yet
        }
        return false;
    }

    //  checkuser function to check if user is real or anonymous
    //  If anonymous, warn him that all data will be lost
    private  void checkuser(){
        if(fuser.isAnonymous()){
            displayAlert();
        }else{              //  If user is real, signout and go to register screen
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), SplashScreen.class));
            overridePendingTransition(R.anim.slide_up,R.anim.slide_down);

        }
    }

    //  Function to display alert when signout is pressed for anonymous user ( create a alert dialog )
    private void displayAlert(){
        AlertDialog.Builder warning = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.AlertDialogCustom))
                .setTitle("Are you sure ??")                                                        //  Title of the alert dialog
                .setMessage("All the notes will be permanently lost if you logout")                 //  Message in alert dialog
                .setPositiveButton("Create ID", new DialogInterface.OnClickListener() {        //  Positive button will directing to register screen
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(),Register.class));
                        overridePendingTransition(R.anim.slide_up,R.anim.slide_down);

                        finish();
                    }

                    //  Negative button will delete all the guest user data, and then also delete the user
                }).setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //  We need a doc reference with docID to access the required note
                        DocumentReference docRef = fStore.collection("notes").document(fuser.getUid());
                        //  Delete database of the user
                        docRef.delete();
                        fuser.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startActivity(new Intent(getApplicationContext(),SplashScreen.class));
                                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                                finish();
                            }
                        });
                    }
                });
//        warning.show();
          AlertDialog alert = warning.create();
          alert.show();
          //    Customising buttons for dialog
        Button p = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        p.setBackgroundColor(Color.parseColor("#222831"));
        p.setTextColor(Color.parseColor("#D90091EA"));
        Button n = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        n.setBackgroundColor(Color.parseColor("#222831"));
        n.setTextColor(Color.parseColor("#DEFFFFFF"));

    }



    //  To implement options menu with settings in right corner (3 dot menu)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //  inflating the option_menu.xml file from menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //  Handling click in the options menu with settings and location service in right corner(3 dot menu)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.settings){
            Toast.makeText(this,"Settings Menu is clicked.",Toast.LENGTH_SHORT).show();
        }

        if(item.getItemId() == R.id.locationServiceStart){
            Toast.makeText(this, "Location Service enabled", Toast.LENGTH_SHORT).show();
            startLocationService();
        }
        if(item.getItemId() == R.id.locationServiceStop){
            Toast.makeText(this, "Location Service disabled", Toast.LENGTH_SHORT).show();
            stopLocationService();
        }

        return super.onOptionsItemSelected(item);
    }

    private void startLocationService() {
        //start background location service
        Intent intent = new Intent(this, MyBackgroundLocationService.class);
        intent.putExtra("location",backLoc);
        ContextCompat.startForegroundService(this, intent);
        Toast.makeText(this, "Location Service Started", Toast.LENGTH_SHORT).show();
    }

    private void stopLocationService() {
        //stop background location service
        Intent intent = new Intent(this, MyBackgroundLocationService.class);
        stopService(intent);
        Toast.makeText(this, "Location Service Stopped", Toast.LENGTH_SHORT).show();

    }


    //  NoteViewHolder class

    public static class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView noteTitle, noteContent;
        ImageView noteImage;
        View view;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle  = itemView.findViewById(R.id.titles);
            noteContent= itemView.findViewById(R.id.content);
            noteImage = itemView.findViewById(R.id.noteViewImage);
            view = itemView;
        }
    }

    //  For listening the data change from cloud if we open app or come back from another app
    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();

    //  For shared preference interface
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

    }
    //  Stop listening when app is closed
    @Override
    protected void onStop() {
        super.onStop();
        noteAdapter.stopListening();

        // For shared preference interface
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);


    }



    //notificaton channel for android oreo+
    private void createNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel_name";
            String description = "channel_description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name,
                    importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviours after this
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // To implement the interface shared preferences


    @Override
    protected void onPause() {
        super.onPause();
//        if (mLocationCallback != null) {
//            mLocationClient.removeLocationUpdates(mLocationCallback);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        backLoc = (LocationResultHelper.getSavedLocationResults(this));
        Toast.makeText(this, "resumed: location is: " + backLoc, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(LocationResultHelper.KEY_LOCATION_RESULTS)) {
            backLoc = LocationResultHelper.getSavedLocationResults(this);
            Toast.makeText(this, backLoc, Toast.LENGTH_SHORT).show();
        }

    }



    // Functions to fetch user permission for location

    private boolean initPermission() {

        if (isServicesOk()) {
            if (isGPSEnabled()) {
                if (checkLocationPermission()) {
                    Toast.makeText(this, "Ready to Roll!!!", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    requestLocationPermission();
                }
            }
        }
        return false;
    }


    private boolean isGPSEnabled() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (providerEnabled) {
            return true;
        } else {

            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("GPS Permissions")
                    .setMessage("GPS is required for this app to work. Please enable GPS.")
                    .setPositiveButton("Enable GPS", ((dialogInterface, i) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, GPS_REQUEST_CODE);
                    }))
                    .setCancelable(false)
                    .show();

        }

        return false;
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isServicesOk() {

        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();

        int result = googleApi.isGooglePlayServicesAvailable(this);

        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApi.isUserResolvableError(result)) {
            Dialog dialog = googleApi.getErrorDialog(this, result, PLAY_SERVICES_ERROR_CODE, task ->
                    Toast.makeText(this, "Dialog is cancelled by User", Toast.LENGTH_SHORT).show());
            dialog.show();
        } else {
            Toast.makeText(this, "Play services are required by this application", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void requestLocationPermission() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                Toast.makeText(this, "Please enable location permission to be used all the time", Toast.LENGTH_LONG).show();
                openPermissionSettings(this);
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }
    }


    public static void openPermissionSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + activity.getPackageName()));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GPS_REQUEST_CODE) {

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (providerEnabled) {
                Toast.makeText(this, "GPS is enabled", Toast.LENGTH_SHORT).show();
            } else {
                // alert again if gps not opened
                Toast.makeText(this, "You must enable GPS in order to use this app.", Toast.LENGTH_SHORT).show();
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("GPS Permissions")
                        .setMessage("GPS is required for this app to work. Please enable GPS.")
                        .setPositiveButton("Enable GPS", ((dialogInterface, i) -> {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, GPS_REQUEST_CODE);
                        }))
                        .setCancelable(false)
                        .show();            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this, "Connected to Location Services", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    protected void onDestroy() {
        startLocationService();
        super.onDestroy();
    }
}
