package com.george.getreadychat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.george.getreadychat.data.UserDetails;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UserToUserMessageNotification extends AppCompatActivity {

    //check if activity is active
    static boolean isActiveNotification = false;

    private static final int RC_PHOTO_PICKER = 2;

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    private int lastListViewPosition;

    private ListView mMessageListView;
    private UserMessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;
    private LinearLayout emptyLinearLayout;

    private List<UserMessage> userMessages;

    private FirebaseDatabase mFirebaseDatabase;

    //A class that reference to spesific part of database
    private DatabaseReference mMessagesDatabaseReference;
    private DatabaseReference mMessagesDatabaseReferenceSecondName;

    //Child event listener to understand that has new messages
    private ChildEventListener mDeliveryChildEventListener;
    private ChildEventListener mChildEventListener;

    //instance of firebase storage
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;

    private StatusBarNotification mStatusBarNotification;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_to_user_message);

        isActiveNotification = true;


        Intent intent = getIntent();
        UserDetails.UserChatsWith = intent.getStringExtra("chatsWith");
        /*String ad = intent.getStringExtra("title");*/
        Log.e("UserChatsWith", UserDetails.UserChatsWith);


        /*Toast.makeText(this,"Second User= " + UserDetails.secondUser,Toast.LENGTH_LONG).show();*/

        //Instantiating the database..access point of the database reference
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        //initializing the storage
        mFirebaseStorage = FirebaseStorage.getInstance();

        //making the references
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child(UserDetails.username);
        mMessagesDatabaseReferenceSecondName = mFirebaseDatabase.getReference().child(UserDetails.UserChatsWith);

        //making the reference for the storage
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");

        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);
        emptyLinearLayout = (LinearLayout) findViewById(R.id.emptyLinearLayout);

        // Initialize message ListView and its adapter
        userMessages = new ArrayList<>();
        mMessageAdapter = new UserMessageAdapter(this, R.layout.item_message, userMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        mMessageListView.setEmptyView(emptyLinearLayout);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Creating a message
                UserMessage userMessage = new UserMessage(mMessageEditText.getText().toString(), UserDetails.username, null, null, getTheDateTime(), UserDetails.notReaded, getTimestampInMIliseconds());
                //The push method is exactly what you want to be using in this case because you need a new id generated for each message
                mMessagesDatabaseReference.child(UserDetails.UserChatsWith).push().setValue(userMessage);


                UserMessage userMessage2 = new UserMessage(mMessageEditText.getText().toString(), UserDetails.username, null, null, getTheDateTime(), UserDetails.notReaded, getTimestampInMIliseconds());
                //The push method is exactly what you want to be using in this case because you need a new id generated for each message
                mMessagesDatabaseReferenceSecondName.child(UserDetails.username).push().setValue(userMessage2);


                // Clear input box
                mMessageEditText.setText("");

                //Hide softKeybord
                /*InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);*/
            }
        });

        /*attachDatabaseReadListener();*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            //to check in the log
            String image = selectedImageUri.toString();
            Log.e("PhotoUri:", image);

            Toast.makeText(UserToUserMessageNotification.this, getResources().getString(R.string.uploading_image), Toast.LENGTH_SHORT).show();

            //reference of the last segment of the uri
            StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());

            //upload with putfile method
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    UserMessage userMessage = new UserMessage(null, UserDetails.username, null, downloadUrl.toString(), getTheDateTime(), UserDetails.readed, getTimestampInMIliseconds());
                    mMessagesDatabaseReference.child(UserDetails.UserChatsWith).push().setValue(userMessage);

                    UserMessage userMessage2 = new UserMessage(null, UserDetails.username, null, downloadUrl.toString(), getTheDateTime(), UserDetails.notReaded, getTimestampInMIliseconds());
                    //The push method is exactly what you want to be using in this case because you need a new id generated for each message
                    mMessagesDatabaseReferenceSecondName.child(UserDetails.username).push().setValue(userMessage2);

                }
            });
        }
    }

    private void attachDatabaseReadListenerDeliveryStatus() {
        if (mDeliveryChildEventListener == null) {
            // Child event listener
            mDeliveryChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    /*UserMessage userMessage = dataSnapshot.getValue(UserMessage.class);
                    userMessage.setIsReaded("true");
                    mMessageAdapter.add(userMessage);*/


                    String datasnapshoti = dataSnapshot.getKey();
                    mMessagesDatabaseReference.child(UserDetails.UserChatsWith).child(datasnapshoti).child("isReaded").setValue("true");

                    String datasnapshotOfLastMessage = mMessagesDatabaseReferenceSecondName.child(UserDetails.username).getKey();
                    Log.e("datasnapsot", datasnapshoti + "----" + datasnapshotOfLastMessage);

                    mMessageAdapter.notifyDataSetChanged();
                    ////
                    /*friendlyMessage.setIsReaded(true);*/

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    mMessageAdapter.notifyDataSetChanged();

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

            mMessagesDatabaseReference.child(UserDetails.UserChatsWith).addChildEventListener(mDeliveryChildEventListener);
        }

    }

    private void attachDatabaseReadListenertoListView() {
        if (mChildEventListener == null) {
            // Child event listener
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    /*getAllMessagesBetweenUsers(dataSnapshot);*/
                    /////working
                    /*UserMessage userMessage = dataSnapshot.getValue(UserMessage.class);
                    mMessageAdapter.add(userMessage);
                    mMessageAdapter.notifyDataSetChanged();*/
                    ///working

                    UserMessage userMessagee = dataSnapshot.getValue(UserMessage.class);

                    userMessages.add(userMessagee);

                    mMessageAdapter.notifyDataSetChanged();

                    String datasnapshoti = dataSnapshot.getKey();
                    String datasnapshotOfLastMessage = mMessagesDatabaseReferenceSecondName.child(UserDetails.username).getKey();
                    Log.e("datasnapsotToListView", datasnapshoti + "----" + datasnapshotOfLastMessage);


                    ////
                    /*friendlyMessage.setIsReaded(true);*/


                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                   /*UserMessage userMessage = dataSnapshot.getValue(UserMessage.class);
                    mMessageAdapter.add(userMessage);
                    mMessageAdapter.notifyDataSetChanged();*/

                    /*updateItemAtPosition(lastListViewPosition);*/

                    /*//remove last item
                    mMessageListView.removeViewAt(mMessageListView.getLastVisiblePosition());
                    mMessageAdapter.notifyDataSetChanged();
*/
                    /*UserMessage userMessage = dataSnapshot.getValue(UserMessage.class);
                    mMessageAdapter.add(userMessage);*/

                    /*runOnUiThread(new Runnable() {
                        public void run() {
                            mMessageAdapter.notifyDataSetChanged();
                        }
                    });*/

                    /*recreate();
                    mMessageAdapter.notifyDataSetChanged();*/

                    /////////
                    userMessages.remove(userMessages.size() - 1);

                    UserMessage userMessagee = dataSnapshot.getValue(UserMessage.class);

                    userMessages.add(userMessagee);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            mMessageAdapter.notifyDataSetChanged();
                        }
                    });

                    /////////


                    /*mMessageAdapter.notifyDataSetChanged();*/


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

            mMessagesDatabaseReferenceSecondName.child(UserDetails.username).addChildEventListener(mChildEventListener);
        }
    }

    @Override
    protected void onStart() {

        //to read messages for discovering/refreshing the delivery status
        attachDatabaseReadListenerDeliveryStatus();

        //for loading messages to the listview
        attachDatabaseReadListenertoListView();


        super.onStart();
        isActiveNotification = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {

        mMessagesDatabaseReference.child(UserDetails.UserChatsWith).removeEventListener(mDeliveryChildEventListener);
        mMessagesDatabaseReferenceSecondName.child(UserDetails.username).removeEventListener(mChildEventListener);
        mChildEventListener = null;
        mDeliveryChildEventListener = null;

        super.onPause();
        isActiveNotification = false;
    }


    @Override
    protected void onStop() {

        if (mChildEventListener != null) {
            mMessagesDatabaseReference.child(UserDetails.UserChatsWith).removeEventListener(mDeliveryChildEventListener);
            mMessagesDatabaseReferenceSecondName.child(UserDetails.username).removeEventListener(mChildEventListener);
            mChildEventListener = null;
            mDeliveryChildEventListener = null;
        }
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        isActiveNotification = true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_total_messages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.action_totalmessages:
                Intent intentToTotalMessages = new Intent(UserToUserMessageNotification.this, TotalMessages.class);
                startActivity(intentToTotalMessages);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getTheDateTime() {
        DateFormat df = new SimpleDateFormat("EEE, d MMM, HH:mm");
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }

    private long getTimestampInMIliseconds() {
        Date curDate = new Date();
        long curMillis = curDate.getTime();
        return curMillis;
    }

}