package com.example.tagtodo.note;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.tagtodo.MainActivity;
import com.example.tagtodo.R;
import com.example.tagtodo.SplashScreen;
import com.example.tagtodo.authentication.Register;
import com.example.tagtodo.map.SearchMap;
import com.example.tagtodo.model.Note;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class AddNote extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 102;


    FirebaseFirestore fStore;
    EditText noteTitle, noteContent;
    ProgressBar progressBar_add_note;
    FirebaseUser fUser;
    TextView locationDetails ,alarmDetails;
    String latitude, longitude, location;
    String nTitle, nContent;
    FloatingActionButton imageCapture, imageSelect;
    ImageView noteImage;
    StorageReference storagereference;
    FirebaseStorage storage;
    String currentPhotoPath;
    Uri imageuri;
    FirestoreRecyclerAdapter<Note, MainActivity.NoteViewHolder> noteAdapter;      //  Takes model class and viewholder
    Button button;
    Calendar now = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //getting back button on top left of NoteDetails,implement it in onOptionsItemSelected


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1114);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant that should be quite unique
            }
        }


        fUser = FirebaseAuth.getInstance().getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        noteTitle = findViewById(R.id.addNoteTitle);
        noteContent = findViewById(R.id.addNoteContent);
        progressBar_add_note = findViewById(R.id.progressBar_addNote);
        locationDetails = findViewById(R.id.locationDetails_addNote);
        alarmDetails    = findViewById(R.id.alarmDetails_addNote);
        imageCapture = findViewById(R.id.addNoteCapture);
        imageSelect = findViewById(R.id.addNoteImageSelect);
        noteImage = findViewById(R.id.addNoteImage);

        storage = FirebaseStorage.getInstance();
//        //use this storage reference to upload the image
        storagereference = storage.getReference();

        //      Add location data if it exists
        if (getIntent().getExtras() != null) {
            latitude = getIntent().getStringExtra("latitude");
            longitude = getIntent().getStringExtra("longitude");
            location = getIntent().getStringExtra("location");
            locationDetails.setText(location);
            locationDetails.setVisibility(View.VISIBLE);
            //  If the user added any title or content before adding map location
            noteTitle.setText(getIntent().getStringExtra("title"));
            noteContent.setText(getIntent().getStringExtra("content"));
        }

        FloatingActionButton fab = findViewById(R.id.fab);          //  Floating save button
        fab.setOnClickListener(new View.OnClickListener() {         //  When save button is pressed
            @Override
            public void onClick(View view) {

                nTitle = noteTitle.getText().toString();
                nContent = noteContent.getText().toString();

                //  Check whether title or content is empty;

                if (nTitle.isEmpty()) {
                    Toast.makeText(AddNote.this, "Please add title to your note", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (nContent.isEmpty()) {
                    Toast.makeText(AddNote.this, "Please add content to your note", Toast.LENGTH_SHORT).show();
                    return;
                }

                //  Proceed if both fields are non empty -> Save Note
                //  Making a Notes Collection to save multiple notes (having it's own fields live title and content)
                progressBar_add_note.setVisibility(View.VISIBLE);

                DocumentReference docref = fStore.collection("notes").document(fUser.getUid()).collection("userNotes").document();

                Map<String, Object> note = new HashMap<>();
                note.put("title", nTitle);
                note.put("content", nContent);
                //  If location exists, put it as well
                if (getIntent().getExtras() != null) {
                    note.put("location", location);
                    note.put("latitude", latitude);
                    note.put("longitude", longitude);
                } else {
                    note.put("location", null);
                    note.put("latitude", null);
                    note.put("longitude", null);
                }

                docref.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {   // Check whether saving was successful
                    @Override
                    public void onSuccess(Void aVoid) {
                        //alarm
                        GetNotification(now);
                            //  Save Image to firebase storage
                            uploadImageToFirebase(imageuri,docref);

                        progressBar_add_note.setVisibility(View.GONE);
                        Toast.makeText(AddNote.this, "Note added!", Toast.LENGTH_SHORT).show();
//                        onBackPressed();                    //  Send user back to main activity(parent activity which is defined in Manifest file)
                    }
                }).addOnFailureListener(new OnFailureListener() {       //  If saving fails
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar_add_note.setVisibility(View.GONE);
                        Toast.makeText(AddNote.this, "Error, Try again", Toast.LENGTH_SHORT).show();
                    }
                });


                //  send user back to main  activity (this helps when user is offline)
//                onBackPressed();
            }
        });
        //  Fab for select image from gallery
        imageSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open gallery
                Intent opengalleryintent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //picking and extracting the data in the same intent
                startActivityForResult(opengalleryintent, GALLERY_REQUEST_CODE);
            }
        });

        //Fab for select image from camera
        imageCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //permission for opening the camera
                askCameraPermission();
            }
        });

        //  Delete image if it is long pressed

        noteImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                displayAlert();
                return true;
            }
        });

    }


    //  To show reminder menu in the top right of add note activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.close_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //  When the close button in top right is pressed

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {   //  to check if back button is clicked(it has default id of home)
            onBackPressed();
            Toast.makeText(this, "Note not saved :(", Toast.LENGTH_SHORT).show();
        }
        //  If add alert button pressed
        if (item.getItemId() == R.id.add_alert) {
            addAlertDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    //  Alert dialog to set reminder according to time or place
    private void addAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        alertDialog.setIcon(R.drawable.ic_add_alert_black_24dp);
        alertDialog.setTitle("Add reminder");
        String[] items = {"Time", "Place"};
        int checkedItem = 0;
        final int[] selectedAlert = new int[1];
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        selectedAlert[0] = 0;
                        break;
                    case 1:
                        selectedAlert[0] = 1;
                        break;
                }
            }
        });
        alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectedAlert[0] == 0) {
                    setTimeNotification();
                }
                if (selectedAlert[0] == 1) {
                    Intent i = new Intent(AddNote.this, SearchMap.class);
                    i.putExtra("title", noteTitle.getText().toString());
                    i.putExtra("content", noteContent.getText().toString());
                    i.putExtra("source", "AddNote");
                    startActivity(i);
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();

        //    Customising buttons for dialog
        Button p = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        p.setBackgroundColor(Color.parseColor("#222831"));
        p.setTextColor(Color.parseColor("#D90091EA"));
        Button n = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        n.setBackgroundColor(Color.parseColor("#222831"));
        n.setTextColor(Color.parseColor("#DEFFFFFF"));
    }


    //  Functions for image

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                assert data != null;
                imageuri = data.getData();
                //setting the image view to the user selected image using its URI
                noteImage.setImageURI(imageuri);
                //uplaod iamge to firebase by calling the below method and passing the image uri as parameter
//                uploadImageToFirebase(imageuri);
            }

        }
        //ignore the below commented code its for the camera result after opening camera permission is granted
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentPhotoPath);
                imageuri = Uri.fromFile(f);
                noteImage.setImageURI(imageuri);

//                uploadImageToFirebase(Uri.fromFile(f));
//                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                Uri contenturi = Uri.fromFile(f);
//                mediaScanIntent.setData(contenturi);
//                this.sendBroadcast(mediaScanIntent);
            }
        }
    }

    //camera permission
    private void askCameraPermission() {
        //check if permission is granted or not
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            dispatchTakePictureIntent();
        }
    }

    //check if camera permission is granted or not then accordingly perform the action
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            }
        } else {
            Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        //create n image file name
        String TimeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        String ImageFileName = "JPEG_" + TimeStamp + "_";
        File storagedir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // File storagedir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File profileimage = File.createTempFile(
                ImageFileName, ".jpg", storagedir
        );
        currentPhotoPath = profileimage.getAbsolutePath();
        return profileimage;
    }


    private void dispatchTakePictureIntent() {
        Intent TakePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (TakePictureIntent.resolveActivity(getPackageManager()) != null) {
            File PhotoFile = null;
            try {
                PhotoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (PhotoFile != null) {
                imageuri = FileProvider.getUriForFile(this, "com.example.tagtodo.fileprovider", PhotoFile);
                TakePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
                startActivityForResult(TakePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }


    //uploading image to firebase again take care of the naming and it maynot be same
    private void uploadImageToFirebase(Uri imageuri, DocumentReference docref) {
        if(imageuri != null){
            String loc = docref.getId();
            final StorageReference fileref = storagereference.child("Users/" + fUser.getUid() + "/" + loc + "/Images.jpeg");

            Bitmap bmp = null;
            try{
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageuri);
            }catch (IOException e){
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //Choose image quality factor
            bmp.compress(Bitmap.CompressFormat.JPEG,25,baos);
            byte[] fileInBytes = baos.toByteArray();

            fileref.putBytes(fileInBytes).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(AddNote.this, "voila", Toast.LENGTH_SHORT).show();
                    fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).networkPolicy(NetworkPolicy.OFFLINE).into(noteImage);
                        }
                    });
                }
            });
        }
        onBackPressed();
    }


    //  Function to display alert when image is long pressed  ( create a alert dialog )
    private void displayAlert(){
        AlertDialog.Builder warning = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.AlertDialogCustom))
                .setTitle("Are you sure ??")                                                        //  Title of the alert dialog
                .setMessage("The image will be removed from the note.")                             //  Message in alert dialog
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {        //  Positive button will directing to register screen
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        noteImage.setImageDrawable(null);
                        imageuri = null;
                    }

                    //  Negative button will delete all the guest user data, and then also delete the user
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(AddNote.this, "Image not removed.", Toast.LENGTH_SHORT).show();
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



    // Function for time based notification

    private void setTimeNotification(){

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                //setting the date variable of calendar picked by date picker dialog
                now.set(Calendar.YEAR , year);
                now.set(Calendar.MONTH,month);
                now.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                //same as datepicker dialog
                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        now.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        now.set(Calendar.MINUTE,minute);
                        now.set(Calendar.SECOND,0);
                        //date and time format
                        new SimpleDateFormat("yy-MM-dd HH:mm");
                        //below line displays the date and time picked by user using the edit text
                        //date_time_in.setText(simpleDateFormat.format(calendar.getTime()));
                         UpdateTimeText(now);
                    }
                };
                new TimePickerDialog(AddNote.this,timeSetListener,now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE),false).show();
            }
        };
        //Getting date info from the user and using it in datesetlistener
        new DatePickerDialog(AddNote.this,dateSetListener,now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH)).show();

    }


    public void GetNotification(Calendar now) {
        //below title variable is the title of the note
        String Title = noteTitle.getText().toString();
        String content = noteContent.getText().toString();
        //Alarm Reciever class is the new java class for building the notification
        Intent intent = new Intent(this,AlarmReceiver.class);
        intent.putExtra("Title",Title);
        intent.putExtra("Content",content);


        PendingIntent Pendingintent = PendingIntent.getBroadcast(this,1,intent,0);;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, 4000,Pendingintent);
//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,now.getTimeInMillis(),10*60*1000,Pendingintent);
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,now.getTimeInMillis(),10*60*1000,Pendingintent);
    }


    private void UpdateTimeText(Calendar calendar) {
        if(calendar != null) {
            String TimeText = DateFormat.getDateTimeInstance().format(calendar.getTime());
            alarmDetails.setText(TimeText);
            alarmDetails.setVisibility(View.VISIBLE);
        }else{
            alarmDetails.setText("No alarm");
            alarmDetails.setVisibility(View.INVISIBLE);
        }
    }



}
