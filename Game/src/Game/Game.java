package Game;

import Game.BackgroundPing.MultipleTargetsLiveChecker;
import Game.BackgroundPing.SingleTargetLiveChecker;
import Game.Player.Command;
import Game.Player.PlayerType;
import Interface.GameInterface;
import Interface.TrackerInterface;
import Utility.PlayerHelper;
import Utility.PrimaryServerHelper;

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
    public static final int DEFAULT_PLAYER_ACCESS_PORT = 8888;

    public static int MazeSize;
    public static int TreasureSize;

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
        String playerName = "";

        if (args.length < 3) {
            System.err.println("Not enough parameters.");
            System.exit(0);
            return;
        }

        if (args.length == 3) {
            trackerIP = args[0];
            playerName = args[2];
            try {
                trackerPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Failed to get tracker port.");
                System.exit(0);
                return;
            }
        }

        Registry trackerRegistry;
        Game game = new Game();
        game.gameLocalState.setName(playerName);

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
            game.gameLocalState.setTrackerStub((TrackerInterface) trackerRegistry.lookup("Tracker"));
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Failed to locate Tracker");
            System.exit(0);
        }

        /**
         * Setup local stub and exit when fail.
         */
        try {
            game.gameLocalState.setLocalStub((GameInterface) UnicastRemoteObject.exportObject(game, Game.DEFAULT_PLAYER_ACCESS_PORT));
        } catch (RemoteException e) {
            System.err.println("Failed to setup local stub");
            System.exit(0);
        }

        Map playerStubsMap = null;
        boolean isRegistered = false;
        try {
            MazeSize = game.gameLocalState.getTrackerStub().getN();
            TreasureSize = game.gameLocalState.getTrackerStub().getK();
            isRegistered = game.gameLocalState.getTrackerStub().registerNewPlayer(playerName, game.gameLocalState.getLocalStub());
            playerStubsMap = game.gameLocalState.getTrackerStub().serveStubs();
        } catch (RemoteException | InterruptedException e) {
            System.err.println("Failed to contact Tracker");
            System.exit(0);
        }
        if(isRegistered){

            /**
             * Initialize and collect necessary parameters from tracker
             */

            game.gameLocalState.setPlayerStubsMap(playerStubsMap);

            switch (playerStubsMap.size()) {
                case 1: {
                    if(game.setupPrimary(true)){
                        PrimaryServerHelper.initializeGlobalState(playerName, game);
                    }else{
                        System.err.println("Failed to setup primary");
                        System.exit(0);
                    }
                    break;
                }
                default: {
                    boolean isPrimaryAlive = false;

                    for(Map.Entry<String, GameInterface> stubEntry : game.getGameLocalState().getPlayerStubsMap().entrySet()){

                        if(playerName.equals(stubEntry.getKey())){
                            continue;
                        }

                        List<GameInterface> primaryAndBackupStubs;
                        try {
                            primaryAndBackupStubs = stubEntry.getValue().getPrimaryAndBackupStubs();
                        } catch(RemoteException e) {
                            /**
                             * The player is offline.
                             */
                            continue;
                        }

                        /**
                         * Primary server is confirmed to be alive
                         * by the time. Assign the stub.
                         */
                        game.gameLocalState.setPrimaryStub(primaryAndBackupStubs.get(0));
                        game.gameLocalState.setBackupStub(primaryAndBackupStubs.get(1));
                        boolean isBackupAvailable = primaryAndBackupStubs.get(1) != null;
                        GameGlobalState updatedState = null;
                        try{
                            updatedState = (GameGlobalState) game.gameLocalState.getPrimaryStub().primaryExecuteJoin(playerName, game.gameLocalState.getLocalStub());
                        } catch (RemoteException e){
                            /**
                             * The primary server is offline at the time.
                             */
                            if(isBackupAvailable){
                                game.gameLocalState.setPrimaryStub(game.gameLocalState.getBackupStub());
                                try {
                                    TimeUnit.MILLISECONDS.sleep(1300);
                                    game.getGameLocalState().getPrimaryStub().primaryExecuteJoin(playerName, game.gameLocalState.getLocalStub());
                                    List<GameInterface> updatedPrimaryAndBackupStubs = game.getGameLocalState().getPrimaryStub().getPrimaryAndBackupStubs();
                                    game.getGameLocalState().setPrimaryStub(updatedPrimaryAndBackupStubs.get(0));
                                    game.getGameLocalState().setBackupStub(updatedPrimaryAndBackupStubs.get(1));
                                    isBackupAvailable = updatedPrimaryAndBackupStubs.get(1) == null;
                                } catch (Exception e1) {
                                    System.err.println("Failed to contact both primary server and backup server.");
                                    System.exit(0);
                                }
                            }else{
                                break;
                            }
                        }
                        if(!isBackupAvailable){
                            if(!game.setupBackup(updatedState)){
                                System.err.println("Failed to setup backup");
                                System.exit(0);
                            }
                        }

                        isPrimaryAlive = true;
                        break;
                    }

                    if(!isPrimaryAlive){
                        if(game.setupPrimary(true)){
                            PrimaryServerHelper.initializeGlobalState(playerName, game);
                        }else{
                            System.err.println("Failed to setup primary");
                            System.exit(0);
                        }
                    }
                    break;
                }
            }

            /**
             * Continuously read user input from
             * standard input.
             */
            Scanner inputScanner = new Scanner(System.in);
            while (inputScanner.hasNext()) {
                PlayerHelper.issueRequest(inputScanner.nextLine(), game);
            }
        } else {
            System.err.println("Failed to register.");
            System.exit(0);
            return;
        }
    }

    public boolean setupPrimary(boolean isTheOnlyPlayer){
        if (PlayerHelper.setupSelfAsPrimary(this, isTheOnlyPlayer)){
            /**
             * Setup periodic ping to each player
             */
            this.scheduler = Executors.newScheduledThreadPool(0);
            this.backgroundScheduledTask = this.scheduler.scheduleAtFixedRate(
                    new MultipleTargetsLiveChecker(
                            PlayerHelper.retrieveEnhancedStubsMap(this.gameLocalState),
                            (name) -> this.handleStandardPlayerUnavailability(name),
                            () -> this.handleBackupServerUnavailability()
                    ),
                    500, 500, TimeUnit.MILLISECONDS
            );
            return true;
        }
        return false;
    }

    public boolean setupBackup(GameGlobalState updatedState){
        PlayerHelper.setupSelfAsBackup(this, updatedState);
        this.scheduler = Executors.newScheduledThreadPool(0);

        /**
         * Setup periodic ping to primary server
         */
        this.backgroundScheduledTask = this.scheduler.scheduleAtFixedRate(
                new SingleTargetLiveChecker(
                        this.gameLocalState.getPrimaryStub(),
                        () -> this.handlePrimaryServerUnavailability()
                ),
                500, 500, TimeUnit.MILLISECONDS
        );
        return true;
    }

    /*** GameInterface Implementation ***/

    /**
     * read and process the player requests
     * @param playerName
     * @param request
     * @return updated global game state
     */
    public Object primaryExecuteRemoteRequest(String playerName, String request){
        Command command;
        try{
            command = Command.valueOf(request);
        }catch (Exception e){
            command = Command.Invalid;
        }
        if(command.equals(Command.Exit)){
            this.gameLocalState.removePlayerStubByName(playerName);
            this.gameGlobalState.removePlayerByName(playerName);
        }
        if(command.equals(Command.East) || command.equals(Command.West) ||
                command.equals(Command.South) || command.equals(Command.North)){
            this.gameGlobalState.makeMove(command, playerName);
        }
        try {
            /**
             * Remote call backup server to update its global state
             */
            this.gameLocalState.getBackupStub().backupUpdateGameState(this.gameGlobalState);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return this.gameGlobalState;
    }

    /**
     * handler to manage new player joining
     * @param playerName
     * @param stub
     * @return updated global game state
     */
    public Object primaryExecuteJoin(String playerName, GameInterface stub){
        if(this.gameLocalState.getPlayerStubsMap().size() == 1){
            this.gameGlobalState.addNewPlayerByName(playerName, PlayerType.Backup);

            /**
             * setup the stub to be backup server
             */
            this.gameLocalState.setBackupStub(stub);
        }else{
            this.gameGlobalState.addNewPlayerByName(playerName, PlayerType.Standard);
        }
        this.gameLocalState.addPlayerStub(playerName, stub);
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
            PlayerHelper.updateGameGlobalState(this.gameGlobalState, (GameGlobalState) gameState);
        }
        return false;
    }

    /**
     * Get primary and backup server endPoints
     * @return
     */
    public List<GameInterface> getPrimaryAndBackupStubs(){
        List<GameInterface> result = new ArrayList<>();
        result.add(this.gameLocalState.getPrimaryStub());
        result.add(this.gameLocalState.getBackupStub());
        return result;
    }

    /**
     * Notify caller that current player is alive.
     * @return player alive status
     */
    public boolean isAlive(){
        return true;
    }

    /*** End of GameInterface Implementation ***/

    /** Unavailability handlers **/

    public void handlePrimaryServerUnavailability(){
        String primaryPlayerName = this.gameLocalState.getPlayerNameByStub(this.gameLocalState.getPrimaryStub());
        this.gameLocalState.removePlayerStubByName(primaryPlayerName);
        this.gameGlobalState.removePlayerByName(primaryPlayerName);
        this.setupPrimary((this.gameLocalState.getPlayerStubsMap().size() == 1));
        PrimaryServerHelper.updateTrackerStubMap(this.gameLocalState);
    }

    public void handleBackupServerUnavailability(){
        String backupPlayerName = this.gameLocalState.getPlayerNameByStub(this.gameLocalState.getBackupStub());
        this.gameLocalState.removePlayerStubByName(backupPlayerName);
        this.gameGlobalState.removePlayerByName(backupPlayerName);
        PrimaryServerHelper.assignBackupServer(this.gameLocalState, this.gameGlobalState);
        PrimaryServerHelper.updateTrackerStubMap(this.gameLocalState);
    }

    public void handleStandardPlayerUnavailability(String playerName){
        this.gameLocalState.removePlayerStubByName(playerName);
        this.gameGlobalState.removePlayerByName(playerName);
    }

    /** End of Unavailability handlers **/

    /***Getters and Setters ***/
    public GameGlobalState getGameGlobalState() {
        return gameGlobalState;
    }

    public GameLocalState getGameLocalState() {
        return gameLocalState;
    }
}
