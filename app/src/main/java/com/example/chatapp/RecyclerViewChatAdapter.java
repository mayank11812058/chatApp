package com.example.chatapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewChatAdapter extends RecyclerView.Adapter<RecyclerViewChatAdapter.viewHolder>{
    private Context context;
    private ArrayList<Message> messages;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    public RecyclerViewChatAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
    }

    @NonNull
    @Override
    public RecyclerViewChatAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_chat, parent, false);
        return new RecyclerViewChatAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewChatAdapter.viewHolder holder, int position) {
        Message message = messages.get(position);

        if(message.getUserId().equals(firebaseUser.getUid().toString())){
            if(!message.getImageUri().isEmpty()){
                holder.imageView2.setBackgroundResource(R.drawable.sendborder);
                Glide.with(context).load(message.getImageUri()).into(holder.imageView2);
            }else{
                holder.messageView2.setBackgroundResource(R.drawable.sendborder);
                holder.messageView2.setText(message.getTextMessage());
            }
        }else{
            if(!message.getImageUri().isEmpty()){
                holder.imageView1.setBackgroundResource(R.drawable.receiveborder);
                Glide.with(context).load(message.getImageUri()).into(holder.imageView1);
            }else{
                holder.messageView1.setBackgroundResource(R.drawable.receiveborder);
                holder.messageView1.setText(message.getTextMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        ImageView imageView1, imageView2;
        TextView messageView1, messageView2;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imageView1 = itemView.findViewById(R.id.chatImageView1);
            imageView2 = itemView.findViewById(R.id.chatImageView2);
            messageView1 = itemView.findViewById(R.id.chatTextView1);
            messageView2 = itemView.findViewById(R.id.chatTextView2);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
