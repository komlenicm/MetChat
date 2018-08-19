package com.metropolitan.milos.metchat;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter{

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private List<Messages> userMessagesList;

    private FirebaseAuth mAuth;
    public View v;


    public MessagesAdapter() {
    }

    public MessagesAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    public List<Messages> getUserMessagesList() {
        return userMessagesList;
    }

    public void setUserMessagesList(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if( viewType == VIEW_TYPE_MESSAGE_SENT){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout_of_user_sender,parent,false);

            return new SentMessageHolder(v);

        }else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED){

            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout_of_user_received,parent,false);

            return new ReceivedMessageHolder(v);

        }


        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Messages messages = userMessagesList.get(position);

        switch (holder.getItemViewType()){
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(messages);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(messages);

        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageTextReceived;


        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageTextReceived = (TextView) itemView.findViewById(R.id.tvMessageTextReceived);

        }

        void bind(Messages message) {
            messageTextReceived.setText(message.getMessage());

        }
    }

    @Override
    public int getItemViewType(int position) {
        Messages message = userMessagesList.get(position);
        mAuth = FirebaseAuth.getInstance();
        String messageSenderUID = mAuth.getCurrentUser().getUid();

        if (message.getFrom().equals(messageSenderUID)) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageTextSent;


        SentMessageHolder(View itemView) {
            super(itemView);
            messageTextSent = (TextView) itemView.findViewById(R.id.tvMessageTextSent);

        }

        void bind(Messages message) {
            messageTextSent.setText(message.getMessage());

        }
    }


//********************************OVAJ DEO RADI********************
//
//    @NonNull
//    @Override
//    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//
//
//        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout_of_user, parent,false);
//
//        mAuth = FirebaseAuth.getInstance();
//
//        return new MessagesViewHolder(v);
//
//
//
//
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull MessagesViewHolder holder, @SuppressLint("RecyclerView") int position) {
//
//        String messageSenderUID = mAuth.getCurrentUser().getUid();
//
//        Messages messages = userMessagesList.get(position);
//
//        String fromUserUID = messages.getFrom();
//
//
//        if (fromUserUID.equals(messageSenderUID)) {
//
//            holder.tvMessageText.setBackgroundResource(R.drawable.message_text_background);
//
//
//        } else {
//
//            holder.tvMessageText.setBackgroundResource(R.drawable.message_text_background_two);
//
//            holder.tvMessageText.setGravity(Gravity.LEFT);
//
//        }
//
//        holder.tvMessageText.setText(messages.getMessage());
//
//
//    }
//
//
//    @Override
//    public int getItemCount() {
//        return userMessagesList.size();
//    }
//
//    public MessagesAdapter(List<Messages> userMessagesList) {
//        this.userMessagesList = userMessagesList;
//    }
//
//    public List<Messages> getUserMessagesList() {
//        return userMessagesList;
//    }
//
//    public void setUserMessagesList(List<Messages> userMessagesList) {
//        this.userMessagesList = userMessagesList;
//    }
//
//    public class MessagesViewHolder extends RecyclerView.ViewHolder{
//
//        public TextView tvMessageText;
//
//
//        public MessagesViewHolder(View itemView) {
//            super(itemView);
//
//            tvMessageText = (TextView) itemView.findViewById(R.id.tvMessageText);
//        }
//    }
// ****************OVAJ DEO RADI*****************************





}
