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
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;

public class ContactsActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<UserData> users = new ArrayList<>();
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser firebaseUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference usersReference = db.collection("User");
    CollectionReference groupReference = db.collection("Group");

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contacts_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menuRefresh:
                refresh();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        toolbar = findViewById(R.id.contactsToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.backbtn));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitleTextColor(Color.WHITE);
        recyclerView = findViewById(R.id.contactsRecyclerView);
        recyclerViewAdapter = new RecyclerViewAdapter(this, users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerViewAdapter);

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

//        usersReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @SuppressLint("NewApi")
//            @Override
//            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                users.clear();
//
//                for(QueryDocumentSnapshot snapshots : queryDocumentSnapshots){
//                    String username = snapshots.getString("username");
//                    String email = snapshots.getString("email");
//                    String imageUri = snapshots.getString("imageUri");
//                    String userId = snapshots.getString("userId");
//                    String status = snapshots.getString("status");
//
//                    if(!userId.equals(firebaseUser.getUid().toString())){
//
//                        UserData userData = new UserData(username, email, imageUri, userId, status);
//                        users.add(userData);
//                        recyclerViewAdapter.notifyDataSetChanged();
//                    }
//                }
//            }
//        });
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
        startActivity(new Intent(ContactsActivity.this, ChatsActivity.class));
        finish();
    }

    public void refresh(){
        users.clear();

        usersReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot snapshots : queryDocumentSnapshots){
                    String username = snapshots.getString("username");
                    String email = snapshots.getString("email");
                    String imageUri = snapshots.getString("imageUri");
                    String userId = snapshots.getString("userId");
                    String status = snapshots.getString("status");

                    if(!userId.equals(firebaseUser.getUid().toString())){

                        UserData userData = new UserData(username, email, imageUri, userId, status);
                        users.add(userData);
                        recyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        groupReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @SuppressLint("NewApi")
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot snapshots : queryDocumentSnapshots){
                    final String title = snapshots.getString("title");
                    final String imageUri = snapshots.getString("imageUri");
                    final String groupId = snapshots.getId();
                    ArrayList<String> members = (ArrayList<String>) snapshots.get("members");

                    if(members.contains(firebaseUser.getUid().toString())){
                        UserData userData = new UserData();
                        userData.setTitle(title);
                        userData.setImageUri(imageUri);
                        userData.setGroupId(groupId);
                        userData.setMembers(members);
                        users.add(userData);
                        recyclerViewAdapter.notifyDataSetChanged();
                    }
                }

                users.sort(new Comparator<UserData>() {
                    @Override
                    public int compare(UserData o1, UserData o2) {
                        return (int)(o2.getTime() - o1.getTime());
                    }
                });
                recyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }
}