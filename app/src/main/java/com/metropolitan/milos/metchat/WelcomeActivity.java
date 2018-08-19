package com.metropolitan.milos.metchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Thread thread = new Thread(){
            @Override
            public void run() {
                try{
                    sleep(5000);

                }catch (Exception e){
                    e.printStackTrace();

                }

                finally {
                    Intent mainActivityIntent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(mainActivityIntent);
                }
            }
        };

        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        finish();


    }
}
