package com.example.vpn;

import static com.example.vpn.AppDataParcer.getAppDetails;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Typeface;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

//import androidx.core.splashscreen.SplashScreen;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.App;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;

public class MainActivity extends AppCompatActivity {

    private boolean isLoadDataReady = false;
    private String StringGetAppURL = "https://raw.githubusercontent.com/D0ytr6/ToyVPN_Testing/master/appdetails.json";
    private String StringGetConnectionURL = "https://raw.githubusercontent.com/D0ytr6/ToyVPN_Testing/master/filedetails.json";
    private String AppDetails, FileDetails;
    private InputStream inputStream;
    private BufferedReader bufferedReader;
    private ConfigParser configParser;
    private VpnProfile vpnProfile;
    private ProfileManager profileManager;

    private String Flag;

    private SharedPreferences SharedAppDetails;
    private Button btn_connect;
    private TextView load_counter, App_logo;
    boolean isPlay_anim = false;
    private LottieAnimationView lottie_animation;
    private ImageView chosen_server_img;
    private int timer;
    int Random;

    ConfigParser cp;
    VpnProfile vp;
    ProfileManager pm;
    String TODAY;


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
//
//        // Set up an OnPreDrawListener to the root view.
//        final View content = findViewById(android.R.id.content);
//        content.getViewTreeObserver().addOnPreDrawListener(
//                new ViewTreeObserver.OnPreDrawListener() {
//                    @Override
//                    public boolean onPreDraw() {
//                        // Check if the initial data is ready.
//                        if (isLoadDataReady) {
//                            // The content is ready; start drawing.
//                            content.getViewTreeObserver().removeOnPreDrawListener(this);
//                            return true;
//                        } else {
//
//                            // The content is not ready; suspend.
//                            //dismissSplashScreen();
//                            getAppDetails(MainActivity.this, StringGetAppURL, StringGetConnectionURL);
//                            isLoadDataReady = true;
//                            return false;
//                        }
//                    }
//                });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_window);

        btn_connect = findViewById(R.id.btn_connect);
        lottie_animation = findViewById(R.id.animation);
        chosen_server_img = findViewById(R.id.chosen_server);
        load_counter = findViewById(R.id.tv_count);
        App_logo = findViewById(R.id.app_name);

        Typeface pixel_typeface = Typeface.createFromAsset(getAssets(),"fonts/pixel_font2.ttf");
        Typeface roboto_typeface = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Bold.ttf");

        load_counter.setTypeface(roboto_typeface);
        App_logo.setTypeface(roboto_typeface);

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasInternetConnection()){
                    lottie_animation.setAnimation(R.raw.loading_circle);
                    lottie_animation.playAnimation();
                    timer = 30;
                    SharedPreferences file_details = getSharedPreferences("connection_data", 0);
                    String VPN_File = file_details.getString("file", "");
                    EncryptData En = new EncryptData();
                    VPN_File = En.decrypt(VPN_File);
                    start_vpn(VPN_File);
                    CountDownTimer countDownTimer = new CountDownTimer(10000, 1000){
                        @Override
                        public void onTick(long millisUntilFinished) {
                            timer = timer - 1;
                            load_counter.setText(Integer.toString(timer));

                        }

                        @Override
                        public void onFinish() {
                            load_counter.setText("");
                            lottie_animation.cancelAnimation();
                            lottie_animation.setAnimation(R.raw.noconnection);
                            lottie_animation.playAnimation();

                        }
                    }.start();
                }
            }
        });

//        start_service.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent service = new Intent(getBaseContext(), MyServiceStarting.class);
//                Log.d("Service", "Thread id: " + Long.toString(Thread.currentThread().getId()) + " MainActivity");
//                startService(service);
//
//            }
//        });
//
//        stop_service.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent service = new Intent(getBaseContext(), MyServiceStarting.class);
//                stopService(service);
//            }
//        });

        SharedPreferences connection_app_details = getSharedPreferences("connection_data", 0);
        Flag = connection_app_details.getString("image", "None");

        // ui update thread
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setFlag(Flag);
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d("Error", e.toString());
                    }
                }

            }
        };

        Thread ui_update = new Thread(r);
        ui_update.start();

        Intent SplashScreen = new Intent(MainActivity.this, WelcomeSplashScreen.class);
        startActivity(SplashScreen);
    }
    private void setFlag(String img_flag){
        switch (img_flag){
            case "unitedstates":
                chosen_server_img.setImageResource(R.drawable.ic_flag_united_states);
        }
    }

    private void start_vpn(String VPNFile) {
        SharedPreferences sp_settings;
        sp_settings = getSharedPreferences("daily_usage", 0);

        // connections count
        long connection_today = sp_settings.getLong(TODAY + "_connections", 0);
        long connection_total = sp_settings.getLong("total_connections", 0);

        // increment
        SharedPreferences.Editor editor = sp_settings.edit();
        editor.putLong(TODAY + "_connections", connection_today + 1);
        editor.putLong("total_connections", connection_total + 1);
        editor.apply();


        App.connection_status = 1;
        try {
            inputStream = null;
            bufferedReader = null;
            try {
                assert VPNFile != null;

                // ByteArrayInputStream for reading bytes stream
                inputStream = new ByteArrayInputStream(VPNFile.getBytes(Charset.forName("UTF-8")));
            } catch (Exception e) {

            }

            try { // M8
                assert inputStream != null;
                // create reader
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream/*, Charset.forName("UTF-8")*/));
            } catch (Exception e) {

            }
            // creating openvpn configure
            cp = new ConfigParser();
            try {
                // push config file to parser
                cp.parseConfig(bufferedReader);
            } catch (Exception e) {

            }

            // return and save VPN profile. return VpnProfile object
            vp = cp.convertProfile();
            // Allow apps witch blocked
            vp.mAllowedAppsVpnAreDisallowed = true;

            EncryptData En = new EncryptData();
            SharedPreferences AppValues = getSharedPreferences("app_values", 0);
            String AppDetailsValues = En.decrypt(AppValues.getString("app_details", "NA"));

            try {
                JSONObject json_response = new JSONObject(AppDetailsValues);
                JSONArray jsonArray = json_response.getJSONArray("blocked");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json_object = jsonArray.getJSONObject(i);
                    // add blocked aps to profile set
                    vp.mAllowedAppsVpn.add(json_object.getString("app"));
                    Log.e("packages", json_object.getString("app"));
                }
            } catch (JSONException e) {

            }


            try {
                // set build name
                vp.mName = Build.MODEL;
            } catch (Exception e) {
            }

            vp.mUsername = Data.FileUsername;
            vp.mPassword = Data.FilePassword;

            try {
                // get Profile manager
                pm = ProfileManager.getInstance(MainActivity.this);
                // add our profile and save
                pm.addProfile(vp);
                pm.saveProfileList(MainActivity.this);
                pm.saveProfile(MainActivity.this, vp);

                // start LaunchVPN activity
                // Нада йти в глибину LaunchVPN!
                vp = pm.getProfileByName(Build.MODEL);
                Intent intent = new Intent(getApplicationContext(), LaunchVPN.class);
                intent.putExtra(LaunchVPN.EXTRA_KEY, vp.getUUID().toString());
                intent.setAction(Intent.ACTION_MAIN);
                startActivity(intent);
                App.isStart = false;

            } catch (Exception e) {

            }
        } catch (Exception e) {

        }
    }


    private void start_vpn_connection(String VPN_File){
        if (VPN_File != null){
            try {
                inputStream = new ByteArrayInputStream(VPN_File.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Error", e.toString());
            }
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Error", e.toString());
            }

            // creating openvpn configure
            configParser = new ConfigParser();
            try {
                // push config file to parser
                configParser.parseConfig(bufferedReader);
            }
            catch (Exception e){
                e.printStackTrace();
                Log.d("Error", e.toString());
            }

            // return and save VPN profile. return VpnProfile object
            try {
                vpnProfile = configParser.convertProfile();
            } catch (Exception e){
                e.printStackTrace();
                Log.d("Error", e.toString());
            }
            // Allow apps witch blocked
            vpnProfile.mAllowedAppsVpnAreDisallowed = true;

            // Todo add disallowed apps

            // set name and password
            vpnProfile.mName = Build.MODEL;
            vpnProfile.mUsername = Data.FileUsername;
            vpnProfile.mPassword = Data.FilePassword;

            try {
                // singleton object
                profileManager = ProfileManager.getInstance(MainActivity.this);
                profileManager.addProfile(vpnProfile);
                profileManager.saveProfileList(MainActivity.this);
                profileManager.saveProfile(MainActivity.this, vpnProfile);
                vpnProfile = profileManager.getProfileByName(Build.MODEL);
                // start LaunchVPN activity
                Intent launchVPN_intent = new Intent(getApplicationContext(), LaunchVPN.class);
                // put UUID inside intent param
                launchVPN_intent.putExtra(LaunchVPN.EXTRA_KEY, vpnProfile.getUUID().toString());
                launchVPN_intent.setAction(Intent.ACTION_MAIN);
                startActivity(launchVPN_intent);
                App.isStart = false;

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Error", e.toString());
            }

        }
    }

    void getAppDetails2() {
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
                    getFileDetails2();
                }
            }
        });
    }

    void getFileDetails2() {
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













