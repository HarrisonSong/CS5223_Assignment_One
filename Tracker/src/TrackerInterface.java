import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface TrackerInterface extends Remote {
    boolean registerNewPlayer(String playName, GameInterface stub) throws RemoteException;
    boolean resetTrackerStubs(List<String> removedPlayers, String playerName) throws RemoteException;
    Map<String, GameInterface> serveStubs() throws RemoteException;
    int getK() throws RemoteException;
    int getN() throws RemoteException;
}
