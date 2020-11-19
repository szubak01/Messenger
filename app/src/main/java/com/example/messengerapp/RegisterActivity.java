package com.example.messengerapp;

import android.app.ProgressDialog;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etUsernameRegister, etEmailRegister, etPasswordRegister;
    private Button buttonRegister;
    private TextView tvAlreadyHaveAnAccountRegister;
    private String currentUserID;
    private ProgressDialog loadingBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseFirestore fireStore = FirebaseFirestore.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        InitializeFields();


        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });

        tvAlreadyHaveAnAccountRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

    }

    public void onStart(){
        super.onStart();

        //checking if the user is logged in or not
        if(firebaseAuth.getCurrentUser() != null) {
            SendUserToMainActivity();
        }
    }


    private void InitializeFields(){

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();


        etUsernameRegister = findViewById(R.id.etUsernameRegister);
        etEmailRegister = findViewById(R.id.etEmailLogin);
        etPasswordRegister = findViewById(R.id.etPasswordLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        tvAlreadyHaveAnAccountRegister = findViewById(R.id.tvAlreadyHaveAnAccountRegister);

        loadingBar = new ProgressDialog(this);

    }


    private void createNewAccount(){

            final String username = etUsernameRegister.getText().toString();
            final String email = etEmailRegister.getText().toString();
            final String password = etPasswordRegister.getText().toString();


            Log.d("RegisterActivity", "Username is: " + username);
            Log.d("RegisterActivity", "E-mail is: " + email);
            Log.d("RegisterActivity", "Password is: " + password);

            if(TextUtils.isEmpty(username)){
                etUsernameRegister.setError("Username is requried.");
                return;
            }
            if(TextUtils.isEmpty(email)){
                etEmailRegister.setError("E-mail is requried.");
                return;
            }
            if(TextUtils.isEmpty(password)){
                etPasswordRegister.setError("Password is requried.");
                return;
            }


            loadingBar.setTitle("Creating new account.");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            // register the user in firebase
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        loadingBar.dismiss();
                        SendUserToMainActivity();
                        Toast.makeText(RegisterActivity.this, "User created successfuly!", Toast.LENGTH_SHORT).show();

                        currentUserID = firebaseAuth.getCurrentUser().getUid();
                        // save user data to database
                        HashMap<String, String> user = new HashMap<>();
                        user.put("username", username);
                        user.put("email", email);
                        user.put("password", password);
                        user.put("uid", currentUserID);
                        myRef.child("Users").child(currentUserID).setValue(user);

                    }
                    else
                    {
                        loadingBar.dismiss();
                        String message = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, "Error!" + message, Toast.LENGTH_LONG).show();
                    }
                }
            });

        }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onClick(View v) {
    }
}


