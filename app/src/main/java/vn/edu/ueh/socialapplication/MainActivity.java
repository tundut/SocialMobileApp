package vn.edu.ueh.socialapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PostAdapter.OnPostActionListener {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList;
    
    // Profile related
    private View scrollViewFeed;
    private View layoutProfile;
    private View topBar;
    private RecyclerView rvProfilePosts;
    private ProfileAdapter profileAdapter;
    private List<Post> profilePostList;
    
    private FirebaseFirestore db;
    private DocumentSnapshot lastVisible;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private static final int LIMIT = 10;
    
    // Hardcoded user for demo purposes
    private static final String CURRENT_USER_ID = "user_123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        
        // Initialize Feed Components
        postList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new PostAdapter(this, postList, CURRENT_USER_ID, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Initialize Profile Components
        scrollViewFeed = findViewById(R.id.scrollViewFeed);
        layoutProfile = findViewById(R.id.layoutProfile);
        topBar = findViewById(R.id.topBar);
        rvProfilePosts = findViewById(R.id.rvProfilePosts);
        ImageView btnProfileSettings = findViewById(R.id.btnProfileSettings);
        
        profilePostList = new ArrayList<>();
        profileAdapter = new ProfileAdapter(this, profilePostList);
        rvProfilePosts.setLayoutManager(new GridLayoutManager(this, 3));
        rvProfilePosts.setAdapter(profileAdapter);

        // Navigation Components
        ImageView btnNavHome = findViewById(R.id.btnNavHome);
        ImageView btnNavProfile = findViewById(R.id.btnNavProfile);
        ImageView btnBottomAdd = findViewById(R.id.btnBottomAdd);

        // Scroll listener for pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLoading && !isLastPage && !recyclerView.canScrollVertically(1)) {
                    loadMorePosts();
                }
            }
        });

        // Navigation Logic
        btnNavHome.setOnClickListener(v -> showFeed());
        btnNavProfile.setOnClickListener(v -> showProfile());

        btnBottomAdd.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreatePostActivity.class));
        });
        
        // Profile Settings Button
        if (btnProfileSettings != null) {
            btnProfileSettings.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            });
        }
        
        // Initial Load
        loadPosts();
        loadProfilePosts();
    }

    private void showFeed() {
        scrollViewFeed.setVisibility(View.VISIBLE);
        topBar.setVisibility(View.VISIBLE);
        layoutProfile.setVisibility(View.GONE);
    }

    private void showProfile() {
        scrollViewFeed.setVisibility(View.GONE);
        topBar.setVisibility(View.GONE);
        layoutProfile.setVisibility(View.VISIBLE);
        // Refresh profile posts if needed
        loadProfilePosts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPosts();
    }

    private void loadPosts() {
        if (isLoading) return;
        isLoading = true;
        isLastPage = false;
        postList.clear();
        adapter.notifyDataSetChanged();

        Query query = db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(LIMIT);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    Post post = document.toObject(Post.class);
                    if (post != null) {
                        post.setId(document.getId());
                        postList.add(post);
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                isLastPage = true;
            }
            isLoading = false;
        }).addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, "Error loading posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            isLoading = false;
        });
    }
    
    private void loadProfilePosts() {
        profilePostList.clear();
        profileAdapter.notifyDataSetChanged();
        
        // Load only posts by current user
        db.collection("posts")
                .whereEqualTo("userId", CURRENT_USER_ID)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Post post = document.toObject(Post.class);
                        if (post != null) {
                            post.setId(document.getId());
                            profilePostList.add(post);
                        }
                    }
                    profileAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle error silently or log
                    e.printStackTrace();
                });
    }

    private void loadMorePosts() {
        if (isLoading || isLastPage) return;
        isLoading = true;

        Query query = db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(LIMIT);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    Post post = document.toObject(Post.class);
                    if (post != null) {
                        post.setId(document.getId());
                        postList.add(post);
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                isLastPage = true;
            }
            isLoading = false;
        }).addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, "Error loading more posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            isLoading = false;
        });
    }

    @Override
    public void onEdit(Post post) {
        Intent intent = new Intent(this, CreatePostActivity.class);
        intent.putExtra("postId", post.getId());
        intent.putExtra("content", post.getContent());
        intent.putExtra("imageBase64", post.getImageBase64());
        startActivity(intent);
    }

    @Override
    public void onDelete(Post post) {
        if (post.getId() != null) {
            db.collection("posts").document(post.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                        int position = postList.indexOf(post);
                        if (position != -1) {
                            postList.remove(position);
                            adapter.notifyItemRemoved(position);
                        }
                        // Also update profile list if needed
                        loadProfilePosts();
                    })
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error deleting post", Toast.LENGTH_SHORT).show());
        }
    }
}
