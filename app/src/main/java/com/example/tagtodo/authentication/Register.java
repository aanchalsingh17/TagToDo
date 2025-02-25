package com.example.tagtodo.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tagtodo.MainActivity;
import com.example.tagtodo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class Register extends AppCompatActivity {


    EditText name_reg,email_reg, password_reg,phone_reg;
    TextView loginBtn_reg,anon_user_reg;
    Button createBtn_reg;
    ProgressBar progressBar_reg;
    FirebaseAuth fAuth_reg;
    String userID;
    FirebaseFirestore fStore;

    public static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        name_reg        = findViewById(R.id.name_reg);
        email_reg       = findViewById(R.id.email_reg);
        password_reg    = findViewById(R.id.password_reg);
        phone_reg       = findViewById(R.id.mobile_reg);
        createBtn_reg   = findViewById(R.id.register_reg);
        loginBtn_reg    = findViewById(R.id.login_reg);
        progressBar_reg = findViewById(R.id.progressBar_reg);
        anon_user_reg   = findViewById(R.id.anon_auth_reg);

        fAuth_reg       = FirebaseAuth.getInstance();
        fStore          = FirebaseFirestore.getInstance();

            //if user is already logged in and user isn't anonymous
//        if(fAuth_reg.getCurrentUser() != null && (!fAuth_reg.getCurrentUser().isAnonymous())){
//            startActivity(new Intent(getApplicationContext(), MainActivity.class));
//            finish();
//        }


        createBtn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = email_reg.getText().toString().trim();
                final String password = password_reg.getText().toString().trim();
                final String fullName = name_reg.getText().toString();
                final String phone    = phone_reg.getText().toString();

                if(TextUtils.isEmpty(fullName)){
                    name_reg.setError("Username is Required.");
                    return;
                }

                if(TextUtils.isEmpty(email)){
                    email_reg.setError("Email is Required.");
                    return;
                }

                if(TextUtils.isEmpty(phone)){
                    phone_reg.setError("Phone Number is Required.");
                    return;
                }

                if(phone.length() <10){
                    phone_reg.setError("Enter valid Phone Number");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    password_reg.setError("Password is Required.");
                    return;
                }

                if(password.length() < 6){
                    password_reg.setError("Password Must be >= 6 Characters");
                    return;
                }

                progressBar_reg.setVisibility(View.VISIBLE);

                //  If anonymous user tries to create account, we need to merge his data to his new account
                if(fAuth_reg.getCurrentUser()!=null && fAuth_reg.getCurrentUser().isAnonymous()){
                    AuthCredential credential = EmailAuthProvider.getCredential(email,password);    // Creating new credential of user
                    // Link anonymous user to given credentials
                    fAuth_reg.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            //Storing data in firestore (other details of the user which were not present in guest user

                            userID = fAuth_reg.getCurrentUser().getUid();                                                           //user id stored
                            DocumentReference documentReference = fStore.collection("users").document(userID);          // firestore cloud database
                            Map<String, Object> user = new HashMap<>();                                                             //user data stored in HashMap
                            user.put("fName",fullName);
                            user.put("email",email);
                            user.put("phone",phone);

                            documentReference.set(user);
        //  Data can be also stored in profile of a user instead of creating a database

//                            FirebaseUser user = fAuth_reg.getCurrentUser();
//                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
//                                        .setDisplayName(fullName)
//                                        .build();
//                            user.updateProfile(request);


                            Toast.makeText(Register.this, "Notes synced", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Register.this, "Error! "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar_reg.setVisibility(View.GONE);
                        }
                    });
                }else{
                    // register the user in firebase

                    fAuth_reg.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                //send email verification link
                                FirebaseUser fuser = fAuth_reg.getCurrentUser();
                                fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Register.this, "Verification email has been sent.", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG,"onFailure : Email not sent "+ e.getMessage());
                                    }
                                });


                                Toast.makeText(Register.this, "User Created.", Toast.LENGTH_SHORT).show();

                                //Storing data in firestore

                                userID = fAuth_reg.getCurrentUser().getUid();                                                           //user id stored
                                DocumentReference documentReference = fStore.collection("users").document(userID);          // firestore cloud database
                                Map<String, Object> user = new HashMap<>();                                                             //user data stored in HashMap
                                user.put("fName",fullName);
                                user.put("email",email);
                                user.put("phone",phone);

                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: user Profile is created for "+ userID);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: " + e.toString());
                                    }
                                });


//                                fuser = fAuth_reg.getCurrentUser();
//                                UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
//                                        .setDisplayName(fullName)
//                                        .build();
//                                 fuser.updateProfile(request);

                                // after registration redirect to main class
                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);


                            }else if(task.getException()!=null) {
                                Toast.makeText(Register.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                progressBar_reg.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });

            //when login button is pressed

        loginBtn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);

            }
        });

        //  When sign in as guest is pressed, login anonymously and go to main activity or display the error
        anon_user_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar_reg.setVisibility(View.VISIBLE);
                fAuth_reg.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        progressBar_reg.setVisibility(View.GONE);
                        Toast.makeText(Register.this, "Logged in as guest user!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(R.anim.slide_up,R.anim.slide_down);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar_reg.setVisibility(View.GONE);
                        Toast.makeText(Register.this, "Error!"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });


    }
}