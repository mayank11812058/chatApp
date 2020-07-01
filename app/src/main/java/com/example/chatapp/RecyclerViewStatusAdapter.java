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

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewStatusAdapter extends RecyclerView.Adapter<RecyclerViewStatusAdapter.viewHolder> {
    Context context;
    ArrayList<UserData> users;

    public RecyclerViewStatusAdapter(Context context, ArrayList<UserData> users){
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_status, parent, false);
        return new RecyclerViewStatusAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        UserData userData = users.get(position);
        holder.textView.setText(userData.getName());
        Glide.with(context).load(userData.getImageUri()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        CircleImageView imageView;
        TextView textView;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.recyclerStatusImageView);
            textView = itemView.findViewById(R.id.recyclerStatusNameTextView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, StatusView.class);
                    intent.putExtra("Images", users.get(getAdapterPosition()).getImageStatus());
                    context.startActivity(intent);
                }
            });
        }
    }
}
