package login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.samman.main.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import map.MapActivity;
import resetpassword.ResetPasswordActivity;
import root.App;
import signup.SignUpActivity;

public class LoginActivity extends AppCompatActivity
{
    @Inject
    LoginActivityMVP.Presenter presenter;

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

        ((App)getApplication()).getComponent().inject(this);

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
                progressDialog=ProgressDialog.show(this,"LoginActivity Progress","Loggin in",true);
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    Toast.makeText(LoginActivity.this, "LogIn successful", Toast.LENGTH_LONG).show();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
        Intent reset=new Intent(LoginActivity.this, ResetPasswordActivity.class);
        startActivity(reset);
    }

    public  void onButtonRegisterClick(View v){
        Intent reset=new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(reset);
    }

}
