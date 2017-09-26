package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface TrackerInterface extends Remote {
    <T> boolean registerNewPlayer(String IP, int port, String playName, GameInterface stub) throws RemoteException, InterruptedException;
    <T> boolean resetTrackerStubs(Map<String, T> updatedStubs) throws RemoteException, InterruptedException;
    Map retrieveStubs() throws RemoteException, InterruptedException;
    int getK() throws RemoteException;
    int getN() throws RemoteException;
}
