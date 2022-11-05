package Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.CommentActivity;
import com.example.instagram.Model.Post;
import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostVH> {
    public List<Post> mPosts;
    public Context mContext;

    private FirebaseUser firebaseUser;

    public PostAdapter( Context mContext,List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;

    }

    @NonNull
    @Override
    public PostVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item,parent,false);

        return new PostAdapter.PostVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostVH holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = mPosts.get(position);



        Glide.with(mContext).load(post.getPostImage()).into(holder.imgPost);
        if(post.getDescription().equals("")){
            holder.tvDescription.setVisibility(View.GONE);
        }else{
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(post.getDescription());
        }
        publisherInfo(holder.imgProfile,holder.tvUsername,holder.tvPublisher,post.getPublisher());

        isLiked(post.getPostId(), holder.imgLike);
        nrLike(holder.tvLikes,post.getPostId());
        getComments(post.getPostId(),holder.tvComments);

        holder.imgLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if(holder.imgLike.getTag().equals("like")){
                        FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                                .child(firebaseUser.getUid()).setValue(true);
                    }else {
                        FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                                .child(firebaseUser.getUid()).removeValue();

                    }

            }
        });
        holder.imgPost.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick(View v) {

            }

            @Override
            public void onDoubleClick(View v) {
                if(holder.imgLike.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                            .child(firebaseUser.getUid()).setValue(true);
                }else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                            .child(firebaseUser.getUid()).removeValue();

                }

            }
        });
        holder.imgComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("postid",post.getPostId());
                intent.putExtra("publisherid",post.getPublisher());

                mContext.startActivity(intent);
            }
        });
        holder.tvComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("postid",post.getPostId());
                intent.putExtra("publisherid",post.getPublisher());
                mContext.startActivity(intent);
            }
        });




    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class PostVH extends RecyclerView.ViewHolder {

        TextView tvUsername,tvPublisher,tvDescription,tvComments,tvLikes;
        ImageView imgProfile, imgPost,imgLike,imgComment,imgSave;

        public PostVH(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            imgPost = itemView.findViewById(R.id.imgPost);
            imgLike = itemView.findViewById(R.id.imgLike);
            imgComment = itemView.findViewById(R.id.imgComment);
            imgSave = itemView.findViewById(R.id.imgSave);
            tvPublisher = itemView.findViewById(R.id.tvPublisher);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvComments = itemView.findViewById(R.id.tvComments);
            tvLikes = itemView.findViewById(R.id.tvLikes);


        }
    }

    private void getComments(String postid,TextView tvComments){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvComments.setText("View All " + snapshot.getChildrenCount()+" Comments" );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isLiked(String postId, ImageView imageView){
        FirebaseUser firebaseUser =  FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference =  FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                }else{
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void nrLike(TextView likes , String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likes.setText(snapshot.getChildrenCount() +" likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public abstract class DoubleClickListener implements View.OnClickListener {

        private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

        long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
                onDoubleClick(v);
                lastClickTime = 0;
            } else {
                onSingleClick(v);
            }
            lastClickTime = clickTime;
        }

        public abstract void onSingleClick(View v);
        public abstract void onDoubleClick(View v);
    }


    private void publisherInfo(final ImageView imgProfile,final TextView tvUsername,final TextView tvPublisher,final String userid){
        DatabaseReference reference  = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageURL()).into(imgProfile);
                tvUsername.setText(user.getUsername());
                tvPublisher.setText(user.getUsername());



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
