package com.example.messengerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView userNameProfile;
    private Button btnSendRequest, btnDeclineRequest;
    private CircleImageView userProfileImage;
    private String receiverUserID,
                   senderUserID,
                   CurrentState;


    private DatabaseReference UserRef,
                              ChatRequestRef,
                              ContactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();
        CurrentState = "new";

        userNameProfile = findViewById(R.id.visit_username);
        userProfileImage = findViewById(R.id.visit_profile_image);
        btnSendRequest = findViewById(R.id.send_request_button);
        btnDeclineRequest = findViewById(R.id.decline_request_button);



        RetrieveUserInfo();
        ManageChatRequest();
    }

        private void ManageChatRequest()
        {
            ChatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.hasChild(receiverUserID))
                    {
                        String TYPE_OF_REQUEST = snapshot.child(receiverUserID).child("TYPE_OF_REQUEST").getValue().toString();
                        if(TYPE_OF_REQUEST.equals("sent")){
                            CurrentState = "request_sent";
                            btnSendRequest.setText("Decline Chat Request");
                        }
                        else if(TYPE_OF_REQUEST.equals("received")){
                            CurrentState = "request_received";
                            btnSendRequest.setText("Accept Chat Request");

                            btnDeclineRequest.setVisibility(View.VISIBLE);
                            btnDeclineRequest.setEnabled(true);
                            btnDeclineRequest.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CancelChatRequest();
                                }
                            });
                        }
                    }
                    else
                    {
                        ContactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.hasChild(receiverUserID)){
                                    CurrentState = "friends";
                                    btnSendRequest.setText("Remove this contact");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {

                }
            });

            if(!senderUserID.equals(receiverUserID))
            {
                btnSendRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        btnSendRequest.setEnabled(false);
                        if(CurrentState.equals("new"))
                        {
                            SendChatRequest();
                        }
                        if(CurrentState.equals("request_sent"))
                        {
                            CancelChatRequest();
                        }
                        if(CurrentState.equals("request_received"))
                        {
                            AcceptChatRequest();
                        }
                        if(CurrentState.equals("friends"))
                        {
                            RemoveContact();
                        }
                    }
                });
            }
            else
            {
                btnSendRequest.setVisibility(View.INVISIBLE);
            }
        }

    private void RemoveContact() {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                btnSendRequest.setEnabled(true);
                                                CurrentState = "new";
                                                btnSendRequest.setText("Send Chat Request");

                                                btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        ContactsRef.child(senderUserID).child(receiverUserID).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ContactsRef.child(receiverUserID).child(senderUserID).child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                ChatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    ChatRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    btnSendRequest.setEnabled(true);
                                                                                    CurrentState = "friends";
                                                                                    btnSendRequest.setText("Remove this contact");

                                                                                    btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                                                    btnDeclineRequest.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                btnSendRequest.setEnabled(true);
                                                CurrentState = "new";
                                                btnSendRequest.setText("Send Chat Request");

                                                btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest()
    {
        ChatRequestRef.child(senderUserID).child(receiverUserID).child("TYPE_OF_REQUEST").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            ChatRequestRef.child(receiverUserID).child(senderUserID).child("TYPE_OF_REQUEST").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                btnSendRequest.setEnabled(true);
                                                CurrentState = "request_sent";
                                                btnSendRequest.setText("Cancel chat request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }



    private void RetrieveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if((snapshot.exists()) && (snapshot.hasChild("image"))){
                    String userName = snapshot.child("username").getValue().toString();
                    String userImage = snapshot.child("image").getValue().toString();

                    userNameProfile.setText(userName);
                    Picasso.get().load(userImage).placeholder(R.drawable.default_image_profile).into(userProfileImage);
                }
                else
                {
                    String userName = snapshot.child("username").getValue().toString();
                    userNameProfile.setText(userName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }
}