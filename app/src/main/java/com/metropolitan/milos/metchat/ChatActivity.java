package com.metropolitan.milos.metchat;

import android.content.Context;
import android.media.Image;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId;
    private String messageReceiverName;

    private Toolbar chatToolbar;

    private TextView tvChatUsername;
    private TextView tvChatLastSeenOnline;
    private RoundedImageView roundedChatProfileImage;

    private ImageButton btnSendMessage;
    private ImageButton selectImageToSend;
    private EditText etSendMessage;


    private DatabaseReference databaseRootReference;

    private FirebaseAuth mAuth;
    private String messageSenderUID;

    private RecyclerView messagesListOfUsers;

    private final List<Messages> messageList = new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;

    private MessagesAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        databaseRootReference = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        messageSenderUID = mAuth.getCurrentUser().getUid();

        messageReceiverId = getIntent().getExtras().get("visitUserUID").toString();
        messageReceiverName = getIntent().getExtras().get("user_name").toString();

        chatToolbar = (Toolbar) findViewById(R.id.chatBarLayout);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View actionBarView = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(actionBarView);

        tvChatUsername = (TextView) findViewById(R.id.tvChatUsername);
        tvChatLastSeenOnline = (TextView) findViewById(R.id.tvChatLastSeenOnline);
        roundedChatProfileImage = (RoundedImageView) findViewById(R.id.roundedChatProfileImage);

        btnSendMessage = (ImageButton) findViewById(R.id.btnSendMessage);
        selectImageToSend = (ImageButton) findViewById(R.id.selectImageToSend);
        etSendMessage = (EditText) findViewById(R.id.etSendMessage);
        messagesListOfUsers = (RecyclerView) findViewById(R.id.messagesListOfUsers);


        messagesAdapter = new MessagesAdapter(messageList);



        linearLayoutManager = new LinearLayoutManager(this);
        messagesListOfUsers.setHasFixedSize(true);


        messagesListOfUsers.postDelayed(new Runnable() {
            @Override
            public void run() {
                messagesListOfUsers.scrollToPosition(messagesAdapter.getItemCount() - 1);
            }
        }, 100);



        messagesListOfUsers.setLayoutManager(linearLayoutManager);
        messagesListOfUsers.setAdapter(messagesAdapter);

        etSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messagesListOfUsers.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        messagesListOfUsers.scrollToPosition(messagesListOfUsers.getAdapter().getItemCount() - 1);
                    }
                }, 100);
            }
        });

        FetchMessages();

        tvChatUsername.setText(messageReceiverName);


        databaseRootReference.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String online = dataSnapshot.child("online").getValue().toString();
                final String userThumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                Picasso.get().load(userThumbImage).placeholder(R.drawable.default_profile).into(roundedChatProfileImage);

                if(online.equals("true")){

                    tvChatLastSeenOnline.setText("Online");

                }else{

                    LastSeenOnlineTime getTime = new LastSeenOnlineTime();

                    long lastSeenOnline = Long.parseLong(online);

                    String lastSeenOnlineDisplayTime = getTime.getTimeAgo(lastSeenOnline).toString();

                    tvChatLastSeenOnline.setText(lastSeenOnlineDisplayTime);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendMessage();

                messagesListOfUsers.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        messagesListOfUsers.scrollToPosition(messagesListOfUsers.getAdapter().getItemCount() - 1);
                    }
                }, 100);
            }
        });

    }


    private void SendMessage() {
        String messageText = etSendMessage.getText().toString();

        if(TextUtils.isEmpty(messageText)){

            Toast.makeText(getBaseContext(), "Morate uneti poruku pre slanja.", Toast.LENGTH_SHORT).show();

        }else{

            String messageSenderReference = "Messages/" + messageSenderUID + "/" + messageReceiverId;
            String messageReceiverReference = "Messages/" + messageReceiverId + "/" + messageSenderUID;

            DatabaseReference userMessageKey = databaseRootReference.child("Messages").child(messageSenderUID).child(messageReceiverId).push();
            String messagePushId = userMessageKey.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("seen", false);
            messageTextBody.put("type", "text");
            messageTextBody.put("time", ServerValue.TIMESTAMP);
            messageTextBody.put("from", messageSenderUID);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderReference + "/" + messagePushId, messageTextBody);
            messageBodyDetails.put(messageReceiverReference + "/" + messagePushId, messageTextBody);

            databaseRootReference.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d("Chat_Log", databaseError.getMessage().toString());
                    }

                    etSendMessage.getText().clear();
                }
            });

        }

    }

    private void FetchMessages() {



        databaseRootReference.child("Messages").child(messageSenderUID).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);

                messageList.add(messages);

                messagesAdapter.notifyDataSetChanged();

                messagesListOfUsers.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        messagesListOfUsers.scrollToPosition(messagesListOfUsers.getAdapter().getItemCount() - 1);
                    }
                }, 100);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

}
