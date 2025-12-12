package vn.edu.ueh.socialapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etPostContent;
    private ImageView ivSelectedImage;
    private ImageView btnPost;
    private ImageView btnClose;
    private String encodedImage;
    private FirebaseFirestore db;
    private String postIdToEdit = null;

    private static final String CURRENT_USER_ID = "user_123"; 
    private static final String CURRENT_USER_NAME = "Demo User";

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        ivSelectedImage.setImageBitmap(bitmap);
                        ivSelectedImage.setVisibility(View.VISIBLE);
                        encodeBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ivSelectedImage.setImageBitmap(imageBitmap);
                    ivSelectedImage.setVisibility(View.VISIBLE);
                    encodeBitmap(imageBitmap);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        etPostContent = findViewById(R.id.etPostContent);
        ivSelectedImage = findViewById(R.id.ivSelectedImage);
        TextView btnSelectImage = findViewById(R.id.btnSelectImage);
        TextView btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnPost = findViewById(R.id.btnPost);
        btnClose = findViewById(R.id.btnClose);

        if (getIntent().hasExtra("postId")) {
            postIdToEdit = getIntent().getStringExtra("postId");
            String content = getIntent().getStringExtra("content");
            String imageBase64 = getIntent().getStringExtra("imageBase64");

            etPostContent.setText(content);
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                encodedImage = imageBase64;
                try {
                    byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivSelectedImage.setImageBitmap(decodedByte);
                    ivSelectedImage.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        btnTakePhoto.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(takePictureIntent);
            }
        });

        btnPost.setOnClickListener(v -> savePost());
        btnClose.setOnClickListener(v -> finish());
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return image;
        }

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void encodeBitmap(Bitmap bitmap) {
        Bitmap resizedBitmap = getResizedBitmap(bitmap, 1080); // Resize to max 1080px
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void savePost() {
        String content = etPostContent.getText().toString().trim();
        if (content.isEmpty() && encodedImage == null) {
            Toast.makeText(this, "Please write a caption or select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPost.setEnabled(false);

        if (postIdToEdit != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("content", content);
            updates.put("imageBase64", encodedImage);

            db.collection("posts").document(postIdToEdit)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreatePostActivity.this, "Post updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreatePostActivity.this, "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnPost.setEnabled(true);
                    });
        } else {
            Post post = new Post(content, encodedImage, CURRENT_USER_ID, CURRENT_USER_NAME, new Timestamp(new Date()));
            
            db.collection("posts")
                    .add(post)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(CreatePostActivity.this, "Post created", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreatePostActivity.this, "Error posting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnPost.setEnabled(true);
                    });
        }
    }
}
