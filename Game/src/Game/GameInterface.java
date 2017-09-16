package Game;

import Game.GameState;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameInterface extends Remote{
    GameState retrieveGameState() throws RemoteException;
}