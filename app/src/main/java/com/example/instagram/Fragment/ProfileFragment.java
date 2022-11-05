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

import com.bumptech.glide.Glide;
import com.example.instagram.Model.Post;
import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.example.instagram.StartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {
    TextView tvUsername,tvPosts,tvFollower,tvFollowing,tvFullname,tvBio;
    ImageView imgProfileFragment,imgOptions;
    Button btEdit;

    FirebaseUser firebaseUser ;
    String profileid;

    ImageButton imgBPhotos,imgBSaves;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view =  inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences pref = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = pref.getString("profiled","none");
        System.out.println("profileid_profile_fragment"+profileid);

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

        userInfo();
        getFollowers();
        getNrPosts();
        imgOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), StartActivity.class));
            }
        });


        if (profileid.equals(firebaseUser.getUid())){
            btEdit.setText("Edit Profile");
        }else{
            checkFollow();
            imgBSaves.setVisibility(View.GONE);
        }

        btEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String btn  = btEdit.getText().toString();
                if(btn.equals("Edit Profile")){
                    //go to edit
                }else if(btn.equals("follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                }else if(btn.equals("following")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).removeValue();

                }
            }
        });
        return view;
    }

    private void userInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (getContext() == null){
                    return;
                }
                User user = snapshot.getValue(User.class);
                System.out.println(user.toString() +"usertostring");
                System.out.println(user.getUsername().toString() +"user_tho1");
                Glide.with(getContext()).load(user.getImageURL()).into(imgProfileFragment);
                tvUsername.setText(user.getUsername());
                tvFullname.setText(user.getFullName());
                tvBio.setText(user.getBio());
                System.out.println("user_Profile, username:"+user.getUsername()+"fullname:"+user.getFullName()+"bio"+user.getBio());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });
    }
    private void checkFollow(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(profileid).exists()){
                    btEdit.setText("following");
                }else {
                    btEdit.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getFollowers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(profileid).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvFollower.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Follow").child(profileid).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvFollowing.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getNrPosts(){
        DatabaseReference reference  = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post.getPublisher().equals(profileid)){
                        i++;
                    }
                }
                tvPosts.setText(""+i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}