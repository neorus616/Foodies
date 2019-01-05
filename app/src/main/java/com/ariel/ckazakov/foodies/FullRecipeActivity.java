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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class FullRecipeActivity extends AppCompatActivity {

    private ImageView fullRecipeImage;
    private TextView fullRecipe, listIngredients;
    private Button deleteButton, editButton;

    private String postKey, currentUserUid, dbUserUid, recipe, image;
    private List<String> listElementsArrayList;

    private DatabaseReference fullrecipedb, adminRef;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_recipe);

        postKey = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("postKey")).toString();
        fullrecipedb = FirebaseDatabase.getInstance().getReference().child("Recipes").child(postKey);
        firebaseAuth = FirebaseAuth.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            currentUserUid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        else
            currentUserUid = "null";
        adminRef = FirebaseDatabase.getInstance().getReference().child("Admins");

        fullRecipeImage = findViewById(R.id.fullRecipeImage);
        fullRecipe = findViewById(R.id.fullRecipe);
        listIngredients = findViewById(R.id.listFullIngredients);
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
                    dbUserUid = Objects.requireNonNull(dataSnapshot.child("uid").getValue()).toString();
                    GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                    };
                    listElementsArrayList = dataSnapshot.child("ingredients").getValue(t);

                    StringBuilder ingredients = new StringBuilder();
                    if (listElementsArrayList != null) {
                        for (int i = 0; i < listElementsArrayList.size() - 1; i++)
                            ingredients.append(listElementsArrayList.get(i)).append("\n");
                        ingredients.append(listElementsArrayList.get(listElementsArrayList.size() - 1));
                    }
                    listIngredients.setText(ingredients);
                    fullRecipe.setText(recipe);
                    Picasso.get().load(image).into(fullRecipeImage);

                    adminRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(currentUserUid).exists()) {
                                deleteButton.setVisibility(View.VISIBLE);
                                editButton.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

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
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.holo_blue_dark);
    }

    private void DeletePost() {
        fullrecipedb.removeValue();
        SendUserToMainActivity();
        Toast.makeText(this, "Post has been deleted", Toast.LENGTH_SHORT).show();
    }

    private void SendUserToMainActivity() {
        Intent mainActivity = new Intent(FullRecipeActivity.this, MainActivity.class);
        startActivity(mainActivity);
    }
}
