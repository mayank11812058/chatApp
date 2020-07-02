package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Comparator;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    RecyclerView recyclerView;
    RecyclerViewChatAdapter recyclerViewChatAdapter;
    ArrayList<Message> messagesList = new ArrayList<>();
    EditText editText;
    ImageButton sendButton;
    ImageButton galleryButton;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userReference = db.collection("User");
    CollectionReference messageReference = db.collection("Messages");
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    String recevierName, recevierId, recevierEmail, recevierImageUri, recevierStatus, groupId, title;
    ArrayList<String> members;
    Uri imageUri;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuProfile:
                Intent intent = new Intent(ChatActivity.this, ContactProfile.class);
                intent.putExtra("recevierName", recevierName);
                intent.putExtra("recevierEmail", recevierEmail);
                intent.putExtra("recevierId", recevierId);
                intent.putExtra("recevierImageUri", recevierImageUri);
                intent.putExtra("recevierStatus", recevierStatus);
                intent.putExtra("groupId", groupId);
                intent.putExtra("title", title);
                intent.putExtra("members", members);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        recevierName = intent.getStringExtra("recevierName");
        recevierId = intent.getStringExtra("recevierId");
        recevierEmail = intent.getStringExtra("recevierEmail");
        recevierImageUri = intent.getStringExtra("recevierImageUri");
        recevierStatus = intent.getStringExtra("recevierStatus");
        groupId = intent.getStringExtra("groupId");
        title = intent.getStringExtra("title");
        members = intent.getStringArrayListExtra("members");
        toolbar = findViewById(R.id.chatToolbar);

        if(recevierName != null) {
            toolbar.setTitle(recevierName);
        }else{
            toolbar.setTitle(title);
        }

        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.backbtn));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        recyclerView = findViewById(R.id.messagesRecyclerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerViewChatAdapter = new RecyclerViewChatAdapter(getApplicationContext(), messagesList);
        recyclerView.setAdapter(recyclerViewChatAdapter);

        editText = findViewById(R.id.currentMessage);
        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);
        galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        messageReference.orderBy("createdAt", Query.Direction.ASCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                messagesList.clear();

                for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                    if(recevierId != null){
                        if(snapshot.getString("userId").equals(firebaseUser.getUid().toString()) && recevierId.equals(snapshot.getString("recevierId")) ||
                            (snapshot.getString("userId").equals(recevierId) && snapshot.getString("recevierId").equals(firebaseUser.getUid().toString()))){
                            Message tempMessage = new Message();
                            tempMessage.setUserId(snapshot.getString("userId"));
                            tempMessage.setRecevierId(snapshot.getString("recevierId"));
                            tempMessage.setTextMessage(snapshot.getString("textMessage"));
                            tempMessage.setImageUri(snapshot.getString("imageUri"));
                            tempMessage.setCreatedAt(snapshot.getLong("createdAt"));
                            messagesList.add(tempMessage);
                            recyclerViewChatAdapter.notifyDataSetChanged();
                        }
                    }

                    if(recevierId == null && groupId.equals(snapshot.getString("groupId"))){
                        Message tempMessage = new Message();
                        tempMessage.setUserId(snapshot.getString("userId"));
                        tempMessage.setGroupId(snapshot.getString("groupId"));
                        tempMessage.setTextMessage(snapshot.getString("textMessage"));
                        tempMessage.setImageUri(snapshot.getString("imageUri"));
                        tempMessage.setCreatedAt(snapshot.getLong("createdAt"));
                        messagesList.add(tempMessage);
                        recyclerViewChatAdapter.notifyDataSetChanged();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        messageReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("NewApi")
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                messagesList.clear();

                for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                    if(recevierId != null){
                        if(snapshot.getString("userId").equals(firebaseUser.getUid().toString()) && recevierId.equals(snapshot.getString("recevierId")) ||
                            (snapshot.getString("userId").equals(recevierId) && snapshot.getString("recevierId").equals(firebaseUser.getUid().toString()))){
                            Message tempMessage = new Message();
                            tempMessage.setUserId(snapshot.getString("userId"));
                            tempMessage.setRecevierId(snapshot.getString("recevierId"));
                            tempMessage.setTextMessage(snapshot.getString("textMessage"));
                            tempMessage.setImageUri(snapshot.getString("imageUri"));
                            tempMessage.setCreatedAt(snapshot.getLong("createdAt"));
                            messagesList.add(tempMessage);
                            recyclerViewChatAdapter.notifyDataSetChanged();
                        }
                    }

                    if(recevierId == null && groupId.equals(snapshot.getString("groupId"))){
                        Message tempMessage = new Message();
                        tempMessage.setUserId(snapshot.getString("userId"));
                        tempMessage.setGroupId(snapshot.getString("groupId"));
                        tempMessage.setTextMessage(snapshot.getString("textMessage"));
                        tempMessage.setImageUri(snapshot.getString("imageUri"));
                        tempMessage.setCreatedAt(snapshot.getLong("createdAt"));
                        messagesList.add(tempMessage);
                        recyclerViewChatAdapter.notifyDataSetChanged();
                    }
                }
                messagesList.sort(new Comparator<Message>() {
                    @Override
                    public int compare(Message o1, Message o2) {
                        return (int)(o1.createdAt - o2.createdAt);
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ChatActivity.this, ChatsActivity.class));
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sendButton:
                sendMessage();
                break;
            case R.id.galleryButton:
                chooseImage();
                break;
            default:
        }
    }

    public void sendMessage(){
        final Message message = new Message();
        message.setUserId(firebaseUser.getUid().toString());

        if(recevierId != null) {
            message.setRecevierId(recevierId);
        }else{
            message.setGroupId(groupId);
        }
        message.setTextMessage(editText.getText().toString());

        if(imageUri != null) {
            final StorageReference imageReference = storageReference.child("Images").child("images" + String.valueOf(Timestamp.now().getSeconds()));
            imageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            message.setImageUri(uri.toString());
                            messageReference.add(message)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
//                                            messagesList.add(message);
//                                            recyclerViewChatAdapter.notifyDataSetChanged();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                        }
                    });
                }
            });
            imageUri = null;
        }else{
            message.setImageUri("");
            messageReference.add(message)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
//                            messagesList.add(message);
//                            recyclerViewChatAdapter.notifyDataSetChanged();
                            editText.setText("");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    public void chooseImage(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();

            final AlertDialog alertDialog;
            final AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            builder.setTitle("Are you sure?")
                    .setMessage("Do you want to send this image?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendMessage();
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
}