package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactProfile extends AppCompatActivity {
    Toolbar toolbar;
    CircleImageView circleImageView;
    TextView recevierNameTextView;
    TextView recevierStatusTextView;
    TextView recevierEmailTextView;
    TextView membersTextView;
    String recevierName;
    String recevierEmail;
    String recevierId;
    String recevierImageUri;
    String recevierStatus;
    String groupId;
    String title;
    ArrayList<String> members = new ArrayList<>();
    ArrayList<UserData> users = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference usersReference = db.collection("User");

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
        title = intent.getStringExtra("title");
        groupId = intent.getStringExtra("groupId");
        members = intent.getStringArrayListExtra("members");

        Glide.with(getApplicationContext()).load(recevierImageUri).into(circleImageView);

        if(recevierName != null) {
            recevierNameTextView.setText(recevierName);
        }else{
            recevierNameTextView.setText(title);
        }

        if(recevierEmail != null) {
            recevierEmailTextView.setText(recevierEmail);
        }

        if(recevierStatus != null) {
            recevierStatusTextView.setText(recevierStatus);
        }

        membersTextView = findViewById(R.id.profileMembersTextView);
        recyclerView = findViewById(R.id.profileRecyclerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerViewAdapter = new RecyclerViewAdapter(getApplicationContext(), users);
        recyclerView.setAdapter(recyclerViewAdapter);

        if(members != null && members.size() > 0){
            membersTextView.setVisibility(View.VISIBLE);
            recevierEmailTextView.setVisibility(View.GONE);
            recevierStatusTextView.setVisibility(View.GONE);

            usersReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for(QueryDocumentSnapshot snapshots : queryDocumentSnapshots){
                        String username = snapshots.getString("username");
                        String email = snapshots.getString("email");
                        String imageUri = snapshots.getString("imageUri");
                        String userId = snapshots.getString("userId");
                        String status = snapshots.getString("status");

                        if(members.contains(userId)){
                            UserData userData = new UserData(username, email, imageUri, userId, status);
                            users.add(userData);
                            recyclerViewAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
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
        intent.putExtra("title", title);
        intent.putExtra("groupId", groupId);
        intent.putExtra("members", members);
        startActivity(intent);
        finish();
    }
}