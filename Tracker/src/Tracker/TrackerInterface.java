package Tracker;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface TrackerInterface extends Remote {
    boolean registerNewPlayer(String IP, int port, String playName) throws RemoteException, InterruptedException;
    boolean resetTrackerEndPointsMap(Map<String, EndPoint> updatedMap) throws RemoteException, InterruptedException;
    Map<String, EndPoint> retrieveEndPointsMap() throws RemoteException, InterruptedException;
    int getK() throws RemoteException;
    int getN() throws RemoteException;
}
