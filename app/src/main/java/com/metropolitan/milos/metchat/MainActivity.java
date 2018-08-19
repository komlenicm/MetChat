package com.metropolitan.milos.metchat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private TabsPagerAdapter tabsPagerAdapter;

    private FirebaseUser currentUser;
    private DatabaseReference databaseUsersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if(currentUser != null){

            String onlineUserUID = mAuth.getCurrentUser().getUid();
            databaseUsersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(onlineUserUID);
        }

        //Tabs for MainActivity
        viewPager = (ViewPager) findViewById(R.id.mainTabsPager);
        tabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsPagerAdapter);
        tabLayout = (TabLayout) findViewById(R.id.mainTabs);
        tabLayout.setupWithViewPager(viewPager);



        mToolbar = (Toolbar) findViewById(R.id.toolbarMainPage);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("MetChat");
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = mAuth.getCurrentUser();

        if(currentUser == null){

            LogoutUser();
        }else if(currentUser != null){

            databaseUsersReference.child("online").setValue("true");



        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(currentUser != null){

            databaseUsersReference.child("online").setValue(ServerValue.TIMESTAMP);

        }

    }

    private void LogoutUser() {
        Intent startPageIntent = new Intent(MainActivity.this,StartPageActivity.class);

        //Sprecava korisnika da prilikom pritiska na BACK dugme, da se vrati na MainActivity
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);


        if(item.getItemId() == R.id.btnSettings){
            Intent settingsActivityIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsActivityIntent);
        }

        if(item.getItemId() == R.id.btnAllUsers){
            Intent allUsersActivityIntent = new Intent(MainActivity.this, AllUsersActivity.class);
            startActivity(allUsersActivityIntent);
        }

        if(item.getItemId() == R.id.btnLokacijaUniverziteta){
            Intent mapsActivityIntent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(mapsActivityIntent);

        }


        if(item.getItemId() == R.id.btnLogoutMain){

            if(currentUser != null){
                databaseUsersReference.child("online").setValue(ServerValue.TIMESTAMP);
            }

            mAuth.signOut();

            LogoutUser();
        }


        return true;

    }
}
