package com.metropolitan.milos.metchat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {


    private Button btnSendFriendRequest;
    private Button btnDeclineFriendRequest;

    private TextView profileVisitUserName;
    private TextView profileVisitUserStatus;

    private ImageView profileVisitUserImage;

    private DatabaseReference databaseReference;
    private DatabaseReference databaseFriendRequestReference;
    private DatabaseReference databaseFriendsReference;
    private DatabaseReference databaseNotificationsReference;
    private FirebaseAuth mAuth;


    private String CURRENT_STATE;
    private String senderUserUID;
    private String receiverUserUID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        databaseFriendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        mAuth = FirebaseAuth.getInstance();
        senderUserUID = mAuth.getCurrentUser().getUid();




        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        receiverUserUID = getIntent().getExtras().get("visitUserUID").toString();

        databaseFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");

        databaseNotificationsReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        databaseNotificationsReference.keepSynced(true);
        databaseFriendsReference.keepSynced(true);
        databaseFriendRequestReference.keepSynced(true);
        databaseReference.keepSynced(true);

        btnSendFriendRequest = (Button) findViewById(R.id.btnSendFriendRequest);
        btnDeclineFriendRequest = (Button) findViewById(R.id.btnDeclineFriendRequest);
        profileVisitUserImage = (ImageView) findViewById(R.id.profileVisitUserImage);
        profileVisitUserName = (TextView) findViewById(R.id.profileVisitUserName);
        profileVisitUserStatus = (TextView) findViewById(R.id.profileVisitUserStatus);

        CURRENT_STATE = "not_friends";



        databaseReference.child(receiverUserUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String image = dataSnapshot.child("user_image").getValue().toString();
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();

                Picasso.get().load(image).placeholder(R.drawable.default_profile).into(profileVisitUserImage);
                profileVisitUserName.setText(name);
                profileVisitUserStatus.setText(status);

                databaseFriendRequestReference.child(senderUserUID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                            if(dataSnapshot.hasChild(receiverUserUID)){
                                String requestType = dataSnapshot.child(receiverUserUID).child("request_type").getValue().toString();

                                if (requestType.equals("sent")){
                                    CURRENT_STATE = "request_sent";
                                    btnSendFriendRequest.setText("Obustavi zahtev");

                                    btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                    btnDeclineFriendRequest.setEnabled(false);

                                }else if(requestType.equals("received")){
                                    CURRENT_STATE = "request_received";
                                    btnSendFriendRequest.setText("Prihvati zahtev");


                                    btnDeclineFriendRequest.setText("Izbrisi zahtev");
                                    btnDeclineFriendRequest.setVisibility(View.VISIBLE);
                                    btnDeclineFriendRequest.setEnabled(true);

                                    btnDeclineFriendRequest.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            DeclineFriendRequest();
                                        }
                                    });

                                }
                            }
                        else{

                            databaseFriendsReference.child(senderUserUID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(receiverUserUID)){
                                        CURRENT_STATE = "friends";
                                        btnSendFriendRequest.setText("Izbriši prijatelja");

                                        btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                        btnDeclineFriendRequest.setEnabled(false);

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
        btnDeclineFriendRequest.setEnabled(false);


        if(!senderUserUID.equals(receiverUserUID)){
            btnSendFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    btnSendFriendRequest.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends")){
                        SendFriendRequest();
                    }

                    if(CURRENT_STATE.equals("request_sent")){
                        CancelFriendRequest();
                    }

                    if(CURRENT_STATE.equals("request_received")){
                        AcceptFriendRequest();
                    }

                    if(CURRENT_STATE.equals("friends")){
                        UnfriendPerson();
                    }

                }
            });
        }else{

            btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
            btnSendFriendRequest.setVisibility(View.INVISIBLE);

        }

    }

    private void DeclineFriendRequest() {
        databaseFriendRequestReference.child(senderUserUID).child(receiverUserUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()) {
                    databaseFriendRequestReference.child(receiverUserUID).child(senderUserUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                btnSendFriendRequest.setEnabled(true);
                                CURRENT_STATE = "not_friends";
                                btnSendFriendRequest.setText("Dodaj prijatelja");

                                btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                btnDeclineFriendRequest.setEnabled(false);
                            }

                        }
                    });
                }
            }
        });

    }



    private void UnfriendPerson() {

        databaseFriendsReference.child(senderUserUID).child(receiverUserUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    databaseFriendsReference.child(receiverUserUID).child(senderUserUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    btnSendFriendRequest.setEnabled(true);
                                    CURRENT_STATE = "not_friends";
                                    btnSendFriendRequest.setText("Dodaj prijatelja");

                                    btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                    btnDeclineFriendRequest.setEnabled(false);

                                }
                        }
                    });

                }
            }
        });


    }

    private void AcceptFriendRequest() {

        Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        final String saveCurrentDate = currentDate.format(calendar.getTime());

        databaseFriendsReference.child(senderUserUID).child(receiverUserUID).child("date").setValue(saveCurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                databaseFriendsReference.child(receiverUserUID).child(senderUserUID).child("date").setValue(saveCurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        databaseFriendRequestReference.child(senderUserUID).child(receiverUserUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()) {
                                    databaseFriendRequestReference.child(receiverUserUID).child(senderUserUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                btnSendFriendRequest.setEnabled(true);
                                                CURRENT_STATE = "friends";
                                                btnSendFriendRequest.setText("Izbriši osobu iz prijatelja");

                                                btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineFriendRequest.setEnabled(false);
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

    private void CancelFriendRequest() {
        databaseFriendRequestReference.child(senderUserUID).child(receiverUserUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()) {
                    databaseFriendRequestReference.child(receiverUserUID).child(senderUserUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                btnSendFriendRequest.setEnabled(true);
                                CURRENT_STATE = "not_friends";
                                btnSendFriendRequest.setText("Dodaj prijatelja");

                                btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                btnDeclineFriendRequest.setEnabled(false);
                            }

                        }
                    });
                }
            }
        });

    }



    private void SendFriendRequest() {

        databaseFriendRequestReference.child(senderUserUID).child(receiverUserUID).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    databaseFriendRequestReference.child(receiverUserUID).child(senderUserUID).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                HashMap<String, String> notificationsData = new HashMap<String, String>();
                                notificationsData.put("from", senderUserUID);
                                notificationsData.put("type", "request");

                                databaseNotificationsReference.child(receiverUserUID).push().setValue(notificationsData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){
                                            btnSendFriendRequest.setEnabled(true);
                                            CURRENT_STATE = "request_sent";
                                            btnSendFriendRequest.setText("Obustavi zahtev");

                                            btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                            btnDeclineFriendRequest.setEnabled(false);
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
}
