package com.george.getreadychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.george.getreadychat.data.UserDetails;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TotalMessages extends AppCompatActivity {

    private TotalMessagesAdapter mMessageAdapter;
    private ListView mMessageListView;

    private FirebaseDatabase mFirebaseDatabase;

    //A class that reference to spesific part of database
    private DatabaseReference mMessagesDatabaseReference;
    private DatabaseReference mMessagesDatabaseReferenceSecondName;

    //Child event listener to understand that has new messages
    private ChildEventListener mChildEventListener;
    private ChildEventListener mChildEventListener2;

    //Value event listener
    private ValueEventListener mValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_messages);

        /*Toast.makeText(this,"First User= "+UserDetails.username+"---Second User= " + UserDetails.secondUser,Toast.LENGTH_LONG).show();*/

        mMessageListView = (ListView) findViewById(R.id.allMessageListView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child(UserDetails.username);

        // Initialize message ListView and its adapter
        List<String> userNameMessages = new ArrayList<>();
        mMessageAdapter = new TotalMessagesAdapter(this, R.layout.item_all_message, userNameMessages);
        mMessageListView.setAdapter(mMessageAdapter);


        attachDatabaseReadListener();

        //adding listener to listview
        mMessageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView textView = (TextView) view.findViewById(R.id.allMessageTextView);
                String text = textView.getText().toString();

                /*mMessagesDatabaseReference2 = mFirebaseDatabase.getReference().child(strUsername).child(text);*/
                UserDetails.secondUser = text;

/*
                /////
                ////////
                if (mChildEventListener2 == null) {
                    // Child event listener
                    mChildEventListener2 = new ChildEventListener() {

                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                   *//* UserNameMessage usersMessage = dataSnapshot.getValue(UserNameMessage.class);
                    String datasnapshoti = dataSnapshot.getKey();
                    Log.e("datasnapsotAllMessages",datasnapshoti);
                    mMessageAdapter.add(usersMessage);*//*


                            string2 = dataSnapshot.getKey();
                            Log.e("454545", string2);

                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };

                    mMessagesDatabaseReference2.addChildEventListener(mChildEventListener2);


                }

                mChildEventListener2 =null;*/

                Intent a = new Intent(TotalMessages.this, UserToUserMessage.class);
                startActivity(a);

            }
        });
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            // Child event listener
            mChildEventListener = new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                   /* UserNameMessage usersMessage = dataSnapshot.getValue(UserNameMessage.class);
                    String datasnapshoti = dataSnapshot.getKey();
                    Log.e("datasnapsotAllMessages",datasnapshoti);
                    mMessageAdapter.add(usersMessage);*/


                    String datasnapshoti = dataSnapshot.getKey();
                    Log.e("datasnapsotAllMessages", datasnapshoti);
                    mMessageAdapter.add(datasnapshoti);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }

    }
}
