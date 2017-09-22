package Game;
import Common.EndPoint;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface GameInterface extends Remote{
    GameGlobalState executeRequest(String name, String request) throws RemoteException;
    GameGlobalState join(EndPoint IPAddress, String name) throws RemoteException;
    boolean updateBackupGameState(GameGlobalState gameGlobalState) throws RemoteException;
    boolean promoteToBeBackup(GameGlobalState gameGlobalState) throws RemoteException;
    List<EndPoint> getPrimaryAndBackupEndPoints() throws RemoteException;
}