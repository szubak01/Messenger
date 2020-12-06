package com.example.messengerapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.core.content.ContextCompat.getSystemService;


public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView myContactsList;

    private DatabaseReference ContactsRef,
                              UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        myContactsList = (RecyclerView) ContactsView.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {
                String usersID = getRef(position).getKey();

                UsersRef.child(usersID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild("image"))
                        {
                            String profileName = snapshot.child("username").getValue().toString();
                            String userImage = snapshot.child("image").getValue().toString();

                            holder.userName.setText(profileName);
                            Picasso.get().load(userImage).placeholder(R.drawable.default_image_profile).into(holder.profileImage);
                        }
                        else{
                            String profileName = snapshot.child("username").getValue().toString();
                            holder.userName.setText(profileName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };
        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView userName;
        CircleImageView profileImage;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.users_username);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}