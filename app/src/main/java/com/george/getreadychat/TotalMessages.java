package com.george.getreadychat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

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

    private List<String> userNameMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_messages);

        /*Toast.makeText(this,"First User= "+UserDetails.username+"---Second User= " + UserDetails.secondUser,Toast.LENGTH_LONG).show();*/

        mMessageListView = (ListView) findViewById(R.id.allMessageListView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child(UserDetails.username);

        // Initialize message ListView and its adapter
        userNameMessages = new ArrayList<>();
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

                Intent a = new Intent(TotalMessages.this, UserToUserMessage.class);
                startActivity(a);


            }
        });

        mMessageListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, final View view, final int position, long l) {

                TextView textView = (TextView) view.findViewById(R.id.allMessageTextView);
                final String text = textView.getText().toString();

                // Create an AlertDialog.Builder and set the message, and click listeners
                // for the postivie and negative buttons on the dialog.
                AlertDialog.Builder builder = new AlertDialog.Builder(TotalMessages.this);
                builder.setMessage("Delete conversation?");
                builder.setPositiveButton(getResources().getString(R.string.dialogYes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Delete" button, so delete the product.
                        mMessagesDatabaseReference.child(text).setValue(null);
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.dialogCancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Cancel" button, so dismiss the dialog
                        // and continue editing the product.
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

                // Create and show the AlertDialog
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
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
                    userNameMessages.add(datasnapshoti);
                    mMessageAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    mMessageAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                    String datasnapshoti = dataSnapshot.getKey();

                    for(String removedString : userNameMessages){

                        if(datasnapshoti.equals(removedString)){
                            userNameMessages.remove(removedString);
                            mMessageAdapter.notifyDataSetInvalidated();
                            break;
                        }
                    }


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
