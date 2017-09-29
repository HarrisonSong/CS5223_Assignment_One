package Game;

import Game.BackgroundPing.MultipleTargetsLiveChecker;
import Game.BackgroundPing.SingleTargetLiveChecker;
import Game.Player.Command;
import Game.Player.PlayerType;
import Game.State.GameGlobalState;
import Game.State.GameLocalState;
import Interface.GameInterface;
import Interface.TrackerInterface;
import Game.Utility.PlayerHelper;
import Game.Utility.PrimaryServerHelper;

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

    public static int MazeSize;
    public static int TreasureSize;

    private int port;
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
        this.gameGlobalState = new GameGlobalState(MazeSize, TreasureSize);
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

        /**
         * Setup security manager
         */
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        /**
         * Setup tracker connection and exit when fail.
         */
        Registry trackerRegistry;
        TrackerInterface tracker = null;
        try {
            trackerRegistry = LocateRegistry.getRegistry(trackerIP, trackerPort);
            tracker = (TrackerInterface) trackerRegistry.lookup("Tracker");
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Failed to locate Tracker");
            System.exit(0);
        }

        /**
         * Fetch maze and treasures size to initialize game
         */
        try {
            MazeSize = tracker.getN();
            TreasureSize = tracker.getK();
        }catch (RemoteException e) {
            System.err.println("Failed to contact Tracker METHOD: getN|getK");
            System.exit(0);
        }

        Game game = new Game();
        game.gameLocalState.setName(playerName);
        game.gameLocalState.setTrackerStub(tracker);

        try {
            game.port = tracker.seedPlayerPort();
            System.out.printf("Player port is %d\n", game.port);
        } catch (RemoteException e) {
            System.err.println("Failed to contact Tracker METHOD: seedPlayerPort");
            System.exit(0);
        }

        /**
         * Setup local stub and exit when fail.
         */
        try {
            game.gameLocalState.setLocalStub((GameInterface) UnicastRemoteObject.exportObject(game, game.port));
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
        } catch (RemoteException e) {
            System.err.println("Failed to contact Tracker METHOD: getN|getK|registerNewPlayer|serveStubs");
            System.exit(0);
        }

        if(isRegistered){
            System.out.println("Successfully registered");

            /**
             * Initialize and collect necessary parameters from tracker
             */
            game.gameLocalState.setPlayerStubsMap(playerStubsMap);

            switch (playerStubsMap.size()) {
                case 1: {
                    game.getGameGlobalState().addNewPlayerWithName(playerName, PlayerType.Standard);
                    if(!game.setupPrimary(true)){
                        System.err.println("Failed to setup primary");
                        System.exit(0);
                    }
                    System.out.println("Successfully setup as Primary");
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

                            /**
                             * The primary server is online, and join successfully
                             * 1> Update primary and backup stub
                             * 2> Set up using returned Global state
                             */
                            List<GameInterface> updatedPrimaryAndBackupStubs = game.getGameLocalState().getPrimaryStub().getPrimaryAndBackupStubs();
                            game.getGameLocalState().setPrimaryStub(updatedPrimaryAndBackupStubs.get(0));
                            game.getGameLocalState().setBackupStub(updatedPrimaryAndBackupStubs.get(1));

                            if(!game.getGameLocalState().getLocalStub().equals(game.getGameLocalState().getBackupStub())){
                                game.setupStandard(updatedState);
                            }else if(!game.setupBackup(updatedState)){
                                System.err.println("Failed to setup self as backup server");
                                System.exit(0);
                            }
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
                        game.getGameGlobalState().addNewPlayerWithName(playerName, PlayerType.Standard);
                        if(!game.setupPrimary(true)){
                            System.err.println("Failed to setup primary");
                            System.exit(0);
                        }
                        System.out.println("Successfully setup as Primary");
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
            if(this.scheduler == null) {
                this.scheduler = Executors.newScheduledThreadPool(0);
            }
            if(this.backgroundScheduledTask != null) {
                this.backgroundScheduledTask.cancel(true);
            }
            this.backgroundScheduledTask = this.scheduler.scheduleAtFixedRate(
                    new MultipleTargetsLiveChecker(
                            PlayerType.Primary,
                            PlayerHelper.retrieveEnhancedStubsMap(this.gameLocalState),
                            () -> this.primaryHandleBackupServerUnavailability(),
                            (name) -> this.primaryHandleStandardPlayerUnavailability(name),
                            null,
                            null
                    ),
                    500, 500, TimeUnit.MILLISECONDS
            );
            return true;
        }
        return false;
    }

    public boolean setupBackup(GameGlobalState updatedState){
        if(PlayerHelper.setupSelfAsBackup(this, updatedState)){
            this.scheduler = Executors.newScheduledThreadPool(0);

            /**
             * Setup periodic ping to primary server
             */
            if(this.scheduler == null) {
                this.scheduler = Executors.newScheduledThreadPool(0);
            }
            if(this.backgroundScheduledTask != null) {
                this.backgroundScheduledTask.cancel(true);
            }
            this.backgroundScheduledTask = this.scheduler.scheduleAtFixedRate(
                    new SingleTargetLiveChecker(
                            this.gameLocalState.getPrimaryStub(),
                            () -> this.backupHandlePrimaryServerUnavailability()
                    ),
                    500, 500, TimeUnit.MILLISECONDS
            );
            return true;
        }
        return false;
    }

    public void setupStandard(GameGlobalState updatedState){
        PlayerHelper.setupSelfAsStandard(this, updatedState);
        this.scheduler = Executors.newScheduledThreadPool(0);
        /**
         *  Setup periodic ping as a standard player
         */
        if(this.scheduler == null) {
            this.scheduler = Executors.newScheduledThreadPool(0);
        }
        if(this.backgroundScheduledTask != null) {
            this.backgroundScheduledTask.cancel(true);
        }
        this.backgroundScheduledTask = this.scheduler.scheduleAtFixedRate(
                new MultipleTargetsLiveChecker(
                        PlayerType.Primary,
                        PlayerHelper.retrieveEnhancedStubsMap(this.gameLocalState),
                        null,
                        null,
                        () -> this.standardPlayerHandlePrimaryServerUnavailability(),
                        () -> this.standardPlayerHandleBackupServerUnavailability()
                ),
                500, 500, TimeUnit.MILLISECONDS
        );
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
            command = Command.fromString(request);
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
        if(this.gameLocalState.getBackupStub() != null && !command.equals(Command.Refresh)){
            try {
                /**
                 * Remote call backup server to update its global state
                 */
                this.gameLocalState.getBackupStub().backupUpdateGameState(this.gameGlobalState);
            } catch (RemoteException e) {
                System.out.println("Primary Server failed to contact Backup Server");
            }
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
            this.gameGlobalState.addNewPlayerWithName(playerName, PlayerType.Backup);

            /**
             * setup the stub to be backup server
             */
            this.gameLocalState.setBackupStub(stub);
        }else{
            this.gameGlobalState.addNewPlayerWithName(playerName, PlayerType.Standard);
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
            this.gameGlobalState.resetAllStates(
                    ((GameGlobalState) gameState).getPlayersMap(),
                    ((GameGlobalState) gameState).getTreasuresLocation(),
                    ((GameGlobalState) gameState).getActivePlayerQueue()
            );
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

    public void primaryHandleBackupServerUnavailability(){
        String backupPlayerName = this.gameLocalState.getPlayerNameByStub(this.gameLocalState.getBackupStub());
        this.gameLocalState.removePlayerStubByName(backupPlayerName);
        this.gameGlobalState.removePlayerByName(backupPlayerName);
        PrimaryServerHelper.assignBackupServer(this.gameLocalState, this.gameGlobalState);
        PrimaryServerHelper.updateTrackerStubMap(this.gameLocalState);
    }

    public void primaryHandleStandardPlayerUnavailability(String playerName){
        this.gameLocalState.removePlayerStubByName(playerName);
        this.gameGlobalState.removePlayerByName(playerName);
    }

    public void backupHandlePrimaryServerUnavailability(){
        String primaryPlayerName = this.gameLocalState.getPlayerNameByStub(this.gameLocalState.getPrimaryStub());
        this.gameLocalState.removePlayerStubByName(primaryPlayerName);
        this.gameGlobalState.removePlayerByName(primaryPlayerName);
        this.setupPrimary((this.gameLocalState.getPlayerStubsMap().size() == 1));
        PrimaryServerHelper.updateTrackerStubMap(this.gameLocalState);
    }

    public void standardPlayerHandlePrimaryServerUnavailability(){
        try {
            /*** Wait for some time ***/
            TimeUnit.MILLISECONDS.sleep(1300);

            try {
                /*** Contact backup server ***/
                List<GameInterface> primaryAndBackupStubs = this.gameLocalState.getBackupStub().getPrimaryAndBackupStubs();
                this.gameLocalState.setPrimaryStub(primaryAndBackupStubs.get(0));
                this.gameLocalState.setBackupStub(primaryAndBackupStubs.get(1));
            } catch (RemoteException e) {
                System.out.println("Both primary and backup are offline");
                System.exit(0);
            }
        } catch (InterruptedException e1) {
            System.out.println("System interrupted");
            System.exit(0);
        }
    }

    public void standardPlayerHandleBackupServerUnavailability(){
        try {
            /*** Wait for some time ***/
            TimeUnit.MILLISECONDS.sleep(1300);

            try {
                /*** Contact primary server ***/
                List<GameInterface> primaryAndBackupStubs = this.gameLocalState.getPrimaryStub().getPrimaryAndBackupStubs();
                this.gameLocalState.setPrimaryStub(primaryAndBackupStubs.get(0));
                this.gameLocalState.setBackupStub(primaryAndBackupStubs.get(1));
            } catch (RemoteException e) {
                System.out.println("Both primary and backup are offline");
                System.exit(0);
            }
        } catch (InterruptedException e1) {
            System.out.println("System interrupted");
            System.exit(0);
        }
    }

    /** End of Unavailability handlers **/

    /***Getters and Setters ***/
    public GameGlobalState getGameGlobalState() {
        return gameGlobalState;
    }

    public GameLocalState getGameLocalState() {
        return gameLocalState;
    }

    public void setGameGlobalState(GameGlobalState newState){
        gameGlobalState = newState;
    }
}
