package Game;

import Common.*;
import Game.BackgroundPing.Pinger;
import Game.Player.Command;
import Game.Player.Player;
import Game.Player.PlayerType;

import java.util.*;

public class Game implements GameInterface {

    /**
     * static game global constants
     */
    public static int NameLength = 2;
    public static int MazeSize = 15;
    public static int TreasureSize = 10;

    /**
     * game data to be bind with GUI
     */
    private GameGlobalState gameGlobalState;

    /**
     * local game data
     */
    private GameLocalState gameLocalState;

    private HashMap<String, EndPoint> playerEndPointsMap;

    public Game() {
        this.gameGlobalState = new GameGlobalState();
        this.gameLocalState = new GameLocalState();
        this.playerEndPointsMap = new HashMap<>();
    }

    /**
     * entrance for the Game
     * @param args
     */
    public static void main(String args[]) {
        // Args: [Tracker IP], [Tracker Port], [Player ID]

        /* Init Server */
        // Set up Tracker Connection

        // Get local IP and ports

        // Try to contact Tracker
        // Success, return with list of player Ip
        // If the list is empty, become primary server, setup game (No one in the list) -- END
        // Else,
        // For LOOP: try contact with Play Ip(starting at the last one)
        // All IPs in the list fail to contact, then become primary server, setup game (No alive one in the list) -- END
        // Any one success, get Primary & backup, break the for loop
        // While LOOP: while (Primary IP is not null):
        // If contact primary server to join game
        // Fail[primary crash],
        // If backup IP is null, become primary server, setup game (No alive one in the list) -- END
        // Else, send getPrimaryAndBackupEndPoints to backup server, update IPs, get new primary and backup
        // Success,
        // Fail, no server alive, setup itself as primary, and setup game -- END
        // Success, break the while loop: return game state and current Primary & Backup. If backup == itself, setup its self to backup -- END
        // Fail, exit with error


        /* Play Mode */
        // Get input from IO, loop
        // '9' observed, exit
        // non '9' observed,
        // Case 1: Standard
        //Try to call primary to makeMove
        //timeout retry
        //fail, send getPrimaryAndBackupEndPoints to backup stored, update Ips, call (new)server to makeMove (while loop?)
        //success, update local game state
        // Case 2: Primary
        //Update locally, try to call backup to updateBackupGameState
        // Fail, select an Standard player to be new backup, update BackupIp, gameGlobalState (while loop?)
        // Success, do nothing
        // Case 3: Backup
        //Try to call primary to makeMove
        // fail, promotePlayerToBePrimary, makeMove locally, then select an Standard player to be new backup and (while loop?)
        // update backup's game state
        // Success

        //Ping (only do when no operation execute locally) (N-1)+ 1 + (N-1)*2 -> O(N)
        //Ping primary server every 0.5s, (primary no need ping itself)
        //fail,wait for 1s, send getPrimaryAndBackupEndPoints to backup stored, update Ips
        //success do nothing
        //Ping Backup every 0.5s, (backup no need ping itself)
        //fail,wait for 1s, send getPrimaryAndBackupEndPoints to primary stored, update Ips
        //success do nothing

        //(Primary Only) Ping standard player one by one
        //fail, remove from list, update gameGlobalState
        //success do nothing
    }

    /**
     *
     * PRIMARY SPECIFIC METHODS
     *
     */

    /**
     * game setup when first player joins
     * @param name
     */
    private void setupGameAsPrimaryServer(String name){
        this.gameLocalState.setPlayName(name);
        this.gameLocalState.setPlayerType(PlayerType.Primary);

        String localIPAddress = EndPoint.getLocalIP();
        this.gameLocalState.setLocalEndPoint(new EndPoint(localIPAddress, 80));

        Player primaryPlayer = new Player(name, new Pair(Game.MazeSize), 0, PlayerType.Primary);
        this.gameGlobalState.initialize(primaryPlayer);
    }

    /**
     * Assign player to be backup server
     */
    private void assignBackupServer(){
        setBackupServer(this.gameGlobalState.getLatestActivePlayerIndex());
    }

    private void setBackupServer(int playerIndex){
        if(this.gameLocalState.getPlayerType() == PlayerType.Standard && playerIndex != -1) {
            this.gameGlobalState.getPlayerList().get(playerIndex).setType(PlayerType.Backup);
            this.gameLocalState.setBackupEndPoint(this.playerEndPointsMap.get(this.gameGlobalState.getPlayerList().get(playerIndex).getName()));
        }
    }

    /**
     * read and process the player requests
     * @param playerName
     * @param request
     * @return updated global game state
     */
    public GameGlobalState executeRequest(String playerName, char request){
        Command classifiedRequest = classifyPlayerInput(request);
        if(classifiedRequest.equals(Command.Exit)){
            this.playerEndPointsMap.remove(playerName);
            this.gameGlobalState.removePlayer(playerName);
            return this.gameGlobalState;
        }
        if(!classifiedRequest.equals(Command.Invalid)){
            if(classifiedRequest.equals(Command.East) || classifiedRequest.equals(Command.West) ||
                    classifiedRequest.equals(Command.South) || classifiedRequest.equals(Command.North)){
                this.gameGlobalState.makeMove(classifiedRequest, playerName);
            }
            return this.gameGlobalState;
        }
        return this.gameGlobalState;
    }

    /**
     * handler to manage new player joining
     * @param IPAddress
     * @param playName
     * @return updated global game state
     */
    public GameGlobalState join(EndPoint IPAddress, String playName){
        if(this.playerEndPointsMap.size() == 1){
            this.gameLocalState.setBackupEndPoint(IPAddress);
            this.gameGlobalState.addNewPlayerByName(playName, PlayerType.Backup);
            this.playerEndPointsMap.put(playName, IPAddress);
        }else{
            this.gameGlobalState.addNewPlayerByName(playName, PlayerType.Standard);
            this.playerEndPointsMap.put(playName, IPAddress);
        }
        return this.gameGlobalState;
    }

    /**
     * method to detect endpoint status
     * @param IPAddress
     * @return endpoint alive status
     */
    public boolean checkAlive(EndPoint IPAddress){
        Pinger pinger = new Pinger(IPAddress);
        return pinger.isReachable();
    }

    /**
     *
     * BACKUP SPECIFIC METHODS
     *
     */
    /**
     * promote current game to be primary server
     * @param isTheOnlyPlayer
     */
    private void promoteToBePrimary(boolean isTheOnlyPlayer){
        if(isTheOnlyPlayer || this.gameLocalState.getPlayerType() == PlayerType.Backup) {
            this.gameLocalState.setPlayerType(PlayerType.Primary);
            this.gameGlobalState.getPlayerList().get(this.gameGlobalState.getIndexOfPlayerByName(this.gameLocalState.getPlayName())).setType(PlayerType.Primary);
            this.gameLocalState.setPrimaryEndPoint(this.gameLocalState.getLocalEndPoint());
        }
    }

    /**
     *
     * COMMON PLAYER METHODS
     *
     */

    /**
     * Update game global state according to primary
     * server response
     * @param gameState
     */
    private void updateGameGlobalState(GameGlobalState gameState){
        this.gameGlobalState.setPlayerList(gameState.getPlayerList());
        this.gameGlobalState.setTreasureLocation(gameState.getTreasureLocation());
    }

    /**
     * update local backup server global state from
     * updated primary server data.
     * @param gameState
     * @return
     */
    public boolean updateBackupGameState(GameGlobalState gameState){
        if(this.gameLocalState.getPlayerType() == PlayerType.Backup){
            updateGameGlobalState(gameState);
        }
        return false;
    }

    /**
     * promote current game to be backup server
     * @param gameState: updated global game state from primary server
     * @return promotion status
     */
    public boolean promoteToBeBackup(GameGlobalState gameState) {
        if(this.gameLocalState.getPlayerType() == PlayerType.Standard){
            this.gameLocalState.setPlayerType(PlayerType.Backup);
            this.gameLocalState.setBackupEndPoint(this.gameLocalState.getLocalEndPoint());
            this.updateGameGlobalState(gameState);
            return true;
        }
        return false;
    }

    /**
     * get primary and backup server endPoints
     * @return
     */
    public List<EndPoint> getPrimaryAndBackupEndPoints(){
        List<EndPoint> result = new ArrayList<EndPoint>();
        result.add(this.gameLocalState.getPrimaryEndPoint());
        result.add(this.gameLocalState.getBackupEndPoint());
        return result;
    }

    /**
     * helper methods
     */

    /**
     * categorize player requests
     * @param request
     * @return classified command
     */
    private Command classifyPlayerInput(char request){
        Command result = Command.Invalid;
        switch (request){
            case '0':
                result = Command.Refresh;
                break;
            case '1':
                result = Command.West;
                break;
            case '2':
                result = Command.South;
                break;
            case '3':
                result = Command.East;
                break;
            case '4':
                result = Command.North;
                break;
            case '9':
                result = Command.Exit;
                break;
            default: break;
        }
        return result;
    }
}
