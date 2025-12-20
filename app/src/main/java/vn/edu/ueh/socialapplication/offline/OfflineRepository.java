package vn.edu.ueh.socialapplication.offline;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.socialapplication.data.model.Post;
import vn.edu.ueh.socialapplication.utils.NetworkUtils;

public class OfflineRepository {
    private static final String TAG = "OfflineRepository";
    private final DatabaseHelper dbHelper;
    private final FirebaseFirestore db;
    private final Context context;
    private final ExecutorService executorService;

    // We need MutableLiveData to manually update UI since SQLiteOpenHelper doesn't return LiveData directly
    private final MutableLiveData<List<Post>> allPostsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Post>> userPostsLiveData = new MutableLiveData<>();

    public OfflineRepository(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.db = FirebaseFirestore.getInstance();
        this.executorService = Executors.newFixedThreadPool(4);
        
        // Load initial data
        loadAllPostsFromLocal();
    }

    public LiveData<List<Post>> getAllPosts() {
        return allPostsLiveData;
    }

    public LiveData<List<Post>> getUserPosts(String userId) {
        // Initial load for a specific user
        loadUserPostsFromLocal(userId);
        return userPostsLiveData;
    }

    private void loadAllPostsFromLocal() {
        executorService.execute(() -> {
            List<Post> posts = dbHelper.getAllPosts();
            allPostsLiveData.postValue(posts);
        });
    }

    private void loadUserPostsFromLocal(String userId) {
        executorService.execute(() -> {
            List<Post> posts = dbHelper.getPostsByUserId(userId);
            userPostsLiveData.postValue(posts);
        });
    }

    public void refreshPosts() {
        if (NetworkUtils.isNetworkAvailable(context)) {
            db.collection("posts")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(20)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Post> posts = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                if (post.getPostId() == null || post.getPostId().isEmpty()) {
                                    post.setPostId(doc.getId());
                                }
                                posts.add(post);
                            }
                        }
                        
                        executorService.execute(() -> {
                            dbHelper.insertPosts(posts);
                            // After inserting, reload from DB to update UI
                            loadAllPostsFromLocal();
                        });
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching posts", e));
        } else {
            Log.d(TAG, "No network, using offline data");
            loadAllPostsFromLocal();
        }
    }
    
    public void fetchUserPosts(String userId) {
        if (NetworkUtils.isNetworkAvailable(context)) {
            db.collection("posts")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Post> posts = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                if (post.getPostId() == null) post.setPostId(doc.getId());
                                posts.add(post);
                            }
                        }
                        executorService.execute(() -> {
                            dbHelper.insertPosts(posts);
                            // After inserting, reload specific user posts
                            loadUserPostsFromLocal(userId);
                        });
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching user posts", e));
        } else {
            loadUserPostsFromLocal(userId);
        }
    }
}
