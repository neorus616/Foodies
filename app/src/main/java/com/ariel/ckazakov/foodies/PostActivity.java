package com.ariel.ckazakov.foodies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    private ImageButton recipeImage;
    private Button button;
    private EditText recipe, title;

    private Toolbar toolbar;
    private ProgressDialog loadingBar;

    private StorageReference db;
    private DatabaseReference userRef, recipeRef;
    private FirebaseAuth firebaseAuth;
    private String downloadUrl, saveCurrentTime, saveCurrentDate;
    private long postCounter = 0;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        recipeImage = findViewById(R.id.recipeImage);
        button = findViewById(R.id.createRecipe);
        recipe = findViewById(R.id.recipe);
        title = findViewById(R.id.title);

        toolbar = findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(Boolean.TRUE);
        getSupportActionBar().setDisplayShowHomeEnabled(Boolean.TRUE);
        getSupportActionBar().setTitle("Update post");

        loadingBar = new ProgressDialog(this);

        db = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        recipeRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
        firebaseAuth = FirebaseAuth.getInstance();

        recipeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePic();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateRecipe();
            }
        });

    }

    private void validateRecipe() {

        if (imageUri == null)
            Toast.makeText(this, "You must upload an image of the recipe", Toast.LENGTH_SHORT).show();
        else if (recipe.getText().toString().isEmpty())
            Toast.makeText(this, "You must write the recipe", Toast.LENGTH_SHORT).show();
        else if (title.getText().toString().isEmpty())
            Toast.makeText(this, "You must write the title", Toast.LENGTH_SHORT).show();
        else {
            loadingBar.setTitle("Posting recipe");
            loadingBar.setMessage("Please wait while we validate your recipe..");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(Boolean.TRUE);
            saveRecipePicToDB();
        }
    }

    private void saveRecipePicToDB() {
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy", Locale.US);
        saveCurrentDate = currentDate.format(calendarDate.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm", Locale.US);
        saveCurrentTime = currentTime.format(calendarDate.getTime());

        final StorageReference path = db.child("Recipe Images")
                .child(saveCurrentDate + saveCurrentTime + imageUri.getLastPathSegment() + ".jpg");
        path.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return path.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    downloadUrl = Objects.requireNonNull(downloadUri).toString();
                    Toast.makeText(PostActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    SaveRecipeToDB();
                } else {
                    Toast.makeText(PostActivity.this, "upload failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SaveRecipeToDB() {
        recipeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    postCounter = dataSnapshot.getChildrenCount();
                } else postCounter = 0;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        userRef.child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String currentUserUid = firebaseAuth.getCurrentUser().getUid();
                    String userFullName = Objects.requireNonNull(dataSnapshot.child("firstname").getValue()).toString()
                            + " " + Objects.requireNonNull(dataSnapshot.child("lastname").getValue()).toString();
                    HashMap<String, Object> posts = new HashMap<>();
                    posts.put("uid", currentUserUid);
                    posts.put("recipeimage", downloadUrl);
                    posts.put("recipe", recipe.getText().toString());
                    posts.put("time", saveCurrentTime);
                    posts.put("date", saveCurrentDate);
                    posts.put("profileimage", Objects.requireNonNull(dataSnapshot.child("profileimage").getValue()).toString());
                    posts.put("fullname", userFullName);
                    posts.put("title", title.getText().toString());
                    posts.put("counter", postCounter);
                    recipeRef.child(currentUserUid + "" + saveCurrentDate + "" + saveCurrentTime).updateChildren(posts).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "Recipe updated successfully", Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(PostActivity.this, "Error occurred: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void choosePic() {
        Intent picIntent = new Intent();
        picIntent.setAction(Intent.ACTION_GET_CONTENT);
        picIntent.setType("image/*");
        startActivityForResult(picIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            recipeImage.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
            SendUserToMainActivity();

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainActivity = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainActivity);
    }
}
