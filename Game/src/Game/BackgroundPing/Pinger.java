package Game.BackgroundPing;

import Common.EndPoint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Pinger {
    private static final int TIMEOUT = 500;
    private InetSocketAddress destination;

    public Pinger(EndPoint endPoint) {
        this.destination = new InetSocketAddress(endPoint.getIPAddress(), endPoint.getPort());
    }

    public Pinger(String IP, int port) {
        this.destination = new InetSocketAddress(IP, port);
    }

    public InetSocketAddress getDestination() {
        return destination;
    }

    public boolean isReachable() {
        Socket connection = new Socket();
        try {
            try {
                connection.connect(this.destination, TIMEOUT);
            } finally {
                connection.close();
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
