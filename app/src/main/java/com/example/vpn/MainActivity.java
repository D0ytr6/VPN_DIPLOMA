package com.example.vpn;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.IOpenVPNServiceInternal;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VpnStatus;


public class MainActivity extends AppCompatActivity implements VpnStatus.StateListener {

    private String LogInf = "LOGGING_INFO";
    private String Mylogging = "Mylogging";

    private InputStream ByteInputStream;
    private BufferedReader ByteBufferedReader;
    private ConfigParser configParser;
    private VpnProfile vpnProfile;
    private ProfileManager profileManager;
    private CountDownTimer countDownTimer;

    private String Flag, ServerLocationCountry, VpnFileConnection;

    private SharedPreferences SharedAppDetails;
    private Button btn_connect;
    private TextView load_counter, App_logo;
    private LinearLayout chose_server, connection_data;

    boolean isPlay_anim = false;
    private LottieAnimationView lottie_animation;
    private ImageView chosen_server_img;
    private int timer;

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    private TextView tv_server_location, current_ip, protected_state;
    private EncryptData encryptData = new EncryptData();
    private boolean IsConButtonPushed = false, isTimerStarted = false;

    IOpenVPNServiceInternal vpn_service;
    ConfigParser conf_parcer;
    VpnProfile vpn_profile;
    ProfileManager profile_manager;
    String TODAY;

    //TODO Add click event to drawer elements
    //TODO fix bug whith connect button, if clear app when connection established and open again
    //TODO reduce bold of timer +
    //TODO save chosen server in SharedPreferences
    //TODO fix slow load of ip
    //TODO bug, app load old SharedPreferences and after update it, if change other server app anim will crash

    private ServiceConnection ConnectionService = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            vpn_service = IOpenVPNServiceInternal.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            vpn_service = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!Data.isAppDetails && !Data.isConnectionDetails){
            Intent SplashScreen = new Intent(MainActivity.this, WelcomeSplashScreen.class);
            startActivity(SplashScreen);

            // TODO Fix shit with second start of splash
            Data.isAppDetails = true;
            Data.isConnectionDetails = true;
        }

        VpnStatus.addStateListener(this);

        Intent intent = new Intent(this, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        bindService(intent, ConnectionService, Context.BIND_AUTO_CREATE);

        SharedPreferences connection_app_details = getSharedPreferences("connection_data", 0);
        Flag = connection_app_details.getString("image", "None");
        ServerLocationCountry = connection_app_details.getString("country", "None");
        VpnFileConnection = encryptData.decrypt(connection_app_details.getString("file", "None"));
        Log.d("Flag", Flag);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nav_logout){
            Log.d(Mylogging, "close clicked");
            finish();
        }

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btn_connect = findViewById(R.id.btn_connect);
        lottie_animation = findViewById(R.id.animation);
        chosen_server_img = findViewById(R.id.chosen_server);
        load_counter = findViewById(R.id.tv_count);
        App_logo = findViewById(R.id.app_name);
        chose_server = findViewById(R.id.chose_menu);
        tv_server_location = findViewById(R.id.tv_server_location);
        current_ip = findViewById(R.id.current_ip);
        protected_state = findViewById(R.id.protected_state);
        connection_data = findViewById(R.id.connection_data);

        Typeface pixel_typeface = Typeface.createFromAsset(getAssets(),"fonts/pixel_font2.ttf");
        Typeface roboto_typeface = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Bold.ttf");

        load_counter.setTypeface(roboto_typeface);
        App_logo.setTypeface(roboto_typeface);

        update_ip();

        Handler animation_handler = new Handler();
        animation_handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation slide_down_anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down);
                chose_server.startAnimation(slide_down_anim);
                btn_connect.startAnimation(slide_down_anim);
                lottie_animation.startAnimation(slide_down_anim);
                connection_data.startAnimation(slide_down_anim);
            }
        }, 1500);

        protected_state.setText("Unprotected");
        protected_state.setTextColor(getResources().getColor(R.color.colorRed));
        lottie_animation.setAnimation(R.raw.noconnection);
        lottie_animation.playAnimation();

        // TODO if cancel pushed when timer don't shown, timer will stand on ui
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!IsConButtonPushed){
                    if(hasInternetConnection()){
                        IsConButtonPushed = true;
                        create_animation(lottie_animation, R.anim.fade_out);
                        btn_connect.setText("CANCEL");
                        timer = 30;

                        // wait until fade_uot
                        Handler ui_handler = new Handler();
                        ui_handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                lottie_animation.cancelAnimation();
                                create_animation(lottie_animation, R.anim.fade_in);
                                create_animation(load_counter, R.anim.fade_in);
                                load_counter.setText(Integer.toString(timer));
                                lottie_animation.setAnimation(R.raw.loading_circle);
                                lottie_animation.playAnimation();
                            }
                        }, 1000);

                        start_vpn(VpnFileConnection);
                        isTimerStarted = true;

                        countDownTimer = new CountDownTimer(30000, 1000){
                            @Override
                            public void onTick(long millisUntilFinished) {
                                timer = timer - 1;
                                if(timer != 29){
                                    load_counter.setText(Integer.toString(timer));
                                }
                                // wait to return connection status from state listener
                                if (App.connection_status == 2){
                                    countDownTimer.cancel();
                                    isTimerStarted = false;
                                    App.isStart = true;

                                    create_animation(load_counter, R.anim.fade_out);
                                    create_animation(lottie_animation, R.anim.fade_out);
                                    btn_connect.setText("DISCONNECT");
                                    protected_state.setText("Protected");
                                    protected_state.setTextColor(getResources().getColor(R.color.darkGreen));

                                    update_ip();

                                    Handler successHandler = new Handler();
                                    successHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            load_counter.setText("");
                                            lottie_animation.cancelAnimation();
                                            create_animation(lottie_animation, R.anim.fade_in);
                                            lottie_animation.setAnimation(R.raw.space_user);
                                            lottie_animation.playAnimation();
                                        }
                                    }, 1000);
                                }

                            }
                            // if time passed and connection doesn't happened
                            @Override
                            public void onFinish() {
                                IsConButtonPushed = false;
                                stop_vpn();
                                App.isStart = false;
                                countDownTimer.cancel();
                                isTimerStarted = false;

                                create_animation(load_counter, R.anim.fade_out);
                                create_animation(lottie_animation, R.anim.fade_out);
                                btn_connect.setText("CONNECT");

                                update_ip();

                                Handler successHandler = new Handler();
                                successHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        load_counter.setText("");
                                        lottie_animation.cancelAnimation();
                                        create_animation(lottie_animation, R.anim.fade_in);
                                        lottie_animation.setAnimation(R.raw.noconnection);
                                        lottie_animation.playAnimation();
                                    }
                                }, 1000);

                            }
                        }.start();
                    }
                }
                else{
                    IsConButtonPushed = false;
                    App.isStart = false;
                    stop_vpn();

                    update_ip();
                    create_animation(load_counter, R.anim.fade_out);
                    create_animation(lottie_animation, R.anim.fade_out);

                    countDownTimer.cancel();
                    btn_connect.setText("CONNECT");
                    protected_state.setText("Unprotected");
                    protected_state.setTextColor(getResources().getColor(R.color.colorRed));

                    Handler cancelHandler = new Handler();
                    cancelHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            load_counter.setText("");
                            lottie_animation.cancelAnimation();
                            create_animation(lottie_animation, R.anim.fade_in);
                            lottie_animation.setAnimation(R.raw.noconnection);
                            lottie_animation.playAnimation();
                        }
                    }, 1000);

                }

            }
        });

        chose_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent server_activity = new Intent(MainActivity.this, ServersActivity.class);
                startActivity(server_activity);
                overridePendingTransition(R.anim.bottom_in, R.anim.alpha);
            }
        });

        // ui update thread
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setFlag(Flag);
                            tv_server_location.setText(ServerLocationCountry);
                            if(App.abortConnection){
                                if(App.isStart){
                                    App.abortConnection = false;
                                    stop_vpn();
                                    update_ip();
                                    IsConButtonPushed = false;
                                    btn_connect.setText("CONNECT");
                                    protected_state.setText("Unprotected");
                                    protected_state.setTextColor(getResources().getColor(R.color.colorRed));
                                    lottie_animation.cancelAnimation();
                                    lottie_animation.setAnimation(R.raw.noconnection);
                                    lottie_animation.playAnimation();

                                }
                                else {
                                    if(isTimerStarted){
                                        App.abortConnection = false;
                                        IsConButtonPushed = false;
                                        load_counter.setText("");
                                        countDownTimer.cancel();
                                        btn_connect.setText("CONNECT");
                                        protected_state.setText("Unprotected");
                                        protected_state.setTextColor(getResources().getColor(R.color.colorRed));
                                        lottie_animation.cancelAnimation();
                                        lottie_animation.setAnimation(R.raw.noconnection);
                                        lottie_animation.playAnimation();
                                    }
                                }

                            }

                        }
                    });
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d("Error", e.toString());
                    }
                }

            }
        };

        Thread ui_update = new Thread(r);
        ui_update.start();

    }



    private void create_animation(View view, int anim_id){
        Animation animation =  AnimationUtils.loadAnimation(MainActivity.this, anim_id);
        view.setAnimation(animation);
    }

    private void update_ip(){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.getCache().clear();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://api.ipify.org",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String Response) {
                        Log.d("Response", Response);
                        current_ip.setText("Your ip: " + Response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
            }
        });
        queue.add(stringRequest);
    }

    private void stop_vpn(){
        App.connection_status = 0;
        OpenVPNService.abortConnectionVPN = true;

        // set this profile disconnected
        ProfileManager.setConntectedVpnProfileDisconnected(this);

        try {
            vpn_service.stopVPN(false);
        }
        catch (RemoteException e){
            Log.d("Error", e.toString());
        }

        // get profile manager and remove current profile
        profileManager = ProfileManager.getInstance(this);
        vpnProfile = profileManager.getProfileByName(Build.MODEL);
        profileManager.removeProfile(this, vpnProfile);

    }

    // TODO Add all flags
    private void setFlag(String img_flag){
        switch (img_flag){
            case "japan":
                chosen_server_img.setImageResource(R.drawable.ic_flag_japan);
                break;
            case "russia":
                chosen_server_img.setImageResource(R.drawable.ic_flag_russia);
                break;
            case "southkorea":
                chosen_server_img.setImageResource(R.drawable.ic_flag_south_korea);
                break;
            case "thailand":
                chosen_server_img.setImageResource(R.drawable.ic_flag_thailand);
                break;
            case "vietnam":
                chosen_server_img.setImageResource(R.drawable.ic_flag_vietnam);
                break;
            case "unitedstates":
                chosen_server_img.setImageResource(R.drawable.ic_flag_united_states);
                break;
            case "unitedkingdom":
                chosen_server_img.setImageResource(R.drawable.ic_flag_united_kingdom);
                break;
            case "singapore":
                chosen_server_img.setImageResource(R.drawable.ic_flag_singapore);
                break;
            case "france":
                chosen_server_img.setImageResource(R.drawable.ic_flag_france);
                break;
            case "germany":
                chosen_server_img.setImageResource(R.drawable.ic_flag_germany);
                break;
            case "canada":
                chosen_server_img.setImageResource(R.drawable.ic_flag_canada);
                break;
            case "luxemburg":
                chosen_server_img.setImageResource(R.drawable.ic_flag_luxemburg);
                break;
            case "netherlands":
                chosen_server_img.setImageResource(R.drawable.ic_flag_netherlands);
                break;
            case "spain":
                chosen_server_img.setImageResource(R.drawable.ic_flag_spain);
                break;
            case "finland":
                chosen_server_img.setImageResource(R.drawable.ic_flag_finland);
                break;
            case "poland":
                chosen_server_img.setImageResource(R.drawable.ic_flag_poland);
                break;
            case "australia":
                chosen_server_img.setImageResource(R.drawable.ic_flag_australia);
                break;
            case "italy":
                chosen_server_img.setImageResource(R.drawable.ic_flag_italy);
                break;
            default:
                chosen_server_img.setImageResource(R.drawable.ic_flag_unknown_mali);
                break;
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
            ByteInputStream = null;
            ByteBufferedReader = null;
            try {
                assert VPNFile != null;

                // ByteArrayInputStream for reading bytes stream
                ByteInputStream = new ByteArrayInputStream(VPNFile.getBytes(Charset.forName("UTF-8")));
            } catch (Exception e) {

            }

            try { // M8
                assert ByteInputStream != null;
                // create reader
                ByteBufferedReader = new BufferedReader(new InputStreamReader(ByteInputStream/*, Charset.forName("UTF-8")*/));
            } catch (Exception e) {

            }
            // creating openvpn configure
            conf_parcer = new ConfigParser();
            try {
                // push config file to parser
                conf_parcer.parseConfig(ByteBufferedReader);
            } catch (Exception e) {

            }

            // return and save VPN profile. return VpnProfile object
            vpn_profile = conf_parcer.convertProfile();
            // Allow apps witch blocked
            vpn_profile.mAllowedAppsVpnAreDisallowed = true;

            EncryptData En = new EncryptData();
            SharedPreferences AppValues = getSharedPreferences("app_values", 0);
            String AppDetailsValues = En.decrypt(AppValues.getString("app_details", "NA"));

            try {
                JSONObject json_response = new JSONObject(AppDetailsValues);
                JSONArray jsonArray = json_response.getJSONArray("blocked");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json_object = jsonArray.getJSONObject(i);
                    // add blocked aps to profile set
                    vpn_profile.mAllowedAppsVpn.add(json_object.getString("app"));
                    Log.e("packages", json_object.getString("app"));
                }
            } catch (JSONException e) {

            }


            try {
                // set build name
                vpn_profile.mName = Build.MODEL;
            } catch (Exception e) {
            }

            vpn_profile.mUsername = Data.FileUsername;
            vpn_profile.mPassword = Data.FilePassword;

            try {
                // get Profile manager
                profile_manager = ProfileManager.getInstance(MainActivity.this);
                // add our profile and save
                profile_manager.addProfile(vpn_profile);
                profile_manager.saveProfileList(MainActivity.this);
                profile_manager.saveProfile(MainActivity.this, vpn_profile);

                // start LaunchVPN activity
                // Нада йти в глибину LaunchVPN!
                vpn_profile = profile_manager.getProfileByName(Build.MODEL);
                Intent intent = new Intent(getApplicationContext(), LaunchVPN.class);
                intent.putExtra(LaunchVPN.EXTRA_KEY, vpn_profile.getUUID().toString());
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
                ByteInputStream = new ByteArrayInputStream(VPN_File.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Error", e.toString());
            }
            try {
                ByteBufferedReader = new BufferedReader(new InputStreamReader(ByteInputStream));
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Error", e.toString());
            }

            // creating openvpn configure
            configParser = new ConfigParser();
            try {
                // push config file to parser
                configParser.parseConfig(ByteBufferedReader);
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

    @Override
    public void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level) {
        if(state.equals("CONNECTED")){
            App.isStart = true;
            App.connection_status = 2;
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            });
        }
    }

    @Override
    public void setConnectedVPN(String uuid) {
        Log.d(LogInf, uuid);
    }
}













