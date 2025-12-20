package vn.edu.ueh.socialapplication.data.model;

import java.io.Serializable;

public class Post implements Serializable {
    private String postId;
    private String userId;
    private String caption;
    private String imageUrl;
    private long createdAt;

    public Post() {
        // Default constructor for Firebase
    }

    public Post(String postId, String userId, String caption, String imageUrl, long createdAt) {
        this.postId = postId;
        this.userId = userId;
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
