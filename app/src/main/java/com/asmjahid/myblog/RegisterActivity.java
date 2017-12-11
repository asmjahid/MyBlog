package com.asmjahid.myblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mRegisterBtn;
    private ProgressDialog mProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mProgress = new ProgressDialog(this);

        mNameField = findViewById(R.id.nameField);
        mEmailField = findViewById(R.id.emailField);
        mPasswordField = findViewById(R.id.passwordField);
        mRegisterBtn = findViewById(R.id.registerBtn);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegister();
            }
        });
    }

    private void startRegister() {

        final String name = mNameField.getText().toString().trim();
        String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            mProgress.setMessage("Signing........");
            mProgress.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        /*String user_id = mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db = mDatabase.child(user_id);
                        current_user_db.child("name").setValue(name);
                        current_user_db.child("image").setValue("default");
                        */
                        mProgress.dismiss();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else {
                        //FirebaseAuthException e = (FirebaseAuthException )task.getException();
                        Toast.makeText(RegisterActivity.this, "Failed Registration: "/*+e.getMessage()*/, Toast.LENGTH_SHORT).show();
                        mProgress.hide();
                        return;
                    }
                }
            });
        }

    }
}
