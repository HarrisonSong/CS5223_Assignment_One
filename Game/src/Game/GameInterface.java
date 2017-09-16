package Game;

import Common.EndPoint;
import Game.GameState;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface GameInterface extends Remote{
    GameState makeMove() throws RemoteException;
    GameState join() throws RemoteException;
    void updateBackupGameState(GameState gs) throws RemoteException;
    void becomeBackup(GameState gs) throws RemoteException;
    List<EndPoint> getPrimaryAndBackupIp()throws RemoteException;
}