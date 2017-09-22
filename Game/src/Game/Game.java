package Game;

import Common.*;
import Game.BackgroundPing.Pinger;
import Game.Player.Command;
import Game.Player.Player;
import Game.Player.PlayerType;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Game implements GameInterface {

    /**
     * static game global constants
     */
    public static final int NAME_LENGTH = 2;
    public static final int DEFAULT_PLAYER_ACCESS_PORT = 80;

    public static int MazeSize;
    public static int TreasureSize;

    /**
     * game data to be bind with GUI
     */
    private GameGlobalState gameGlobalState;

    /**
     * local game data
     */
    private GameLocalState gameLocalState;

    public Game() {
        this.gameGlobalState = new GameGlobalState();
        this.gameLocalState = new GameLocalState();
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

        String trackerIP = "";
        int trackerPort = 0;
        String playName = "";

        if(args.length < 3){
            System.err.println("Not enough parameters.");
            System.exit(0);
            return;
        }

        if (args.length == 3) {
            trackerIP = args[0];
            playName = args[2];
            try {
                trackerPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Failed to get tracker port.");
                System.exit(0);
                return;
            }
        }
        try {
            Registry trackerRegistry = LocateRegistry.getRegistry(trackerIP, trackerPort);
            TrackerInterface trackerStub = (TrackerInterface) trackerRegistry.lookup("Tracker");

            String localIP = EndPoint.getLocalIP();
            if(trackerStub.registerNewPlayer(localIP, DEFAULT_PLAYER_ACCESS_PORT, playName)){

                /**
                 * Initialize and collect necessary parameters from tracker
                 */
                Game game = new Game();
                MazeSize = trackerStub.getN();
                TreasureSize = trackerStub.getK();
                Map playerEndPoints = trackerStub.retrieveEndPointsMap();
                game.gameLocalState.setPlayerEndPointsMap(playerEndPoints);

                /**
                 * Setup player remote access
                 */
                game.setupRegistry(playName, game);

                switch (playerEndPoints.size()){
                    case 1: {
                        game.setupGameAsPrimaryServer(playName, localIP);
                        // TODO: SETUP PERIODIC PING TO EACH PLAYER
                        break;
                    }
                    case 2: {
                        game.setupGame(playName, localIP, PlayerType.Backup);
                        // TODO: ASK PRIMARY TO JOIN
                        // TODO: SETUP PERIODIC PING TO PRIMARY
                        break;
                    }
                    default: {
                        game.setupGame(playName, localIP, PlayerType.Standard);
                        // TODO: ASK PRIMARY TO JOIN
                        break;
                    }
                }

                /**
                 * Continuously read user input from
                 * standard input.
                 */
                Scanner inputScanner = new Scanner(System.in);
                while(inputScanner.hasNext()){
                    game.issueRequest(inputScanner.nextLine());
                }
            }else{
                System.err.println("Failed to register.");
                System.exit(0);
                return;
            }


        } catch (Exception e) {
            System.err.println("Player exception: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     *
     * PRIMARY SPECIFIC METHODS
     *
     */

    /**
     * game setup when first player joins
     * @param playName
     */
    private void setupGameAsPrimaryServer(String playName, String localIPAddress){
        this.gameLocalState.setPlayName(playName);
        this.gameLocalState.setPlayerType(PlayerType.Primary);
        this.gameLocalState.setLocalEndPoint(new EndPoint(localIPAddress, DEFAULT_PLAYER_ACCESS_PORT));
        Player primaryPlayer = new Player(playName, new Pair(Game.MazeSize), 0, PlayerType.Primary);
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
            this.gameLocalState.setBackupEndPoint(this.gameLocalState.getPlayerEndPointsMap().get(this.gameGlobalState.getPlayerList().get(playerIndex).getName()));
        }
    }

    /**
     * read and process the player requests
     * @param playerName
     * @param request
     * @return updated global game state
     */
    public GameGlobalState executeRequest(String playerName, String request){
        Command classifiedRequest = classifyPlayerInput(request);
        if(classifiedRequest.equals(Command.Exit)){
            this.gameLocalState.removePlayerEndPoint(playerName);
            this.gameGlobalState.removePlayerByName(playerName);
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
        if(this.gameLocalState.getPlayerEndPointsMap().size() == 1){
            this.gameLocalState.setBackupEndPoint(IPAddress);
            this.gameGlobalState.addNewPlayerByName(playName, PlayerType.Backup);
            this.gameLocalState.addPlayerEndPoint(playName, IPAddress);
        }else{
            this.gameGlobalState.addNewPlayerByName(playName, PlayerType.Standard);
            this.gameLocalState.addPlayerEndPoint(playName, IPAddress);
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

    private void setupGame(String playName, String localIPAddress, PlayerType type){
        this.gameLocalState.setPlayName(playName);
        this.gameLocalState.setPlayerType(type);
        this.gameLocalState.setLocalEndPoint(new EndPoint(localIPAddress, DEFAULT_PLAYER_ACCESS_PORT));
        this.gameGlobalState.addNewPlayerByName(playName, type);
    }

    /**
     * Method to setup local RMI registry
     * @param playName
     * @param game
     * @throws RemoteException
     * @throws AlreadyBoundException
     */
    private void setupRegistry(String playName, Game game) throws RemoteException, AlreadyBoundException {
        GameInterface stub = (GameInterface) UnicastRemoteObject.exportObject(game, DEFAULT_PLAYER_ACCESS_PORT);
        Registry registry = LocateRegistry.createRegistry(DEFAULT_PLAYER_ACCESS_PORT);
        registry.bind("Player_" + playName, stub);
    }

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
     * Get primary and backup server endPoints
     * @return
     */
    public List<EndPoint> getPrimaryAndBackupEndPoints(){
        List<EndPoint> result = new ArrayList<EndPoint>();
        result.add(this.gameLocalState.getPrimaryEndPoint());
        result.add(this.gameLocalState.getBackupEndPoint());
        return result;
    }

    /**
     * Method for player to issue the user operation
     * @param request
     */
    public void issueRequest(String request){
        executeRequest(this.gameLocalState.getPlayName(), request);
        if(request.equals(Command.Exit.getValue())){
            System.exit(0);
        }
    }

    /**
     * helper methods
     */

    /**
     * categorize player requests
     * @param request
     * @return classified command
     */
    private Command classifyPlayerInput(String request){
        Command result = Command.Invalid;
        switch (request){
            case "0":
                result = Command.Refresh;
                break;
            case "1":
                result = Command.West;
                break;
            case "2":
                result = Command.South;
                break;
            case "3":
                result = Command.East;
                break;
            case "4":
                result = Command.North;
                break;
            case "9":
                result = Command.Exit;
                break;
            default: break;
        }
        return result;
    }
}
