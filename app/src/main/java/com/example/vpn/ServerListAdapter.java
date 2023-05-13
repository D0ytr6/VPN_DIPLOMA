package com.example.vpn;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ServerListAdapter extends ArrayAdapter<Server> {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Server> servers_list;

    public ServerListAdapter(Context context, ArrayList<Server> objects){
        super(context, 0, objects);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.servers_list = objects;
        Log.d("Size", Integer.toString(servers_list.size()));

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = inflater.inflate(R.layout.server_item, parent, false);
        }

        Server server = servers_list.get(position);

        TextView name_server_country = convertView.findViewById(R.id.name_server_country);
        ImageView flag = convertView.findViewById(R.id.country_flag);

        name_server_country.setText(server.getName_country());
        flag.setImageResource(server.getResourse_flag_id());

        return convertView;
    }
}
