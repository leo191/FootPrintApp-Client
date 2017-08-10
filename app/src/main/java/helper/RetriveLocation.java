package helper;

import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request.Method;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.leo.footprint.LoginActivity;
import com.example.leo.footprint.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import app.AppConfig;

/**
 * Created by leo on 20/06/17.
 */

public class RetriveLocation implements Runnable{
    RequestQueue reQ;
    Request rqst;
    private Context context;
    private double latitude,longitude;
    private String bus_no;
    Bundle bundle;
    Message message;
    public RetriveLocation(Context context,String bus_no)
    {
        reQ=Volley.newRequestQueue(context);
        this.bus_no=bus_no;
        this.context = context;
        bundle = new Bundle();
    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    volatile boolean shutdown = false;
    @Override
    public void run() {



            while (!Thread.currentThread().isInterrupted()) {
                message = Message.obtain();
                rqst = new StringRequest(Method.POST,
                        AppConfig.URL_RETRIVE_LOC, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");

                            // Check for error node in json
                            if (!error) {


                                JSONObject bus_location = jObj.getJSONObject("bus_location");

//                            latitude = Double.parseDouble(bus_location.getString("latitude"));
//                            longitude = Double.parseDouble(bus_location.getString("longitude"));
                                setLatitude(Double.parseDouble(bus_location.getString("latitude")));
                                setLongitude(Double.parseDouble(bus_location.getString("longitude")));


                            } else {
                                // Error in login. Get the error message
                                String errorMsg = jObj.getString("error_msg");

//
                                message.arg1 = 1;
                                MainActivity.i = 0;


                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            //Toast.makeText(context, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            message.arg1 = 1;


                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
//                    Toast.makeText(context,
//                            error.getMessage(), Toast.LENGTH_LONG).show();
                        message.arg1 = 1;

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

                if (message.arg1 == 1) {
                    MainActivity.mhandler.sendMessage(message);

                    Thread.currentThread().interrupt();
                    break;
                } else {


                    bundle.putDouble("latitude", latitude);
                    bundle.putDouble("longitude", longitude);


                    message.setData(bundle);

                    MainActivity.mhandler.sendMessage(message);


                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }

                }

            }
        }







    }


