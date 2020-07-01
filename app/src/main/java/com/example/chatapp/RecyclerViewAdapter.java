package com.example.chatapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.viewHolder> {
    private Context context;
    private ArrayList<UserData> users;

    public RecyclerViewAdapter(Context context, ArrayList<UserData> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_chats, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        UserData userData = users.get(position);
        holder.usernameView.setText(userData.getName());
        holder.statusView.setText(userData.getStatus());
        Glide.with(context).load(userData.getImageUri()).into(holder.circleImageView);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        CircleImageView circleImageView;
        TextView usernameView;
        TextView statusView;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.recyclerChatsImageView);
            usernameView = itemView.findViewById(R.id.recyclerChatsNameTextView);
            statusView = itemView.findViewById(R.id.recyclerChatsStatusTextView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("recevierName", users.get(getAdapterPosition()).getName());
                    intent.putExtra("recevierEmail", users.get(getAdapterPosition()).getEmail());
                    intent.putExtra("recevierImageUri", users.get(getAdapterPosition()).getImageUri());
                    intent.putExtra("recevierId", users.get(getAdapterPosition()).getUserId());
                    intent.putExtra("recevierStatus", users.get(getAdapterPosition()).getStatus());
                    context.startActivity(intent);
                }
            });
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
