package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.TimeUnit;

public class ChatsActivity extends AppCompatActivity implements View.OnClickListener {
    androidx.appcompat.widget.Toolbar toolbar;
    FloatingActionButton contactsButton;
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chats_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menuProfile:
                startActivity(new Intent(ChatsActivity.this, ProfileActivity.class));
                return true;
            case R.id.menuLogout:
                firebaseAuth.signOut();
                startActivity(new Intent(ChatsActivity.this, LoginActivity.class));
                finish();
                return true;
            case R.id.menuRefresh:
                progressBar.setVisibility(View.VISIBLE);
                viewPagerAdapter.clear();
                viewPagerAdapter.addItem(new ChatsFragment(), "Chats");
                viewPagerAdapter.addItem(new UsersFragment(), "Status");
                viewPagerAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                return true;
            default:
                return false;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        toolbar = findViewById(R.id.chatsToolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        contactsButton = findViewById(R.id.contactsButton);
        contactsButton.setOnClickListener(this);
        progressBar = findViewById(R.id.progressBar);
        tabLayout = findViewById(R.id.chatsTabLayout);
        viewPager = findViewById(R.id.chatsViewPager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addItem(new ChatsFragment(), "Chats");
        viewPagerAdapter.addItem(new UsersFragment(), "Status");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.contactsButton:
                startActivity(new Intent(ChatsActivity.this, ContactsActivity.class));
                break;
        }
    }
}