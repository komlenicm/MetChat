package com.metropolitan.milos.metchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private RoundedImageView roundedImageView;
    private TextView tvName;
    private TextView tvProfileStatus;

    private Button btnChangeImage;
    private Button btnChangeStatus;

    private final static int Gallery_Pick = 1;

    private StorageReference storeUserProfileImageReference;

    private DatabaseReference getUserDataReference;

    private StorageReference thumbImageReference;

    private FirebaseAuth mAuth;

    private CropImageView cropImageView;

    private Bitmap thumb_bitmap = null;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        String currentOnlineUserUID = mAuth.getCurrentUser().getUid();
        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentOnlineUserUID);
        getUserDataReference.keepSynced(true);
        storeUserProfileImageReference = FirebaseStorage.getInstance().getReference().child("Profile_Images");
        thumbImageReference = FirebaseStorage.getInstance().getReference().child("Thumb_Images");
        progressDialog = new ProgressDialog(this);

        roundedImageView = (RoundedImageView) findViewById(R.id.roundedImageView);
        tvName = (TextView) findViewById(R.id.tvName);
        tvProfileStatus = (TextView) findViewById(R.id.tvProfileStatus);

        btnChangeImage = (Button) findViewById(R.id.btnChangeImage);
        btnChangeStatus = (Button) findViewById(R.id.btnChangeStatus);


        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                final String image = dataSnapshot.child("user_image").getValue().toString();
                String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                tvName.setText(name);
                tvProfileStatus.setText(status);

                if(!image.equals("default_profile")){


                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile).into(roundedImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).into(roundedImageView);

                        }
                    });
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);

            }
        });

        btnChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String oldStatus = tvProfileStatus.getText().toString();

                Intent statusActivityIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusActivityIntent.putExtra("user_status", oldStatus);
                startActivity(statusActivityIntent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Gallery_Pick && resultCode == RESULT_OK){

            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog.setTitle("Ažuriranje profilne slike");
                progressDialog.setMessage("Molimo Vas, sačekate...");
                progressDialog.show();

                Uri resultUri = result.getUri();

                File thumbFilePathUri = new File(resultUri.getPath());

                String userUID = mAuth.getCurrentUser().getUid();


                try{
                    thumb_bitmap = new Compressor(this).setMaxWidth(200).setMaxHeight(200).setQuality(50).compressToBitmap(thumbFilePathUri);


                }catch (IOException e){
                    e.printStackTrace();

                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();


                StorageReference filePath = storeUserProfileImageReference.child(userUID + ".jpg");
                final StorageReference thumbFilePath = thumbImageReference.child(userUID + ".jpg");



                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Ažuriranje profilne slike.",Toast.LENGTH_LONG).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumbFilePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    String thumbDownloadUrl = task.getResult().getDownloadUrl().toString();

                                    if(task.isSuccessful()){
                                        Map updateUserData = new HashMap();
                                        updateUserData.put("user_image", downloadUrl);
                                        updateUserData.put("user_thumb_image", thumbDownloadUrl);

                                        getUserDataReference.updateChildren(updateUserData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                Toast.makeText(SettingsActivity.this, "Profilna slika je uspešno ažurirana.",Toast.LENGTH_SHORT).show();

                                                progressDialog.dismiss();

                                            }
                                        });

                                    }

                                }
                            });



                        }else{
                            Toast.makeText(SettingsActivity.this, "Greška prilikom ažuriranja profilne slike",Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
