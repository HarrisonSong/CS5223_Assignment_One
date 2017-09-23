package Game;

import Common.*;
import Common.Pair.IdEndPointPair;
import Common.Pair.NameTypePair;
import Common.Pair.mazePair;
import Game.BackgroundPing.EndPointsLiveChecker;
import Game.BackgroundPing.SingleEndPointLiveChecker;
import Game.Player.Command;
import Game.Player.Player;
import Game.Player.PlayerType;
import sun.awt.SunHints;

import java.rmi.AlreadyBoundException;
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
    public static final int DEFAULT_PLAYER_ACCESS_PORT = 80;

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
            TrackerInterface tracker = (TrackerInterface) trackerRegistry.lookup("Tracker");

            String localIP = EndPoint.getLocalIP();
            int localPort = DEFAULT_PLAYER_ACCESS_PORT;

            if(tracker.registerNewPlayer(localIP, localPort, playName)){

                /**
                 * Initialize and collect necessary parameters from tracker
                 */
                Game game = new Game();
                game.tracker = tracker;
                MazeSize = tracker.getN();
                TreasureSize = tracker.getK();
                Map playerEndPoints = tracker.retrieveEndPointsMap();
                game.gameLocalState.setPlayerEndPointsMap(playerEndPoints);

                /**
                 * Setup player remote access
                 */
                game.setupRegistry(playName, game);

                switch (playerEndPoints.size()){
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
                        for(Map.Entry<String, EndPoint> entry : game.gameLocalState.getPlayerEndPointsMap().entrySet()) {
                            String key = entry.getKey();
                            EndPoint value = entry.getValue();

                            if (key == playName)
                                continue;

                            GameInterface t_Alive = game.contactGameEndPoint(new IdEndPointPair(key, value));

                            if (t_Alive != null){
                                List<IdEndPointPair> BPList = null;
                                try {
                                    BPList = t_Alive.getPrimaryAndBackupEndPoints();
                                } catch (RemoteException e){
                                    continue;
                                }

                                t_primary = game.contactGameEndPoint(BPList.get(0));
                                if (t_primary != null)
                                {
                                    try{
                                        game.gameGlobalState =  t_primary.join(new EndPoint(localIP, localPort), playName);

                                        List<IdEndPointPair> t_list = game.getPrimaryAndBackupEndPoints();

                                        IdEndPointPair t_back = t_list.get(1);

                                        // Check if i am the backup
                                        if (t_back != null) {
                                            game.gameLocalState.setBackupEndPoint(t_back);

                                            if (t_back.getId() == playName)
                                            {
                                                game.setupGame(playName, localIP, PlayerType.Backup );
                                            } else {
                                                game.setupGame(playName, localIP, PlayerType.Standard);
                                            }
                                        }

                                    } catch (RemoteException e){
                                        // No backup
                                        if (BPList.get(1) == null) {
                                            PrimaryIsAlive = false;
                                            break;
                                        }

                                        // Wait for backup to become Primary
                                        TimeUnit.MILLISECONDS.sleep(1300);

                                        t_primary = game.contactGameEndPoint(BPList.get(1));

                                        game.gameGlobalState =  t_primary.join(new EndPoint(localIP, localPort), playName);

                                        List<IdEndPointPair> t_list = game.getPrimaryAndBackupEndPoints();

                                        IdEndPointPair t_back = t_list.get(1);

                                        // Check if i am the backup
                                        if (t_back != null) {
                                            game.gameLocalState.setBackupEndPoint(t_back);

                                            if (t_back.getId() == playName)
                                            {
                                                game.setupGame(playName, localIP, PlayerType.Backup );
                                            } else {
                                                game.setupGame(playName, localIP, PlayerType.Standard);
                                            }
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

                                    game.gameGlobalState =  t_primary.join(new EndPoint(localIP, localPort), playName);

                                    List<IdEndPointPair> t_list = game.getPrimaryAndBackupEndPoints();

                                    IdEndPointPair t_back = t_list.get(1);

                                    // Check if i am the backup
                                    if (t_back != null) {
                                        game.gameLocalState.setBackupEndPoint(t_back);

                                        if (t_back.getId() == playName)
                                        {
                                            game.setupGame(playName, localIP, PlayerType.Backup );
                                        } else {
                                            game.setupGame(playName, localIP, PlayerType.Standard);
                                        }
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
                            game.backgroundScheduledTask = game.scheduler.scheduleAtFixedRate(new EndPointsLiveChecker(game.retrieveEnhancedEndPointsMap(), (name) -> game.handleStandardPlayerUnavailability(name), () -> game.handleBackupServerUnavailability()), 500, 500, TimeUnit.MILLISECONDS);

                        }

                        break;
                    } // End of Default

                } // End of Switch

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
    public void setupGameAsPrimaryServer(String playName, String localIPAddress){
        this.gameLocalState.setPlayName(playName);
        this.gameLocalState.setPlayerType(PlayerType.Primary);
        this.gameLocalState.setLocalEndPoint(new IdEndPointPair(playName, new EndPoint(localIPAddress, DEFAULT_PLAYER_ACCESS_PORT)));
        Player primaryPlayer = new Player(playName, new mazePair(Game.MazeSize), 0, PlayerType.Primary);
        this.gameGlobalState.initialize(primaryPlayer);
    }

    /**
     *
     */
    private boolean handlePrimaryCrash(IdEndPointPair backup, String localIP, int localPort, String playName){


        return false;
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
            this.backupServer = this.contactGameEndPoint(new IdEndPointPair(backupPlayerName,newEndPoint));
            if(this.backupServer != null){
                setBackupServer(latestActivePlayerIndex);
                break;
            }
        }
    }

    private void setBackupServer(int playerIndex){
        if(this.gameLocalState.getPlayerType() == PlayerType.Standard) {
            this.gameGlobalState.getPlayerList().get(playerIndex).setType(PlayerType.Backup);
            this.gameLocalState.setBackupEndPoint(new IdEndPointPair(
                    this.gameLocalState.getPlayName(),
                    this.gameLocalState.getPlayerEndPointsMap().get(this.gameLocalState.getPlayName()))
            );
        }
    }

    /**
     * read and process the player requests
     * @param playerName
     * @param request
     * @return updated global game state
     */
    public GameGlobalState executeRequest(String playerName, Command request){
        if(request.equals(Command.Exit)){
            this.gameLocalState.removePlayerEndPoint(playerName);
            this.gameGlobalState.removePlayerByName(playerName);
        }
        if(request.equals(Command.East) || request.equals(Command.West) ||
                request.equals(Command.South) || request.equals(Command.North)){
            this.gameGlobalState.makeMove(request, playerName);
        }
        try {
            backupServer.updateBackupGameState(this.gameGlobalState);
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
    public GameGlobalState join(EndPoint IPAddress, String playName){
        if(this.gameLocalState.getPlayerEndPointsMap().size() == 1){
            this.gameLocalState.setBackupEndPoint(new IdEndPointPair(playName, IPAddress));
            this.gameGlobalState.addNewPlayerByName(playName, PlayerType.Backup);
            this.gameLocalState.addPlayerEndPoint(playName, IPAddress);

            /**
             * setup RMI connection to backup server
             */
            try {
                Registry backupRegistry = LocateRegistry.getRegistry(gameLocalState.getBackupEndPoint().getEndPoint().getIPAddress(), gameLocalState.getBackupEndPoint().getEndPoint().getPort());
                String backupPlayerName = gameLocalState.getBackupEndPoint().getId();
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

    /**
     *
     * BACKUP SPECIFIC METHODS
     *
     */

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

    /**
     * promote current game to be primary server
     * @param isTheOnlyPlayer
     */
    private void promoteToBePrimary(boolean isTheOnlyPlayer){
        if(isTheOnlyPlayer || this.gameLocalState.getPlayerType() == PlayerType.Backup) {
            this.gameLocalState.setPlayerType(PlayerType.Primary);
            this.gameGlobalState.getPlayerList().get(this.gameGlobalState.getIndexOfPlayerByName(this.gameLocalState.getPlayName())).setType(PlayerType.Primary);
            this.gameLocalState.setPrimaryEndPoint(this.gameLocalState.getLocalEndPoint());

            /**
             * Reschedule the background ping task
             */
            this.backgroundScheduledTask.cancel(true);
            this.backgroundScheduledTask = this.scheduler.scheduleAtFixedRate(new EndPointsLiveChecker(this.retrieveEnhancedEndPointsMap(), (name) -> this.handleStandardPlayerUnavailability(name), () -> this.handleBackupServerUnavailability()), 500, 500, TimeUnit.MILLISECONDS);
        }
    }

    /**
     *
     * COMMON PLAYER METHODS
     *
     */

    public void setupGame(String playName, String localIPAddress, PlayerType type){
        this.gameLocalState.setPlayName(playName);
        this.gameLocalState.setPlayerType(type);

        this.gameLocalState.setLocalEndPoint(new IdEndPointPair(playName, new EndPoint(localIPAddress, DEFAULT_PLAYER_ACCESS_PORT)));
        //this.gameGlobalState.addNewPlayerByName(playName, type);

    }

    /**
     * Method to setup local RMI registry
     * @param playName
     * @param game
     * @throws RemoteException
     * @throws AlreadyBoundException
     */
    public void setupRegistry(String playName, Game game) throws RemoteException, AlreadyBoundException {
        GameInterface stub = (GameInterface) UnicastRemoteObject.exportObject(game, DEFAULT_PLAYER_ACCESS_PORT);
        Registry registry = LocateRegistry.createRegistry(DEFAULT_PLAYER_ACCESS_PORT);
        registry.bind("Player_" + playName, stub);
    }

    /**
     * update local backup server global state from
     * updated primary server data.
     * @param gameState
     * @return
     */
    public boolean updateBackupGameState(GameGlobalState gameState){
        if(this.gameLocalState.getPlayerType() == PlayerType.Backup){
            this.updateGameGlobalState(gameState);
        }
        return false;
    }

    /**
     * Get primary and backup server endPoints
     * @return
     */
    public List<IdEndPointPair> getPrimaryAndBackupEndPoints(){
        List<IdEndPointPair> result = new ArrayList<>();
        result.add(this.gameLocalState.getPrimaryEndPoint());
        result.add(this.gameLocalState.getBackupEndPoint());
        return result;
    }

    /**
     * Method for player to issue the user operation
     * @param request
     */
    public void issueRequest(String request){
        Command playerCommand = classifyPlayerInput(request);
        if(!playerCommand.equals(Command.Invalid)){
            try {
                primaryServer.executeRequest(this.gameLocalState.getPlayName(), playerCommand);
            } catch (RemoteException e) {

                try {
                    TimeUnit.MILLISECONDS.sleep(1300);


                    List<IdEndPointPair> t_list = backupServer.getPrimaryAndBackupEndPoints();
                    gameLocalState.setPrimaryEndPoint(t_list.get(0));
                    gameLocalState.setBackupEndPoint(t_list.get(1));
                    primaryServer = contactGameEndPoint(t_list.get(0));
                    backupServer = contactGameEndPoint(t_list.get(1));

                    primaryServer.executeRequest(this.gameLocalState.getPlayName(), playerCommand);

                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                e.printStackTrace();
            }
            if(request.equals(Command.Exit.getValue())){
                System.exit(0);
            }
        }
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
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(new SingleEndPointLiveChecker(this.gameLocalState.getPrimaryEndPoint().getEndPoint(), () -> this.handlePrimaryServerUnavailability()), 500, 500, TimeUnit.MILLISECONDS);
            return true;
        }
        return false;
    }

    /**
     * Method to retrieve player name/type mapping with endpoint
     * @return
     */
    public Map retrieveEnhancedEndPointsMap(){
        Map<NameTypePair, EndPoint> enhancedEndPointsMap = new HashMap<>();
        for(Map.Entry<String, EndPoint> endpoint : this.gameLocalState.getPlayerEndPointsMap().entrySet()){
            if(!endpoint.getValue().equals(this.gameLocalState.getPrimaryEndPoint())){
                if(endpoint.getValue().equals(this.gameLocalState.getBackupEndPoint())){
                    enhancedEndPointsMap.put(new NameTypePair(endpoint.getKey(), PlayerType.Backup), endpoint.getValue());
                }else{
                    enhancedEndPointsMap.put(new NameTypePair(endpoint.getKey(), PlayerType.Standard), endpoint.getValue());
                }
            }
        }
        return enhancedEndPointsMap;
    }

    /**
     * HELPER METHODS
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

    private GameInterface contactGameEndPoint(IdEndPointPair pair){
        try {
            Registry backupRegistry = LocateRegistry.getRegistry(pair.getEndPoint().getIPAddress(), pair.getEndPoint().getPort());
            try {
                GameInterface targetPlayer = (GameInterface)backupRegistry.lookup("Player_" + pair.getId());
                return targetPlayer;
            } catch (NotBoundException notBoundException) {
                notBoundException.printStackTrace();
                return null;
            }
        } catch (RemoteException remoteException) {
            remoteException.printStackTrace();
            return null;
        }
    }
}
