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

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView chatsList;

    private DatabaseReference databaseFriendsReference;
    private DatabaseReference databaseUsersReference;
    private FirebaseAuth mAuth;

    String onlineUserUID;

    private View myMainView;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        chatsList = (RecyclerView) myMainView.findViewById(R.id.chatsList);

        mAuth = FirebaseAuth.getInstance();
        onlineUserUID = mAuth.getCurrentUser().getUid();
        databaseFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(onlineUserUID);
        databaseUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        chatsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        chatsList.setLayoutManager(linearLayoutManager);



        return myMainView;
    }


    @Override
    public void onStart() {
        super.onStart();




        FirebaseRecyclerAdapter<Chats, ChatsFragment.ChatsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chats, ChatsFragment.ChatsViewHolder>(
                Chats.class,
                R.layout.allusers_display_layout,
                ChatsFragment.ChatsViewHolder.class,
                databaseFriendsReference

        ) {
            @Override
            protected void populateViewHolder(final ChatsFragment.ChatsViewHolder viewHolder, Chats model, final int position) {




                final String listUserUID = getRef(position).getKey();
                databaseUsersReference.child(listUserUID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                        String userStatus = dataSnapshot.child("user_status").getValue().toString();


                        if(dataSnapshot.hasChild("online")){
                            String onlineStatus = (String) dataSnapshot.child("online").getValue().toString();

                            viewHolder.setUserOnline(onlineStatus);
                        }

                        viewHolder.setUsername(userName);
                        viewHolder.setThumbImage(thumbImage);
                        viewHolder.setUserStatus(userStatus);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

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
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        chatsList.setAdapter(firebaseRecyclerAdapter);




    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public ChatsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
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

        public void setUserStatus(String userStatus) {

            TextView userStatus2 = (TextView) mView.findViewById(R.id.allusers_status);
            userStatus2.setText(userStatus);

        }
    }



}
