package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactProfile extends AppCompatActivity {
    Toolbar toolbar;
    CircleImageView circleImageView;
    TextView recevierNameTextView;
    TextView recevierStatusTextView;
    TextView recevierEmailTextView;
    String recevierName;
    String recevierEmail;
    String recevierId;
    String recevierImageUri;
    String recevierStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_profile);
        toolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.backbtn));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        circleImageView = findViewById(R.id.profileImageView);
        recevierNameTextView = findViewById(R.id.profileNameTextView);
        recevierStatusTextView = findViewById(R.id.profileStatusTextView);
        recevierEmailTextView = findViewById(R.id.profileEmailTextView);
        Intent intent = getIntent();
        recevierName = intent.getStringExtra("recevierName");
        recevierEmail = intent.getStringExtra("recevierEmail");
        recevierId = intent.getStringExtra("recevierId");
        recevierImageUri = intent.getStringExtra("recevierImageUri");
        recevierStatus = intent.getStringExtra("recevierStatus");

        Glide.with(getApplicationContext()).load(recevierImageUri).into(circleImageView);
        recevierNameTextView.setText(recevierName);
        recevierEmailTextView.setText(recevierEmail);
        recevierStatusTextView.setText(recevierStatus);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra("recevierName", recevierName);
        intent.putExtra("recevierEmail", recevierEmail);
        intent.putExtra("recevierImageUri", recevierImageUri);
        intent.putExtra("recevierId", recevierId);
        intent.putExtra("recevierStatus", recevierStatus);
        startActivity(intent);
        finish();
    }
}