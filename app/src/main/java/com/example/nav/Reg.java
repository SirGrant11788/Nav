package com.example.nav;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Reg extends AppCompatActivity {

    DatabaseReference NavDatabase;

    EditText editTextEmail;
    TextView editTextPassword;
    Button submit;

    private String email=null;
    private String password=null;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "Reg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };

        NavDatabase = FirebaseDatabase.getInstance().getReference("User");

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword=(EditText) findViewById(R.id.editTextPassword);

       submit = (Button) findViewById(R.id.submit);
       submit.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               vibrate();
               addUser();
           }
       });
    }//end oncreate

    private void addUser() {

        setEmail(editTextEmail.getText().toString());
        setPassword(editTextPassword.getText().toString());

        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        try {
            if (!(TextUtils.isEmpty(email) && TextUtils.isEmpty(password))) {
                mAuth.createUserWithEmailAndPassword(email, password);
                Toast.makeText(Reg.this, "User created: "+email, Toast.LENGTH_LONG).show();

                startActivity(new Intent(Reg.this, Login.class));
            } else {
                Toast.makeText(Reg.this, "Please enter details", Toast.LENGTH_LONG).show();

            }
        }catch (Exception e){
            Toast.makeText(Reg.this, e + " register", Toast.LENGTH_LONG).show();
        }
    }


    public Reg(){

        this.setEmail(email);
        this.setPassword(password);

    }

@Override
public void onStart() {
    super.onStart();
    // Check if user is signed in (non-null) and update UI accordingly.
    FirebaseUser currentUser = mAuth.getCurrentUser();
    //
    // updateUI(currentUser);
}
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @SuppressLint("MissingPermission")
    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(100);
        }
    }

}
