package com.ariel.ckazakov.foodies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ariel.ckazakov.models.Recipe;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference recipeRef, userRef, likesRef;

    private CircleImageView navProfileImage;
    private TextView navProfileUser;
    private String currentUserID;
    private Boolean likeChecker = Boolean.FALSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        firebaseAuth = FirebaseAuth.getInstance();
        recipeRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        if (firebaseAuth.getCurrentUser() != null) {
            setContentView(R.layout.activity_main);
            currentUserID = firebaseAuth.getCurrentUser().getUid();
        } else {
            setContentView(R.layout.activity_main_guest);
            currentUserID = "";
        }
        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Home");

        drawerLayout = findViewById(R.id.drawable_layout);
        navigationView = findViewById(R.id.navigation_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = navView.findViewById(R.id.profile_image);
        navProfileUser = navView.findViewById(R.id.username);
        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    if (dataSnapshot.hasChild("firstname") && dataSnapshot.hasChild("lastname") && dataSnapshot.hasChild("profileimage")) {
                        String firstname = Objects.requireNonNull(dataSnapshot.child("firstname").getValue()).toString();
                        String lastname = Objects.requireNonNull(dataSnapshot.child("lastname").getValue()).toString();
                        String fullname = firstname + " " + lastname;
                        String profileImage = Objects.requireNonNull(dataSnapshot.child("profileimage").getValue()).toString();

                        navProfileUser.setText(fullname);
                        Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(navProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.all_users_post_list);
        recyclerView.setHasFixedSize(Boolean.TRUE);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });
        displayAllRecipes();
    }

    private void displayAllRecipes() {
        Query sortByNew = recipeRef.orderByChild("counter");
        FirebaseRecyclerOptions<Recipe> options = new FirebaseRecyclerOptions.Builder<Recipe>().setQuery(sortByNew, Recipe.class).build();
        FirebaseRecyclerAdapter<Recipe, RecipeViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Recipe, RecipeViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RecipeViewHolder holder, int position, @NonNull Recipe model) {
                final String postKey = getRef(position).getKey();

                holder.setFullname(model.getFullName());
                holder.setTime(String.format(" %s", model.getTime()));
                holder.setDate(String.format(" %s", model.getDate()));
                holder.setTitle(model.getTitle());
                holder.setRecipeImage(model.getRecipeImage());
                holder.setProfileImage(model.getProfileImage());
                if (!currentUserID.isEmpty())
                    holder.setLikeButtonStatus(postKey);
                else {
                    holder.likeRecipeButton.setVisibility(View.INVISIBLE);
                    holder.commentRecipeButton.setVisibility(View.INVISIBLE);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MainActivity.this, FullRecipeActivity.class);
                        clickPostIntent.putExtra("postKey", postKey);
                        startActivity(clickPostIntent);
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
                                    if (dataSnapshot.child(Objects.requireNonNull(postKey)).hasChild(currentUserID)) {
                                        likesRef.child(postKey).child(currentUserID).removeValue();
                                        likeChecker = Boolean.FALSE;
                                    } else {
                                        likesRef.child(postKey).child(currentUserID).setValue(Boolean.TRUE);
                                        likeChecker = Boolean.FALSE;
                                    }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
                holder.commentRecipeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent(MainActivity.this, CommentsActivity.class);
                        commentsIntent.putExtra("postKey", postKey);
                        startActivity(commentsIntent);
                    }
                });
            }

            @NonNull
            @Override
            public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_recipes_layout, parent, false);
                return new RecipeViewHolder(view);
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void CheckUserExistence() {
        final String currentUserID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(currentUserID)) {
                    SendUserToProfileSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null)
            CheckUserExistence();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }


    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void UserMenuSelector(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_logout:
                firebaseAuth.signOut();
                SendUserToLoginActivity();
                break;
            case R.id.nav_post:
                SendUserToPostActivity();
                break;
            case R.id.nav_settings:
                SendUserToSettingsActivity();
                break;
            case R.id.nav_profile:
                SendUserToProfileActivity();
                break;
            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                break;
            case R.id.nav_follows:
                SendUserToFollowsActivity();
                break;
            case R.id.nav_login:
                SendUserToLoginActivity();
                break;
            case R.id.nav_register:
                SendUserToRegisterActivity();
                break;
        }
    }

    private void SendUserToFollowsActivity() {
        Intent followActivity = new Intent(MainActivity.this, FollowActivity.class);
        startActivity(followActivity);
    }

    private void SendUserToFindFriendsActivity() {
        Intent profileActivity = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(profileActivity);
    }

    private void SendUserToProfileActivity() {
        Intent profileActivity = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileActivity);
    }

    private void SendUserToSettingsActivity() {
        Intent settingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsActivity);
    }

    private void SendUserToPostActivity() {
        Intent postActivity = new Intent(MainActivity.this, PostActivity.class);
        startActivity(postActivity);
    }

    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
        finish();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        View view;

        TextView fullname, date, time, title;
        CircleImageView user_post_image;
        ImageView post_image;

        ImageButton likeRecipeButton, commentRecipeButton;
        TextView numOfLikes;
        int countLikes;
        String currentUserUid;
        DatabaseReference likesRef;

        RecipeViewHolder(View itemView) {
            super(itemView);

            view = itemView;

            likeRecipeButton = view.findViewById(R.id.likeButton);
            commentRecipeButton = view.findViewById(R.id.commentButton);
            numOfLikes = view.findViewById(R.id.display_num_likes);
            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            if (FirebaseAuth.getInstance().getCurrentUser() != null)
                currentUserUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            else {
                numOfLikes.setVisibility(View.INVISIBLE);
                currentUserUid = "";
            }

            fullname = view.findViewById(R.id.post_username);
            date = view.findViewById(R.id.post_date);
            time = view.findViewById(R.id.post_time);
            title = view.findViewById(R.id.post_title);
            post_image = view.findViewById(R.id.post_image);
            user_post_image = view.findViewById(R.id.post_profile_image);
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
    }

    private void SendUserToProfileSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, ProfileSetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}
