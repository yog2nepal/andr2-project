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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    //firebase part
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    //controls
    protected EditText emailEditText;
    protected EditText passwordEditText;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.firebaseAuth=null;
        this.firebaseUser=null;
        this.emailEditText = (EditText) findViewById(R.id.emailField);
        this.passwordEditText = (EditText) findViewById(R.id.passwordField);
    }

    public  void onButtonLoginClick(View view){
        if(this.firebaseUser==null) {
            this.firebaseAuth=FirebaseAuth.getInstance();
            this.firebaseUser=this.firebaseAuth.getCurrentUser();
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            email = email.trim();
            password = password.trim();
            if (email.isEmpty() || password.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.login_error_message)
                        .setTitle(R.string.login_error_title)
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                progressDialog=ProgressDialog.show(this,"Login Progress","Loggin in",true);
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(Login.this, Map.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    Toast.makeText(Login.this, "LogIn successful", Toast.LENGTH_LONG).show();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                                    builder.setMessage(task.getException().getMessage())
                                            .setTitle(R.string.login_error_title)
                                            .setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                    if(progressDialog!=null)
                                        progressDialog.dismiss();
                                }
                            }
                        });
            }
        }
        else
        {
            if(this.firebaseAuth!=null&&this.firebaseUser!=null) {
                this.firebaseAuth.signOut();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                email = email.trim();
                password = password.trim();
                if (email.isEmpty() || password.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.login_error_message)
                            .setTitle(R.string.login_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    if(progressDialog!=null)
                        progressDialog.dismiss();
                }
                this.firebaseAuth = null;
                this.firebaseUser = null;
            }
        }
    }

    public  void onButtonResetClick(View v) {
        Intent reset=new Intent(Login.this,ResetPassword.class);
        startActivity(reset);
    }

    public  void onButtonRegisterClick(View v){
        Intent reset=new Intent(Login.this,SignUp.class);
        startActivity(reset);
    }

}
