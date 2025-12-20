package vn.edu.ueh.socialapplication.offline;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Post;

public class OfflinePostsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OfflinePostAdapter adapter;
    private List<Post> postList;
    private OfflineRepository offlineRepository;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_posts);

        backButton = findViewById(R.id.back_button_offline);
        recyclerView = findViewById(R.id.recycler_view_offline);
        
        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        backButton.setOnClickListener(v -> finish());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        postList = new ArrayList<>();
        adapter = new OfflinePostAdapter(this, postList);
        recyclerView.setAdapter(adapter);

        offlineRepository = new OfflineRepository(this);
        
        // Load posts
        offlineRepository.getAllPosts().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> posts) {
                if (posts != null) {
                    postList.clear();
                    postList.addAll(posts);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        
        // Optional: trigger a refresh if we have network, to update the cache
        // offlineRepository.refreshPosts(); 
    }
}
