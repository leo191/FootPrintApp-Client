package com.example.leo.footprint;

import java.util.ArrayList;
import java.util.HashMap;

import android.*;
import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leo.footprint.Services.Load;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.SphericalUtil;
import com.myhexaville.smartimagepicker.ImagePicker;
import com.myhexaville.smartimagepicker.OnImagePickedListener;

import helper.LatLngInterpolator;
import helper.RetriveLocation;
import helper.SQLiteHandler;
import helper.SessionManager;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{


    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FragmentTransaction fragmentTransaction;
    private  Toolbar toolbar;
    private Button btnLogout;
    private de.hdodenhof.circleimageview.CircleImageView profile_image_view;
    TextView txvName,txvEmail;
    private ProgressBar psb;
    private SQLiteHandler db;
    private SessionManager session;
    private GoogleMap mGmap;
    public static Handler mhandler;
    private Thread mlocationRetThread;
    RetriveLocation retriveLocation;
    private String name, email, bus_no;
    private View header;
    private Polyline mBusploy;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    Marker mBusMarker;
    LatLng startlatLng, endlatlng;
    MapFragment mapFragment;
    //test views
    Marker mainMarker,userMarker;
    ImageView mTrack,mstatus;
    Bitmap icon;
    SeekBar mSeekbar;
    int RAD=100;

    private int REQUEST_CODE_PICKER = 2000;

    ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mTrack = (ImageView) findViewById(R.id.check_btn);
        mstatus = (ImageView) findViewById(R.id.started_btn);
        mstatus.setVisibility(View.INVISIBLE);
        mSeekbar=(SeekBar)findViewById(R.id.seekBar) ;
        mSeekbar.setVisibility(View.INVISIBLE);
        setUpToolBar();
        initMap();
        txvName = (TextView)header.findViewById(R.id.user_name);
        txvEmail = (TextView)header.findViewById(R.id.user_email);
        profile_image_view = (de.hdodenhof.circleimageview.CircleImageView) header.findViewById(R.id.profile_image);


        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }
        set_User_info();


        startlatLng=endlatlng=null;
        // Logout button click event

        mTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRetrivalLoc();
                mstatus.setVisibility(View.VISIBLE);
                mTrack.setVisibility(View.INVISIBLE);
            }
        });


        profile_image_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"Ho",Toast.LENGTH_SHORT).show();
                ImagePicker imagePicker = new ImagePicker(MainActivity.this, null, new OnImagePickedListener() {
                    @Override
                    public void onImagePicked(Uri imageUri) {


                        //profile_image_view.setImageURI(imageUri);
                    }
                });
                imagePicker.choosePicture(true);
            }
        });






    }



    public void start() {

    }




    boolean doubleBackToExitPressedOnce = false;


    @Override
    public void onBackPressed() {
        //Checking for fragment count on backstack



        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
        } else if (!doubleBackToExitPressedOnce) {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this,"Please click BACK again to exit.", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            super.onBackPressed();
            return;
        }
    }



    public void startRetrivalLoc() {

        retriveLocation = new RetriveLocation(this, bus_no);
        Thread th = new Thread(retriveLocation);
        th.start();
        mhandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (mGmap != null && mainMarker !=null) {
                    mainMarker.setVisible(true);
                    Bundle bundle = msg.getData();
                    startlatLng = new LatLng(bundle.getDouble("latitude"), bundle.getDouble("longitude"));
//                      if(mBusMarker!=null){mBusMarker.remove();}
//                        mBusMarker = mGmap.addMarker(new MarkerOptions().position(latLng).title("Bus"));
                   synchronized (this){
                        if (endlatlng != null && endlatlng != startlatLng) {
                            float rotation = (float) SphericalUtil.computeHeading(startlatLng, endlatlng);

                            //animateCarMove(mainMarker,endlatlng,startlatLng,1000);
                            rotateMarker(mainMarker,startlatLng,rotation);
                        }
                    }


                }
                endlatlng=startlatLng;
                checkBusDistance();
            }
        };


    }


    Circle circle;
    float[] distance = new float[2];
    int c=0;
    public void checkBusDistance()
    {
        if(circle!=null)
        {
            Location.distanceBetween( mainMarker.getPosition().latitude, mainMarker.getPosition().longitude,
                    circle.getCenter().latitude, circle.getCenter().longitude, distance);
            if( distance[0] < circle.getRadius()  ){

                if(c==0)
                {
                    NotifyParents();

                }
            }
            else {
                c=0;
            }
        }
    }







    //





    void NotifyParents()
    {
        c=1;
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        new Intent(getApplicationContext(),MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(R.mipmap.ic_launcher).setSound(uri)
                        .setContentTitle("Your child is Arriving")
                        .setContentText("Bus is just few minutes away from you.. :)")
                        .setContentIntent(resultPendingIntent);
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(001,mBuilder.build());



    }





//    private  synchronized void animateCarMove(final Marker marker, final LatLng beginLatLng, final LatLng endLatLng, final long duration) {
//        final Handler handler = new Handler();
//        final long startTime = SystemClock.uptimeMillis();
//
//        final Interpolator interpolator = new LinearInterpolator();
//
//
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                // calculate phase of animation
//                long elapsed = SystemClock.uptimeMillis() - startTime;
//                float t = interpolator.getInterpolation((float) elapsed / duration);
//                // calculate new position for marker
//                double lat = (endLatLng.latitude - beginLatLng.latitude) * t + beginLatLng.latitude;
//                double lngDelta = endLatLng.longitude - beginLatLng.longitude;
//
//                if (Math.abs(lngDelta) > 180) {
//                    lngDelta -= Math.signum(lngDelta) * 360;
//                }
//                double lng = lngDelta * t + beginLatLng.longitude;
//                synchronized (this) {
//                    marker.setPosition(new LatLng(lat, lng));
//                }
//                // if not end of line segment of path
//                if (t < 1.0) {
//                    // call next marker position
//                    handler.postDelayed(this, 16);
//                } else {
//                    // call turn animation
//                    //nextTurnAnimation();
//                }
//            }
//        });
//    }



  /*  public synchronized  void rotateMarker(final Marker marker, final float toRotation) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = marker.getRotation();
        final long duration = 1555;

        final Interpolator interpolator = new LinearInterpolator();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;

                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        },1000);
    }*/

    //////////////

    private  void  rotateMarker(final Marker marker, final LatLng destination, final float rotation) {

        if (marker != null) {

            final LatLng startPosition = marker.getPosition();
            final float startRotation = marker.getRotation();

            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Spherical();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(800); // duration 3 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, destination);
                        float bearing = computeRotation(v, startRotation, rotation);

                        marker.setRotation(bearing);
                        marker.setPosition(newPosition);

                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            valueAnimator.start();
        }
    }
    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }

        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }



    ///////////////////



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logoutUser();
                return true;
            case R.id.normalV:
                CameraPosition cameraPosition = new CameraPosition.Builder().target(
                        startlatLng).zoom(8).build();

                mGmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                mGmap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

                return true;
            case R.id.satelliteV:
                if(userMarker!=null)
                    mSeekbar.setVisibility(View.VISIBLE);
                    mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            RAD = progress;
                            circle.setRadius(RAD);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                                mSeekbar.setVisibility(View.INVISIBLE);
                        }
                    });
                //mGmap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void set_User_info() {
        HashMap<String, String> user = db.getUserDetails();
        bus_no = user.get("bus_no");
        Toast.makeText(this, bus_no, Toast.LENGTH_SHORT).show();
        txvName.setText(user.get("name"));
        txvEmail.setText(user.get("email"));
    }


    private void initMap() {


       mapFragment  = (MapFragment) getFragmentManager().findFragmentById(R.id.map_of_bus);
        mapFragment.getMapAsync(this);
        icon = BitmapFactory.decodeResource(getResources(),R.drawable.bus);
        icon = Bitmap.createScaledBitmap(icon,40,75,true);
    }


    public void setUpToolBar() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView = (NavigationView)findViewById(R.id.nav_view);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerlayout);
        header = navigationView.getHeaderView(0);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close){
            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();




        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                int id = item.getItemId();
                FragmentTransaction fragmentTransaction;

                if (id == R.id.user_detalis_edit) {
                    Fragment fragment = EditUserDetails.newInEditUserDetails();
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    getSupportFragmentManager().popBackStack();
                    fragmentTransaction.add(R.id.content_frame,new EditUserDetails()).addToBackStack("fragBack").commit();
                    getSupportActionBar().setTitle(item.getTitle());

                    // Handle the camera action
                } else if (id == R.id.settings) {

                }


                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }






    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGmap = googleMap;
        if (mGmap != null) {
          mainMarker = mGmap.addMarker(new MarkerOptions().position(new LatLng(0,0))
                    .title("Hamburg").icon(BitmapDescriptorFactory.fromBitmap(icon)).flat(true));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(20.5937,78.9629)).zoom(8).build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            mGmap.getUiSettings().setZoomControlsEnabled(true);
            View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            rlp.setMargins(0,50, 0, 0);




            mGmap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){

                @Override
                public void onMapClick(LatLng latLng) {
                    if(userMarker!=null)
                        userMarker.remove();
                    userMarker=mGmap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("TouchPoint"));
                    if(circle!=null)
                        circle.remove();

                    circle = mGmap.addCircle(new CircleOptions()
                            .center(latLng)
                            .radius(RAD)
                            .strokeColor(Color.BLUE).strokeWidth(1).fillColor(Color.GREEN)
                    );
                }
            });

        }




        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mainMarker.setVisible(false);
        mGmap.setMyLocationEnabled(true);

    }





}