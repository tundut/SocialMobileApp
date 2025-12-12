package vn.edu.ueh.socialapplication;

import com.google.firebase.Timestamp;

public class Post {
    private String id;
    private String content;
    private String imageBase64;
    private String userId;
    private String authorName;
    private Timestamp timestamp;

    public Post() {
        // Required for Firestore
    }

    public Post(String content, String imageBase64, String userId, String authorName, Timestamp timestamp) {
        this.content = content;
        this.imageBase64 = imageBase64;
        this.userId = userId;
        this.authorName = authorName;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
