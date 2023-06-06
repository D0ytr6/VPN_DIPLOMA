package com.example.vpn;

public class ByteConverter {

    public static long KiloBytes = 1024;
    public static long MegaBytes = 1048576;
    public static long GigaBytes = 1073741824;

    public static long toKiloBytes(long bytes){
        return bytes / KiloBytes;
    }
    public static long toMegaBytes(long bytes){
        return bytes / MegaBytes;
    }
    public static long toGigaBytes(long bytes){
        return bytes / GigaBytes;
    }

}
