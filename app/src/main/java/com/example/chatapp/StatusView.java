package com.example.chatapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class StatusView extends AppCompatActivity implements View.OnClickListener {
    ProgressBar progressBar;
    ImageView imageView;
    String userId;
    ImageButton imageButton;
    Handler handler;
    Runnable runnableCode;
    final int[] position = {0};
    final ArrayList<String> imageUri = new ArrayList<>();
    int size;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_view);
        progressBar = findViewById(R.id.statusLinearProgressBar);
        imageView = findViewById(R.id.statusImageView);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    handler.removeCallbacks(runnableCode);
                    return true;
                }

                if(event.getAction() == MotionEvent.ACTION_UP){
                    handler.post(runnableCode);
                    return true;
                }

                return false;
            }
        });
        imageButton = findViewById(R.id.statusRemoveButton);
        Intent intent = getIntent();

        for(String uri: intent.getStringArrayListExtra("Images")){
            imageUri.add(uri);
        }

        size = imageUri.size();

        if(intent.getStringExtra("userId") != null){
            userId = intent.getStringExtra("userId");
            imageButton.setVisibility(View.VISIBLE);
            imageButton.setOnClickListener(this);
            key = intent.getStringExtra("key");
        }

        progressBar.setMax(4 * size);
        progressBar.setProgress(0);

        handler = new Handler();
        runnableCode = new Runnable() {
            @Override
            public void run() {
                if(position[0] < 4 * size) {
                    Glide.with(getApplicationContext()).load(imageUri.get((int)(position[0] / 4))).into(imageView);
                    position[0]++;
                    progressBar.incrementProgressBy(1);
                    handler.postDelayed(this, 250);
                }else{
                    Intent backIntent = new Intent(getApplicationContext(), ChatsActivity.class);
                    backIntent.putExtra("statusView", "true");
                    startActivity(backIntent);
                    finish();
                }
            }
        };

        handler.post(runnableCode);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.statusRemoveButton:
                handler.removeCallbacks(runnableCode);
                AlertDialog alertDialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(StatusView.this);
                builder.setTitle("Are you sure?");
                builder.setMessage("Do you want to remove this image from your status?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println(position[0]);
                        if(position[0] != 4 * size) {
                            imageUri.remove((int)(position[0] / 4));
                        }else{
                            imageUri.remove(size - 1);
                        }
                        size--;
                        progressBar.setMax(size * 4);
                        progressBar.setProgress(progressBar.getProgress() - progressBar.getProgress() % 4);
                        System.out.println(position[0]);
                        handler.post(runnableCode);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.post(runnableCode);
                    }
                });
                alertDialog = builder.create();
                alertDialog.show();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(userId != null){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference usersReference = db.collection("User");
            usersReference.document(key).update("ImageStatus", imageUri);
        }
    }
}