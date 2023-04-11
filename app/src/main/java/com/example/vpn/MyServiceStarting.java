package com.example.vpn;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MyServiceStarting extends Service {

    public MyServiceStarting() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service", "onStartCommand");
        Work();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.d("Service", "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Log.d("Service", "onCreate");
        super.onCreate();
    }

    private void Work(){
        for(int i = 0; i < 10; i++){
            Log.d("Service", Long.toString(Thread.currentThread().getId()) + " " + Integer.toString(i));
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }

        }
    }
}