package com.ariel.ckazakov.foodies;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView fullname;
    private CircleImageView profilePic;

    private DatabaseReference userRef;
    private FirebaseAuth firebaseAuth;

    private String currentUserUid, userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fullname = findViewById(R.id.profile_fullname_public);
        profilePic = findViewById(R.id.profile_pic_public);

        if (getIntent().getExtras() != null)
            userKey = Objects.requireNonNull(getIntent().getExtras().get("userKey")).toString();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserUid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        if (userKey != null)
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userKey);
        else
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String firstname = Objects.requireNonNull(dataSnapshot.child("firstname").getValue()).toString();
                    String lastname = Objects.requireNonNull(dataSnapshot.child("lastname").getValue()).toString();
                    String profileImage = Objects.requireNonNull(dataSnapshot.child("profileimage").getValue()).toString();

                    fullname.setText(String.format("%s %s", firstname, lastname));
                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(profilePic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
