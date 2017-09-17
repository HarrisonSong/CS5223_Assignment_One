package Game;
import Common.EndPoint;
import Game.GameState;
import Game.Player.Command;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface GameInterface extends Remote{
    GameState makeMove(Command cmd, char[] id) throws RemoteException;
    GameState join(EndPoint Ip, char[] id) throws RemoteException;
    boolean updateBackupGameState(GameState gs) throws RemoteException;
    boolean becomeBackup(GameState gs) throws RemoteException;
    List<EndPoint> getPrimaryAndBackupIp()throws RemoteException;
}