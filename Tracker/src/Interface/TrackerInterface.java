package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface TrackerInterface extends Remote {
    boolean registerNewPlayer(String playName, GameInterface stub) throws RemoteException, InterruptedException;
    boolean resetTrackerStubs(Map<String, GameInterface> updatedStubs) throws RemoteException, InterruptedException;
    Map serveStubs() throws RemoteException, InterruptedException;
    int getK() throws RemoteException;
    int getN() throws RemoteException;
}
