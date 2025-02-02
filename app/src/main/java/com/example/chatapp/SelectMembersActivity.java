package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SelectMembersActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    RecyclerView recyclerView;
    RecyclerViewSelectAdapter recyclerViewSelectAdapter;
    Button button;
    ArrayList<UserData> users = new ArrayList<>();
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser firebaseUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference usersReference = db.collection("User");
    Boolean isTrue;
    String recevierName;
    String recevierEmail;
    String recevierId;
    String recevierImageUri;
    String recevierStatus;
    String groupId;
    String title;
    ArrayList<String> members = new ArrayList<>();
    ArrayList<String> groupMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_members);
        Intent intent = getIntent();
        isTrue = intent.getBooleanExtra("from", false);
        recevierName = intent.getStringExtra("recevierName");
        recevierEmail = intent.getStringExtra("recevierEmail");
        recevierId = intent.getStringExtra("recevierId");
        recevierImageUri = intent.getStringExtra("recevierImageUri");
        recevierStatus = intent.getStringExtra("recevierStatus");
        title = intent.getStringExtra("title");
        groupId = intent.getStringExtra("groupId");
        members = intent.getStringArrayListExtra("members");

        for(String member: SharedClass.selected){
            groupMembers.add(member);
        }

        SharedClass.selected.clear();
        toolbar = findViewById(R.id.selectMembersToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.backbtn));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitleTextColor(Color.WHITE);
        recyclerView = findViewById(R.id.SelectMembersRecyclerView);
        recyclerViewSelectAdapter = new RecyclerViewSelectAdapter(this, users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerViewSelectAdapter);
        button = findViewById(R.id.SelectMembersSaveButton);
        button.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();

                if(firebaseUser != null){
                    refresh();
                }
            }
        };

        usersReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("NewApi")
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                users.clear();

                for(QueryDocumentSnapshot snapshots : queryDocumentSnapshots){
                    String username = snapshots.getString("username");
                    String email = snapshots.getString("email");
                    String imageUri = snapshots.getString("imageUri");
                    String userId = snapshots.getString("userId");
                    String status = snapshots.getString("status");

                    if(!groupMembers.contains(userId)){
                        UserData userData = new UserData(username, email, imageUri, userId, status);
                        users.add(userData);
                        recyclerViewSelectAdapter.notifyDataSetChanged();
                    }
                }


            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.SelectMembersSaveButton:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(isTrue){
            Intent intent = new Intent(SelectMembersActivity.this, ChatActivity.class);
            intent.putExtra("from", true);
            intent.putExtra("recevierName", recevierName);
            intent.putExtra("recevierEmail", recevierEmail);
            intent.putExtra("recevierImageUri", recevierImageUri);
            intent.putExtra("recevierId", recevierId);
            intent.putExtra("recevierStatus", recevierStatus);
            intent.putExtra("title", title);
            intent.putExtra("groupId", groupId);
            intent.putExtra("members", members);
            startActivity(intent);
        }else{
            startActivity(new Intent(SelectMembersActivity.this, CreateGroupActivity.class));
        }
        finish();
    }

    public void refresh(){
        usersReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                users.clear();

                for(QueryDocumentSnapshot snapshots : queryDocumentSnapshots){
                    String username = snapshots.getString("username");
                    String email = snapshots.getString("email");
                    String imageUri = snapshots.getString("imageUri");
                    String userId = snapshots.getString("userId");
                    String status = snapshots.getString("status");

                    if(!groupMembers.contains(userId)){

                        UserData userData = new UserData(username, email, imageUri, userId, status);
                        users.add(userData);
                        recyclerViewSelectAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}