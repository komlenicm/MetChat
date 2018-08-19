package com.metropolitan.milos.metchat;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView rvFriendsList;

    private DatabaseReference databaseFriendsReference;
    private DatabaseReference databaseUsersReference;
    private FirebaseAuth mAuth;

    String onlineUserUID;

    private View myMainView;

    public FriendsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        myMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        rvFriendsList = (RecyclerView) myMainView.findViewById(R.id.rvFriendsList);

        mAuth = FirebaseAuth.getInstance();
        onlineUserUID = mAuth.getCurrentUser().getUid();

        databaseFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(onlineUserUID);
        databaseFriendsReference.keepSynced(true);
        databaseUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsersReference.keepSynced(true);

        rvFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));




        return myMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.allusers_display_layout,
                FriendsViewHolder.class,
                databaseFriendsReference

        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, final int position) {
                viewHolder.setDate(model.getDate());


                final String listUserUID = getRef(position).getKey();
                databaseUsersReference.child(listUserUID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();


                        if(dataSnapshot.hasChild("online")){
                            String onlineStatus = (String) dataSnapshot.child("online").getValue().toString();

                            viewHolder.setUserOnline(onlineStatus);
                        }

                        viewHolder.setUsername(userName);
                        viewHolder.setThumbImage(thumbImage);


                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{
                                        "Prikaži profil",
                                        "Pošalji poruku korisniku"
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Izaberite: ");

                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(which == 0){

                                            Intent profileActivityIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileActivityIntent.putExtra("visitUserUID", listUserUID);
                                            startActivity(profileActivityIntent);
                                        }

                                        if(which == 1){

                                            if(dataSnapshot.child("online").exists()){

                                                Intent chatActivityIntent = new Intent(getContext(), ChatActivity.class);
                                                chatActivityIntent.putExtra("visitUserUID", listUserUID);
                                                chatActivityIntent.putExtra("user_name", userName);
                                                startActivity(chatActivityIntent);

                                            }else{

                                                databaseUsersReference.child(listUserUID).child("online").setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Intent chatActivityIntent = new Intent(getContext(), ChatActivity.class);
                                                        chatActivityIntent.putExtra("visitUserUID", listUserUID);
                                                        chatActivityIntent.putExtra("user_name", userName);
                                                        startActivity(chatActivityIntent);

                                                    }
                                                });


                                            }

                                        }

                                    }
                                });

                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        rvFriendsList.setAdapter(firebaseRecyclerAdapter);


    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDate(String date){
            TextView sinceFriendsDate = (TextView) mView.findViewById(R.id.allusers_status);
            sinceFriendsDate.setText("Prijatelji od: \n" + date);
        }

        public  void setUsername(String username){
            TextView userNameDisplay = (TextView) mView.findViewById(R.id.allusers_name);
            userNameDisplay.setText(username);
        }

        public  void setThumbImage(String thumbImage){
            RoundedImageView thumbimage = (RoundedImageView) mView.findViewById(R.id.allusers_profile_image);
            Picasso.get().load(thumbImage).placeholder(R.drawable.default_profile).into(thumbimage);
        }

        public void setUserOnline(String onlineStatus) {

            ImageView onlineStatusView = (ImageView) mView.findViewById(R.id.statusOnline);

            if(onlineStatus.equals("true")){
                onlineStatusView.setVisibility(View.VISIBLE);
            }else{
                onlineStatusView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
