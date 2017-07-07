package com.george.getreadychat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.george.getreadychat.data.ChatContract;
import com.george.getreadychat.data.UserDetails;
import com.george.getreadychat.mapsdata.MyItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
import com.google.maps.android.clustering.ClusterManager;

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

    private int countNotifications;

    private ClusterManager<MyItem> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SharedPreferences mUsernameInfo = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
        UserDetails.username = mUsernameInfo.getString("usernameusername", "");
        UserDetails.usernameID = mUsernameInfo.getString("usernameIDusernameID", "");


        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        // If there is a network connection, fetch data
        if (networkInfo == null) {
            Toast.makeText(MapsActivity.this, getResources().getString(R.string.NoInternet), Toast.LENGTH_LONG).show();
        }

        markerMap = new HashMap<String, String>();

        //Instantiating the database..access point of the database reference
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        //making the references
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child(UserDetails.username);
    }

    private void setupMapIfNeeded() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    public class MapStateManager {

        private static final String LONGITUDE = "longitude";
        private static final String LATITUDE = "latitude";
        private static final String ZOOM = "zoom";
        private static final String BEARING = "bearing";
        private static final String TILT = "tilt";
        private static final String MAPTYPE = "MAPTYPE";

        private static final String PREFS_NAME = "mapCameraStateOfChat";

        private SharedPreferences mapStatePrefs;

        public MapStateManager(Context context) {
            mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        public void saveMapState(GoogleMap mapMie) {
            SharedPreferences.Editor editor = mapStatePrefs.edit();
            CameraPosition position = mapMie.getCameraPosition();

            editor.putFloat(LATITUDE, (float) position.target.latitude);
            editor.putFloat(LONGITUDE, (float) position.target.longitude);
            editor.putFloat(ZOOM, position.zoom);
            editor.putFloat(TILT, position.tilt);
            editor.putFloat(BEARING, position.bearing);
            editor.putInt(MAPTYPE, mapMie.getMapType());
            editor.commit();
        }

        public CameraPosition getSavedCameraPosition() {
            double latitude = mapStatePrefs.getFloat(LATITUDE, 0);
            if (latitude == 0) {
                return null;
            }
            double longitude = mapStatePrefs.getFloat(LONGITUDE, 0);
            LatLng target = new LatLng(latitude, longitude);

            float zoom = mapStatePrefs.getFloat(ZOOM, 0);
            float bearing = mapStatePrefs.getFloat(BEARING, 0);
            float tilt = mapStatePrefs.getFloat(TILT, 0);

            CameraPosition position = new CameraPosition(target, zoom, tilt, bearing);
            return position;
        }

        public int getSavedMapType() {
            return mapStatePrefs.getInt(MAPTYPE, GoogleMap.MAP_TYPE_NORMAL);
        }
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

        // Add a marker for George and move the camera
        LatLng farmakeioGS = new LatLng(37.297319, 21.701375);
        farmakeioGeorgeSoloupis = mMap.addMarker(new MarkerOptions()
                .position(farmakeioGS)
                .title(getResources().getString(R.string.pharmacy))
                .snippet("Γεώργιου Σολούπη")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.farmaker)));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(farmakeioGS, 7));

        id = farmakeioGeorgeSoloupis.getId();
        markerMap.put(id, ChatContract.FarmakeioGeorgioSoloupi.FARMAKEIO_NAME);

        LatLng farmakeioMV = new LatLng(40.871234, 22.909053);
        farmakeioMariaVakalopoulou = mMap.addMarker(new MarkerOptions()
                .position(farmakeioMV)
                .title(getResources().getString(R.string.pharmacy))
                .snippet("Μαρία Βακαλοπούλου")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pharm)));

        id = farmakeioMariaVakalopoulou.getId();
        markerMap.put(id, ChatContract.FarmakeioMariaVakalopoulou.FARMAKEIO_NAME);

        /////
        /*setUpClusterer();*/
        /////


        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

                farmakeioMariaVakalopoulou.setVisible(cameraPosition.zoom > 8);

            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                String m = markerMap.get(marker.getId());

                if (m.equals(ChatContract.FarmakeioGeorgioSoloupi.FARMAKEIO_NAME)) {
                    Intent intent = new Intent(MapsActivity.this, UserToUserMessage.class);
                    startActivity(intent);

                    SharedPreferences mUsersInfo = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
                    SharedPreferences.Editor editor = mUsersInfo.edit();
                    editor.putString("secondUsersecondUser", ChatContract.FarmakeioGeorgioSoloupi.FARMAKEIO_NAME);
                    editor.putString("secondUserIDsecondUserID", ChatContract.FarmakeioGeorgioSoloupi.FARMAKEIO_KEY);
                    editor.commit();

                    /*UserDetails.secondUser = ChatContract.FarmakeioGeorgioSoloupi.FARMAKEIO_NAME;
                    UserDetails.secondUserID = ChatContract.FarmakeioGeorgioSoloupi.FARMAKEIO_KEY;*/
                }

                if (m.equals(ChatContract.FarmakeioMariaVakalopoulou.FARMAKEIO_NAME)) {
                    Intent intent = new Intent(MapsActivity.this, UserToUserMessage.class);
                    startActivity(intent);

                    SharedPreferences mUsersInfo = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
                    SharedPreferences.Editor editor = mUsersInfo.edit();
                    editor.putString("secondUsersecondUser", ChatContract.FarmakeioMariaVakalopoulou.FARMAKEIO_NAME);
                    editor.putString("secondUserIDsecondUserID", ChatContract.FarmakeioMariaVakalopoulou.FARMAKEIO_KEY);
                    editor.commit();

                    /*
                    UserDetails.secondUser = ChatContract.FarmakeioMariaVakalopoulou.FARMAKEIO_NAME;
                    UserDetails.secondUserID = ChatContract.FarmakeioMariaVakalopoulou.FARMAKEIO_KEY;*/
                }

            }
        });

        /////
        MapStateManager mgr = new MapStateManager(this);
        CameraPosition position = mgr.getSavedCameraPosition();
        if (position != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(update);

            mMap.setMapType(mgr.getSavedMapType());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MapStateManager mgr = new MapStateManager(this);
        mgr.saveMapState(mMap);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences mUsernameInfo = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
        UserDetails.username = mUsernameInfo.getString("usernameusername", "");
        UserDetails.usernameID = mUsernameInfo.getString("usernameIDusernameID", "");

        setupMapIfNeeded();
        attachListenerForNotifications();
    }


    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private View mWindow;


        /*CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            *//*mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);*//*
        }*/

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
        getMenuInflater().inflate(R.menu.menu_map, menu);
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
            case R.id.instructions:
                Intent intentToInstructions = new Intent(MapsActivity.this, Instructions.class);
                startActivity(intentToInstructions);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void attachListenerForNotifications() {

        if (mValueEventListener == null) {
            Log.e("attachlistener", "executed");
            mValueEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot secPostsnapshot : postSnapshot.getChildren()) {
                            for (DataSnapshot thirdPostSnapsot : secPostsnapshot.getChildren()) {

                                queryTimestamp = mMessagesDatabaseReference.child(postSnapshot.getKey())
                                        .child(secPostsnapshot.getKey())
                                        .child(thirdPostSnapsot.getKey())
                                        .orderByChild("timeStamp").limitToLast(1);
                                Log.e("minima", mMessagesDatabaseReference.child(UserDetails.usernameID).child(postSnapshot.getKey())
                                        .child(secPostsnapshot.getKey())
                                        .child(thirdPostSnapsot.getKey()).toString());

                                queryTimestamp.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        UserMessage usermessageOfLast = dataSnapshot.getValue(UserMessage.class);

                                        String stringText = usermessageOfLast.getText();
                                        String stringName = usermessageOfLast.getName();
                                        String stringIsReaded = usermessageOfLast.getIsReaded();

                                        UserToUserMessage userForCheckActive = null;
                                        UserToUserMessageNotification userForCheckNotificationActive = null;

                                        int notifyID = 1;

                                        if (!stringName.equals(UserDetails.username) && stringIsReaded.equals("false") &&
                                                !userForCheckActive.isActive && !userForCheckNotificationActive.isActiveNotification) {

                                            String tagStringForNotification = usermessageOfLast.getName();
                                            String tagStringForID = usermessageOfLast.getNameId();

                                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MapsActivity.this)
                                                    .setSmallIcon(R.drawable.ic_launcher)
                                                    .setContentTitle(usermessageOfLast.getName() + ":")
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

                                            //use tag string to update current
                                            mNotificationManager.notify(tagStringForNotification, notifyID, mBuilder.build());

                                        } else if (!stringName.equals(UserDetails.username) && stringIsReaded.equals("false") &&
                                                !userForCheckNotificationActive.isActiveNotification && userForCheckActive.isActive && !stringName.equals(UserDetails.secondUser)) {

                                            String tagStringForNotification = usermessageOfLast.getName();
                                            String tagStringForID = usermessageOfLast.getNameId();

                                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MapsActivity.this)
                                                    .setSmallIcon(R.drawable.ic_launcher)
                                                    .setContentTitle(usermessageOfLast.getName() + ":")
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
                                                    .setContentTitle(usermessageOfLast.getName() + ":")
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

                                queryChildrenCount = mMessagesDatabaseReference.child(postSnapshot.getKey())
                                        .child(secPostsnapshot.getKey())
                                        .child(thirdPostSnapsot.getKey())
                                        .orderByChild("false").limitToLast(1);
                                if (mChildEventListener == null) {
                                    mChildEventListener = new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                            UserMessage userMessage = dataSnapshot.getValue(UserMessage.class);
                                            String falseCount = userMessage.getIsReaded();
                                            if (falseCount.equals("false")) {
                                                UserDetails.numberOfMessages++;
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
                                    queryChildrenCount.addChildEventListener(mChildEventListener);
                                }
                            }

                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }


            };
            mMessagesDatabaseReference.addValueEventListener(mValueEventListener);
        }
    }


    //trying to future cluster markers
    protected GoogleMap getMap() {
        return mMap;
    }

    private void setUpClusterer() {
        // Position the map.
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.8696, 22.91), 10));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MyItem>(this, getMap());

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        getMap().setOnCameraIdleListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);

        // Add cluster items (markers) to the cluster manager.
        addItems();
    }

    private void addItems() {

        // Set some lat/lng coordinates to start with.
        double lat = 40.8696;
        double lng = 22.91;

        double lat1 = 40.871234;
        double lng1 = 22.909053;

        // Add ten cluster items in close proximity, for purposes of this example.
        /*for (int i = 0; i < 10; i++) {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            MyItem offsetItem = new MyItem(lat, lng);
            mClusterManager.addItem(offsetItem);
        }*/

        MyItem offsetItem = new MyItem(lat, lng);
        mClusterManager.addItem(offsetItem);
        MyItem offsetItem2 = new MyItem(lat1, lng1);
        mClusterManager.addItem(offsetItem2);

    }

}
