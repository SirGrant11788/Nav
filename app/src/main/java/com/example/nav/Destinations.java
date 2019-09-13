package com.example.nav;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Destinations extends AppCompatActivity {

    ListView list;
    List<String> arrayList;
    String[] values = {"Previous Destinations Listed Below"};//remove test
    private static final String TAG = "Destinations";
    private String email;
     static String clickDest;
    static boolean blDest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destinations);

        list = (ListView)findViewById(R.id.dest);

        arrayList = new ArrayList<String>();
        Collections.addAll(arrayList,values);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arrayList);
        list.setAdapter(arrayAdapter);

        try {
//get current user
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                email = user.getEmail();
                // Check if user's email is verified
                boolean emailVerified = user.isEmailVerified();
                // The user's ID, unique to the Firebase project. Do NOT use this value to
                // authenticate with your backend server, if you have one. Use
                // FirebaseUser.getIdToken() instead.
                String uid = user.getUid();
            }

            final DatabaseReference History = FirebaseDatabase.getInstance().getReference();
            //read from database
            History.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot child: dataSnapshot.getChildren())//int i=0;i<=dataSnapshot.getChildrenCount()+1;i++
                    {
                        String val = (child.getValue().toString()).substring(1,21);
                        String mail = dataSnapshot.child("History").child(val).child("email").getValue(String.class);//.child("email").getValue(String.class);
                    String location = dataSnapshot.child("History").child(val).child("search").getValue(String.class);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

//try get all
            DatabaseReference ref1= FirebaseDatabase.getInstance().getReference();
            DatabaseReference ref2,ref3,ref4;
            ref2 = ref1.child("History");

            ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Result will be holded Here
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        if(dsp.child("email").getValue().toString().equals(email)) {
                            arrayList.add((dsp.child("search").getValue().toString())); //add result into array list
                            arrayAdapter.notifyDataSetChanged();

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "Failed to read value.", databaseError.toException());
                }
            });


        }catch (Exception e){
            Toast.makeText(Destinations.this, "Destinations!:  "+e, Toast.LENGTH_LONG).show();
        }


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView <? > arg0, View view, int position, long id) {
                // When clicked, show a toast with the TextView text
//                Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
//                        Toast.LENGTH_SHORT).show();
                clickDest =""+ ((TextView) view).getText();
                blDest=true;
                startActivity(new Intent(Destinations.this, MainActivity.class));

            }

        });

    }

}
