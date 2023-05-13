package com.example.vpn;

public class Server {

    private String name_country;
    private int resourse_flag_id;
    private String vpn_file_id;
    private String image;

    public Server(String name_country, int resourse_flag_id, String vpn_id, String image) {
        this.name_country = name_country;
        this.resourse_flag_id = resourse_flag_id;
        this.vpn_file_id = vpn_id;
        this.image = image;
    }

    public String getName_country() {
        return name_country;
    }

    public int getResourse_flag_id() {
        return resourse_flag_id;
    }

    public String getVpn_file_id() {
        return vpn_file_id;
    }

    public String getImage() {
        return image;
    }

}
