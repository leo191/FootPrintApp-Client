package com.example.leo.footprint;

import java.util.HashMap;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leo.footprint.Services.Load;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.SphericalUtil;


import Animation.LatLngInterpolator;
import helper.RetriveLocation;
import helper.SQLiteHandler;
import helper.SessionManager;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;
    private ProgressBar psb;
    private SQLiteHandler db;
    private SessionManager session;
    private GoogleMap mGmap;
    public static Handler mhandler;
    private Thread mlocationRetThread;
    RetriveLocation retriveLocation;
    private String name, email, bus_no;
    private Polyline mBusploy;
    Marker mBusMarker;
    LatLng startlatLng, endlatlng;

    //test views
    Marker mainMarker;
    Button mTrack;
    Bitmap icon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMap();


        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        mTrack = (Button) findViewById(R.id.bus_trackbtn);
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
                mTrack.setVisibility(View.INVISIBLE);
            }
        });


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
            }
        };


    }


    private  synchronized void animateCarMove(final Marker marker, final LatLng beginLatLng, final LatLng endLatLng, final long duration) {
        final Handler handler = new Handler();
        final long startTime = SystemClock.uptimeMillis();

        final Interpolator interpolator = new LinearInterpolator();


        handler.post(new Runnable() {
            @Override
            public void run() {
                // calculate phase of animation
                long elapsed = SystemClock.uptimeMillis() - startTime;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                // calculate new position for marker
                double lat = (endLatLng.latitude - beginLatLng.latitude) * t + beginLatLng.latitude;
                double lngDelta = endLatLng.longitude - beginLatLng.longitude;

                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * t + beginLatLng.longitude;
                synchronized (this) {
                    marker.setPosition(new LatLng(lat, lng));
                }
                // if not end of line segment of path
                if (t < 1.0) {
                    // call next marker position
                    handler.postDelayed(this, 16);
                } else {
                    // call turn animation
                    //nextTurnAnimation();
                }
            }
        });
    }



    public synchronized  void rotateMarker(final Marker marker, final float toRotation) {
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
    }

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

    private double getAngle(LatLng beginLatLng, LatLng endLatLng) {
        double f1 = Math.PI * beginLatLng.latitude / 180;
        double f2 = Math.PI * endLatLng.latitude / 180;
        double dl = Math.PI * (endLatLng.longitude - beginLatLng.longitude) / 180;
        return Math.atan2(Math.sin(dl) * Math.cos(f2), Math.cos(f1) * Math.sin(f2) - Math.sin(f1) * Math.cos(f2) * Math.cos(dl));
    }


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
                return true;
            case R.id.satelliteV:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void set_User_info() {
        HashMap<String, String> user = db.getUserDetails();
        bus_no = user.get("bus_no");
        Toast.makeText(this, bus_no, Toast.LENGTH_SHORT).show();
        name = user.get("name");
        email = user.get("email");
        txtEmail.setText(email);
        txtName.setText(name);
    }


    private void initMap() {


        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_of_bus);
        mapFragment.getMapAsync(this);
        icon = BitmapFactory.decodeResource(getResources(),R.drawable.bus);
        icon = Bitmap.createScaledBitmap(icon,80,150,true);
    }


    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
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
                    .title("Hamburg").icon(BitmapDescriptorFactory.fromBitmap(icon)));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(0,0)).zoom(8).build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mainMarker.setVisible(false);
        mGmap.setMyLocationEnabled(true);
    }
}