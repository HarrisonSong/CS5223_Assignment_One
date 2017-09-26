package Game;

import Common.*;
import Common.Pair.NameEndPointPair;
import Common.Pair.NameTypePair;
import Common.Pair.mazePair;
import Game.BackgroundPing.EndPointsLiveChecker;
import Game.BackgroundPing.SingleEndPointLiveChecker;
import Game.Player.Command;
import Game.Player.Player;
import Game.Player.PlayerType;
import Interface.GameInterface;
import Interface.TrackerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Game implements GameInterface {

    /**
     * static game global constants
     */
    public static final int NAME_LENGTH = 2;
    public static final int DEFAULT_PLAYER_ACCESS_PORT = 8888;

    public static int MazeSize;
    public static int TreasureSize;

    private TrackerInterface tracker;
    private GameInterface primaryServer;
    private GameInterface backupServer;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture backgroundScheduledTask;

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

        String trackerIP = "";
        int trackerPort = 0;
        String playName = "";

        if (args.length < 3) {
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


        String localIP = EndPoint.getLocalIP();
        Registry trackerRegistry;
        Game game = new Game();

        /**
         * Setup security manager
         */
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        /**
         * Setup tracker connection and exit when fail.
         */
        try {
            trackerRegistry = LocateRegistry.getRegistry(trackerIP, trackerPort);
            game.tracker = (TrackerInterface) trackerRegistry.lookup("Tracker");
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            System.err.println("Failed to locate Tracker");
            System.exit(0);
        }

        try {
            if (game.tracker.registerNewPlayer(localIP, DEFAULT_PLAYER_ACCESS_PORT, playName, (GameInterface) UnicastRemoteObject.exportObject(game, Game.DEFAULT_PLAYER_ACCESS_PORT))) {

                /**
                 * Initialize and collect necessary parameters from tracker
                 */
                MazeSize = game.tracker.getN();
                TreasureSize = game.tracker.getK();
                Map playerEndPoints = game.tracker.retrieveEndPointsMap();
                game.gameLocalState.setPlayerEndPointsMap(playerEndPoints);

                switch (playerEndPoints.size()) {
                    case 1: {
                        game.setupGameAsPrimaryServer(playName, localIP);
                        /**
                         * Setup periodic ping to each player
                         */
                        game.scheduler = Executors.newScheduledThreadPool(0);
                        game.backgroundScheduledTask = game.scheduler.scheduleAtFixedRate(new EndPointsLiveChecker(game.retrieveEnhancedEndPointsMap(), (name) -> game.handleStandardPlayerUnavailability(name), () -> game.handleBackupServerUnavailability()), 500, 500, TimeUnit.MILLISECONDS);

                        break;
                    } // End of Case 1
                    default: {
                        /**
                         *  Check primary
                         */

                        boolean PrimaryIsAlive = true;
                        GameInterface t_primary = null;

                        /**
                         *  Go trough PlayerEndPoint List to find one alive, and contact Primary successfully.
                         */
                        for (Map.Entry<String, EndPoint> entry : game.gameLocalState.getPlayerEndPointsMap().entrySet()) {
                            String key = entry.getKey();
                            EndPoint value = entry.getValue();

                            if (key == playName)
                                continue;

                            GameInterface t_Alive = game.contactGameEndPoint(new NameEndPointPair(key, value));

                            if (t_Alive != null) {
                                List<NameEndPointPair> BPList = null;
                                try {
                                    BPList = t_Alive.getPrimaryAndBackupStubs();
                                } catch (RemoteException e) {
                                    continue;
                                }

                                t_primary = game.contactGameEndPoint(BPList.get(0));
                                if (t_primary != null) {
                                    try {
                                        game.gameGlobalState = t_primary.primaryExecuteJoin(new EndPoint(localIP, DEFAULT_PLAYER_ACCESS_PORT), playName);

                                        String primID = game.gameGlobalState.getIdByType(PlayerType.Primary);
                                        String backID = game.gameGlobalState.getIdByType(PlayerType.Backup);

                                        EndPoint temp_p = game.gameLocalState.getPlayerEndPointsMap().get(primID);
                                        EndPoint temp_b = game.gameLocalState.getPlayerEndPointsMap().get(backID);

                                        game.gameLocalState.setBackupEndPoint(new NameEndPointPair(backID, temp_b));
                                        game.gameLocalState.setPrimaryEndPoint(new NameEndPointPair(primID, temp_p));

                                        if (backID == playName) {
                                            game.setupGame(playName, localIP, PlayerType.Backup);
                                        } else {
                                            game.setupGame(playName, localIP, PlayerType.Standard);
                                        }


                                    } catch (RemoteException e) {
                                        // No backup
                                        if (BPList.get(1) == null) {
                                            PrimaryIsAlive = false;
                                            break;
                                        }

                                        // Wait for backup to become Primary
                                        TimeUnit.MILLISECONDS.sleep(1300);

                                        t_primary = game.contactGameEndPoint(BPList.get(1));

                                        game.gameGlobalState = t_primary.primaryExecuteJoin(new EndPoint(localIP, DEFAULT_PLAYER_ACCESS_PORT), playName);

                                        String primID = game.gameGlobalState.getIdByType(PlayerType.Primary);
                                        String backID = game.gameGlobalState.getIdByType(PlayerType.Backup);

                                        EndPoint temp_p = game.gameLocalState.getPlayerEndPointsMap().get(primID);
                                        EndPoint temp_b = game.gameLocalState.getPlayerEndPointsMap().get(backID);

                                        game.gameLocalState.setBackupEndPoint(new NameEndPointPair(backID, temp_b));
                                        game.gameLocalState.setPrimaryEndPoint(new NameEndPointPair(primID, temp_p));

                                        if (backID == playName) {
                                            game.setupGame(playName, localIP, PlayerType.Backup);
                                        } else {
                                            game.setupGame(playName, localIP, PlayerType.Standard);
                                        }
                                    }

                                } else {

                                    // No backup
                                    if (BPList.get(1) == null) {
                                        PrimaryIsAlive = false;
                                        break;
                                    }

                                    // Wait for backup to become Primary
                                    TimeUnit.MILLISECONDS.sleep(1300);

                                    t_primary = game.contactGameEndPoint(BPList.get(1));

                                    game.gameGlobalState = t_primary.primaryExecuteJoin(new EndPoint(localIP, DEFAULT_PLAYER_ACCESS_PORT), playName);

                                    String primID = game.gameGlobalState.getIdByType(PlayerType.Primary);
                                    String backID = game.gameGlobalState.getIdByType(PlayerType.Backup);

                                    EndPoint temp_p = game.gameLocalState.getPlayerEndPointsMap().get(primID);
                                    EndPoint temp_b = game.gameLocalState.getPlayerEndPointsMap().get(backID);

                                    game.gameLocalState.setBackupEndPoint(new NameEndPointPair(backID, temp_b));
                                    game.gameLocalState.setPrimaryEndPoint(new NameEndPointPair(primID, temp_p));

                                    if (backID == playName) {
                                        game.setupGame(playName, localIP, PlayerType.Backup);
                                    } else {
                                        game.setupGame(playName, localIP, PlayerType.Standard);
                                    }

                                }

                            } else {
                                continue;
                            }

                        }


                        /**
                         *  If there is Primary Alive
                         */
                        if (!PrimaryIsAlive) {

                            /**
                             * Set up as primary
                             */
                            game.setupGameAsPrimaryServer(playName, localIP);
                            /**
                             * Setup periodic ping to each player
                             */
                            game.scheduler = Executors.newScheduledThreadPool(0);
                            game.backgroundScheduledTask = game.scheduler.scheduleAtFixedRate(new SingleEndPointLiveChecker(game.gameLocalState.getPrimaryEndPoint().getEndPoint(), () -> game.handlePrimaryServerUnavailability()), 500, 500, TimeUnit.MILLISECONDS);
                        }
                        break;
                    } // End of Default

                } // End of Switch

                /**
                 * Continuously read user input from
                 * standard input.
                 */
                Scanner inputScanner = new Scanner(System.in);
                while (inputScanner.hasNext()) {
                    game.issueRequest(inputScanner.nextLine());
                }
            } else {
                System.err.println("Failed to register.");
                System.exit(0);
                return;
            }
        } catch (RemoteException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Failed to contact Tracker");
            System.exit(0);
        }
    }


    //region GameInterface Functions
    /**
     * read and process the player requests
     * @param playerName
     * @param request
     * @return updated global game state
     */
    public Object primaryExecuteRemoteRequest(String name, String command){
        if(request.equals(Command.Exit)){
            this.gameLocalState.removePlayerEndPoint(playerName);
            this.gameGlobalState.removePlayerByName(playerName);
        }
        if(request.equals(Command.East) || request.equals(Command.West) ||
                request.equals(Command.South) || request.equals(Command.North)){
            this.gameGlobalState.makeMove(request, playerName);
        }
        try {
            backupServer.backupUpdateGameState(this.gameGlobalState);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return this.gameGlobalState;
    }

    /**
     * handler to manage new player joining
     * @param IPAddress
     * @param playName
     * @return updated global game state
     */
    public Object primaryExecuteJoin(GameInterface stub, String name){
        if(this.gameLocalState.getPlayerEndPointsMap().size() == 1){
            this.gameLocalState.setBackupEndPoint(new NameEndPointPair(playName, IPAddress));
            this.gameGlobalState.addNewPlayerByName(playName, PlayerType.Backup);
            this.gameLocalState.addPlayerEndPoint(playName, IPAddress);

            /**
             * setup RMI connection to backup server
             */
            try {
                Registry backupRegistry = LocateRegistry.getRegistry(gameLocalState.getBackupEndPoint().getEndPoint().getIPAddress(), gameLocalState.getBackupEndPoint().getEndPoint().getPort());
                String backupPlayerName = gameLocalState.getBackupEndPoint().getName();
                backupServer = (GameInterface) backupRegistry.lookup("Player_" + backupPlayerName);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        }else{
            this.gameGlobalState.addNewPlayerByName(playName, PlayerType.Standard);
            this.gameLocalState.addPlayerEndPoint(playName, IPAddress);
        }
        return this.gameGlobalState;
    }

    /**
     * update local backup server global state from
     * updated primary server data.
     * @param gameState
     * @return
     */
    public boolean backupUpdateGameState(Object gameState){
        if(this.gameLocalState.getPlayerType() == PlayerType.Backup){
            this.updateGameGlobalState(gameState);
        }
        return false;
    }

    /**
     * Get primary and backup server endPoints
     * @return
     */
    public List<NameEndPointPair> getPrimaryAndBackupStubs(){
        List<NameEndPointPair> result = new ArrayList<>();
        result.add(this.gameLocalState.getPrimaryEndPoint());
        result.add(this.gameLocalState.getBackupEndPoint());
        return result;
    }

    /**
     * promote current game to be backup server
     * @param gameState: updated global game state from primary server
     * @return promotion status
     */
    public boolean playerPromoteAsBackup(Object gameState) {
        if(this.gameLocalState.getPlayerType() == PlayerType.Standard){
            this.gameLocalState.setPlayerType(PlayerType.Backup);
            this.gameLocalState.setBackupEndPoint(this.gameLocalState.getLocalEndPoint());
            this.updateGameGlobalState(gameState);
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(new SingleEndPointLiveChecker(this.gameLocalState.getPrimaryEndPoint().getEndPoint(), () -> this.handlePrimaryServerUnavailability()), 500, 500, TimeUnit.MILLISECONDS);
            return true;
        }
        return false;
    }
    //endregion

    //region Unavailability function calls
    public void handleStandardPlayerUnavailability(String playerName){
        this.gameLocalState.removePlayerEndPoint(playerName);
        this.gameGlobalState.removePlayerByName(playerName);
    }

    public void handleBackupServerUnavailability(){
        String backupPlayerName = this.gameLocalState.getPlayerByEndPoint(this.gameLocalState.getBackupEndPoint().getEndPoint());
        this.gameLocalState.removePlayerEndPoint(backupPlayerName);
        this.gameGlobalState.removePlayerByName(backupPlayerName);
        this.assignBackupServer();
        try {
            tracker.resetTrackerEndPointsMap(this.gameLocalState.getPlayerEndPointsMap());
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void handlePrimaryServerUnavailability(){
        String primaryPlayerName = this.gameLocalState.getPlayerByEndPoint(this.gameLocalState.getPrimaryEndPoint().getEndPoint());
        this.gameLocalState.removePlayerEndPoint(primaryPlayerName);
        this.gameGlobalState.removePlayerByName(primaryPlayerName);
        promoteToBePrimary(this.gameLocalState.getPlayerEndPointsMap().size() == 1);
        try {
            tracker.resetTrackerEndPointsMap(this.gameLocalState.getPlayerEndPointsMap());
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //endregion

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
     * categorize player requests
     * @param request
     * @return classified command
     */
//    private Command classifyPlayerInput(String request){
//        Command result = Command.Invalid;
//        switch (request){
//            case "0":
//                result = Command.Refresh;
//                break;
//            case "1":
//                result = Command.West;
//                break;
//            case "2":
//                result = Command.South;
//                break;
//            case "3":
//                result = Command.East;
//                break;
//            case "4":
//                result = Command.North;
//                break;
//            case "9":
//                result = Command.Exit;
//                break;
//            default: break;
//        }
//        return result;
//    }
//
//    private GameInterface contactGameEndPoint(NameEndPointPair pair){
//        try {
//            Registry backupRegistry = LocateRegistry.getRegistry(pair.getEndPoint().getIPAddress(), pair.getEndPoint().getPort());
//            try {
//                GameInterface targetPlayer = (GameInterface)backupRegistry.lookup("Player_" + pair.getName());
//                return targetPlayer;
//            } catch (NotBoundException notBoundException) {
//                notBoundException.printStackTrace();
//                return null;
//            }
//        } catch (RemoteException remoteException) {
//            remoteException.printStackTrace();
//            return null;
//        }
//    }
}
