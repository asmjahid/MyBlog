package com.asmjahid.myblog.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.asmjahid.myblog.R;
import com.asmjahid.myblog.model.Post;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DatabaseReference mDatabaseRefPosts;
    DatabaseReference mDatabaseRefLikes;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    @BindView(R.id.list_post)
    RecyclerView mPostList;

    private boolean processLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NewPostActivity.class));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ButterKnife.bind(this);


        mDatabaseRefPosts = FirebaseDatabase.getInstance().getReference().child("posts");
        mDatabaseRefLikes = FirebaseDatabase.getInstance().getReference().child("likes");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mPostList.setHasFixedSize(true);
        mPostList.setLayoutManager(layoutManager);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_logout:
                logout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation mView item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            fetchBlogPosts();
        }
    }

    private void fetchBlogPosts() {
        FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(
                Post.class,
                R.layout.list_post,
                PostViewHolder.class,
                mDatabaseRefPosts
        ) {
            @Override
            protected void populateViewHolder(PostViewHolder viewHolder, Post model, int position) {

                final String postId = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setUser(getApplicationContext(), model.getUser());
                viewHolder.setLikeButton(postId);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, PostDetailActivity.class);
                        intent.putExtra("postId", postId);
                        startActivity(intent);
                    }
                });

                viewHolder.mLikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        processLike = true;
                        mDatabaseRefLikes.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (processLike) {
                                    if (dataSnapshot.child(postId).hasChild(mAuth.getCurrentUser().getUid())) {
                                        mDatabaseRefLikes.child(postId).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        processLike = false;
                                    } else {
                                        mDatabaseRefLikes.child(postId).child(mAuth.getCurrentUser().getUid()).setValue("RandomValue");
                                        processLike = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }

        };

        mPostList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        View mView;

        TextView mPostTitleText;
        TextView mPostDescText;
        ImageView mPostMediaImage;
        TextView mUserFullNameText;
        ImageView mUserAvatarImage;
        ImageButton mLikeButton;

        DatabaseReference mDatabaseRefLike;

        FirebaseAuth mAuth;

        public PostViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mPostTitleText = (TextView) mView.findViewById(R.id.text_post_title);
            mPostDescText = (TextView) mView.findViewById(R.id.text_post_description);
            mPostMediaImage = (ImageView) mView.findViewById(R.id.image_post_media);
            mUserFullNameText = (TextView) mView.findViewById(R.id.text_user_name);
            mUserAvatarImage = (ImageView) mView.findViewById(R.id.image_user_avatar);
            mLikeButton = (ImageButton) mView.findViewById(R.id.button_like);

            mAuth = FirebaseAuth.getInstance();

            mDatabaseRefLike = FirebaseDatabase.getInstance().getReference().child("likes");

            mPostTitleText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v("CLICK", "CLICKOU");
                }
            });
        }

        public void setTitle(String title) {
            mPostTitleText.setText(title);
        }

        public void setDescription(String description) {
            mPostDescText.setText(description);
        }

        public void setImage(final Context context, final String image) {
            Picasso.with(context).load(image).into(mPostMediaImage);
        }

        public void setUser(final Context context, final Post.User user) {
            mUserFullNameText.setText(user.getName());

            Picasso.with(context).load(user.getImage()).fit().centerCrop().into(mUserAvatarImage);
        }

        public void setLikeButton(final String postId) {
            mDatabaseRefLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postId).hasChild(mAuth.getCurrentUser().getUid())) {
                        mLikeButton.setImageResource(R.drawable.ic_thumb_up_active);
                    } else {
                        mLikeButton.setImageResource(R.drawable.ic_thumb_up);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

}