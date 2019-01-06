package com.ariel.ckazakov.foodies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ariel.ckazakov.models.Recipe;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private RecyclerView myPostList;

    private DatabaseReference recipeRef, userRef, likesRef;
    private FirebaseAuth firebaseAuth;
    private String currentUid, userKey;
    private Query myPostQuery;
    private Boolean likeChecker = Boolean.FALSE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        firebaseAuth = FirebaseAuth.getInstance();
        recipeRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        currentUid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        if (getIntent().getExtras() != null && getIntent().getExtras().get("userKey") != null) {
            userKey = Objects.requireNonNull(getIntent().getExtras().get("userKey")).toString();
            myPostQuery = recipeRef.orderByChild("uid").startAt(userKey).endAt(userKey + "\uf8ff");
        } else
            myPostQuery = recipeRef.orderByChild("uid").startAt(currentUid).endAt(currentUid + "\uf8ff");

        toolbar = findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(Boolean.TRUE);
        getSupportActionBar().setDisplayShowHomeEnabled(Boolean.TRUE);
        if (userKey == null)
            getSupportActionBar().setTitle("My Posts");
        else
            getSupportActionBar().setTitle("Posts");


        myPostList = findViewById(R.id.all_my_posts_list);
        myPostList.setHasFixedSize(Boolean.TRUE);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(Boolean.TRUE);
        linearLayoutManager.setStackFromEnd(Boolean.TRUE);
        myPostList.setLayoutManager(linearLayoutManager);

        DisplayMyAllPosts();
    }

    private void DisplayMyAllPosts() {
        FirebaseRecyclerOptions<Recipe> options = new FirebaseRecyclerOptions.Builder<Recipe>().setQuery(myPostQuery, Recipe.class).build();
        FirebaseRecyclerAdapter<Recipe, MyPostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Recipe, MyPostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyPostsActivity.MyPostsViewHolder holder, int position, @NonNull Recipe model) {
                final String postKey = getRef(position).getKey();

                holder.setFullname(model.getFullName());
                holder.setTime(String.format(" %s", model.getTime()));
                holder.setDate(String.format(" %s", model.getDate()));
                holder.setTitle(model.getTitle());
                holder.setRecipeImage(model.getRecipeImage());
                holder.setProfileImage(model.getProfileImage());

                holder.setLikeButtonStatus(postKey);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MyPostsActivity.this, FullRecipeActivity.class);
                        clickPostIntent.putExtra("postKey", postKey);
                        startActivity(clickPostIntent);
                    }
                });

                holder.commentRecipeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent(MyPostsActivity.this, CommentsActivity.class);
                        commentsIntent.putExtra("postKey", postKey);
                        startActivity(commentsIntent);
                    }
                });

                holder.likeRecipeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeChecker = Boolean.TRUE;
                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (likeChecker)
                                    if (dataSnapshot.child(Objects.requireNonNull(postKey)).hasChild(currentUid)) {
                                        likesRef.child(postKey).child(currentUid).removeValue();
                                        likeChecker = Boolean.FALSE;
                                    } else {
                                        likesRef.child(postKey).child(currentUid).setValue(Boolean.TRUE);
                                        likeChecker = Boolean.FALSE;
                                    }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }

            @NonNull
            @Override
            public MyPostsActivity.MyPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_recipes_layout, parent, false);
                return new MyPostsActivity.MyPostsViewHolder(view);
            }
        };
        myPostList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder {
        View view;

        ImageButton likeRecipeButton, commentRecipeButton;
        TextView numOfLikes;
        int countLikes;
        String currentUserUid;
        DatabaseReference likesRef;

        public MyPostsViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            likeRecipeButton = view.findViewById(R.id.likeButton);
            commentRecipeButton = view.findViewById(R.id.commentButton);
            numOfLikes = view.findViewById(R.id.display_num_likes);
            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        }

        public void setLikeButtonStatus(final String postKey) {
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild(currentUserUid)) {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likeRecipeButton.setImageResource(R.drawable.like);
                        numOfLikes.setText(String.format("%s Likes", String.valueOf(countLikes)));
                    } else {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likeRecipeButton.setImageResource(R.drawable.dislike);
                        numOfLikes.setText(String.format("%s Likes", String.valueOf(countLikes)));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setFullname(String fullname) {
            TextView username = itemView.findViewById(R.id.post_username);
            username.setText(fullname);
        }

        public void setTime(String time) {
            TextView recipeTime = itemView.findViewById(R.id.post_time);
            recipeTime.setText(time);
        }

        public void setDate(String date) {
            TextView recipeDate = itemView.findViewById(R.id.post_date);
            recipeDate.setText(date);
        }

        public void setTitle(String title) {
            TextView titlePost = itemView.findViewById(R.id.post_title);
            titlePost.setText(title);
        }

        public void setProfileImage(String profileimage) {
            CircleImageView image = itemView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(image);
        }

        public void setRecipeImage(String recipeimage) {
            ImageView recipeImage = itemView.findViewById(R.id.post_image);
            Picasso.get().load(recipeimage).placeholder(R.drawable.add_post_high).into(recipeImage);
        }
    }
}