package com.example.vpn;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class AppDataParcer {

    private static RequestQueue queue;
    private static String AppDetails;
    private static String FileDetails;
    private static SharedPreferences SharedAppDetails;
    private static int auto_connect_index = 0;

    public static void getAppDetails(Context context, String StringGetAppURL, String GetFileURL){
        // Create request queue from Volley library
        queue = Volley.newRequestQueue(context);
        // clear cache
        queue.getCache().clear();
        // Create request object
        String file_details;
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
                    getFileDetails(context, GetFileURL);
                }
            }
        });
    }


    private static void getFileDetails(Context context, String StringGetConnectionURL) {
        queue = Volley.newRequestQueue(context);
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
                // TODO: Remove JsonObjects
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
                    JSONObject json_object = jsonArray.getJSONObject(auto_connect_index);
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
                    PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                    cuVersion = pInfo.versionName;
                    if (cuVersion.isEmpty()) {
                        cuVersion = "0.0.0";
                    }

                    // saving app details to SharedPreferences file
                    SharedAppDetails = context.getSharedPreferences("app_details", 0);
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
                    SharedAppDetails = context.getSharedPreferences("connection_data", 0);
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
                    SharedAppDetails = context.getSharedPreferences("app_values", 0);
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

}
