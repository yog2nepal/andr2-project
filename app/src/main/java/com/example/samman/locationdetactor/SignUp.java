package com.example.samman.locationdetactor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity implements View.OnClickListener {

    //firebase part
    private FirebaseAuth firebaseAuth;

    private Button buttonRegister;
    private Button buttonSignUp;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextName;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        this.firebaseAuth=FirebaseAuth.getInstance();

        editTextEmail = (EditText) findViewById(R.id.email);
        editTextName  = (EditText) findViewById(R.id.Name);
        editTextPassword = (EditText) findViewById(R.id.password);

        buttonRegister = (Button) findViewById(R.id.buttonRegister);

        //attaching listener to button
        buttonRegister.setOnClickListener(this);

    }
    private void registerUser(){

        //getting email and password from edit texts
        String email = editTextEmail.getText().toString().trim();
        final String name=editTextName.getText().toString().trim();
        String password  = editTextPassword.getText().toString().trim();

        //checking if email and passwords are empty
        if(email.isEmpty()||name.isEmpty()||password.isEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.signup_error_message)
                    .setTitle(R.string.signup_error_title)
                    .setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else{
            if(firebaseAuth!=null) {
                progressDialog=ProgressDialog.show(this,"Sign up in progress","Logging in",true);
                //creating a new user
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //checking if success
                                if (task.isSuccessful()) {
                                    //display some message here
                                    Toast.makeText(SignUp.this, "Successfully registered", Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(SignUp.this, Map.class);
                                    intent.putExtra("name", name);
                                    startActivity(intent);
                                    Toast.makeText(SignUp.this, "You are logged in", Toast.LENGTH_LONG).show();
                                } else {
                                    //display some message here
                                    Toast.makeText(SignUp.this, "Registration Error", Toast.LENGTH_LONG).show();
                                    if(progressDialog!=null)
                                        progressDialog.dismiss();
                                }
                                //  progressDialog.dismiss();
                            }
                        });
            }
        }
    }

    @Override
    public void onClick(View view) {
        //calling register method on click
        registerUser();
    }
}
