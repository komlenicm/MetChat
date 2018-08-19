package com.metropolitan.milos.metchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private ProgressDialog progressDialog;

    private EditText etStatusChange;
    private Button btnSaveStatusChange;

    private DatabaseReference changeStatusReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = (Toolbar) findViewById(R.id.statusAppBar);
        etStatusChange = (EditText) findViewById(R.id.etStatusChange);
        btnSaveStatusChange = (Button) findViewById(R.id.btnSaveStatusChange);
        progressDialog = new ProgressDialog(this);

        String oldStatus = getIntent().getExtras().get("user_status").toString();
        etStatusChange.setHint(oldStatus);

        mAuth = FirebaseAuth.getInstance();
        String userUID= mAuth.getCurrentUser().getUid();
        changeStatusReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userUID);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnSaveStatusChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newStatus = etStatusChange.getText().toString();

                ChangeProfileStatus(newStatus);
            }
        });

    }

    private void ChangeProfileStatus(String newStatus) {

        if(TextUtils.isEmpty(newStatus)){

            Toast.makeText(StatusActivity.this,"Morate uneti novi status.",Toast.LENGTH_LONG).show();

        }else{

            progressDialog.setTitle("Ažuriranje statusa");
            progressDialog.setMessage("Molimo Vas, sačekajte...");
            progressDialog.show();

            changeStatusReference.child("user_status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){

                        progressDialog.dismiss();

                        Intent settingsActivityIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                        startActivity(settingsActivityIntent);

                        Toast.makeText(StatusActivity.this, "Status je uspešno ažuriran.", Toast.LENGTH_SHORT).show();

                    }else{

                        Toast.makeText(StatusActivity.this, "Došlo je do greške prilikom ažuriranja statusa.", Toast.LENGTH_SHORT).show();

                    }

                }
            });



        }



    }
}
