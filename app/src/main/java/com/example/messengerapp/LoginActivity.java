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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button buttonLogin;
    EditText etEmailLogin, etPasswordLogin;
    TextView tvDontHaveAnAccountLogin;
    FirebaseAuth firebaseAuth;
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
              super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        InitializeFields();


        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        tvDontHaveAnAccountLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
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

        etEmailLogin = findViewById(R.id.etEmailLogin);
        etPasswordLogin = findViewById(R.id.etPasswordLogin);
        buttonLogin = findViewById(R.id.buttonLogin);
        tvDontHaveAnAccountLogin = findViewById(R.id.tvDontHaveAnAccountLogin);

        loadingBar = new ProgressDialog(this);
    }


    void performLogin(){
        final String email = etEmailLogin.getText().toString();
        final String password = etPasswordLogin.getText().toString();

        Log.d("LoginActivity", "E-mail is: " + email);
        Log.d("LoginActivity", "Password is: " + password);


        if(TextUtils.isEmpty(email)){
            etEmailLogin.setError("E-mail is requried.");
            return;
        }
        if(TextUtils.isEmpty(password)){
            etPasswordLogin.setError("Password is requried.");
            return;
        }

        loadingBar.setTitle("Logging in.");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            loadingBar.dismiss();
                            SendUserToMainActivity();
                            Toast.makeText(LoginActivity.this, "Logged in successfully.", Toast.LENGTH_LONG).show();


                        }else{
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onClick(View v) {
    }
}


