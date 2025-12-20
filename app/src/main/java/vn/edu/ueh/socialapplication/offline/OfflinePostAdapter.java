package vn.edu.ueh.socialapplication.offline;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.ueh.socialapplication.ImageUtils;
import vn.edu.ueh.socialapplication.R;
import vn.edu.ueh.socialapplication.data.model.Post;

public class OfflinePostAdapter extends RecyclerView.Adapter<OfflinePostAdapter.ViewHolder> {

    private final Context context;
    private final List<Post> postList;

    public OfflinePostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_offline_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.username.setText(post.getUserId()); // In offline, we might not have username details, just ID unless we join tables
        holder.caption.setText(post.getCaption());
        holder.date.setText(String.valueOf(post.getCreatedAt())); // Simplified date

        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.postImage.setVisibility(View.VISIBLE);
            ImageUtils.loadImage(post.getImageUrl(), holder.postImage);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }
        
        // Since we don't have user avatar URL in Post model (it's in User model), we use placeholder
        holder.profileImage.setImageResource(R.drawable.ic_account_circle);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView profileImage;
        public TextView username;
        public ImageView postImage;
        public TextView caption;
        public TextView date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.offline_post_profile_image);
            username = itemView.findViewById(R.id.offline_post_username);
            postImage = itemView.findViewById(R.id.offline_post_image);
            caption = itemView.findViewById(R.id.offline_post_caption);
            date = itemView.findViewById(R.id.offline_post_date);
        }
    }
}
