package com.example.chatapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class UsersFragment extends Fragment implements View.OnClickListener {
    CircleImageView circleImageView;
    TextView textView;
    RecyclerView recyclerView;
    RecyclerViewStatusAdapter recyclerViewStatusAdapter;
    ArrayList<UserData> users = new ArrayList<>();
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser firebaseUser;
    StorageReference storage = FirebaseStorage.getInstance().getReference();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userReference = db.collection("User");
    CurrentUser currentUser;
    String key;
    ArrayList<String> userImages = new ArrayList<>();

    public UsersFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        circleImageView = view.findViewById(R.id.statusImageView);
        circleImageView.setOnClickListener(this);
        textView = view.findViewById(R.id.statusNameTextView);
        textView.setOnClickListener(this);
        recyclerView = view.findViewById(R.id.statusRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewStatusAdapter = new RecyclerViewStatusAdapter(getContext(), users);
        recyclerView.setAdapter(recyclerViewStatusAdapter);
        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();

                if(firebaseUser != null){
                    userReference.whereEqualTo("userId", firebaseUser.getUid().toString())
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                    if(e == null){
                                        for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                                            currentUser = new CurrentUser(snapshot.getString("username"), snapshot.getString("imageUri"),
                                                    snapshot.getString("email"), snapshot.getString("userId"), snapshot.getString("status"));

                                            key = snapshot.getId();
                                            userImages = (ArrayList<String>) snapshot.get("ImageStatus");
                                        }
                                    }
                                }
                            });

                    userReference.orderBy("time", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            users.clear();

                            for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                                if(!snapshot.getString("userId").equals(firebaseUser.getUid().toString()) &&
                                        ((ArrayList<String>)snapshot.get("ImageStatus")).size() > 0){
                                    UserData userData = new UserData(snapshot.getString("username"),
                                                                     snapshot.getString("email"),
                                                                     snapshot.getString("imageUri"),
                                                                     snapshot.getString("userId"),
                                                                     snapshot.getString("status"));

                                    userData.setImageStatus((ArrayList<String>) snapshot.get("ImageStatus"));
                                    users.add(userData);
                                    recyclerViewStatusAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
                }else{

                }
            }
        };

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.statusImageView:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
                break;
            case R.id.statusNameTextView:
                if(userImages.size() > 0) {
                    Intent viewIntent = new Intent(getContext(), StatusView.class);
                    viewIntent.putExtra("Images", userImages);
                    viewIntent.putExtra("userId", currentUser.getUserId().toString());
                    viewIntent.putExtra("key", key);
                    startActivity(viewIntent);
                }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            final Uri imageUri = data.getData();
            final AlertDialog alertDialog;
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Are you sure?")
                    .setMessage("Do you want to add this image in your status?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            uploadStatus(imageUri);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public void uploadStatus(Uri imageUri){
        final StorageReference imageReference =  storage.child("Images").child("images" + Timestamp.now().getSeconds());
        imageReference.putFile(imageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        imageReference.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        userImages.add(uri.toString());
                                        userReference.document(key).update("ImageStatus", userImages, "time", Timestamp.now().getSeconds())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {

                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Try Again", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Try Again", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
