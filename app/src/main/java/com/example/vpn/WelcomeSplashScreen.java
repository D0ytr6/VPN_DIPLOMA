package com.example.vpn;

import static com.example.vpn.AppDataParcer.getAppDetails;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;

public class WelcomeSplashScreen extends AppCompatActivity {

    private String StringGetAppURL = "https://raw.githubusercontent.com/D0ytr6/ToyVPN_Testing/master/appdetails.json";
    private String StringGetConnectionURL = "https://raw.githubusercontent.com/D0ytr6/ToyVPN_Testing/master/filedetails.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_splash_screen);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getAppDetails(WelcomeSplashScreen.this, StringGetAppURL, StringGetConnectionURL);
                finish();
            }
        }, 2000);
    }
}