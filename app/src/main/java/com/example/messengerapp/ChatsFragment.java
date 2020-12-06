package com.example.messengerapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.common.collect.ComparisonChain.start;


public class ChatsFragment extends Fragment {

    private View PrivateChatsView;
    private RecyclerView ChatList;
    private DatabaseReference ChatsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // Inflate the layout for this fragment
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        ChatList = PrivateChatsView.findViewById(R.id.chats_list);
        ChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        return PrivateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder > adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                final String usersIDs = getRef(position).getKey();
                final String[] retrieveImage = {"default_image"};

                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){
                            if(snapshot.hasChild("image"))
                            {
                                retrieveImage[0] = snapshot.child("image").getValue().toString();
                                Picasso.get().load(retrieveImage[0]).placeholder(R.drawable.default_image_profile).into(holder.profileImage);
                            }

                            final String retrieveUserName = snapshot.child("username").getValue().toString();

                            holder.userName.setText(retrieveUserName);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id", usersIDs);
                                    chatIntent.putExtra("visit_user_name", retrieveUserName);
                                    chatIntent.putExtra("visit_user_image", retrieveImage[0]);
                                    startActivity(chatIntent);
                                }
                            });
                            }
                        }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new ChatsViewHolder(view);
            }
        };
        ChatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        TextView userName;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            userName = itemView.findViewById(R.id.users_username);
        }
    }
}