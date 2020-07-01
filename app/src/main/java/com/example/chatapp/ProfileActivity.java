package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.auth.AuthResult;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    CurrentUser currentUser;
    CircleImageView imageView;
    ImageButton imageButton;
    ImageButton removeImageButton;
    TextInputLayout usernameLayout;
    TextInputLayout statusLayout;
    EditText usernameEditText;
    EditText statusEditText;
    ProgressBar progressBar;
    AppCompatButton saveButton;
    Uri imageUri;
    String URI;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser firebaseUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userReference = db.collection("User");
    StorageReference storage = FirebaseStorage.getInstance().getReference();
    String key;
    RequestOptions requestOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
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
        imageView = findViewById(R.id.profileImageView);
        imageButton = findViewById(R.id.profileAddPhotoButton);
        imageButton.setOnClickListener(this);
        removeImageButton = findViewById(R.id.profileRemovePhotoButton);
        removeImageButton.setOnClickListener(this);
        usernameLayout = findViewById(R.id.profileUsernameLayout);
        statusLayout = findViewById(R.id.profileStatusLayout);
        usernameEditText = findViewById(R.id.profileUsernameText);
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

        statusEditText = findViewById(R.id.profileStatusText);
        progressBar = findViewById(R.id.progressBar);
        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);
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
                                            usernameEditText.setText(currentUser.getUsername());

                                            if(currentUser.getStatus() != null && !currentUser.getStatus().isEmpty()){
                                                statusEditText.setText(currentUser.getStatus());
                                            }

                                            key = snapshot.getId();
                                            URI = currentUser.getImageUri();
                                            requestOptions = new RequestOptions();
                                            requestOptions.fitCenter();
                                            Glide.with(getApplicationContext()).applyDefaultRequestOptions(requestOptions).load(currentUser.getImageUri()).into(imageView);
                                        }
                                    }
                                }
                            });
                }else{

                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProfileActivity.this, ChatsActivity.class));
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
            case R.id.profileAddPhotoButton:
                this.addPhoto();
                break;
            case R.id.saveButton:
                this.save();
                break;
            case R.id.profileRemovePhotoButton:
                imageUri = null;
                Glide.with(getApplicationContext()).applyDefaultRequestOptions(requestOptions).load("https://firebasestorage.googleapis.com/v0/b/chatapp-8e9bc.appspot.com/o/Images%2Fmanicon.png?alt=media&token=ced165e8-15ea-42f9-ac2f-5bc161731a50")
                        .into(imageView);
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
            final StorageReference imageReference =  storage.child("Images").child("images" + Timestamp.now().getSeconds());
            imageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(getApplicationContext()).applyDefaultRequestOptions(requestOptions).load(uri.toString()).into(imageView);
                            URI = uri.toString();
                            imageUri = null;
                        }
                    });
                }
            });
        }
    }

    public void save(){
        final String username = usernameEditText.getText().toString();
        final String status = statusEditText.getText().toString();

        if(username.length() < 3){
            usernameLayout.setErrorEnabled(true);
            usernameLayout.setError("Username must be of minimum 3 characters");
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

                                                userReference.document(key).update("username",
                                                        usernameEditText.getText().toString(),
                                                        "status", statusEditText.getText().toString(),
                                                        "imageUri", uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(ProfileActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(ProfileActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {

                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(ProfileActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                userReference.document(key).update("username", usernameEditText.getText().toString(),
                        "status", statusEditText.getText().toString(),
                        "imageUri", URI)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid){
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ProfileActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }else{
            usernameLayout.setErrorEnabled(true);
        }
    }
}