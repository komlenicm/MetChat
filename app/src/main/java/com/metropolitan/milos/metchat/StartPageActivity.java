package com.metropolitan.milos.metchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartPageActivity extends AppCompatActivity {

    private Button btnPrijavljivanje;
    private Button btnRegistracija;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        btnPrijavljivanje = (Button) findViewById(R.id.btnPrijavljivanje);
        btnRegistracija = (Button) findViewById(R.id.btnRegistracija);


        btnPrijavljivanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginActivityIntent = new Intent(StartPageActivity.this, LoginActivity.class);
                startActivity(loginActivityIntent);
            }
        });

        btnRegistracija.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerActivityIntent = new Intent(StartPageActivity.this, RegisterActivity.class);
                startActivity(registerActivityIntent);
            }
        });
    }
}
