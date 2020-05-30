package com.example.tagtodo.note;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

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
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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

import com.example.tagtodo.MainActivity;
import com.example.tagtodo.R;
import com.example.tagtodo.map.SearchMap;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import timber.log.Timber;

public class EditNote extends AppCompatActivity {
    Intent data;
    EditText editNoteTitle, editNoteContent;
    FirebaseFirestore fStore;
    ProgressBar progressBarEditNote;
    FirebaseUser fUser;
    TextView location,alarmDetails;
    String nTitle,nContent,nLoc;         //  For the updated note
    ImageView noteImage;
    String imageUri;
    Uri image;
    StorageReference storagereference;
    FirebaseStorage storage;
    String currentPhotoPath;
    FloatingActionButton imageCapture, imageSelect;
    Calendar now = Calendar.getInstance();



    private static final int GALLERY_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        //Used to set toolbar in a view
        Toolbar toolbar = findViewById(R.id.toolbarEditText);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //getting back button on top left of Edit note,implement it in onOptionsItemSelected


        fUser               = FirebaseAuth.getInstance().getCurrentUser();
        fStore              = FirebaseFirestore.getInstance();
        editNoteTitle       = findViewById(R.id.editNoteTitle);
        editNoteContent     = findViewById(R.id.editNoteContent);
        progressBarEditNote = findViewById(R.id.progressBar_editNote);
        location            = findViewById(R.id.locationDetails_editNote);
        noteImage           = findViewById(R.id.editNoteImage);
        imageCapture = findViewById(R.id.editNoteCapture);
        imageSelect = findViewById(R.id.editNoteImageSelect);
        alarmDetails    = findViewById(R.id.alarmDetails_editNote);


        storage = FirebaseStorage.getInstance();
//        //use this storage reference to upload the image
        storagereference = storage.getReference();



        data = getIntent();         //   Fetching data we got from the Note Details activity or Map activity
        editNoteTitle.setText(data.getStringExtra("title"));
        editNoteContent.setText(data.getStringExtra("content"));
        nTitle = editNoteTitle.getText().toString();
        nContent = editNoteContent.getText().toString();
        imageUri = data.getStringExtra("imageUri");
        image = Uri.parse(imageUri);
        // Add image
        StorageReference profileref = storagereference.child(imageUri);

        profileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(noteImage);
            }
        });

        if(data.getStringExtra("location") != null){
            location.setText(data.getStringExtra("location"));
            location.setVisibility(View.VISIBLE);
        }else{
            location.setVisibility(View.INVISIBLE);
        }
        if(data.getStringExtra("alarm") != null){
            location.setText(data.getStringExtra("alarm"));
            location.setVisibility(View.VISIBLE);
        }else{
            location.setVisibility(View.INVISIBLE);
        }

        //  To change location
        location.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setLocationDialog();
                return true;
            }
        });

        //  To save the edit note if floating save button is pressed
        FloatingActionButton fab = findViewById(R.id.saveEditNote);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  We need to insert data into firestore database after proper checks same as in addnote
                nTitle = editNoteTitle.getText().toString();
                nContent = editNoteContent.getText().toString();
                nLoc    = location.getText().toString();

                //  Check whether title or content is empty;

                if(nTitle.isEmpty()){
                    Toast.makeText(EditNote.this,"Please add title to your note",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(nContent.isEmpty()){
                    Toast.makeText(EditNote.this,"Please add content to your note",Toast.LENGTH_SHORT).show();
                    return;
                }

                //  Proceed if both fields are non empty -> Update Note
                //  Making a Notes Collection to save multiple notes (having it's own fields live title and content) and passing our notes ID
                progressBarEditNote.setVisibility(View.VISIBLE);

                DocumentReference docref = fStore.collection("notes").document(fUser.getUid()).collection("userNotes").document(data.getStringExtra("noteID"));
                Map<String,Object> note = new HashMap<>();
                note.put("title",nTitle);       //  using .put instead of .set as we're updating data
                note.put("content",nContent);
                note.put("location",nLoc);
                note.put("latitude",getIntent().getStringExtra("latitude"));
                note.put("longitude",getIntent().getStringExtra("longitude"));

                docref.update(note).addOnSuccessListener(new OnSuccessListener<Void>() {   // Check whether saving was successful
                    @Override
                    public void onSuccess(Void aVoid) {
                        //alarm
                        GetNotification(now);
                            //  Save Image to firebase storage
                            uploadImageToFirebase(image,docref);

                        progressBarEditNote.setVisibility(View.GONE);
                        Toast.makeText(EditNote.this, "Note updated!", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(getApplicationContext(), MainActivity.class));    //  Send user back to main activity
                    }
                }).addOnFailureListener(new OnFailureListener() {       //  If saving fails
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBarEditNote.setVisibility(View.GONE);
                        Toast.makeText(EditNote.this, "Error, Try again", Toast.LENGTH_SHORT).show();
                    }
                });
                //  Helps in editing in offline mode
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));    //  Send user back to main activity
            }
        });

        FloatingActionButton imageSelect = findViewById(R.id.editNoteImageSelect);
        FloatingActionButton imageCapture = findViewById(R.id.editNoteCapture);


        imageSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open gallery
                Intent opengalleryintent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //picking and extracting the data in the same intent
                startActivityForResult(opengalleryintent, GALLERY_REQUEST_CODE);
            }
        });

        imageCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    //  To show close menu in the top right of add note activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.close_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    //  When the close button in top right is pressed

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()== android.R.id.home){   //  to check if back button is clicked(it has default id of home)
            onBackPressed();
        }
        //  If add alert button pressed
        if(item.getItemId() == R.id.add_alert){
            addAlertDialog();
        }
        return super.onOptionsItemSelected(item);


    }



    //  Alert dialog to set reminder according to time or place
    private void setLocationDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.AlertDialogCustom));
        alertDialog.setIcon(R.drawable.ic_add_alert_black_24dp);
        alertDialog.setTitle("Edit reminder");
        alertDialog.setMessage("Do you really want to edit the location of the note?");
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent i = new Intent(EditNote.this, SearchMap.class);
                i.putExtra("title",editNoteTitle.getText().toString());
                i.putExtra("content",editNoteContent.getText().toString());
                i.putExtra("noteID",data.getStringExtra("noteID"));
                i.putExtra("source","EditNote");
                i.putExtra("imageUri",imageUri);                //Image uri
                startActivity(i);
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
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


    //  Alert dialog to set reminder according to time or place
    private void addAlertDialog() {AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.AlertDialogCustom));
        alertDialog.setIcon(R.drawable.ic_add_alert_black_24dp);
        alertDialog.setTitle("Add reminder");
        String[] items = {"Time","Place"};
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
                if(selectedAlert[0] == 0){
                    setTimeNotification();
                }
                if(selectedAlert[0] == 1){
                    Intent i = new Intent(EditNote.this, SearchMap.class);
                    i.putExtra("title",editNoteTitle.getText().toString());
                    i.putExtra("content",editNoteContent.getText().toString());
                    i.putExtra("source","EditNote");
                    i.putExtra("noteID",data.getStringExtra("noteID"));
                    startActivity(i);
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(EditNote.this, "Cancel clicked", Toast.LENGTH_SHORT).show();
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
                image = data.getData();
                //setting the image view to the user selected image using its URI
                noteImage.setImageURI(image);
                //uplaod iamge to firebase by calling the below method and passing the image uri as parameter
//                uploadImageToFirebase(imageuri);
            }

        }
        //ignore the below commented code its for the camera result after opening camera permission is granted
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentPhotoPath);
                image = Uri.fromFile(f);
                noteImage.setImageURI(image);

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
                image = FileProvider.getUriForFile(this, "com.example.tagtodo.fileprovider", PhotoFile);
                TakePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, image);
                startActivityForResult(TakePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }


    //uploading image to firebase again take care of the naming and it maynot be same
    private void uploadImageToFirebase(Uri imageuri, DocumentReference docref) {

        String loc = docref.getId();
        StorageReference fileref ;

        if(imageuri!=null){
            fileref = storagereference.child("Users/" + fUser.getUid() + "/" + loc + "/Images.jpeg");
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
                    Toast.makeText(EditNote.this, "voila", Toast.LENGTH_SHORT).show();
                    fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).networkPolicy(NetworkPolicy.OFFLINE).into(noteImage);
                        }
                    });
                }
            });

        } else{
            fileref = storagereference.child("Users/" + fUser.getUid() + "/" + loc+ "/Images.jpeg");
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
        }
        startActivity(new Intent(getApplicationContext(), MainActivity.class));    //  Send user back to main activity

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
                        image = null;
                    }

                    //  Negative button will delete all the guest user data, and then also delete the user
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EditNote.this, "Image not removed.", Toast.LENGTH_SHORT).show();
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
                new TimePickerDialog(EditNote.this,timeSetListener,now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE),false).show();
            }
        };
        //Getting date info from the user and using it in datesetlistener
        new DatePickerDialog(EditNote.this,dateSetListener,now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH)).show();

    }


    public void GetNotification(Calendar now) {
        //below title variable is the title of the note
        //Alarm Reciever class is the new java class for building the notification
        Intent intent = new Intent(this,AlarmReceiver.class);
        intent.putExtra("Title",nTitle);
        intent.putExtra("Content",nContent);


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
