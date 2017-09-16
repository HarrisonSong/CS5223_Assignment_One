package Game.Player;

import Game.GameState;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PlayerInterface extends Remote{
    GameState retrieveGameState() throws RemoteException;
}
