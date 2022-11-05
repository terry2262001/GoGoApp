package com.example.instagram.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.AddStoryActivity;
import com.example.instagram.Model.Story;
import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.example.instagram.StoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryVH> {
    private Context mContext;
    private List<Story> mStory;


    public StoryAdapter(Context mContext, List<Story> mStory) {
        this.mContext = mContext;
        this.mStory = mStory;
    }

    @NonNull
    @Override
    public StoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0){
            View view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item,parent,false);
            return new StoryAdapter.StoryVH(view);
        }else{
            View view = LayoutInflater.from(mContext).inflate(R.layout.story_item,parent,false);
            return new StoryAdapter.StoryVH(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull StoryVH holder, int position) {
        Story story = mStory.get(position);

        userInfo(holder,story.getUserid(), position);

        if (holder.getAdapterPosition() != 0){
            seenStory(holder, story.getUserid());
        }
        if (holder.getAdapterPosition() == 0){
            myStory(holder.tvAddStoryText,holder.imgAddStory,false);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getAdapterPosition() == 0){

                    myStory(holder.tvAddStoryText, holder.imgAddStory,true );

                }else {
                    Intent intent = new Intent(mContext, StoryActivity.class);
                    intent.putExtra("userid",story.getUserid());
                    mContext.startActivity(intent);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return mStory.size();
    }

    public class StoryVH extends RecyclerView.ViewHolder {
        ImageView imgAddStory,imgStory,imgStorySeen;
        TextView tvAddStoryText,tvUsername;


        public StoryVH(@NonNull View itemView) {
            super(itemView);

            imgAddStory = itemView.findViewById(R.id.imgAddStory);
            imgStory = itemView.findViewById(R.id.imgStory);
            tvAddStoryText = itemView.findViewById(R.id.tvAddStoryText);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            imgStorySeen = itemView.findViewById(R.id.imgStorySeen);

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return 0;
        }
        return 1;
    }
    private  void  userInfo(final StoryVH holder,final String userid,final int pos){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                Glide.with(mContext).load(user.getImageURL()).into(holder.imgStory);
                if (pos != 0){
                    Glide.with(mContext).load(user.getImageURL()).into(holder.imgStorySeen);
                    holder.tvUsername.setText(user.getUsername());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void myStory(TextView textView,ImageView imageView,boolean click){
        DatabaseReference reference =FirebaseDatabase.getInstance().getReference("Story")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                long timeCurrent = System.currentTimeMillis();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Story story = dataSnapshot.getValue(Story.class);
                    if (timeCurrent > story.getTimestart() && timeCurrent <story.getTimeend() ){
                        count++;


                    }
                }
                if (click){
                    if (count > 0 ){
                        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                       alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Story",
                               new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialogInterface, int i) {
                                       //TODO:go to story
                                       Intent intent = new Intent(mContext, StoryActivity.class);
                                       intent.putExtra("userid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                                       mContext.startActivity(intent);
                                       dialogInterface.dismiss();
                                   }
                               });
                       alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story",
                               new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialogInterface, int i) {
                                       Intent intent =  new Intent(mContext, AddStoryActivity.class);
                                       mContext.startActivity(intent);
                                       dialogInterface.dismiss();
                                   }
                               });
                       alertDialog.show();
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                    }else{
                        Intent intent = new Intent(mContext,AddStoryActivity.class);
                        mContext.startActivity(intent);

                    }
                }else   {
                    if (count > 0){
                        textView.setText("My Story");
                        imageView.setVisibility(View.GONE);
                    }else {
                        textView.setText("Add story");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void  seenStory(StoryVH holder,String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                System.out.println(i+"tho1_i1");
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){

                    if (dataSnapshot.child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()&& System.currentTimeMillis() <dataSnapshot.getValue(Story.class).getTimeend() ){//
                        i++;
                        System.out.println(i+"tho1_i2");

                    }

                }
                if (i > 0){
                    holder.imgStory.setVisibility(View.GONE);
                    holder.imgStorySeen.setVisibility(View.VISIBLE);

                    System.out.println(i+"tho1_i4");


                }else {
                    holder.imgStory.setVisibility(View.VISIBLE);
                    holder.imgStorySeen.setVisibility(View.GONE);
                    System.out.println(i+"tho1_i3");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
