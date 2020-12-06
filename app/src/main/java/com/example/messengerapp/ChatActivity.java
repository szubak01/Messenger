package com.example.messengerapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

     private String messageReceiverID, messageReceiverName,messageReceiverImage ;

     private TextView userName;
     private CircleImageView userImage;

     private Toolbar ChatToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_user_image").toString();

        InitializeControllers();

        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.default_image_profile).into(userImage);
        userName.setText(messageReceiverName);
    }

    private void InitializeControllers() {

        ChatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolbar);
        getSupportActionBar().setTitle("");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage = findViewById(R.id.custom_profile_image);
        userName = findViewById(R.id.custom_user_name);

    }
}