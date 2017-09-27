package Utility;

import Common.mazePair;
import Game.Game;
import Game.GameGlobalState;
import Game.GameLocalState;
import Game.Player.Player;
import Game.Player.PlayerType;
import Interface.GameInterface;

import java.rmi.RemoteException;

public class PrimaryServerHelper {
    /**
     * game setup when first player joins
     * @param playName
     */
    public static void initializeGlobalState(String playName, Game game){
        Player primaryPlayer = new Player(playName, new mazePair(Game.MazeSize), 0, PlayerType.Primary);
        game.getGameGlobalState().initialize(primaryPlayer);
    }

    /**
     * Assign player to be backup server
     */
    public static void assignBackupServer(GameLocalState gameLocalState, GameGlobalState gameGlobalState){
        while(true){
            int latestActivePlayerIndex = gameGlobalState.findNextActivePlayerIndex();
            if(latestActivePlayerIndex == -1){
                break;
            }
            String backupPlayerName = gameGlobalState.getPlayerList().get(latestActivePlayerIndex).getName();
            GameInterface newBackupStub = gameLocalState.getPlayerStubsMap().get(backupPlayerName);
            gameLocalState.setBackupStub(newBackupStub);
            if(gameLocalState.getBackupStub()!= null){
                setBackupServer(latestActivePlayerIndex, gameLocalState, gameGlobalState);
                break;
            }
        }
    }

    private static void setBackupServer(int playerIndex,GameLocalState gameLocalState, GameGlobalState gameGlobalState){
        if(gameLocalState.getPlayerType() == PlayerType.Standard) {
            gameGlobalState.getPlayerList().get(playerIndex).setType(PlayerType.Backup);
            gameLocalState.setBackupStub(gameLocalState.getLocalStub());
        }
    }

    public static void updateTrackerStubMap(GameLocalState localState){
        try {
            localState.getTrackerStub().resetTrackerStubs(localState.getPlayerStubsMap());
        } catch (RemoteException | InterruptedException e) {
            e.printStackTrace();
            System.err.println("Tracker is offline");
            System.exit(0);
        }
    }
}
