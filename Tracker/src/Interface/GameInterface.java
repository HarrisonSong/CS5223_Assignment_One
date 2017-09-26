package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface GameInterface extends Remote {
    /**
     * Primary server exclusive
     */
    Object primaryExecuteRemoteRequest(String name, String command) throws RemoteException;
    Object primaryExecuteJoin(GameInterface stub, String name) throws RemoteException;
    boolean backupUpdateGameState(Object gameGlobalState) throws RemoteException;

    /**
     * Normal player methods
     */
    boolean playerPromoteAsBackup(Object gameGlobalState) throws RemoteException;
    List<GameInterface> getPrimaryAndBackupStubs() throws RemoteException;
}