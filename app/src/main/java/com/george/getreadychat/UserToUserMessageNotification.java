package com.george.getreadychat;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
    private UserMessageAdapterBubbles mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;
    private LinearLayout emptyLinearLayout;
    private FrameLayout mFrameLayout;

    private List<UserMessage> userMessages;

    private FirebaseDatabase mFirebaseDatabase;

    //A class that reference to spesific part of database
    private DatabaseReference mMessagesDatabaseReference;
    private DatabaseReference mMessagesDatabaseReferenceSecondName;
    private DatabaseReference mMessagesDatabaseReferenceUsernameToUsername;

    //Child event listener to understand that has new messages
    private ChildEventListener mDeliveryChildEventListener;
    private ChildEventListener mChildEventListener;
    private ChildEventListener mShopChildEventListener;

    //instance of firebase storage
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;

    private StatusBarNotification mStatusBarNotification;

    private MediaPlayer mMediaPlayer, mMediaPlayer2;
    private int maxVolume;
    private int currVolume, currVolume2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_to_user_message);

        isActiveNotification = true;

        Intent intent = getIntent();
        UserDetails.UserChatsWith = intent.getStringExtra("chatsWith");
        UserDetails.UserChatsWithID = intent.getStringExtra("chatsWithID");
        Log.e("UserChatsWith", UserDetails.UserChatsWith + UserDetails.UserChatsWithID);

        SharedPreferences mUsernameInfo = PreferenceManager.getDefaultSharedPreferences(UserToUserMessageNotification.this);
        UserDetails.username = mUsernameInfo.getString("usernameusername", "");
        UserDetails.usernameID = mUsernameInfo.getString("usernameIDusernameID", "");

        SharedPreferences mUsersInfoNotification = PreferenceManager.getDefaultSharedPreferences(UserToUserMessageNotification.this);
        SharedPreferences.Editor editor = mUsersInfoNotification.edit();
        editor.putString("userChatsWithuserChatsWith", UserDetails.UserChatsWith);
        editor.putString("userChatsWithIDuserChatsWithID", UserDetails.UserChatsWithID);
        editor.commit();

        //Instantiating the database..access point of the database reference
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        //initializing the storage
        mFirebaseStorage = FirebaseStorage.getInstance();

        //making the references
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child(UserDetails.username).child(UserDetails.usernameID).child(UserDetails.UserChatsWith).child(UserDetails.UserChatsWithID);
        mMessagesDatabaseReferenceUsernameToUsername = mFirebaseDatabase.getReference().child(UserDetails.UserChatsWith).child(UserDetails.UserChatsWithID).child(UserDetails.UserChatsWith)
                .child(UserDetails.UserChatsWithID);
        mMessagesDatabaseReferenceSecondName = mFirebaseDatabase.getReference().child(UserDetails.UserChatsWith).child(UserDetails.UserChatsWithID).child(UserDetails.username).child(UserDetails.usernameID);

        //making the reference for the storage
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");

        // Initialize references to views
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);
        emptyLinearLayout = (LinearLayout) findViewById(R.id.emptyLinearLayout);
        mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        mFrameLayout.setBackgroundResource(R.drawable.round3);

        // Initialize message ListView and its adapter
        userMessages = new ArrayList<>();
        mMessageAdapter = new UserMessageAdapterBubbles(this, R.layout.item_message, userMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        mMessageListView.setEmptyView(emptyLinearLayout);

        mMessageListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState != 0) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(absListView.getApplicationWindowToken(), 0);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });

        mMediaPlayer = MediaPlayer.create(UserToUserMessageNotification.this, R.raw.sound1);
        mMediaPlayer2 = MediaPlayer.create(UserToUserMessageNotification.this, R.raw.sound2);
        maxVolume = 10;
        currVolume = 8;
        currVolume2 = 4;

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
                    mFrameLayout.setBackgroundResource(R.drawable.round2);
                } else {
                    mSendButton.setEnabled(false);
                    mFrameLayout.setBackgroundResource(R.drawable.round3);
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

                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                // Get details on the currently active default data
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                // If there is a network connection, fetch data
                if (networkInfo == null) {
                    Toast.makeText(UserToUserMessageNotification.this, getResources().getString(R.string.NoInternet), Toast.LENGTH_LONG).show();
                }

                if (UserDetails.username.equals(UserDetails.UserChatsWith)) {
                    //Creating a message
                    UserMessage userMessage = new UserMessage(mMessageEditText.getText().toString(), UserDetails.username, null, null, getTheDateTime(), UserDetails.notReaded, getTimestampInMIliseconds(), UserDetails.usernameID);
                    //The push method is exactly what you want to be using in this case because you need a new id generated for each message
                    mMessagesDatabaseReference.push().setValue(userMessage);
                } else {
                    //Creating a message
                    UserMessage userMessage = new UserMessage(mMessageEditText.getText().toString(), UserDetails.username, null, null, getTheDateTime(), UserDetails.notReaded, getTimestampInMIliseconds(), UserDetails.usernameID);
                    //The push method is exactly what you want to be using in this case because you need a new id generated for each message
                    mMessagesDatabaseReference.push().setValue(userMessage);

                    UserMessage userMessage2 = new UserMessage(mMessageEditText.getText().toString(), UserDetails.username, null, null, getTheDateTime(), UserDetails.notReaded, getTimestampInMIliseconds(), UserDetails.usernameID);
                    //The push method is exactly what you want to be using in this case because you need a new id generated for each message
                    mMessagesDatabaseReferenceSecondName.push().setValue(userMessage2);
                }


                // Clear input box
                mMessageEditText.setText("");

                //Hide softKeybord
                /*InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);*/
            }
        });

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

                    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    // Get details on the currently active default data
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    // If there is a network connection, fetch data
                    if (networkInfo == null) {
                        Toast.makeText(UserToUserMessageNotification.this, getResources().getString(R.string.NoInternet), Toast.LENGTH_LONG).show();
                    }

                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    if (UserDetails.username.equals(UserDetails.UserChatsWith)) {
                        UserMessage userMessage = new UserMessage(null, UserDetails.username, null, downloadUrl.toString(), getTheDateTime(), UserDetails.readed, getTimestampInMIliseconds(), UserDetails.usernameID);
                        mMessagesDatabaseReference.push().setValue(userMessage);
                    } else {
                        UserMessage userMessage = new UserMessage(null, UserDetails.username, null, downloadUrl.toString(), getTheDateTime(), UserDetails.readed, getTimestampInMIliseconds(), UserDetails.usernameID);
                        mMessagesDatabaseReference.push().setValue(userMessage);

                        UserMessage userMessage2 = new UserMessage(null, UserDetails.username, null, downloadUrl.toString(), getTheDateTime(), UserDetails.notReaded, getTimestampInMIliseconds(), UserDetails.usernameID);
                        //The push method is exactly what you want to be using in this case because you need a new id generated for each message
                        mMessagesDatabaseReferenceSecondName.push().setValue(userMessage2);
                    }
                }
            });
        }
    }

    private void attachDatabaseReadListenerDeliveryStatus() {

        // Child event listener
        mDeliveryChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String datasnapshoti = dataSnapshot.getKey();
                if (!UserDetails.username.equals(null) && !UserDetails.usernameID.equals(null) &&
                        !UserDetails.UserChatsWith.equals(null) && !UserDetails.UserChatsWithID.equals(null) && !datasnapshoti.equals(null)) {

                    mMessagesDatabaseReference.child(datasnapshoti).child("isReaded").setValue("true");
                }


                String datasnapshotOfLastMessage = mMessagesDatabaseReferenceSecondName.getKey();
                Log.e("datasnapsot", datasnapshoti + "----" + datasnapshotOfLastMessage);

                mMessageAdapter.notifyDataSetChanged();

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

        mMessagesDatabaseReference.addChildEventListener(mDeliveryChildEventListener);


    }

    private void attachDatabaseReadListenerFromShop() {
        // Child event listener
        mShopChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                UserMessage userMessagee = dataSnapshot.getValue(UserMessage.class);

                userMessages.add(userMessagee);

                mMessageAdapter.notifyDataSetChanged();

               /* String datasnapshoti = dataSnapshot.getKey();
                String datasnapshotOfLastMessage = mMessagesDatabaseReferenceSecondName.child(UserDetails.secondUserID).child(UserDetails.username).child(UserDetails.usernameID).getKey();
                Log.e("datasnapsotToListView", datasnapshoti + "----" + datasnapshotOfLastMessage);*/

                if (userMessagee.getName().equals(UserDetails.secondUser)) {
                    float log1 = (float) (Math.log(maxVolume - currVolume) / Math.log(maxVolume));
                    mMediaPlayer.setVolume(1 - log1, 1 - log1);

                    mMediaPlayer.start();

                } else if (userMessagee.getName().equals(UserDetails.username)) {
                    float log2 = (float) (Math.log(maxVolume - currVolume2) / Math.log(maxVolume));
                    mMediaPlayer2.setVolume(1 - log2, 1 - log2);

                    mMediaPlayer2.start();

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                userMessages.remove(userMessages.size() - 1);

                UserMessage userMessagee = dataSnapshot.getValue(UserMessage.class);

                userMessages.add(userMessagee);

                runOnUiThread(new Runnable() {
                    public void run() {
                        mMessageAdapter.notifyDataSetChanged();
                    }
                });

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

        mMessagesDatabaseReferenceUsernameToUsername.addChildEventListener(mShopChildEventListener);

    }

    private void attachDatabaseReadListenertoListView() {

        // Child event listener
        mChildEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                UserMessage userMessagee = dataSnapshot.getValue(UserMessage.class);

                userMessages.add(userMessagee);

                mMessageAdapter.notifyDataSetChanged();

                String datasnapshoti = dataSnapshot.getKey();
                String datasnapshotOfLastMessage = mMessagesDatabaseReferenceSecondName.getKey();
                Log.e("datasnapsotToListView", datasnapshoti + "----" + datasnapshotOfLastMessage);

                if (userMessagee.getName().equals(UserDetails.UserChatsWith)) {
                    float log1 = (float) (Math.log(maxVolume - currVolume) / Math.log(maxVolume));
                    mMediaPlayer.setVolume(1 - log1, 1 - log1);
                    mMediaPlayer.start();

                } else if (userMessagee.getName().equals(UserDetails.username)) {
                    float log2 = (float) (Math.log(maxVolume - currVolume2) / Math.log(maxVolume));
                    mMediaPlayer2.setVolume(1 - log2, 1 - log2);
                    mMediaPlayer2.start();

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                userMessages.remove(userMessages.size() - 1);

                UserMessage userMessagee = dataSnapshot.getValue(UserMessage.class);

                userMessages.add(userMessagee);

                runOnUiThread(new Runnable() {
                    public void run() {
                        mMessageAdapter.notifyDataSetChanged();
                    }
                });

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

        mMessagesDatabaseReferenceSecondName.addChildEventListener(mChildEventListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences mUsersInfoNotification = PreferenceManager.getDefaultSharedPreferences(UserToUserMessageNotification.this);
        UserDetails.UserChatsWith = mUsersInfoNotification.getString("userChatsWithuserChatsWith", "");
        UserDetails.UserChatsWithID = mUsersInfoNotification.getString("userChatsWithIDuserChatsWithID", "");

        SharedPreferences mUsernameInfo = PreferenceManager.getDefaultSharedPreferences(UserToUserMessageNotification.this);
        UserDetails.username = mUsernameInfo.getString("usernameusername", "");
        UserDetails.usernameID = mUsernameInfo.getString("usernameIDusernameID", "");

        if(UserDetails.username.equals(UserDetails.UserChatsWith)){

            attachDatabaseReadListenerFromShop();

        } else {

            //for loading the shop proposals
            attachDatabaseReadListenerFromShop();

            //for loading messages to the listview
            attachDatabaseReadListenertoListView();

            //to read messages for discovering/refreshing the delivery status
            attachDatabaseReadListenerDeliveryStatus();
        }



        isActiveNotification = true;

        mMessageAdapter.notifyDataSetChanged();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(UserDetails.UserChatsWith, 1);

    }

    @Override
    protected void onPause() {

        mMessagesDatabaseReference.removeEventListener(mDeliveryChildEventListener);
        mMessagesDatabaseReferenceSecondName.removeEventListener(mChildEventListener);
        mMessagesDatabaseReferenceUsernameToUsername.removeEventListener(mShopChildEventListener);
        /*mChildEventListener = null;
        mDeliveryChildEventListener = null;*/

        super.onPause();
        mMessageAdapter.clear();
        isActiveNotification = false;

    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent toMapActivity = new Intent(UserToUserMessageNotification.this, MapsActivity.class);
        startActivity(toMapActivity);
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
        DateFormat df = new SimpleDateFormat("EEE d MMM,  HH:mm");
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }

    private long getTimestampInMIliseconds() {
        Date curDate = new Date();
        long curMillis = curDate.getTime();
        return curMillis;
    }

}
