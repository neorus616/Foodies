package com.ariel.ckazakov.foodies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileSetupActivity extends AppCompatActivity {

    private EditText firstName, lastName;
    private Button save;
    private CircleImageView profileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference profileRef;
    private StorageReference UserProfileImageRef;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        profileRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        firstName = findViewById(R.id.firstname);
        lastName = findViewById(R.id.lastname);
        save = findViewById(R.id.saveProfile);
        profileImage = findViewById(R.id.profile_image);
        loadingBar = new ProgressDialog(this);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, 1);
            }
        });

        profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = Objects.requireNonNull(dataSnapshot.child("profileimage").getValue()).toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                    } else {
                        Toast.makeText(ProfileSetupActivity.this, "Please select profile image first.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Cropping image");
                loadingBar.setMessage("Please wait, while we are cropping your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri = Objects.requireNonNull(result).getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileSetupActivity.this, "Profile image successfully stored in Firebase storage ...", Toast.LENGTH_SHORT).show();
                            Task<Uri> result = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getMetadata()).getReference()).getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();
                                    profileRef.child("profileimage").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent selfIntent = new Intent(ProfileSetupActivity.this, ProfileSetupActivity.class);
                                                        startActivity(selfIntent);
                                                        Toast.makeText(ProfileSetupActivity.this, "Profile image stored in Firebase Storage successfully ...", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(ProfileSetupActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                    loadingBar.dismiss();
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            } else {
                Toast.makeText(ProfileSetupActivity.this, "Error: The image has not been cut well. Try again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void saveProfile() {
        String firstname = firstName.getText().toString();
        String lastname = lastName.getText().toString();
        if (firstname.isEmpty()) {
            Toast.makeText(this, "Please write your first name...", Toast.LENGTH_SHORT).show();
        }
        if (lastname.isEmpty()) {
            Toast.makeText(this, "Please write your last name...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait, while we are creating your new Account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("firstname", firstname);
            userMap.put("lastname", lastname);
            profileRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        SendUserToMainActivity();
                        Toast.makeText(ProfileSetupActivity.this, "Your profile is updated Successfully.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ProfileSetupActivity.this, "Error Occurred: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    loadingBar.dismiss();
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ProfileSetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}