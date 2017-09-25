package Interface;
import Common.EndPoint;
import Common.Pair.IdEndPointPair;
import Game.GameGlobalState;
import Game.Player.Command;
import com.sun.xml.internal.bind.v2.model.core.ID;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface GameInterface extends Remote{
    GameGlobalState executeRequest(String name, Command playerCommand) throws RemoteException;
    GameGlobalState join(EndPoint IPAddress, String name) throws RemoteException;
    boolean updateBackupGameState(GameGlobalState gameGlobalState) throws RemoteException;
    boolean promoteToBeBackup(GameGlobalState gameGlobalState) throws RemoteException;
    List<IdEndPointPair> getPrimaryAndBackupEndPoints() throws RemoteException;
}