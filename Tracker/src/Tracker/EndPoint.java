package Tracker;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class EndPoint {
    private String IPAddress = "";
    private int port = 0;

    public EndPoint(){
        IPAddress = "";
        port = 0;
    };

    public EndPoint(String IPAddress, int port){
        this.IPAddress = IPAddress;
        this.port = port;
    }

    public EndPoint(String fullPath){
        String[] parts = fullPath.split(":");
        IPAddress = parts[0];
        port = Integer.parseInt(parts[1]);
    }

    public static String getLocalIP(){
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public int getPort(){
        return port;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

