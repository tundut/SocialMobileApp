package vn.edu.ueh.socialapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private Context context;
    private OnPostActionListener listener;
    private String currentUserId;

    public interface OnPostActionListener {
        void onEdit(Post post);
        void onDelete(Post post);
    }

    public PostAdapter(Context context, List<Post> postList, String currentUserId, OnPostActionListener listener) {
        this.context = context;
        this.postList = postList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // Bind Author Name
        holder.tvAuthor.setText(post.getAuthorName());
        
        // Bind Date
        if (post.getTimestamp() != null) {
            holder.tvDate.setText(post.getTimestamp().toDate().toString());
        } else {
            holder.tvDate.setText("");
        }

        // Bind Content/Caption with Author Name prefix
        String caption = post.getAuthorName() + " " + post.getContent();
        holder.tvContent.setText(caption);

        // Bind Post Image
        if (post.getImageBase64() != null && !post.getImageBase64().isEmpty()) {
            holder.ivPostImage.setVisibility(View.VISIBLE);
            try {
                byte[] decodedString = Base64.decode(post.getImageBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Glide.with(context).load(decodedByte).into(holder.ivPostImage);
            } catch (Exception e) {
                e.printStackTrace();
                holder.ivPostImage.setVisibility(View.GONE);
            }
        } else {
            holder.ivPostImage.setVisibility(View.GONE);
        }

        // Handle Menu (Edit/Delete) - only for own posts
        if (post.getUserId() != null && post.getUserId().equals(currentUserId)) {
            holder.btnMenu.setVisibility(View.VISIBLE);
            holder.btnMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, holder.btnMenu);
                popup.inflate(R.menu.menu_post_options);
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit) {
                        listener.onEdit(post);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete) {
                        listener.onDelete(post);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        } else {
            holder.btnMenu.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvDate, tvContent, tvMusic;
        ImageView ivAvatar, ivPostImage, btnLike, btnComment, btnShare, btnSave;
        ImageButton btnMenu;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvMusic = itemView.findViewById(R.id.tvMusic);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvContent = itemView.findViewById(R.id.tvContent);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            btnMenu = itemView.findViewById(R.id.btnMenu);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnSave = itemView.findViewById(R.id.btnSave);
        }
    }
}
