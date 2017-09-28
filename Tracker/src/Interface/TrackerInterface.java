package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface TrackerInterface extends Remote {
    int seedPort() throws RemoteException;
    boolean registerNewPlayer(String playName, GameInterface stub) throws RemoteException;
    boolean resetTrackerStubs(Map<String, GameInterface> updatedStubs) throws RemoteException;
    Map serveStubs() throws RemoteException;
    int getK() throws RemoteException;
    int getN() throws RemoteException;
}
