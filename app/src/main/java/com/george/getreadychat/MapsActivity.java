package com.george.getreadychat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.george.getreadychat.data.ChatContract;
import com.george.getreadychat.data.UserDetails;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker farmakeioGeorgeSoloupis, farmakeioMariaVakalopoulou;
    private HashMap<String, String> markerMap;

    private FirebaseDatabase mFirebaseDatabase;

    //A class that reference to spesific part of database
    private DatabaseReference mMessagesDatabaseReference;
    private DatabaseReference mNumberOfMessages;


    private ValueEventListener mValueEventListener;
    private ChildEventListener mChildEventListener;

    private Query queryTimestamp;
    private Query queryChildrenCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        // If there is a network connection, fetch data
        if (networkInfo == null) {
            Toast.makeText(MapsActivity.this, "No internet access!!", Toast.LENGTH_SHORT).show();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        markerMap = new HashMap<String, String>();

        //Instantiating the database..access point of the database reference
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        //making the references
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child(UserDetails.username);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        String id = null;

        // Add a marker in Sydney and move the camera
        LatLng farmakeioGS = new LatLng(40.8696, 22.91);
        farmakeioGeorgeSoloupis = mMap.addMarker(new MarkerOptions()
                .position(farmakeioGS)
                .title("Φαρμακείο")
                .snippet("Γεώργιου Σολούπη")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.farmaker)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(farmakeioGS, 16));

        id = farmakeioGeorgeSoloupis.getId();
        markerMap.put(id, ChatContract.FarmakeioGeorgioSoloupi.FARMAKEIO_NAME);

        LatLng farmakeioMV = new LatLng(40.871234, 22.909053);
        farmakeioMariaVakalopoulou = mMap.addMarker(new MarkerOptions()
                .position(farmakeioMV)
                .title("Φαρμακείο")
                .snippet("Μαρία Βακαλοπούλου")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pharm)));

        id = farmakeioMariaVakalopoulou.getId();
        markerMap.put(id, ChatContract.FarmakeioMariaVakalopoulou.FARMAKEIO_NAME);


        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                String m = markerMap.get(marker.getId());

                if (m.equals(ChatContract.FarmakeioGeorgioSoloupi.FARMAKEIO_NAME)) {
                    Intent intent = new Intent(MapsActivity.this, UserToUserMessage.class);
                    startActivity(intent);
                    UserDetails.secondUser = ChatContract.FarmakeioGeorgioSoloupi.FARMAKEIO_NAME;
                    UserDetails.secondUserID = ChatContract.FarmakeioGeorgioSoloupi.FARMAKEIO_KEY;
                }
                if (m.equals(ChatContract.FarmakeioMariaVakalopoulou.FARMAKEIO_NAME)) {
                    Intent intent = new Intent(MapsActivity.this, UserToUserMessage.class);
                    startActivity(intent);
                    UserDetails.secondUser = ChatContract.FarmakeioMariaVakalopoulou.FARMAKEIO_NAME;
                    UserDetails.secondUserID = ChatContract.FarmakeioMariaVakalopoulou.FARMAKEIO_KEY;
                }

            }
        });
    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mWindow;


        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            /*mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);*/
        }

        @Override
        public View getInfoWindow(Marker marker) {
            if (marker.equals(farmakeioGeorgeSoloupis)) {
                return null;
            }
            if (marker.equals(farmakeioMariaVakalopoulou)) {
                return null;
            }
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
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
                Intent intentToTotalMessages = new Intent(MapsActivity.this, TotalMessages.class);
                startActivity(intentToTotalMessages);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachListenerForNotifications();
    }

    private void attachListenerForNotifications() {

        if (mValueEventListener == null) {
            Log.e("attachlistener", "executed");
            mValueEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.e("attachlistener", "ondatachanged");

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
                        for(DataSnapshot secPostsnapshot : postSnapshot.getChildren()){
                            for(DataSnapshot thirdPostSnapsot : secPostsnapshot.getChildren()){


                                queryTimestamp = mMessagesDatabaseReference.child(postSnapshot.getKey())
                                        .child(secPostsnapshot.getKey())
                                        .child(thirdPostSnapsot.getKey())
                                        .orderByChild("timeStamp").limitToLast(1);
                                Log.e("minima",mMessagesDatabaseReference.child(UserDetails.usernameID).child(postSnapshot.getKey())
                                        .child(secPostsnapshot.getKey())
                                        .child(thirdPostSnapsot.getKey()).toString());

                                queryTimestamp.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        UserMessage usermessageOfLast = dataSnapshot.getValue(UserMessage.class);

                                        String stringText = usermessageOfLast.getText();
                                        String stringName = usermessageOfLast.getName();
                                        String stringIsReaded = usermessageOfLast.getIsReaded();

                                /*Toast.makeText(MainActivity.this, stringText, Toast.LENGTH_SHORT).show();*/

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
                                            String tagStringForID = usermessageOfLast.getNameId();

                                    /*userForCheckActive.isActive && !userForCheckNotificationActive.isActiveNotification||
                                            !userForCheckActive.isActive && userForCheckNotificationActive.isActiveNotification*/
                                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MapsActivity.this)
                                                    .setSmallIcon(R.drawable.ic_launcher)
                                                    .setContentTitle(usermessageOfLast.getName() + "\t" + "said:")
                                                    .setContentText(usermessageOfLast.getText())
                                                    .setOnlyAlertOnce(true)
                                                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                            mBuilder.setAutoCancel(true);
                                            mBuilder.setLocalOnly(false);

                                            Intent resultIntent = new Intent(MapsActivity.this, UserToUserMessageNotification.class);
                                            resultIntent.putExtra("chatsWith", tagStringForNotification);
                                            resultIntent.putExtra("chatsWithID", tagStringForID);
                                    /*resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);*/

                                            resultIntent.setAction(Long.toString(System.currentTimeMillis()));


                                            //flag to upddate current or create new one
                                            PendingIntent resultPendingIntent = PendingIntent.getActivity(MapsActivity.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                            //building the notification
                                            mBuilder.setContentIntent(resultPendingIntent);

                                            //use tag string to update current
                                            mNotificationManager.notify(tagStringForNotification, notifyID, mBuilder.build());

                                        } else if (!stringName.equals(UserDetails.username) && stringIsReaded.equals("false") &&
                                                !userForCheckNotificationActive.isActiveNotification && userForCheckActive.isActive && !stringName.equals(UserDetails.secondUser)) {

                                            String tagStringForNotification = usermessageOfLast.getName();
                                            String tagStringForID = usermessageOfLast.getNameId();

                                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MapsActivity.this)
                                                    .setSmallIcon(R.drawable.ic_launcher)
                                                    .setContentTitle(usermessageOfLast.getName() + "\t" + "said:")
                                                    .setContentText(usermessageOfLast.getText())
                                                    .setOnlyAlertOnce(true)
                                                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                            mBuilder.setAutoCancel(true);
                                            mBuilder.setLocalOnly(false);

                                            Intent resultIntent = new Intent(MapsActivity.this, UserToUserMessageNotification.class);
                                            resultIntent.putExtra("chatsWith", tagStringForNotification);
                                            resultIntent.putExtra("chatsWithID", tagStringForID);

                                            resultIntent.setAction(Long.toString(System.currentTimeMillis()));

                                            //flag to upddate current or create new one
                                            PendingIntent resultPendingIntent = PendingIntent.getActivity(MapsActivity.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                            //building the notification
                                            mBuilder.setContentIntent(resultPendingIntent);

                                            mNotificationManager.notify(tagStringForNotification, notifyID, mBuilder.build());

                                        } else if (!stringName.equals(UserDetails.username) && stringIsReaded.equals("false") && !userForCheckActive.isActive &&
                                                userForCheckNotificationActive.isActiveNotification && !stringName.equals(UserDetails.UserChatsWith)) {

                                            String tagStringForNotification = usermessageOfLast.getName();
                                            String tagStringForID = usermessageOfLast.getNameId();

                                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MapsActivity.this)
                                                    .setSmallIcon(R.drawable.ic_launcher)
                                                    .setContentTitle(usermessageOfLast.getName() + "\t" + "said:")
                                                    .setContentText(usermessageOfLast.getText())
                                                    .setOnlyAlertOnce(true)
                                                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                            mBuilder.setAutoCancel(true);
                                            mBuilder.setLocalOnly(false);

                                            Intent resultIntent = new Intent(MapsActivity.this, UserToUserMessageNotification.class);
                                            resultIntent.putExtra("chatsWith", tagStringForNotification);
                                            resultIntent.putExtra("chatsWithID", tagStringForID);

                                            resultIntent.setAction(Long.toString(System.currentTimeMillis()));

                                            //flag to upddate current or create new one
                                            PendingIntent resultPendingIntent = PendingIntent.getActivity(MapsActivity.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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

                                mNumberOfMessages = mMessagesDatabaseReference.child(UserDetails.usernameID).child(postSnapshot.getKey()).child(secPostsnapshot.getKey());
                                if (mChildEventListener == null) {
                                    mChildEventListener = new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                            UserMessage userMessage = dataSnapshot.getValue(UserMessage.class);
                                            String falseCount = userMessage.getIsReaded();
                                            if (falseCount.equals("false")) {
                                                UserDetails.numberOfMessages = UserDetails.numberOfMessages + 1;
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
                                    mNumberOfMessages.addChildEventListener(mChildEventListener);
                                }
                            }

                        }
                        /*Toast.makeText(MainActivity.this, postSnapshot.getKey(), Toast.LENGTH_SHORT).show();*/

                        /*queryChildrenCount = mMessagesDatabaseReference.child(postSnapshot.getKey()).orderByChild("isReaded").equalTo("false");*/




                        /*UserDetails.secondUser = postSnapshot.getKey();*/
                        Log.e("MainActivitySecond", UserDetails.secondUser);




                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }


            };
            mMessagesDatabaseReference.addValueEventListener(mValueEventListener);
        }
    }
}
