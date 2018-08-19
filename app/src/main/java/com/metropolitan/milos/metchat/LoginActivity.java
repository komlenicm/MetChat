package com.metropolitan.milos.metchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private EditText etLoginEmail, etLoginPassword;

    private Button btnPrijaviSe;

    private FirebaseAuth mAuth;

    private ProgressDialog progressDialog;

    private DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mToolbar = (Toolbar) findViewById(R.id.toolbarLoginPage);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Prijavljivanje");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        progressDialog = new ProgressDialog(this);

        etLoginEmail = (EditText) findViewById(R.id.etLoginEmail);
        etLoginPassword = (EditText) findViewById(R.id.etLoginPassword);
        btnPrijaviSe = (Button) findViewById(R.id.btnPrijaviSe);


        btnPrijaviSe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etLoginEmail.getText().toString();
                String password = etLoginPassword.getText().toString();

                LoginUser(email, password);
            }
        });

    }

    private void LoginUser(String email, String password) {

        if(TextUtils.isEmpty(email)){
            Toast.makeText(LoginActivity.this, "Morate uneti email.", Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(LoginActivity.this, "Morate uneti lozinku.", Toast.LENGTH_LONG).show();
        }
        else{
            progressDialog.setTitle("Prijavljivanje u toku");
            progressDialog.setMessage("Molimo Vas, sačekajte...");
            progressDialog.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        String onlineUserUID = mAuth.getCurrentUser().getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        usersReference.child(onlineUserUID).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Intent mainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
                                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainActivityIntent);
                                finish();
                            }
                        });



                    }else{
                        Toast.makeText(LoginActivity.this, "Uneti podaci nisu tačni.", Toast.LENGTH_LONG).show();
                    }

                    progressDialog.dismiss();
                }
            });

        }


    }
}
