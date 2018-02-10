package com.carmichael.swip;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class NewAccountActivity extends AppCompatActivity {

    public static final String TAG = "NewAccountActivity";
    EditText etFirstName;
    EditText etLastName;
    EditText etZipcode;
    EditText etEmail;
    EditText etPassword;
    EditText etConfirmPassword;
    EditText etPhone;
    Button btnCreateAccount;
    private FirebaseAuth mAuth;
    FirebaseUser fUser;
    FirebaseDatabase database;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etZipcode = (EditText) findViewById(R.id.etZipcode);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);
        btnCreateAccount = (Button) findViewById(R.id.btnCreateAccount);

        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        mAuth = FirebaseAuth.getInstance();

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make sure all fields are filled
                if(etFirstName.getText().toString().isEmpty() ||
                        etLastName.getText().toString().isEmpty() ||
                        etZipcode.getText().toString().isEmpty() ||
                        etEmail.getText().toString().isEmpty() ||
                        etPassword.getText().toString().isEmpty() ||
                        etConfirmPassword.getText().toString().isEmpty() ||
                        etPhone.getText().toString().isEmpty()){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewAccountActivity.this);
                    alertDialogBuilder
                            .setTitle("Unable to Create Account")
                            .setMessage("Please make sure all fields are filled out.")
                            .setCancelable(true)
                            .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }else{
                    if(etZipcode.getText().toString().length() < 5){
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewAccountActivity.this);
                        alertDialogBuilder
                                .setTitle("Unable to Create Account")
                                .setMessage("Please enter a five digit zip code.")
                                .setCancelable(true)
                                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }else if (!etPassword.getText().toString().equals(etConfirmPassword.getText().toString())){
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewAccountActivity.this);
                        alertDialogBuilder
                                .setTitle("Unable to Create Account")
                                .setMessage("Password entries do not match.")
                                .setCancelable(true)
                                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }else if(etPhone.getText().toString().length() < 10){
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewAccountActivity.this);
                        alertDialogBuilder
                                .setTitle("Unable to Create Account")
                                .setMessage("Please enter a ten digit zip phone number.")
                                .setCancelable(true)
                                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }else{
                        CreateAccount();
                    }
                }
            }
        });

    }

    public void CreateAccount(){
        mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            fUser = mAuth.getCurrentUser();

                            mDatabase.child("Users").child(fUser.getUid())
                                    .child("zipcode").setValue(etZipcode.getText().toString());

                            mDatabase.child("Users").child(fUser.getUid())
                                    .child("phone").setValue(etPhone.getText().toString());

                            mDatabase.child("Users").child(fUser.getUid())
                                    .child("firstName").setValue(etFirstName.getText().toString());

                            mDatabase.child("Users").child(fUser.getUid())
                                    .child("lastName").setValue(etLastName.getText().toString());
                            
                            UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
                            builder.setDisplayName(etFirstName.getText().toString() + " " +
                                etLastName.getText().toString());
                            UserProfileChangeRequest build = builder.build();
                            fUser.updateProfile(build);
                            Toast.makeText(NewAccountActivity.this, "Successfully created account", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(NewAccountActivity.this, SignInActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewAccountActivity.this);
                            alertDialogBuilder
                                    .setTitle("Unable to Create Account")
                                    .setMessage(task.getException().getMessage().toString()
                                    + " Please try again.")
                                    .setCancelable(true)
                                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {
                                            dialog.dismiss();
                                        }
                                    });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                    }
                });
    }
}
