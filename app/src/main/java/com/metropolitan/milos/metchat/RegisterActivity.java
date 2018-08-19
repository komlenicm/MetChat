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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference storeUserDefaultDataReference;

    private Toolbar mToolbar;
    private ProgressDialog progressDialog;

    private EditText etName, etEmail, etPassword;

    private Button btnRegistrujSe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();


        mToolbar = (Toolbar) findViewById(R.id.toolbarRegisterPage);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Registracija");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);

        progressDialog = new ProgressDialog(this);

        btnRegistrujSe = (Button) findViewById(R.id.btnRegistrujSe);

        btnRegistrujSe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String name = etName.getText().toString();
                final String email = etEmail.getText().toString();
                final String password = etPassword.getText().toString();
                
                RegisterAccount(name, email, password);

            }
        });


    }

    private void RegisterAccount(final String name, final String email, final String password) {
        if(TextUtils.isEmpty(name)){
            Toast.makeText(RegisterActivity.this, "Morate uneti ime", Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(email)){
            Toast.makeText(RegisterActivity.this, "Morate uneti email", Toast.LENGTH_LONG).show();
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(RegisterActivity.this, "Morate uneti lozinku", Toast.LENGTH_LONG).show();
        }
        else{

            progressDialog.setTitle("Registracija u toku");
            progressDialog.setMessage("Molimo Vas, sačekajte...");
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        String currentUserUID = mAuth.getCurrentUser().getUid();
                        storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUID);

                        storeUserDefaultDataReference.child("user_name").setValue(name);
                        storeUserDefaultDataReference.child("user_status").setValue("I ja koristim MetChat!");
                        storeUserDefaultDataReference.child("user_image").setValue("default_profile");
                        storeUserDefaultDataReference.child("device_token").setValue(deviceToken);
                        storeUserDefaultDataReference.child("user_thumb_image").setValue("default_image").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Intent mainActivityIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                    mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainActivityIntent);
                                    finish();
                                }

                            }
                        });

                    }else{

                        Toast.makeText(RegisterActivity.this, "Greška...Pokušajte ponovo.", Toast.LENGTH_LONG).show();
                    }

                    progressDialog.dismiss();

                }
            });

        }
    }
}
