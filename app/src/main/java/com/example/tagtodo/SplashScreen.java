package com.example.tagtodo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.tagtodo.authentication.Register;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    //  To check whether user is logged in or anonymously
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        fAuth =FirebaseAuth.getInstance();

        //  Handler sends to main activity after two seconds
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {    //  Delays operation for 2000ms
            @Override
            public void run() {

                //  Check if user is logged in
                if (fAuth.getCurrentUser()!=null){
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }
                //  If not logged in go to register activity
                else{
                    Toast.makeText(SplashScreen.this, "Welcome!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), Register.class));
                    finish();
                }
            }
        },2000);
    }
}
