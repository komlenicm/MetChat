package com.metropolitan.milos.metchat;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView requestList;

    private View myMainView;

    private DatabaseReference databaseFriendsRequestsReference;

    private DatabaseReference databaseFriendsReference;

    private DatabaseReference databaseUsersReference;

    private DatabaseReference databaseFriendsReqReference;

    private FirebaseAuth mAuth;

    public String onlineUserUID;



    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        requestList = (RecyclerView) myMainView.findViewById(R.id.requestList);

        mAuth = FirebaseAuth.getInstance();

        onlineUserUID = mAuth.getCurrentUser().getUid();

        databaseFriendsRequestsReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(onlineUserUID);
        databaseUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        databaseFriendsReqReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        requestList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        requestList.setLayoutManager(linearLayoutManager);

        return myMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests, RequestsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>(
                Requests.class,
                R.layout.friend_request_all_user_layout,
                RequestsViewHolder.class,
                databaseFriendsRequestsReference
        ) {
            @Override
            protected void populateViewHolder(final RequestsViewHolder viewHolder, Requests model, int position) {

                final String listUsersUID = getRef(position).getKey();

                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String requestType = dataSnapshot.getValue().toString();

                            if(requestType.equals("received")){

                                Button btnAcceptFriendRequest = viewHolder.myView.findViewById(R.id.btnAcceptFriendRequest);
                                Button btnCancelFriendRequest = viewHolder.myView.findViewById(R.id.btnCancelFriendRequest);

                                btnAcceptFriendRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Calendar calendar = Calendar.getInstance();
                                        final SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
                                        final String saveCurrentDate = currentDate.format(calendar.getTime());

                                        databaseFriendsReference.child(onlineUserUID).child(listUsersUID).child("date").setValue(saveCurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                databaseFriendsReference.child(listUsersUID).child(onlineUserUID).child("date").setValue(saveCurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        databaseFriendsReqReference.child(onlineUserUID).child(listUsersUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful()) {
                                                                    databaseFriendsReqReference.child(listUsersUID).child(onlineUserUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){

                                                                                Toast.makeText(getContext(),"Zahtev za prijateljstvo je prihvaćen.",Toast.LENGTH_SHORT).show();
                                                                            }

                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });

                                                    }
                                                });
                                            }
                                        });
                                    }
                                });

                                btnCancelFriendRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        databaseFriendsReqReference.child(onlineUserUID).child(listUsersUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()) {
                                                    databaseFriendsReqReference.child(listUsersUID).child(onlineUserUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(getContext(),"Zahtev za prijateljstvo je izbrisan.",Toast.LENGTH_SHORT).show();
                                                            }

                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                });

                                databaseUsersReference.child(listUsersUID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        final String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        final String userStatus = dataSnapshot.child("user_status").getValue().toString();

                                        viewHolder.setUserName(userName);
                                        viewHolder.setUserStatus(userStatus);
                                        viewHolder.setThumbImage(thumbImage);

                                        viewHolder.myView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[] = new CharSequence[]{
                                                        "PRIHVATI",
                                                        "IZBRIŠI"
                                                };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Izaberite: ");

                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        if(which == 0){

                                                            Calendar calendar = Calendar.getInstance();
                                                            final SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
                                                            final String saveCurrentDate = currentDate.format(calendar.getTime());

                                                            databaseFriendsReference.child(onlineUserUID).child(listUsersUID).child("date").setValue(saveCurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    databaseFriendsReference.child(listUsersUID).child(onlineUserUID).child("date").setValue(saveCurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            databaseFriendsReqReference.child(onlineUserUID).child(listUsersUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if(task.isSuccessful()) {
                                                                                        databaseFriendsReqReference.child(listUsersUID).child(onlineUserUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful()){

                                                                                                    Toast.makeText(getContext(),"Zahtev za prijateljstvo je prihvaćen.",Toast.LENGTH_SHORT).show();
                                                                                                }

                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });

                                                                        }
                                                                    });
                                                                }
                                                            });

                                                        }

                                                        if(which == 1){

                                                            databaseFriendsReqReference.child(onlineUserUID).child(listUsersUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if(task.isSuccessful()) {
                                                                        databaseFriendsReqReference.child(listUsersUID).child(onlineUserUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    Toast.makeText(getContext(),"Zahtev za prijateljstvo je izbrisan.",Toast.LENGTH_SHORT).show();
                                                                                }

                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });

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


                            }else if (requestType.equals("sent")){

                                Button btnAcceptFriendRequest = viewHolder.myView.findViewById(R.id.btnAcceptFriendRequest);
                                Button btnCancelFriendRequest = viewHolder.myView.findViewById(R.id.btnCancelFriendRequest);

                                btnAcceptFriendRequest.setText("Zahtev poslat");

                                btnCancelFriendRequest.setVisibility(View.INVISIBLE);

                                databaseUsersReference.child(listUsersUID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        final String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        final String userStatus = dataSnapshot.child("user_status").getValue().toString();

                                        viewHolder.setUserName(userName);
                                        viewHolder.setUserStatus(userStatus);
                                        viewHolder.setThumbImage(thumbImage);

                                        viewHolder.myView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[] = new CharSequence[]{
                                                        "Izbriši zahtev"

                                                };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Zahtev poslat");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        if(which == 0){

                                                            databaseFriendsReqReference.child(onlineUserUID).child(listUsersUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if(task.isSuccessful()) {
                                                                        databaseFriendsReqReference.child(listUsersUID).child(onlineUserUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    Toast.makeText(getContext(),"Zahtev za prijateljstvo je izbrisan.",Toast.LENGTH_SHORT).show();
                                                                                }

                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });

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
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }
        };

        requestList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{

        View myView;

        public RequestsViewHolder(View itemView) {
            super(itemView);

            myView = itemView;
        }

        public void setUserName(String userName) {

            TextView tvRequestProfileName = (TextView) myView.findViewById(R.id.tvRequestProfileName);
            tvRequestProfileName.setText(userName);

        }

        public void setUserStatus(String userStatus) {
            TextView tvRequestProfileStatus = (TextView) myView.findViewById(R.id.tvRequestProfileStatus);
            tvRequestProfileStatus.setText(userStatus);
        }

        public  void setThumbImage(String thumbImage){
            RoundedImageView thumbimage = (RoundedImageView) myView.findViewById(R.id.requestProfileImage);
            Picasso.get().load(thumbImage).placeholder(R.drawable.default_profile).into(thumbimage);
        }
    }

}
