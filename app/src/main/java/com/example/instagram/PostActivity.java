package com.example.instagram;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {


    Uri imageUri;
    String myUrl = "";
    StorageTask uploadTask;
    StorageReference storageReference;

    ImageView imgClose, imgAdded;
    TextView tvPost;
    EditText etDescription;
    private static final int IMAGE_REQUEST = 1;
    private final int CODE_IMG_GALLERY= 1;
    private String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        imgClose = findViewById(R.id.imgClose);
        tvPost = findViewById(R.id.tvPost);
        imgAdded = findViewById(R.id.imgAdded);
        etDescription = findViewById(R.id.etDescription);

        //
        storageReference = FirebaseStorage.getInstance().getReference("posts");
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }
        });
        imgAdded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  openImage();
                openImage1();
            }
        });
        tvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();


            }
        });
       // CropImage.activity().setAspectRatio(1,1).setCropShape(CropImageView.CropShape.Oval).start(EditProfileActivity.this);
//        UCrop.of(imageUri,Uri.fromFile(new File(getCacheDir(),"logo.png")))
//                .withAspectRatio(16, 9)//.withMaxResultSize(50  , maxHeight)
//                .start(PostActivity.this);





    }

//
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();
        if (imageUri != null) {
            StorageReference filerefrence = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            uploadTask = filerefrence.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filerefrence.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {

                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                        String postid = reference.push().getKey();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("postid", postid);
                        hashMap.put("postimage", myUrl);
                        hashMap.put("description", etDescription.getText().toString());
                        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

                        reference.child(postid).setValue(hashMap);

                        progressDialog.dismiss();

                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(PostActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
        }


    }



//    private void openImage() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(intent.ACTION_GET_CONTENT);
//        startActivityForResult(intent, IMAGE_REQUEST);
//
//    }

    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if( requestCode == IMAGE_REQUEST && resultCode == RESULT_OK ){//requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUSET_CODE &&
//            imgAdded .setImageURI(imageUri);
//        }else {
//            Toast.makeText(this, "Something gone wrong !", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(PostActivity.this,MainActivity.class));
//        }
   // }
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK) {
//            imageUri = data.getData();
//
//            imgAdded.setImageURI(imageUri);
//        } else {
//            Toast.makeText(this, "Something gone wrong !", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(PostActivity.this, MainActivity.class));
//        }
//    }
    private void openImage1(){
        startActivityForResult(new Intent()
                .setAction(Intent.ACTION_GET_CONTENT)
                .setType("image/*"),CODE_IMG_GALLERY);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CODE_IMG_GALLERY  && resultCode == RESULT_OK) {
           Uri imageUri1  = data.getData();
            if (imageUri1 != null){
                startCrop(imageUri1);
            }

        }else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK){
            imageUri = UCrop.getOutput(data);
            if (imageUri != null){
                imgAdded.setImageURI(imageUri);
            }

        }
    }
    private void  startCrop(@NonNull Uri uri){
        String destinationFileName =SAMPLE_CROPPED_IMG_NAME ;
        destinationFileName += ".pnj";
        UCrop uCrop = UCrop.of(uri,Uri.fromFile(new File(getCacheDir(),destinationFileName)));
        uCrop.withAspectRatio(16, 9);
        uCrop.withAspectRatio(1,1);
        uCrop.withMaxResultSize(450,450);
        uCrop.withOptions(getOptions());
        uCrop.start(PostActivity.this);

    }

    private UCrop.Options getOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(70);
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(true);

       // options.setStatusBarColor();
        options.setToolbarTitle("Cai lai");
        return options;
    }
}