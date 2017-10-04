import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface GameInterface extends Remote {
    /**
     * Primary server exclusive
     */
    Object primaryExecuteRemoteRequest(String name, String command) throws RemoteException;
    Object primaryExecuteJoin(String name, GameInterface stub) throws RemoteException;

    /**
     * Backup server exclusive
     */
    void backupUpdateGameGlobalState(Object gameGlobalState) throws RemoteException;

    /**
     * Normal player methods
     */
    void playerPromoteAsBackup(Object gameGlobalState, GameInterface primary) throws RemoteException;
    void playerSetupAsStandard(Object gameGlobalState, GameInterface primary, GameInterface backup) throws RemoteException;
    List<GameInterface> getPrimaryAndBackupStubs() throws RemoteException;
    boolean isAlive() throws RemoteException;
}