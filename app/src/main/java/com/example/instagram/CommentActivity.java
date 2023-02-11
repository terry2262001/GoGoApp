package com.example.instagram;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.Model.Comment;
import com.example.instagram.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.instagram.Adapter.CommentAdapter;

public class CommentActivity extends AppCompatActivity {
    private RecyclerView rvComment;
    private List<Comment> commentLists;
    private CommentAdapter commentAdapter ;


    TextView tvPost;
    ImageView imgProfile;
    EditText etAddComment;

    String postId;
    String publisherid;


    FirebaseUser firebaseUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        Intent intent = getIntent();
        postId = intent.getStringExtra("postid");
        publisherid = intent.getStringExtra("publisherid");

        rvComment = findViewById(R.id.rvComment);
        rvComment.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvComment.setLayoutManager(linearLayoutManager);
        commentLists = new ArrayList<>();
        commentAdapter = new CommentAdapter(this,commentLists,postId);
        rvComment.setAdapter(commentAdapter);

        etAddComment = findViewById(R.id.etAddComment);
        tvPost = findViewById(R.id.tvPost);
        imgProfile = findViewById(R.id.imgProfile);

        //
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //

        tvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etAddComment.getText().toString().equals("")){
                    Toast.makeText(CommentActivity.this, "You can't send empty comment", Toast.LENGTH_SHORT).show();
                }else{
                    addComment();
                }
            }
        });
        getImage();
        readComments();


    }

    private void addComment() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);

        String commentid  = reference.push().getKey();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("comment",etAddComment.getText().toString());
        hashMap.put("publisher",firebaseUser.getUid());


       // reference.push().setValue(hashMap);
        reference.child(commentid).setValue(hashMap);
        addNotifications();
        etAddComment.setText("Add comment...");


    }
    private void addNotifications(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(publisherid);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("text","commented: "+etAddComment.getText().toString());
        hashMap.put("postid",postId);
        hashMap.put("ispost",true);

        reference.push().setValue(hashMap);
    }

    private void getImage(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getImageURL()).into(imgProfile);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private  void readComments(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentLists.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Comment comment = dataSnapshot.getValue(Comment.class);
                        commentLists.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}