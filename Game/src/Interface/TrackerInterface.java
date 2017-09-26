package Interface;

import Common.EndPoint;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface TrackerInterface extends Remote {
    <T> boolean registerNewPlayer(String IP, int port, String playName, T stub) throws RemoteException, InterruptedException;
    boolean resetTrackerEndPointsMap(Map<String, EndPoint> updatedMap) throws RemoteException, InterruptedException;
    Map retrieveEndPointsMap() throws RemoteException, InterruptedException;
    int getK() throws RemoteException;
    int getN() throws RemoteException;
}
