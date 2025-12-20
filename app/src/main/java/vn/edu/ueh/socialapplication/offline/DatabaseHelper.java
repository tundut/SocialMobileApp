package vn.edu.ueh.socialapplication.offline;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.socialapplication.data.model.Post;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "social_app_database";
    private static final int DATABASE_VERSION = 1;

    // Table Name
    private static final String TABLE_POSTS = "posts";

    // Column Names
    private static final String KEY_POST_ID = "postId";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_CAPTION = "caption";
    private static final String KEY_IMAGE_URL = "imageUrl";
    private static final String KEY_CREATED_AT = "createdAt";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_POSTS_TABLE = "CREATE TABLE " + TABLE_POSTS + "("
                + KEY_POST_ID + " TEXT PRIMARY KEY,"
                + KEY_USER_ID + " TEXT,"
                + KEY_CAPTION + " TEXT,"
                + KEY_IMAGE_URL + " TEXT,"
                + KEY_CREATED_AT + " INTEGER" + ")";
        db.execSQL(CREATE_POSTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        onCreate(db);
    }

    // Insert or Update a list of posts
    public void insertPosts(List<Post> posts) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Post post : posts) {
                ContentValues values = new ContentValues();
                values.put(KEY_POST_ID, post.getPostId());
                values.put(KEY_USER_ID, post.getUserId());
                values.put(KEY_CAPTION, post.getCaption());
                values.put(KEY_IMAGE_URL, post.getImageUrl());
                values.put(KEY_CREATED_AT, post.getCreatedAt());

                // Conflict resolution: REPLACE
                db.insertWithOnConflict(TABLE_POSTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // Get all posts
    public List<Post> getAllPosts() {
        List<Post> postList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_POSTS + " ORDER BY " + KEY_CREATED_AT + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Post post = new Post();
                post.setPostId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_POST_ID)));
                post.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ID)));
                post.setCaption(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CAPTION)));
                post.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_URL)));
                post.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CREATED_AT)));
                postList.add(post);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return postList;
    }

    // Get posts by User ID
    public List<Post> getPostsByUserId(String userId) {
        List<Post> postList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String[] columns = {KEY_POST_ID, KEY_USER_ID, KEY_CAPTION, KEY_IMAGE_URL, KEY_CREATED_AT};
        String selection = KEY_USER_ID + " = ?";
        String[] selectionArgs = {userId};
        String orderBy = KEY_CREATED_AT + " DESC";

        Cursor cursor = db.query(TABLE_POSTS, columns, selection, selectionArgs, null, null, orderBy);

        if (cursor.moveToFirst()) {
            do {
                Post post = new Post();
                post.setPostId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_POST_ID)));
                post.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ID)));
                post.setCaption(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CAPTION)));
                post.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_URL)));
                post.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CREATED_AT)));
                postList.add(post);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return postList;
    }

    public void clearAllPosts() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_POSTS);
        db.close();
    }
}
