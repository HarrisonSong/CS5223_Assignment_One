package Game;

import Common.Pair.mazePair;
import Game.Player.Player;
import Game.Player.PlayerType;
import Interface.GameInterface;

/**
 *
 * PRIMARY SPECIFIC METHODS
 *
 */
public class PrimaryServerFunctions {
    PrimaryServerFunctions(){}

    /**
     * game setup when first player joins
     * @param playName
     */
    public static void setupGameAsPrimaryServer(String playName, GameInterface localStub, GameLocalState gameLocalState, GameGlobalState gameGlobalState){
        gameLocalState.setName(playName);
        gameLocalState.setPlayerType(PlayerType.Primary);
        gameLocalState.setLocalStub(localStub);
        Player primaryPlayer = new Player(playName, new mazePair(Game.MazeSize), 0, PlayerType.Primary);
        gameGlobalState.initialize(primaryPlayer);
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

    private static  void setBackupServer(int playerIndex,GameLocalState gameLocalState, GameGlobalState gameGlobalState){
        if(gameLocalState.getPlayerType() == PlayerType.Standard) {
            gameGlobalState.getPlayerList().get(playerIndex).setType(PlayerType.Backup);
            gameLocalState.setBackupStub(gameLocalState.getLocalStub());
        }
    }
}
