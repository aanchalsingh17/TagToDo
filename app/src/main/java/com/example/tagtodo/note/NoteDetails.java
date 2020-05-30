package com.example.tagtodo.note;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.example.tagtodo.R;
import com.example.tagtodo.map.ViewMap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class NoteDetails extends AppCompatActivity {
    Intent data;
    TextView content,title,location,alarmDetails;
    String imageUri;
    FirebaseStorage mFirebaseStorage;
    StorageReference storagereference;
    ImageView noteImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //getting back button on top left of NoteDetails,implement it in onOptionsItemSelected

        //Retrieving data passed(from Adapter) through the intent
        data = getIntent();

        mFirebaseStorage= FirebaseStorage.getInstance();
//        //use this storage reference to upload the image
        storagereference = mFirebaseStorage.getReference();

        content = findViewById(R.id.noteDetailsContent);
        title   = findViewById(R.id.noteDetailsTitle);
        location= findViewById(R.id.locationDetails_noteDetails);
        noteImage = findViewById(R.id.NoteDetailsImage);
        content.setMovementMethod(new ScrollingMovementMethod());           //  Allow the content to be scrollable if it is large

        //Passing data received from intent to the text fields with the help of keys
        content.setText(data.getStringExtra("content"));
        title.setText(data.getStringExtra("title"));
        imageUri = data.getStringExtra("imageUri");

        alarmDetails    = findViewById(R.id.alarmDetails_editNote);


        StorageReference profileref = storagereference.child(imageUri);


        profileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).networkPolicy(NetworkPolicy.OFFLINE).into(noteImage);
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

        //  To go to edit note activity if floating edit button is pressed
        FloatingActionButton fab = findViewById(R.id.fabNoteDetails);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  We also need the data to the activity about title and content of note we need to edit
                Intent i = new Intent(view.getContext(),EditNote.class);
                i.putExtra("title",data.getStringExtra("title"));
                i.putExtra("content",data.getStringExtra("content"));
                i.putExtra("noteID",data.getStringExtra("noteID"));
                i.putExtra("location",data.getStringExtra("location"));
                i.putExtra("latitude",data.getStringExtra("latitude"));
                i.putExtra("longitude",data.getStringExtra("longitude"));
                i.putExtra("imageUri",imageUri);                //Image uri
                i.putExtra("alarm",data.getStringExtra("alarm"));
                startActivity(i);
            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), ViewMap.class);
                i.putExtra("title",data.getStringExtra("title"));
                i.putExtra("content",data.getStringExtra("content"));
                i.putExtra("noteID",data.getStringExtra("noteID"));
                i.putExtra("location",data.getStringExtra("location"));
                i.putExtra("latitude",data.getStringExtra("latitude"));
                i.putExtra("longitude",data.getStringExtra("longitude"));
                i.putExtra("alarm",data.getStringExtra("alarm"));
                i.putExtra("imageUri",imageUri);                //Image uri
                startActivity(i);
            }
        });

    }


    //  Adding parent activity where we want to go back to using back button

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)   //  to check if back button is clicked(it has default id of home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


}
