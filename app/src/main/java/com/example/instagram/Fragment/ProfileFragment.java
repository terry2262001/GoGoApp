package com.example.instagram.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.Adapter.MyPhotoAdapter;
import com.example.instagram.EditProfileActivity;
import com.example.instagram.FollowerActivity;
import com.example.instagram.Model.Post;
import com.example.instagram.Model.User;
import com.example.instagram.OptionsActivity;
import com.example.instagram.PostActivity;
import com.example.instagram.R;
import com.example.instagram.StartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class ProfileFragment extends Fragment {
    TextView tvUsername, tvPosts, tvFollower, tvFollowing, tvFullname, tvBio;
    ImageView imgProfileFragment, imgOptions;
    Button btEdit;

    FirebaseUser firebaseUser;
    String profileid;

    private  List<String> mySaveLists;

    RecyclerView rvSaves;
    MyPhotoAdapter myPhotoAdapter_save;
    List<Post> postList_saves;

    RecyclerView rvPhotos;
    MyPhotoAdapter myPhotoAdapter;
    List<Post> postList;


    ImageButton imgBPhotos, imgBSaves;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences pref = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = pref.getString("profileid", "none");
        System.out.println("profileid_profile_fragment" + profileid);

        tvUsername = view.findViewById(R.id.tvUsername);
        tvPosts = view.findViewById(R.id.tvPosts);
        tvFollower = view.findViewById(R.id.tvFollower);
        tvFollowing = view.findViewById(R.id.tvFollowing);
        tvFullname = view.findViewById(R.id.tvFullname);
        tvBio = view.findViewById(R.id.tvBio);
        imgProfileFragment = view.findViewById(R.id.imgProfileFragment);
        imgOptions = view.findViewById(R.id.imgOptions);
        btEdit = view.findViewById(R.id.btEdit);
        imgBPhotos = view.findViewById(R.id.imgBPhotos);
        imgBSaves = view.findViewById(R.id.imgBSaves);



        // set recyclerview posted photo in profile
        rvPhotos = view.findViewById(R.id.rvPhotos);
        rvPhotos.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);

        rvPhotos.setLayoutManager(linearLayoutManager);
         postList = new ArrayList<>();
        myPhotoAdapter = new MyPhotoAdapter(postList, getContext());
        rvPhotos.setAdapter(myPhotoAdapter);

        //set recyclerview saved photo in profile;
        rvSaves  = view.findViewById(R.id.rvSaves);
        rvSaves.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new GridLayoutManager(getContext(),3);
        rvSaves.setLayoutManager(linearLayoutManager1);
        postList_saves = new ArrayList<>();
        myPhotoAdapter_save = new MyPhotoAdapter(postList_saves,getContext());
        rvSaves.setAdapter(myPhotoAdapter_save);
        ///
        userInfo();
        getFollowers();
        getNrPosts();
        myPhotos();
        mySaves();




        if (profileid.equals(firebaseUser.getUid())) {
            btEdit.setText("Edit Profile");
        } else {
            checkFollow();
            imgBSaves.setVisibility(View.GONE);
        }

        btEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String btn = btEdit.getText().toString();
                if (btn.equals("Edit Profile")) {
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                } else if (btn.equals("follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                    addNotifications();
                } else if (btn.equals("following")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).removeValue();

                }
            }
        });
        imgOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), OptionsActivity.class);
                startActivity(intent);
            }
        });
        imgBPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rvPhotos.setVisibility(View.VISIBLE);
                rvSaves.setVisibility(View.GONE);
            }
        });
        imgBSaves.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rvPhotos.setVisibility(View.GONE);
                rvSaves.setVisibility(View.VISIBLE);
            }
        });
        tvFollower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowerActivity.class);
                intent.putExtra("id",profileid);
                intent.putExtra("title","follower");
                startActivity(intent);
            }
        });
        tvFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowerActivity.class);
                intent.putExtra("id",profileid);
                intent.putExtra("title","following");
                startActivity(intent);
            }
        });
        return view;
    }
    private void addNotifications(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(profileid);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("text","started following you");
        hashMap.put("postid","");
        hashMap.put("ispost",false);

        reference.push().setValue(hashMap);
    }


    private void userInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (getContext() == null) {
                    return;
                }
                User user = snapshot.getValue(User.class);
                Glide.with(getContext()).load(user.getImageURL()).into(imgProfileFragment);
                tvUsername.setText(user.getUsername());
                tvFullname.setText(user.getFullName());
                tvBio.setText(user.getBio());


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });
    }

    private void checkFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(profileid).exists()) {
                    btEdit.setText("following");
                } else {
                    btEdit.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(profileid).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvFollower.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Follow").child(profileid).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvFollowing.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getNrPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post.getPublisher().equals(profileid)) {
                        i++;
                    }
                }
                tvPosts.setText("" + i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void myPhotos() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post.getPublisher().equals(profileid)) {
                        postList.add(post);
                    }


                }
                Collections.reverse(postList);
                myPhotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void mySaves (){

        mySaveLists = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    mySaveLists.add(dataSnapshot.getKey());
                }
                readSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readSaves() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList_saves.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Post post = dataSnapshot.getValue(Post.class);

                    for (String id : mySaveLists){
                        if (post.getPostId().equals(id)){
                            postList_saves.add(post);
                        }
                    }
                    
                }
                myPhotoAdapter_save.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}