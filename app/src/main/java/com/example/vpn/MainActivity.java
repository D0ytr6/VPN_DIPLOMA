package com.example.vpn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;

import androidx.core.splashscreen.SplashScreen;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private boolean isLoadDataReady = false;
    private String StringGetAppURL = "https://raw.githubusercontent.com/D0ytr6/ToyVPN_Testing/master/appdetails.json";
    private String StringGetConnectionURL = "https://raw.githubusercontent.com/D0ytr6/ToyVPN_Testing/master/filedetails.json";
    private String AppDetails, FileDetails;
    private SharedPreferences SharedAppDetails;
    private Button btn_connect;
    boolean isPlay_anim = false;
    private LottieAnimationView lottie_animation;
    int Random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        // Set up an OnPreDrawListener to the root view.
        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        // Check if the initial data is ready.
                        if (isLoadDataReady) {
                            // The content is ready; start drawing.
                            content.getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        } else {

                            // The content is not ready; suspend.
                            //dismissSplashScreen();
                            getAppDetails();
                            isLoadDataReady = true;
                            return false;
                        }
                    }
                });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_window);
        btn_connect = findViewById(R.id.btn_connect);
        lottie_animation = findViewById(R.id.animation);

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlay_anim){
                    lottie_animation.cancelAnimation();
                    isPlay_anim = false;
                }
                else{
                    isPlay_anim = true;
                    lottie_animation.setAnimation(R.raw.servers);
                    lottie_animation.playAnimation();
                }

            }
        });

    }

    void getAppDetails() {
        // Create request queue from Volley library
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        // clear cache
        queue.getCache().clear();
        // Create request object
        StringRequest stringRequest = new StringRequest(Request.Method.GET, StringGetAppURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String Response) {
                        Log.d("Response", Response);
                        AppDetails = Response;
                        Data.isAppDetails = true;


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Bundle params = new Bundle();
                //params.putString("device_id", App.device_id);
                ///params.putString("exception", "WA2" + error.toString());
                Log.d("Error", error.toString());
                Data.isAppDetails = false;
            }
        });
        // Add object to queue
        queue.add(stringRequest);
        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
            @Override
            public void onRequestFinished(Request<String> request) {
                if (Data.isAppDetails) {
                    getFileDetails();
                }
            }
        });
    }

    void getFileDetails() {
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        queue.getCache().clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, StringGetConnectionURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String Response) {
                        FileDetails = Response;
                        Data.isConnectionDetails = true;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Bundle params = new Bundle();
//                params.putString("device_id", App.device_id);
//                params.putString("exception", "WA3" + error.toString());
                Data.isConnectionDetails = false;
            }
        });
        // Add to queue requests
        queue.add(stringRequest);

        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
            @Override
            public void onRequestFinished(Request<String> request) {

                String Ads = "NULL", cuVersion = "NULL", upVersion = "NULL", upTitle = "NULL", upDescription = "NULL", upSize = "NULL";
                String ID = "NULL", FileID = "NULL", File = "NULL", City = "NULL", Country = "NULL", Image = "NULL",
                        IP = "NULL", Active = "NULL", Signal = "NULL";
                String BlockedApps = "NULL";

                // Get ads
                try {
                    JSONObject jsonResponse = new JSONObject(AppDetails);
                    Ads = jsonResponse.getString("ads");
                } catch (Exception e) {
                    Log.d("Error", e.toString());
                }

                // App details from json array
                try {
                    JSONObject jsonResponse = new JSONObject(AppDetails);
                    JSONArray jsonArray = jsonResponse.getJSONArray("update");
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    upVersion = jsonObject.getString("version");
                    upTitle = jsonObject.getString("title");
                    upDescription = jsonObject.getString("description");
                    upSize = jsonObject.getString("size");
                } catch (Exception e) {
                    Log.d("Error", e.toString());
                }

                // Get servers data from json array
                try {
                    JSONObject json_response = new JSONObject(AppDetails);
                    JSONArray jsonArray = json_response.getJSONArray("free");
                    JSONObject json_object = jsonArray.getJSONObject(0);
                    ID = json_object.getString("id");
                    FileID = json_object.getString("file");
                    City = json_object.getString("city");
                    Country = json_object.getString("country");
                    Image = json_object.getString("image");
                    IP = json_object.getString("ip");
                    Active = json_object.getString("active");
                    Signal = json_object.getString("signal");

                } catch (Exception e) {
                    Log.d("Error", e.toString());
                }

                // Get ovpn file
                try {
                    JSONObject json_response = new JSONObject(FileDetails);
                    JSONArray jsonArray = json_response.getJSONArray("ovpn_file");
                    JSONObject json_object = jsonArray.getJSONObject(Integer.valueOf(FileID));
                    FileID = json_object.getString("id");
                    File = json_object.getString("file");
                    Log.d("FileID", File);
                } catch (Exception e) {
                    Log.d("Error", e.toString());
                }


                // save details
                EncryptData En = new EncryptData();
                try {
                    // save current version
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    cuVersion = pInfo.versionName;
                    if (cuVersion.isEmpty()) {
                        cuVersion = "0.0.0";
                    }

                    // saving app details to SharedPreferences file
                    SharedAppDetails = getSharedPreferences("app_details", 0);
                    SharedPreferences.Editor Editor = SharedAppDetails.edit();
                    Editor.putString("ads", Ads);
                    Editor.putString("up_title", upTitle);
                    Editor.putString("up_description", upDescription);
                    Editor.putString("up_size", upSize);
                    Editor.putString("up_version", upVersion);
                    Editor.putString("cu_version", cuVersion);
                    Editor.apply();
                } catch (Exception e) {
                    Log.d("Error", e.toString());
                }

                // saving app connection data to SharedPreferences file
                try {
                    SharedAppDetails = getSharedPreferences("connection_data", 0);
                    SharedPreferences.Editor Editor = SharedAppDetails.edit();
                    Editor.putString("id", ID);
                    Editor.putString("file_id", FileID);
                    Editor.putString("file", En.encrypt(File));
                    Editor.putString("city", City);
                    Editor.putString("country", Country);
                    Editor.putString("image", Image);
                    Editor.putString("ip", IP);
                    Editor.putString("active", Active);
                    Editor.putString("signal", Signal);
                    Editor.apply();
                } catch (Exception e) {
                    Log.d("Error", e.toString());
                }

                // Save all string file
                try {
                    SharedAppDetails = getSharedPreferences("app_values", 0);
                    SharedPreferences.Editor Editor = SharedAppDetails.edit();
                    Editor.putString("app_details", En.encrypt(AppDetails));
                    Editor.putString("file_details", En.encrypt(FileDetails));
                    Editor.apply();
                } catch (Exception e) {
                    Log.d("Error", e.toString());
                }
            }
        });
    }

    private boolean hasInternetConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected())
                        haveConnectedWifi = true;
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected())
                        haveConnectedMobile = true;
            }
        } catch (Exception e) {
            Log.d("Error", e.toString());
        }

        return haveConnectedWifi || haveConnectedMobile;
    }
}













