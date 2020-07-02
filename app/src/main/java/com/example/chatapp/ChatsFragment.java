package com.example.chatapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;

public class ChatsFragment extends Fragment {
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<UserData> users = new ArrayList<>();
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser firebaseUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference usersReference = db.collection("User");
    CollectionReference messageReferences = db.collection("Messages");
    CollectionReference groupReference = db.collection("Group");

    public ChatsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerView = view.findViewById(R.id.chatsRecyclerView);
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recyclerViewAdapter);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    public void refresh(){
        users.clear();
        usersReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @SuppressLint("NewApi")
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot snapshots : queryDocumentSnapshots){
                    final String username = snapshots.getString("username");
                    final String email = snapshots.getString("email");
                    final String imageUri = snapshots.getString("imageUri");
                    final String userId = snapshots.getString("userId");
                    final String status = snapshots.getString("status");

                    if(!userId.equals(firebaseUser.getUid().toString())){
                        messageReferences.orderBy("createdAt", Query.Direction.ASCENDING).get().addOnSuccessListener(
                                new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for(QueryDocumentSnapshot snapshots : queryDocumentSnapshots) {
                                            if ((snapshots.getString("userId").equals(firebaseUser.getUid().toString()) &&
                                                    snapshots.getString("recevierId").equals(userId)) ||
                                                    (snapshots.getString("recevierId").equals(firebaseUser.getUid().toString()) &&
                                                            snapshots.getString("userId").equals(userId))) {
                                                UserData userData = new UserData(username, email, imageUri, userId, status);
                                                userData.setTime(snapshots.getLong("createdAt"));
                                                users.add(userData);
                                                recyclerViewAdapter.notifyDataSetChanged();
                                                break;
                                            }
                                        }
                                    }
                                }
                        );
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

        groupReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @SuppressLint("NewApi")
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot snapshots : queryDocumentSnapshots){
                    final String title = snapshots.getString("title");
                    final String imageUri = snapshots.getString("imageUri");
                    final String groupId = snapshots.getId();
                    final ArrayList<String> members = (ArrayList<String>) snapshots.get("members");

                    if(members.contains(firebaseUser.getUid().toString())){
                        messageReferences.orderBy("createdAt", Query.Direction.ASCENDING).get().addOnSuccessListener(
                                new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for(QueryDocumentSnapshot snapshots : queryDocumentSnapshots) {
                                            if (groupId.equals(snapshots.getString("groupId"))) {
                                                UserData userData = new UserData();
                                                userData.setTime(snapshots.getLong("createdAt"));
                                                userData.setTitle(title);
                                                userData.setImageUri(imageUri);
                                                userData.setGroupId(groupId);
                                                userData.setMembers(members);
                                                users.add(userData);
                                                recyclerViewAdapter.notifyDataSetChanged();
                                                break;
                                            }
                                        }
                                    }
                                }
                        );
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
