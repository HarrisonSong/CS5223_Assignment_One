package Game;

import Common.EndPoint;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


public interface TrackerInterface extends Remote {
    List<EndPoint> registerNewPlayer(String IP, int Port) throws RemoteException;
    boolean resetTrackerList(List<EndPoint> updatedList) throws RemoteException;
    int getK() throws RemoteException;
    int getN() throws RemoteException;
    List<EndPoint> getEndPointList() throws RemoteException;

}