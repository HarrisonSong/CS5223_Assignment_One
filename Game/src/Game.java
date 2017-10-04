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

    /**
     * GUID
     */
    private GridGUI gui;

    public Game() {
        this.gameGlobalState = new GameGlobalState(MazeSize, TreasureSize);
        this.gameLocalState = new GameLocalState();
        this.gui = new GridGUI();
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

        System.out.printf("Maze size: %d\n", MazeSize);
        System.out.printf("treasures: %d\n", TreasureSize);
        Game game = new Game();
        game.gameLocalState.setName(playerName);
        game.gameLocalState.setTrackerStub(tracker);

        /**
         * Setup local stub and exit when fail.
         */
        try {
            game.gameLocalState.setLocalStub((GameInterface) UnicastRemoteObject.exportObject(game, 0));
        } catch (RemoteException e) {
            System.err.println("Failed to setup local stub");
            System.exit(0);
        }

        Map playerStubsMapFromTracker = null;
        boolean isRegistered = false;
        try {
            MazeSize = game.gameLocalState.getTrackerStub().getN();
            TreasureSize = game.gameLocalState.getTrackerStub().getK();
            isRegistered = game.gameLocalState.getTrackerStub().registerNewPlayer(playerName, game.gameLocalState.getLocalStub());
            playerStubsMapFromTracker = game.gameLocalState.getTrackerStub().serveStubs();
        } catch (RemoteException e) {
            System.err.println("Failed to contact Tracker METHOD: getN|getK|registerNewPlayer|serveStubs");
            System.exit(0);
        }

        if(isRegistered){
            System.out.println("Successfully registered");

            /**
             * Initialize and collect necessary parameters from tracker
             */
            game.gameGlobalState.setPlayerStubsMap(playerStubsMapFromTracker);

            if (playerStubsMapFromTracker.size() == 1) {
                game.getGameGlobalState().addPlayer(playerName, PlayerType.Standard, game.gameLocalState.getLocalStub());
                if (!game.setupPrimary(true)) {
                    System.err.println("Failed to setup primary");
                    System.exit(0);
                }
                System.out.println("Successfully setup as Primary");
            } else {
                boolean isPrimaryAlive = false;

                for (Map.Entry<String, GameInterface> stubEntry : game.gameGlobalState.getPlayerStubsMap().entrySet()) {
                    if (playerName.equals(stubEntry.getKey())) {
                        continue;
                    }

                    List<GameInterface> primaryAndBackupStubs;
                    try {
                        primaryAndBackupStubs = stubEntry.getValue().getPrimaryAndBackupStubs();
                    } catch (RemoteException e) {
                        /**
                         * The player contacting is offline.
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
                    try {
                        game.gameLocalState.getPrimaryStub().primaryExecuteJoin(playerName, game.gameLocalState.getLocalStub());
                    } catch (RemoteException e) {
                        /**
                         * The primary server is offline at the time.
                         */
                        if (isBackupAvailable) {
                            game.gameLocalState.setPrimaryStub(game.gameLocalState.getBackupStub());
                            try {
                                TimeUnit.MILLISECONDS.sleep(1000);
                                game.getGameLocalState().getPrimaryStub().primaryExecuteJoin(playerName, game.gameLocalState.getLocalStub());
                            } catch (Exception e1) {
                                e1.printStackTrace();
                                System.err.println("Detect both primary server and backup server fail within 2 seconds");
                                System.exit(0);
                            }
                        } else {
                            break;
                        }
                    }

                    /**
                     * Confirm primary is alive and
                     * stop contacting other player
                     * after successfully join.
                     */
                    isPrimaryAlive = true;
                    break;
                }

                if (!isPrimaryAlive) {
                    game.getGameGlobalState().addPlayer(playerName, PlayerType.Standard, game.getGameLocalState().getLocalStub());
                    if (!game.setupPrimary(true)) {
                        System.err.println("Failed to setup primary");
                        System.exit(0);
                    }
                    System.out.println("Successfully setup as Primary");
                }
            }

            /**
             * Continuously launch GUI read user input from
             * standard input.
             */
            game.gui.initialization(game.getGameGlobalState(), game.getGameLocalState().getName(), MazeSize);
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
            if(this.scheduler != null) {
                this.scheduler.shutdown();
            }
            this.scheduler = Executors.newScheduledThreadPool(0);
            if(this.backgroundScheduledTask != null) {
                this.backgroundScheduledTask.cancel(false);
            }
            this.backgroundScheduledTask = this.scheduler.scheduleAtFixedRate(
                    new PrimaryLiveChecker(
                            this.gameLocalState.getPrimaryBackupPair(),
                            this.gameGlobalState.getPlayerStubsMap(),
                            () -> this.primaryHandleBackupServerUnavailability(),
                            (name) -> this.primaryHandleStandardPlayerUnavailability(name)
                    ),
                    500, 500, TimeUnit.MILLISECONDS
            );
            System.out.println("!!!! "+this.getGameLocalState().getName()+" become Primary Now");
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
            if(this.scheduler != null) {
                this.scheduler.shutdown();
            }
            this.scheduler = Executors.newScheduledThreadPool(0);
            if(this.backgroundScheduledTask != null) {
                this.backgroundScheduledTask.cancel(false);
            }
            this.backgroundScheduledTask = this.scheduler.scheduleAtFixedRate(
                    new BackupLiveChecker(
                            this.gameLocalState.getPrimaryBackupPair(),
                            () -> this.backupHandlePrimaryServerUnavailability()
                    ),
                    500, 500, TimeUnit.MILLISECONDS
            );
            System.out.println("!!!! " + this.getGameLocalState().getName() + " become Backup Now");
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
        if(this.scheduler != null) {
            this.scheduler.shutdown();
        }
        this.scheduler = Executors.newScheduledThreadPool(0);
        if(this.backgroundScheduledTask != null) {
            this.backgroundScheduledTask.cancel(false);
        }
        this.backgroundScheduledTask = this.scheduler.scheduleWithFixedDelay(
                new StandardLiveChecker(
                        this.gameLocalState.getPrimaryBackupPair(),
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
                this.gameLocalState.getBackupStub().backupUpdateGameGlobalState(this.gameGlobalState);
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
        if(this.gameGlobalState.getPlayerStubsMap().size() == 1){
            this.gameGlobalState.addPlayer(playerName, PlayerType.Backup, stub);
            /**
             * Setup the stub to be backup server
             */
            try {
                stub.playerPromoteAsBackup(this.gameGlobalState, this.gameLocalState.getPrimaryStub());
                this.gameLocalState.setBackupStub(stub);
            } catch (RemoteException e) {
                System.out.println("Primary Server failed to contact new assigned Backup Server");
                this.gameGlobalState.removePlayerByName(playerName);
            }
        }else{
            this.gameGlobalState.addPlayer(playerName, PlayerType.Standard, stub);
            try {
                /**
                 * Setup the stub to be standard player
                 */
                stub.playerSetupAsStandard(this.gameGlobalState, this.gameLocalState.getPrimaryStub(), this.gameLocalState.getBackupStub());
                try {
                    /**
                     * Remote call backup server to update its global state
                     */
                    this.gameLocalState.getBackupStub().backupUpdateGameGlobalState(this.gameGlobalState);
                } catch (RemoteException e) {
                    System.out.println("Primary Server failed to update Backup Server");
                }
            } catch (RemoteException e) {
                System.out.println("Primary Server failed to contact new Standard Player");
                this.gameGlobalState.removePlayerByName(playerName);
            }
        }
        return this.gameGlobalState;
    }

    /**
     * update local backup server global state from
     * updated primary server data.
     * @param gameGlobalState
     * @return
     */
    public void backupUpdateGameGlobalState(Object gameGlobalState){
        this.gameGlobalState.resetAllStates(
                ((GameGlobalState) gameGlobalState).getPlayersMap(),
                ((GameGlobalState) gameGlobalState).getPlayerStubsMap(),
                ((GameGlobalState) gameGlobalState).getTreasuresLocation()
        );
        System.out.println("Successfully update global state of backup.");
    }

    public void playerPromoteAsBackup(Object gameGlobalState, GameInterface primary){
        this.getGameLocalState().setPrimaryStub(primary);
        this.setupBackup((GameGlobalState) gameGlobalState);
        System.out.printf("Successfully promoted to be backup. Name: %s\n", this.gameLocalState.getName());
    }

    public void playerSetupAsStandard(Object gameGlobalState, GameInterface primary, GameInterface backup){
        this.getGameLocalState().setPrimaryStub(primary);
        this.getGameLocalState().setBackupStub(backup);
        this.setupStandard((GameGlobalState) gameGlobalState);
        System.out.printf("Successfully setup to be standard. Name: %s\n", this.gameLocalState.getName());
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
        String backupPlayerName = this.gameGlobalState.getPlayerNameByStub(this.gameLocalState.getBackupStub());
        this.gameGlobalState.removePlayerByName(backupPlayerName);
        PrimaryServerHelper.assignBackupServer(this);
        PrimaryServerHelper.updateTrackerStubMap(this);
    }

    public void primaryHandleStandardPlayerUnavailability(String playerName){
        this.gameGlobalState.removePlayerByName(playerName);
        try {
            this.gameLocalState.getBackupStub().backupUpdateGameGlobalState(this.gameGlobalState);
        } catch (RemoteException e) {
            System.err.println("Primary failed to contact backup.");
        }
    }

    public void backupHandlePrimaryServerUnavailability(){
        System.out.println("!!!!Found Primary is dead");
        String primaryPlayerName = this.gameGlobalState.getPlayerNameByStub(this.gameLocalState.getPrimaryStub());
        this.gameGlobalState.removePlayerByName(primaryPlayerName);
        this.setupPrimary((this.gameGlobalState.getPlayerStubsMap().size() == 1));
        PrimaryServerHelper.assignBackupServer(this);
        PrimaryServerHelper.updateTrackerStubMap(this);

    }

    public void standardPlayerHandlePrimaryServerUnavailability(){
        try {
            /*** Wait for some time ***/
            TimeUnit.MILLISECONDS.sleep(700);

            /*** Remove primary player ***/
            String oldPrimaryName = this.gameGlobalState.getPlayerNameByStub(this.gameLocalState.getPrimaryStub());
            this.gameGlobalState.removePlayerByName(oldPrimaryName);

            try {
                /*** Contact backup server ***/
                System.out.println("contact backup server");
                List<GameInterface> primaryAndBackupStubs = this.gameLocalState.getBackupStub().getPrimaryAndBackupStubs();
                this.gameLocalState.setPrimaryStub(primaryAndBackupStubs.get(0));
                this.gameLocalState.setBackupStub(primaryAndBackupStubs.get(1));
                this.gameGlobalState.updateServerPlayerType(primaryAndBackupStubs.get(0), primaryAndBackupStubs.get(1));
            } catch (RemoteException e) {
                e.printStackTrace();
                System.out.println("Both primary and backup are offline");
                System.exit(0);
            }
        } catch (InterruptedException e1) {
            System.err.println("System interrupted in standardPlayerHandlePrimaryServerUnavailability");
        }
    }

    public void standardPlayerHandleBackupServerUnavailability(){
        try {
            /*** Wait for some time ***/
            TimeUnit.MILLISECONDS.sleep(700);

            if(!this.gameLocalState.getPlayerType().equals(PlayerType.Backup)) {
                /*** Remove old backup player ***/
                String oldBackupName = this.gameGlobalState.getPlayerNameByStub(this.gameLocalState.getBackupStub());
                this.gameGlobalState.removePlayerByName(oldBackupName);

                try {
                    System.out.println("contact primary server");
                    /*** Contact primary server ***/
                    List<GameInterface> primaryAndBackupStubs = this.gameLocalState.getPrimaryStub().getPrimaryAndBackupStubs();
                    this.gameLocalState.setPrimaryStub(primaryAndBackupStubs.get(0));
                    this.gameLocalState.setBackupStub(primaryAndBackupStubs.get(1));
                    this.gameGlobalState.updateServerPlayerType(primaryAndBackupStubs.get(0), primaryAndBackupStubs.get(1));
                } catch (RemoteException e) {
                    e.printStackTrace();
                    System.out.println("Both primary and backup are offline");
                    System.exit(0);
                }
            }
        } catch (InterruptedException e1) {
            System.err.println("System interrupted in standardPlayerHandleBackupServerUnavailability");
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


}
