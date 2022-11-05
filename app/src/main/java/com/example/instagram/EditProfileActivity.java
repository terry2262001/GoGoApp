package com.example.instagram;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.instagram.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {
    ImageView imgClose,imgProfile;
    TextView tvSave,tvChange;
    MaterialEditText etFullname,etUsername,etBio;

    FirebaseUser firebaseUser;

    private Uri mImageUri ;
    private StorageTask uploadTask;
    StorageReference storageRef;

    private static final int IMAGE_REQUEST = 1;
    private final int CODE_IMG_GALLERY= 1;
    private String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        imgClose = findViewById(R.id.imgClose);
        imgProfile = findViewById(R.id.imgProfile);
        tvSave = findViewById(R.id.tvSave);
        tvChange = findViewById(R.id.tvChange);
        etFullname = findViewById(R.id.etFullname);
        etUsername = findViewById(R.id.etUsername);
        etBio = findViewById(R.id.etBio);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference().child("uploads");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                etFullname.setText(user.getFullName());
                etUsername.setText(user.getUsername());
                etBio.setText(user.getBio());
                Glide.with(getApplicationContext()).load(user.getImageURL()).into(imgProfile);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tvChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  CropImage.activity().setAspectRatio(1,1).setCropShape(CropImageView.CropShape.Oval).start(EditProfileActivity.this);
                openImage();

            }
        });
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile(etFullname.getText().toString(),etUsername.getText().toString(),etBio.getText().toString());

            }
        });

    }

    private void updateProfile(String fullname, String username, String bio) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("fullname",fullname);
        hashMap.put("username",username);
        hashMap.put("bio",bio);
        reference.updateChildren(hashMap);

    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    private void uploadImage(){
        ProgressDialog pd  = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();
        if(mImageUri != null){
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                    +"."+getFileExtension(mImageUri));
            uploadTask = fileReference.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                            throw  task.getException();
                    }
                    return  fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String myUrl = downloadUri.toString();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("imageURL",""+myUrl);
                        reference.updateChildren(hashMap);
                        pd.dismiss();
                    }else{
                        Toast.makeText(EditProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImage(){
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
            mImageUri = UCrop.getOutput(data);
            if (mImageUri != null){
                imgProfile.setImageURI(mImageUri);
                uploadImage();
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
        uCrop.start(EditProfileActivity.this);

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