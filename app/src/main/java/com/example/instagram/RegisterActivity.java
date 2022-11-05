package com.example.instagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    EditText username,fullName,email,password;
    TextView tvLogin;
    Button btRegister;

    FirebaseAuth auth;
    DatabaseReference reference;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        username = findViewById(R.id.etUsername);
        fullName = findViewById(R.id.etFullname);
        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);

        tvLogin = findViewById(R.id.tvLogin);
        btRegister = findViewById(R.id.btRegister);
        auth = FirebaseAuth.getInstance();


        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd =  new ProgressDialog(RegisterActivity.this);
                pd.setMessage("Please wait ...");
                pd.show();

                String str_username = username.getText().toString();
                String str_fullName = fullName.getText().toString();
                String str_email = email.getText().toString();
                String str_password = password.getText().toString();

                System.out.println(str_username+ ""+str_fullName+""+str_password+""+str_email);
                if(TextUtils.isEmpty(str_username) || TextUtils.isEmpty(str_fullName)||TextUtils.isEmpty(str_email)||TextUtils.isEmpty(str_password)){
                    Toast.makeText(RegisterActivity.this, "All fileds are required!", Toast.LENGTH_SHORT).show();
                }else if(str_password.length() <= 6){
                    Toast.makeText(RegisterActivity.this, "Password must hava > 6 characters", Toast.LENGTH_SHORT).show();
                }else{
                    register(str_username,str_fullName,str_email,str_password);
                }
            }
        });


    }

    private void register(String username ,String fullName,String email,String password) {
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            String userid = firebaseUser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("id",userid);
                            hashMap.put("username",username.toLowerCase());
                            hashMap.put("fullname",fullName);
                            hashMap.put("bio","");
                            hashMap.put("imageURL","https://firebasestorage.googleapis.com/v0/b/instagram-a974d.appspot.com/o/placeholder.jpg?alt=media&token=9fb6a02b-57fb-4d15-a121-5151e7149889");

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        pd.dismiss();
                                        Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(RegisterActivity.this, "You can't register with this email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}