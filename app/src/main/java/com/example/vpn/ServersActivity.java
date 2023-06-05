package com.example.vpn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.blinkt.openvpn.core.App;

public class ServersActivity extends AppCompatActivity {

    private LinearLayout exit_ll;
    private ListView servers_list;
    ServerListAdapter adapter;
    ArrayList<Server> servers = new ArrayList<Server>();
    EncryptData en = new EncryptData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servers);
        exit_ll = findViewById(R.id.backToMain);
        servers_list = findViewById(R.id.server_list);

        SharedPreferences SharedAppValues = getSharedPreferences("app_values", 0);
        String AppDetails = en.decrypt(SharedAppValues.getString("app_details", null));

        try{
            JSONObject json_response = new JSONObject(AppDetails);
            JSONArray jsonArray = json_response.getJSONArray("free");
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject= jsonArray.getJSONObject(i);

                int resource_photo_coutry;
                String image = jsonObject.getString("image");

                switch (image){
                    case "japan":
                        resource_photo_coutry = R.drawable.ic_flag_japan;
                        break;
                    case "russia":
                        resource_photo_coutry = R.drawable.ic_flag_russia;
                        break;
                    case "southkorea":
                        resource_photo_coutry = R.drawable.ic_flag_south_korea;
                        break;
                    case "thailand":
                        resource_photo_coutry = R.drawable.ic_flag_thailand;
                        break;
                    case "vietnam":
                        resource_photo_coutry = R.drawable.ic_flag_vietnam;
                        break;
                    case "unitedstates":
                        resource_photo_coutry = R.drawable.ic_flag_united_states;
                        break;
                    case "unitedkingdom":
                        resource_photo_coutry = R.drawable.ic_flag_united_kingdom;
                        break;
                    case "singapore":
                        resource_photo_coutry = R.drawable.ic_flag_singapore;
                        break;
                    case "france":
                        resource_photo_coutry = R.drawable.ic_flag_france;
                        break;
                    case "germany":
                        resource_photo_coutry = R.drawable.ic_flag_germany;
                        break;
                    case "canada":
                        resource_photo_coutry = R.drawable.ic_flag_canada;
                        break;
                    case "luxemburg":
                        resource_photo_coutry = R.drawable.ic_flag_luxemburg;
                        break;
                    case "netherlands":
                        resource_photo_coutry = R.drawable.ic_flag_netherlands;
                        break;
                    case "spain":
                        resource_photo_coutry = R.drawable.ic_flag_spain;
                        break;
                    case "finland":
                        resource_photo_coutry = R.drawable.ic_flag_finland;
                        break;
                    case "poland":
                        resource_photo_coutry = R.drawable.ic_flag_poland;
                        break;
                    case "australia":
                        resource_photo_coutry = R.drawable.ic_flag_australia;
                        break;
                    case "italy":
                        resource_photo_coutry = R.drawable.ic_flag_italy;
                        break;
                    default:
                        resource_photo_coutry = R.drawable.ic_flag_unknown_mali;
                        break;
                }

                Server server = new Server(jsonObject.getString("country"), resource_photo_coutry, jsonObject.getString("file"), image);
                servers.add(server);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


        adapter = new ServerListAdapter(getApplicationContext(), servers);
        servers_list.setAdapter(adapter);
        //servers_list.setClickable(true);

        exit_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_down, R.anim.alpha);
            }
        });

        servers_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Flag", Integer.toString(position));

                Server chosed_server = servers.get(position);
                String FilesDetails = en.decrypt(SharedAppValues.getString("file_details", null));

                try {
                    JSONObject json_file_response = new JSONObject(FilesDetails);
                    JSONArray jsonArray = json_file_response.getJSONArray("ovpn_file");
                    JSONObject jsonObject= jsonArray.getJSONObject(Integer.parseInt(chosed_server.getVpn_file_id()));
                    String connect_file = jsonObject.getString("file");

                    SharedPreferences file_details = getSharedPreferences("connection_data", 0);
                    SharedPreferences.Editor Editor = file_details.edit();
                    Editor.putString("file_id", chosed_server.getVpn_file_id());
                    Editor.putString("file", en.encrypt(connect_file));
                    Editor.putString("image", chosed_server.getImage());
                    Editor.putString("country", chosed_server.getName_country());
                    Editor.apply();
                    App.abortConnection = true;
                    finish();
                    overridePendingTransition(R.anim.slide_down, R.anim.alpha);

                } catch (JSONException e) {
                    e.printStackTrace();

                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_down, R.anim.alpha);
    }
}