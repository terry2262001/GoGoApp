package com.example.instagram.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.Fragment.PostDetailFragment;
import com.example.instagram.Model.Post;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MyPhotoAdapter extends RecyclerView.Adapter<MyPhotoAdapter.MyPhotoVH> {
    private List<Post> mPosts;
    private Context mContext;

    FirebaseUser firebaseUser;

    public MyPhotoAdapter(List<Post> mPosts, Context mContext) {
        this.mPosts = mPosts;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyPhotoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.photo_item,parent,false);
        return new MyPhotoAdapter.MyPhotoVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPhotoVH holder, int position) {
        Post post = mPosts.get(position);
        Glide.with(mContext).load(post.getPostImage()).into(holder.imgPost);
        holder.imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("postid",post.getPostId());
                editor.apply();
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                        ,new PostDetailFragment()).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public  class MyPhotoVH extends RecyclerView.ViewHolder {
        ImageView imgPost;
        public MyPhotoVH(@NonNull View itemView) {
            super(itemView);
            imgPost = itemView.findViewById(R.id.imgPost);

        }
    }
}
