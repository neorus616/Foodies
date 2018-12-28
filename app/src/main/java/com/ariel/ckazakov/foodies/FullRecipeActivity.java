package com.ariel.ckazakov.foodies;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class FullRecipeActivity extends AppCompatActivity {

    private ImageView fullRecipeImage;
    private TextView fullRecipe;
    private Button deleteButton, editButton;

    private String postKey, currentUserUid, dbUserUid, recipe, image;

    private DatabaseReference fullrecipedb;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_recipe);

        postKey = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("postKey")).toString();
        fullrecipedb = FirebaseDatabase.getInstance().getReference().child("Recipes").child(postKey);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserUid = firebaseAuth.getCurrentUser().getUid();

        fullRecipeImage = findViewById(R.id.fullRecipeImage);
        fullRecipe = findViewById(R.id.fullRecipe);
        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setVisibility(View.INVISIBLE);
        editButton = findViewById(R.id.editButton);
        editButton.setVisibility(View.INVISIBLE);

        fullrecipedb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    recipe = Objects.requireNonNull(dataSnapshot.child("recipe").getValue()).toString();
                    image = Objects.requireNonNull(dataSnapshot.child("recipeimage").getValue()).toString();
                    dbUserUid = dataSnapshot.child("uid").getValue().toString();

                    fullRecipe.setText(recipe);
                    Picasso.get().load(image).into(fullRecipeImage);
                    if (currentUserUid.equals(dbUserUid)) {
                        deleteButton.setVisibility(View.VISIBLE);
                        editButton.setVisibility(View.VISIBLE);
                    }

                    editButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditPost(recipe);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeletePost();
            }
        });
    }

    private void EditPost(String recipe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FullRecipeActivity.this);
        builder.setTitle("Edit Post:");

        final EditText newRecipe = new EditText(FullRecipeActivity.this);
        newRecipe.setText(recipe);
        builder.setView(newRecipe);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fullrecipedb.child("recipe").setValue(newRecipe.getText().toString());
                Toast.makeText(FullRecipeActivity.this, "Recipe Updated!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_blue_dark);
    }

    private void DeletePost() {
        fullrecipedb.removeValue();
        sendUserToMainActivity();
        Toast.makeText(this, "Post has been deleted", Toast.LENGTH_SHORT).show();
    }

    private void sendUserToMainActivity() {
        Intent mainActivity = new Intent(FullRecipeActivity.this, MainActivity.class);
        startActivity(mainActivity);
    }
}
