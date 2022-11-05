package com.example.instagram.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.Adapter.PostAdapter;
import com.example.instagram.Adapter.StoryAdapter;
import com.example.instagram.Model.Post;
import com.example.instagram.Model.Story;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class HomeFragment extends Fragment {
    private RecyclerView rvPost;
    private PostAdapter postAdapter;
    private List<Post> postLists;
    //
    private RecyclerView rvStory;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;
    Set<Story> storyList1 = new HashSet<Story>();
    //private  List<String> storyList;
    //

    private List<String> followingList;

    ProgressBar progressBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ///rv POST
        rvPost = view.findViewById(R.id.rvPost);
        rvPost.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvPost.setLayoutManager(linearLayoutManager);
        postLists = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postLists);
        rvPost.setAdapter(postAdapter);


        //rv STORY
        rvStory = view.findViewById(R.id.rvStory);
        rvStory.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvStory.setLayoutManager(linearLayoutManager1);
        storyList = new ArrayList<>();

        storyAdapter = new StoryAdapter(getContext(), storyList);
        rvStory.setAdapter(storyAdapter);

        //
        checkFollowing();
        progressBar = view.findViewById(R.id.progressbar_circular);

        return view;
    }

    private void checkFollowing() {
        followingList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    followingList.add(dataSnapshot.getKey());
                    ;
                }

                readPost();
                readStory();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postLists.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);

                    for (String id : followingList) {
                        if (post.getPublisher().equals(id)) {
                            postLists.add(post);
                        }

                    }
                }
                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readStory1() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story");
        reference.addValueEventListener(new ValueEventListener() {
            long timeCurrent = System.currentTimeMillis();

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyList.clear();
                for (String id : followingList) {
                    int countStory = 0;
                    Story story = null;
                    for (DataSnapshot dataSnapshot : snapshot.child(id).getChildren()) {
                        story = dataSnapshot.getValue(Story.class);
                        if (timeCurrent > story.getTimestart() && timeCurrent < story.getTimeend() ) {
                            countStory++;

                        }
                        if (countStory > 0) {

                            storyList.add(story);
                            storyList1.add(story);
                            for (Story story1 : storyList){
                                System.out.println(story1 + "tho1111");
                            }

                            //storyList.add(story);
                        }





                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readStory() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long timeCurrent = System.currentTimeMillis();
                storyList.clear();
                storyList.add(new Story("", 0, 0, "",
                        FirebaseAuth.getInstance().getCurrentUser().getUid()));
                for (String id : followingList) {

                    int countStory = 0;
                    Story story = null;
                    for (DataSnapshot dataSnapshot : snapshot.child(id).getChildren()) {

                        story = dataSnapshot.getValue(Story.class);
                        if (timeCurrent > story.getTimestart() && timeCurrent < story.getTimeend()) {
                            countStory++;

                        }
                        if (countStory > 0) {

                            storyList1.add(story);
                            for (Story story1 : storyList1){
                                if (!storyList.contains(story1)){
                                    storyList.add(story1);

                                }


                            }


//                            if (!storyList.equals(story)){
//                                storyList.add(story);
//
//                            }





                        }

                        System.out.println(storyList+"tho1");


                    }
                    storyAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}