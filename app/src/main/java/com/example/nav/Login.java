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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class  Login extends AppCompatActivity
{

    private FirebaseAuth mAuth;
    DatabaseReference NavDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;

    Button btnLogin;
    Button btnReg;
    TextView loginemail;
    TextView loginpword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null){
            mAuth.signOut();
        }
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };

        NavDatabase = FirebaseDatabase.getInstance().getReference("User");

        loginemail = (TextView) findViewById(R.id.txteEmail);
        loginpword = (TextView) findViewById(R.id.txtePword);

        btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            //todo check mAuth preload...OnClick sometimes takes two clicks
            public void onClick(View v)
            {
                vibrate();

                try {

                    String e = loginemail.getText().toString(), p = loginpword.getText().toString();
                    mAuth.signInWithEmailAndPassword(e, p);
                    Toast.makeText(Login.this, "input\n"+e+"\n"+p+"\n"+mAuth.getCurrentUser(), Toast.LENGTH_LONG).show();

                    if(mAuth.getCurrentUser() != null){

                        Toast.makeText(Login.this, "Signed in as " + mAuth.getCurrentUser().getEmail(), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(Login.this, MainActivity.class));
                    }

                    if(mAuth.getCurrentUser() == null){
                        Toast.makeText(Login.this, "Incorrect Details" , Toast.LENGTH_LONG).show();
                    }

                }catch (Exception e){
                    Toast.makeText(Login.this, e.toString() + "Incorrect Details", Toast.LENGTH_LONG).show();
                }

            }

        });
        btnReg = (Button) findViewById(R.id.btnReg);
        btnReg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate();
                    startActivity(new Intent(Login.this, Reg.class));
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
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