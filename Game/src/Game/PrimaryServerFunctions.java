package Game;

import Common.EndPoint;
import Common.Pair.NameEndPointPair;
import Common.Pair.mazePair;
import Game.Player.Player;
import Game.Player.PlayerType;

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
    public void setupGameAsPrimaryServer(String playName, String localIPAddress){
        this.gameLocalState.setPlayName(playName);
        this.gameLocalState.setPlayerType(PlayerType.Primary);
        this.gameLocalState.setLocalEndPoint(new NameEndPointPair(playName, new EndPoint(localIPAddress, DEFAULT_PLAYER_ACCESS_PORT)));
        Player primaryPlayer = new Player(playName, new mazePair(Game.MazeSize), 0, PlayerType.Primary);
        this.gameGlobalState.initialize(primaryPlayer);
    }

    /**
     * Assign player to be backup server
     */
    private void assignBackupServer(){
        while(true){
            int latestActivePlayerIndex = this.gameGlobalState.findNextActivePlayerIndex();
            if(latestActivePlayerIndex == -1){
                break;
            }
            String backupPlayerName = this.gameGlobalState.getPlayerList().get(latestActivePlayerIndex).getName();
            EndPoint newEndPoint = this.gameLocalState.getPlayerEndPointsMap().get(backupPlayerName);
            this.backupServer = this.contactGameEndPoint(new NameEndPointPair(backupPlayerName,newEndPoint));
            if(this.backupServer != null){
                setBackupServer(latestActivePlayerIndex);
                break;
            }
        }
    }

    private void setBackupServer(int playerIndex){
        if(this.gameLocalState.getPlayerType() == PlayerType.Standard) {
            this.gameGlobalState.getPlayerList().get(playerIndex).setType(PlayerType.Backup);
            this.gameLocalState.setBackupEndPoint(new NameEndPointPair(
                    this.gameLocalState.getPlayName(),
                    this.gameLocalState.getPlayerEndPointsMap().get(this.gameLocalState.getPlayName()))
            );
        }
    }
}
