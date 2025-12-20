package vn.edu.ueh.socialapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.data.model.User;
import vn.edu.ueh.socialapplication.follow.FollowRepository;
import vn.edu.ueh.socialapplication.follow.FollowViewModel;

public class FollowListActivity extends AppCompatActivity {

    private String id;
    private String title;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private FollowViewModel followViewModel;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");

        // Set content view and initialize views based on title
        if ("followers".equals(title)) {
            setContentView(R.layout.activity_followers);
            backButton = findViewById(R.id.back_button_followers);
            recyclerView = findViewById(R.id.recycler_view_followers);
        } else {
            // Default to following
            setContentView(R.layout.activity_following);
            backButton = findViewById(R.id.back_button_following);
            recyclerView = findViewById(R.id.recycler_view_following);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            userList = new ArrayList<>();
            userAdapter = new UserAdapter(this, userList);
            recyclerView.setAdapter(userAdapter);
        }

        // Initialize ViewModel
        FollowRepository followRepository = new FollowRepository();
        UserRepository userRepository = new UserRepository();
        FollowViewModel.Factory factory = new FollowViewModel.Factory(followRepository, userRepository);
        followViewModel = new ViewModelProvider(this, factory).get(FollowViewModel.class);

        showUsers();
    }

    private void showUsers() {
        if ("followers".equals(title)) {
            followViewModel.getFollowers(id).observe(this, users -> {
                if (users != null) {
                    userList.clear();
                    userList.addAll(users);
                    userAdapter.notifyDataSetChanged();
                }
            });
        } else if ("following".equals(title)) {
            followViewModel.getFollowing(id).observe(this, users -> {
                if (users != null) {
                    userList.clear();
                    userList.addAll(users);
                    userAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}
