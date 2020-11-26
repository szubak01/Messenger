package com.example.messengerapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Instrumentation;
import android.app.Person;
import android.content.Intent;
import android.database.DatabaseErrorHandler;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView username;
    private TextView email;
    private CircleImageView userProfileImage;
    private String currentUserID;
    FirebaseAuth firebaseAuth;
    DatabaseReference myRef;
    StorageReference UserProfileImagesRef;

    private final static int GalleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        myRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        InitializeFields();
        RetrieveUserInfo();
        BackArrowToMainActivity();


        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }

    private void BackArrowToMainActivity()
    {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        // back arrow to MainActivity  (manifest MainActivity set as a parent)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void InitializeFields()
    {
         username = findViewById(R.id.setUsername);
         email = findViewById(R.id.setEmail);
         userProfileImage = findViewById(R.id.setProfileImage);
         mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
    }


    //getting the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();

            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                final Uri resultUri = result.getUri();
                final StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final String downloadUrl = uri.toString();
                                myRef.child("Users").child(currentUserID).child("image").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SettingsActivity.this, "Profile image stored to firebase database successfully.", Toast.LENGTH_SHORT).show();

                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SettingsActivity.this, "Error Occurred..." + message, Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        });
                            }
                        });
                    }
                });
            }
        }

        }


        //displaying user's image, email and username
    private void RetrieveUserInfo(){

        myRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if((snapshot.exists()) && (snapshot.hasChild("username")) && (snapshot.hasChild("email"))  && (snapshot.hasChild("image")))
                {
                    String retrieveUsername = snapshot.child("username").getValue().toString();
                    String retrieveEmail = snapshot.child("email").getValue().toString();
                    String retrieveProfileImage = snapshot.child("image").getValue().toString();

                    username.setText(retrieveUsername);
                    email.setText(retrieveEmail);

                    //using the method from picasso library to display the image to user
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                    userProfileImage.setVisibility(View.VISIBLE);
                }
                else
                {
                    String retrieveUsername = snapshot.child("username").getValue().toString();
                    String retrieveEmail = snapshot.child("email").getValue().toString();

                    username.setText(retrieveUsername);
                    email.setText(retrieveEmail);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                int message = error.getCode();
                Toast.makeText(SettingsActivity.this, "Database Error: " + message, Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}