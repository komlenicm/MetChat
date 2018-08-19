package com.metropolitan.milos.metchat;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView rvAllUsersList;

    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mToolbar = (Toolbar) findViewById(R.id.allUsersAppBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Lista korisnika");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.keepSynced(true);

        rvAllUsersList = (RecyclerView) findViewById(R.id.rvAllUsersList);
        rvAllUsersList.setHasFixedSize(true);
        rvAllUsersList.setLayoutManager(new LinearLayoutManager(this));




    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<AllUsers,AllUsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder>(
                AllUsers.class,
                R.layout.allusers_display_layout,
                AllUsersViewHolder.class,
                databaseReference

        ) {
            @Override
            protected void populateViewHolder(AllUsersViewHolder viewHolder, AllUsers model, final int position) {
                viewHolder.setUser_thumb_image(model.getUser_thumb_image());
                viewHolder.setUser_name(model.getUser_name());
                viewHolder.setUser_status(model.getUser_status());


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visitUserUID = getRef(position).getKey();

                        Intent profileActivityIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                        profileActivityIntent.putExtra("visitUserUID", visitUserUID);
                        startActivity(profileActivityIntent);
                    }
                });

            }
        };

        rvAllUsersList.setAdapter(firebaseRecyclerAdapter);


    }

    public static class AllUsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public AllUsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setUser_name(String user_name){
            TextView name = (TextView) mView.findViewById(R.id.allusers_name);
            name.setText(user_name);
        }

        public void setUser_status(String user_status){
            TextView status = (TextView) mView.findViewById(R.id.allusers_status);
            status.setText(user_status);
        }

        public void setUser_thumb_image(final String user_thumb_image){
            final RoundedImageView thumbimage = (RoundedImageView) mView.findViewById(R.id.allusers_profile_image);


            Picasso.get().load(user_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile).into(thumbimage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(user_thumb_image).placeholder(R.drawable.default_profile).into(thumbimage);

                }
            });

        }



    }
}
