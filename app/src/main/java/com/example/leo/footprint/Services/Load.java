package com.example.leo.footprint.Services;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.leo.footprint.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.AppConfig;

/**
 * Created by leo on 19/06/17.
 */



public class Load extends Service {



    private double latitude,longitude;
    private String bus_no;
    Bundle bundle;
    RequestQueue reQ;
    Request rqst;

    public double getLatitude() {
        return latitude;
    }

   synchronized public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    synchronized public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bus_no = intent.getStringExtra("bus_no");

           Thread th =  new Thread(new Myth(this));
            th.start();

        //retriveLoc();
        return Service.START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }




    public class Myth implements Runnable {

        Context con;
        public Myth(Context con){
            this.con= con;
        }
        @Override
        public void run() {
            while(true)
            {
                Toast.makeText(con,bus_no,Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }




    public void retriveLoc()
    {
        Message message = Message.obtain();
        rqst = new StringRequest(Request.Method.POST,
                AppConfig.URL_RETRIVE_LOC, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {


                        bus_no = jObj.getString("bus_no");

                        JSONObject bus_location = jObj.getJSONObject("bus_location");
                        setLatitude(bus_location.getDouble("latitude"));
                        setLongitude(bus_location.getDouble("longitude"));


                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
//                        Toast.makeText(context,
//                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                   // Toast.makeText(context, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(context,error.getMessage(), Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("bus_no", bus_no);

                return params;
            }

        };


        // Adding request to request queue
        //AppController.getInstance().addToRequestQueue(strReq);
        reQ.add(rqst);

        bundle.putDouble("latitude",latitude);
        bundle.putDouble("longitude",longitude);

        message.setData(bundle);

        MainActivity.mhandler.sendMessage(message);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}
