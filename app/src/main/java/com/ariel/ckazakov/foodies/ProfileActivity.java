package com.ariel.ckazakov.foodies;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private Button followButton, unfollowButton;

    private DatabaseReference userRef, followRef;
    private FirebaseAuth firebaseAuth;

    private String currentUserUid, userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fullname = findViewById(R.id.profile_fullname_public);
        profilePic = findViewById(R.id.profile_pic_public);
        followButton = findViewById(R.id.follow_button);
        unfollowButton = findViewById(R.id.unfollow_button);

        if (getIntent().getExtras() != null)
            userKey = Objects.requireNonNull(getIntent().getExtras().get("userKey")).toString();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserUid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        /*
        other profiles
         */
        if (userKey != null && !userKey.equals(currentUserUid)) {
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userKey);
            followRef = FirebaseDatabase.getInstance().getReference().child("Follows");
            followRef.child(currentUserUid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                        if (dataSnapshot.child(userKey).child("Follow").getValue() != Boolean.TRUE) {
                            followButton.setVisibility(View.VISIBLE);
                            unfollowButton.setVisibility(View.INVISIBLE);
                        } else {
                            followButton.setVisibility(View.INVISIBLE);
                            unfollowButton.setVisibility(View.VISIBLE);
                        }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        /*
        my profile
         */
        } else {
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid);
            followButton.setVisibility(View.INVISIBLE);
            unfollowButton.setVisibility(View.INVISIBLE);
        }

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
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

        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followButton.setVisibility(View.INVISIBLE);
                FollowPerson();
            }
        });

        unfollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followButton.setVisibility(View.VISIBLE);
                UnfollowPerson();
            }
        });

    }

    private void UnfollowPerson() {
        followRef.child(currentUserUid).child(userKey).child("Follow").setValue(Boolean.FALSE).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                followRef.child(userKey).child(currentUserUid).child("Follower").setValue(Boolean.FALSE).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        unfollowButton.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }

    private void FollowPerson() {
        followRef.child(currentUserUid).child(userKey).child("Follow").setValue(Boolean.TRUE).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    followRef.child(userKey).child(currentUserUid).child("Follower").setValue(Boolean.TRUE).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            unfollowButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
    }
}
