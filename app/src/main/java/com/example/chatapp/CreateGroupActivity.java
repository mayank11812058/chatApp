package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    CurrentUser currentUser;
    CircleImageView imageView;
    ImageButton imageButton;
    ImageButton removeImageButton;
    TextInputLayout usernameLayout;
    EditText usernameEditText;
    ProgressBar progressBar;
    Button addMembersButton;
    AppCompatButton saveButton;
    Uri imageUri;
    String URI = "https://firebasestorage.googleapis.com/v0/b/chatapp-8e9bc.appspot.com/o/Images%2Fmanicon.png?alt=media&token=ced165e8-15ea-42f9-ac2f-5bc161731a50";
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser firebaseUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference groupReference = db.collection("Group");
    StorageReference storage = FirebaseStorage.getInstance().getReference();
    String key;
    RequestOptions requestOptions;
    ArrayList<String> members = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        toolbar = findViewById(R.id.createGroupToolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.backbtn));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        imageView = findViewById(R.id.createGroupImageView);

        if(SharedClass.imageUri != null){
            imageView.setImageURI(SharedClass.imageUri);
            SharedClass.imageUri = null;
        }

        imageButton = findViewById(R.id.createGroupAddPhotoButton);
        imageButton.setOnClickListener(this);
        removeImageButton = findViewById(R.id.createGroupRemovePhotoButton);
        removeImageButton.setOnClickListener(this);
        usernameLayout = findViewById(R.id.createGroupUsernameLayout);
        usernameEditText = findViewById(R.id.createGroupUsernameText);

        if(SharedClass.title != null){
            usernameEditText.setText(SharedClass.title);
            SharedClass.title = null;
        }

        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() >= 3){
                    usernameLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        progressBar = findViewById(R.id.progressBar);
        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);
        addMembersButton = findViewById(R.id.createGroupSelect);
        addMembersButton.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();

                if(firebaseUser != null){
                    members.clear();
                    members = SharedClass.selected;
                    members.add(firebaseUser.getUid().toString());
                }else{

                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(CreateGroupActivity.this, ChatsActivity.class));
        finish();
    }

    protected void onStart() {
        super.onStart();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.createGroupAddPhotoButton:
                this.addPhoto();
                break;
            case R.id.saveButton:
                this.save();
                break;
            case R.id.createGroupRemovePhotoButton:
                imageUri = null;
                Glide.with(getApplicationContext()).applyDefaultRequestOptions(requestOptions).load("https://firebasestorage.googleapis.com/v0/b/chatapp-8e9bc.appspot.com/o/Images%2Fmanicon.png?alt=media&token=ced165e8-15ea-42f9-ac2f-5bc161731a50")
                        .into(imageView);
                break;
            case R.id.createGroupSelect:
                SharedClass.title = usernameEditText.getText().toString();
                SharedClass.imageUri = imageUri;
                SharedClass.selected.add(firebaseUser.getUid().toString());
                startActivity(new Intent(CreateGroupActivity.this, SelectMembersActivity.class));
                break;
        }
    }

    public void addPhoto(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    public void save(){
        final String username = usernameEditText.getText().toString();

        if(username.length() < 3){
            usernameLayout.setErrorEnabled(true);
            usernameLayout.setError("Group name must be of minimum 3 characters");
        }

        if(!usernameLayout.isErrorEnabled()){
            progressBar.setVisibility(View.VISIBLE);
            final StorageReference imageReference =  storage.child("Images").child("images" + Timestamp.now().getSeconds());

            if(imageUri != null) {
                imageReference.putFile(imageUri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                imageReference.getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                            GroupData groupData = new GroupData();
                                            groupData.setTitle(usernameEditText.getText().toString());
                                            groupData.setImageUri(uri.toString());
                                            groupData.setMembers(members);

                                            groupReference.add(groupData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    startActivity(new Intent(CreateGroupActivity.this, ChatsActivity.class));
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(CreateGroupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {

                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(CreateGroupActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateGroupActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                GroupData groupData = new GroupData();
                groupData.setTitle(usernameEditText.getText().toString());
                groupData.setImageUri(URI);
                groupData.setMembers(members);

                groupReference.add(groupData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        startActivity(new Intent(CreateGroupActivity.this, ChatsActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CreateGroupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }else{
            usernameLayout.setErrorEnabled(true);
        }
    }
}