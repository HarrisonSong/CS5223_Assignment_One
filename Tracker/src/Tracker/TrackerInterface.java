package Tracker;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


public interface TrackerInterface extends Remote {
    void registerNewPlayer(String IP, int Port) throws RemoteException;
    void resetTrackerList(List<EndPoint> updatedList) throws RemoteException;
    int getK() throws RemoteException;
    int getN() throws RemoteException;
    List<EndPoint> getEndPointList() throws RemoteException;

}
