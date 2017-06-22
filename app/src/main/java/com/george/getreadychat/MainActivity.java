package com.george.getreadychat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.george.getreadychat.data.UserDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    private static final String TAG = "MainActivity";

    public static final int RC_SIGN_IN = 1;

    private String mUsername;

    //instance of Firebase auth
    private FirebaseAuth mFirebaseAuth;

    private FirebaseDatabase mFirebaseDatabase;

    //A class that reference to spesific part of database
    private DatabaseReference mMessagesDatabaseReference;

    //variable fr auth state listener
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private ValueEventListener mValueEventListener;
    private ChildEventListener mChildEventListener;

    private Query queryTimestamp;

    private Button mMessageGeorge, mMessageMaria;

    private ArrayList<Integer> numbers;
    private Random randomGenerator;
    private int random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        // If there is a network connection, fetch data
        if (networkInfo == null) {
            Toast.makeText(MainActivity.this, "No internet access!!", Toast.LENGTH_SHORT).show();
            finish();
        }

        //iniializing the auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {

                    //Method toodo onSingnedIn with the name of the user
                    onSignedInInitialize(user.getDisplayName());
                    attachListenerForNotifications();


                } else {

                    onSignedOutCleanup();

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(AuthUI.EMAIL_PROVIDER, AuthUI.GOOGLE_PROVIDER).build(), RC_SIGN_IN);
                }
            }
        };

        mMessageGeorge = (Button) findViewById(R.id.messageGeorgeButton);
        mMessageMaria = (Button) findViewById(R.id.messageMariaButton);

        //Instantiating the database..access point of the database reference
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        //making the references
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child(UserDetails.username);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        numbers = new ArrayList<Integer>();
        randomGenerator = new Random();

        mMessageGeorge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UserDetails.secondUser = "george soloupis";

                Intent intentToUserMessage = new Intent(MainActivity.this, UserToUserMessage.class);
                startActivity(intentToUserMessage);

            }
        });

        mMessageMaria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UserDetails.secondUser = "Maria Vakalopoulou";

                Intent intentToUserMessage = new Intent(MainActivity.this, UserToUserMessage.class);
                startActivity(intentToUserMessage);
            }
        });

        /*runOnUiThread(new Runnable() {
            public void run() {
                attachListenerForNotifications();
            }
        });*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        runOnUiThread(new Runnable() {
            public void run() {
                attachListenerForNotifications();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMessagesDatabaseReference.removeEventListener(mValueEventListener);
        /*queryTimestamp.removeEventListener(mChildEventListener);*/
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "You are signed in!!", Toast.LENGTH_SHORT).show();
                runOnUiThread(new Runnable() {
                    public void run() {
                        attachListenerForNotifications();
                    }
                });
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, "Cancelled!!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        runOnUiThread(new Runnable() {
            public void run() {
                attachListenerForNotifications();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    private void onSignedInInitialize(String username) {
        mUsername = username;

        UserDetails.username = username;
        Log.e("usernameInDetails", UserDetails.username);
        runOnUiThread(new Runnable() {
            public void run() {
                attachListenerForNotifications();
            }
        });

    }

    private void onSignedOutCleanup() {
        mUsername = "anonymous";
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.action_settings:
                Intent intentToTotalMessages = new Intent(MainActivity.this, TotalMessages.class);
                startActivity(intentToTotalMessages);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void attachListenerForNotifications() {
        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //working loop for every second user,every message
                    /*for (DataSnapshot child : dataSnapshot.getChildren()) {
                        for (DataSnapshot childd : child.getChildren()) {
                            *//*String ssstring = mMessagesDatabaseReference.child(child.getKey()).child(childd.getKey()).getV;*//*

                            UserMessage uuuserMesss = childd.getValue(UserMessage.class);
                            String ssstring = uuuserMesss.getText();
                            Toast.makeText(MainActivity.this,ssstring,Toast.LENGTH_SHORT).show();

                        }
                    }*/

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Toast.makeText(MainActivity.this, postSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                        queryTimestamp = mMessagesDatabaseReference.child(postSnapshot.getKey()).orderByChild("timeStamp").limitToLast(1);

                        /*UserDetails.secondUser = postSnapshot.getKey();*/
                        Log.e("MainActivitySecond", UserDetails.secondUser);


                        /*random = randomGenerator.nextInt(50);
                        String randomized = String.valueOf(random);
                        Toast.makeText(MainActivity.this, randomized, Toast.LENGTH_LONG).show();
                        if (!numbers.contains(random)) {
                            numbers.add(random);
                        }*/


                        /*if(mChildEventListener == null){
                            mChildEventListener = new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    UserMessage usermessageOfLast = dataSnapshot.getValue(UserMessage.class);

                                    String stringText = usermessageOfLast.getText();
                                    String stringName = usermessageOfLast.getName();

                                    Toast.makeText(MainActivity.this, stringText, Toast.LENGTH_SHORT).show();

                                    UserToUserMessage userForCheckActive = null;

                                    if (!usermessageOfLast.getName().equals(UserDetails.username) && !userForCheckActive.isActive) {

                                        int notifyID = 1;

                                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)
                                                .setSmallIcon(R.drawable.ic_launcher)
                                                .setContentTitle("Message from:\n " + usermessageOfLast.getName())
                                                .setContentText(usermessageOfLast.getText())
                                                .setOnlyAlertOnce(true)
                                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                        mBuilder.setAutoCancel(true);
                                        mBuilder.setLocalOnly(false);

                                        Intent resultIntent = new Intent(MainActivity.this, UserToUserMessage.class);


                                        resultIntent.setAction("android.intent.action.MAIN");
                                        resultIntent.addCategory("android.intent.category.LAUNCHER");

                                        PendingIntent resultPendingIntent = PendingIntent.getActivity(MainActivity.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                        //building the notification
                                        mBuilder.setContentIntent(resultPendingIntent);

                                        mNotificationManager.notify(notifyID, mBuilder.build());
                                    }
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
                            queryTimestamp.addChildEventListener(mChildEventListener);
                        }*/

                        queryTimestamp.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                UserMessage usermessageOfLast = dataSnapshot.getValue(UserMessage.class);

                                String stringText = usermessageOfLast.getText();
                                String stringName = usermessageOfLast.getName();
                                String stringIsReaded = usermessageOfLast.getIsReaded();

                                Toast.makeText(MainActivity.this, stringText, Toast.LENGTH_SHORT).show();

                                UserToUserMessage userForCheckActive = null;
                                UserToUserMessageNotification userForCheckNotificationActive = null;

                                /*//giving the notifications different id
                                long notifyTimeStamp = usermessageOfLast.getTimeStamp();
                                String time = Long.toString(notifyTimeStamp);
                                String timi = time.substring(9);
                                int notifyID = Integer.parseInt(timi);*/

                                int notifyID = 1;


                                if (!stringName.equals(UserDetails.username) && stringIsReaded.equals("false") &&
                                        !userForCheckActive.isActive && !userForCheckNotificationActive.isActiveNotification) {

                                    String tagStringForNotification = usermessageOfLast.getName();

                                    /*userForCheckActive.isActive && !userForCheckNotificationActive.isActiveNotification||
                                            !userForCheckActive.isActive && userForCheckNotificationActive.isActiveNotification*/
                                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setContentTitle(usermessageOfLast.getName() +"\t" + "said:")
                                            .setContentText(usermessageOfLast.getText())
                                            .setOnlyAlertOnce(true)
                                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                    mBuilder.setAutoCancel(true);
                                    mBuilder.setLocalOnly(false);

                                    Intent resultIntent = new Intent(MainActivity.this, UserToUserMessageNotification.class);
                                    resultIntent.putExtra("chatsWith", tagStringForNotification);
                                    /*resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);*/


                                    /*resultIntent.putExtra("chatsWith",stringOfChatsWith);*/

                                    ///////
                                    /*Intent a = new Intent(StringUsernameMessages.this, AfterPickingMessages.class);
                                     a.putExtra("4444", string2);
                                     a.putExtra("myUsername", strUsername);
                                     a.putExtra("myPersonalMessages", text);
                                     a.putExtra("secondName", secondName);
                                    startActivity(a);*/

                                    /*Intent intent = getIntent();
                                    String strUsername = intent.getStringExtra("myUsername");
                                    String strPersonal = intent.getStringExtra("myPersonalMessages");
                                    Log.e("AllMessages",strUsername);*/
                                    //////

                                    resultIntent.setAction(Long.toString(System.currentTimeMillis()));
                                    /*resultIntent.setAction("android.intent.action.MAIN");
                                    resultIntent.addCategory("android.intent.category.LAUNCHER");*/

                                    //flag to upddate current or create new one
                                    PendingIntent resultPendingIntent = PendingIntent.getActivity(MainActivity.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT );

                                    //building the notification
                                    mBuilder.setContentIntent(resultPendingIntent);

                                    //use tag string to update current
                                    mNotificationManager.notify(tagStringForNotification, notifyID, mBuilder.build());

                                } else if (!stringName.equals(UserDetails.username) && stringIsReaded.equals("false") &&
                                        !userForCheckNotificationActive.isActiveNotification && userForCheckActive.isActive && !stringName.equals(UserDetails.secondUser)) {

                                    String tagStringForNotification = usermessageOfLast.getName();

                                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setContentTitle(usermessageOfLast.getName() +"\t" + "said:")
                                            .setContentText(usermessageOfLast.getText())
                                            .setOnlyAlertOnce(true)
                                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                    mBuilder.setAutoCancel(true);
                                    mBuilder.setLocalOnly(false);

                                    Intent resultIntent = new Intent(MainActivity.this, UserToUserMessageNotification.class);
                                    resultIntent.putExtra("chatsWith", tagStringForNotification);

                                    /*resultIntent.putExtra("chatsWith",stringOfChatsWith);*/

                                    ///////
                                    /*Intent a = new Intent(StringUsernameMessages.this, AfterPickingMessages.class);
                                     a.putExtra("4444", string2);
                                     a.putExtra("myUsername", strUsername);
                                     a.putExtra("myPersonalMessages", text);
                                     a.putExtra("secondName", secondName);
                                    startActivity(a);*/

                                    /*Intent intent = getIntent();
                                    String strUsername = intent.getStringExtra("myUsername");
                                    String strPersonal = intent.getStringExtra("myPersonalMessages");
                                    Log.e("AllMessages",strUsername);*/
                                    //////

                                    resultIntent.setAction(Long.toString(System.currentTimeMillis()));
                                    /*resultIntent.setAction("android.intent.action.MAIN");
                                    resultIntent.addCategory("android.intent.category.LAUNCHER");*/

                                    //flag to upddate current or create new one
                                    PendingIntent resultPendingIntent = PendingIntent.getActivity(MainActivity.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT );

                                    //building the notification
                                    mBuilder.setContentIntent(resultPendingIntent);

                                    mNotificationManager.notify(tagStringForNotification, notifyID, mBuilder.build());

                                } else if (!stringName.equals(UserDetails.username) && stringIsReaded.equals("false") && !userForCheckActive.isActive &&
                                        userForCheckNotificationActive.isActiveNotification && !stringName.equals(UserDetails.UserChatsWith)) {

                                    String tagStringForNotification = usermessageOfLast.getName();

                                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setContentTitle(usermessageOfLast.getName() +"\t" + "said:")
                                            .setContentText(usermessageOfLast.getText())
                                            .setOnlyAlertOnce(true)
                                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                    mBuilder.setAutoCancel(true);
                                    mBuilder.setLocalOnly(false);

                                    Intent resultIntent = new Intent(MainActivity.this, UserToUserMessageNotification.class);
                                    resultIntent.putExtra("chatsWith", tagStringForNotification);

                                    ///////
                                    /*Intent a = new Intent(StringUsernameMessages.this, AfterPickingMessages.class);
                                     a.putExtra("4444", string2);
                                     a.putExtra("myUsername", strUsername);
                                     a.putExtra("myPersonalMessages", text);
                                     a.putExtra("secondName", secondName);
                                    startActivity(a);*/

                                    /*Intent intent = getIntent();
                                    String strUsername = intent.getStringExtra("myUsername");
                                    String strPersonal = intent.getStringExtra("myPersonalMessages");
                                    Log.e("AllMessages",strUsername);*/
                                    //////


                                    resultIntent.setAction(Long.toString(System.currentTimeMillis()));
                                    /*resultIntent.setAction("android.intent.action.MAIN");
                                    resultIntent.addCategory("android.intent.category.LAUNCHER");*/

                                    //flag to upddate current or create new one
                                    PendingIntent resultPendingIntent = PendingIntent.getActivity(MainActivity.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                    //building the notification
                                    mBuilder.setContentIntent(resultPendingIntent);

                                    mNotificationManager.notify(tagStringForNotification, notifyID, mBuilder.build());
                                }

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
                        });

                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }


            };
            mMessagesDatabaseReference.addValueEventListener(mValueEventListener);
        }
    }

    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     *
     * @return True if eanbled, false otherwise.
     */
    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
