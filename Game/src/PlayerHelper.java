import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayerHelper {

    /**
     * promote current game to be primary server
     * @param isTheOnlyPlayer
     */
    public static boolean setupSelfAsPrimary(Game game, boolean isTheOnlyPlayer){
        if(isTheOnlyPlayer || game.getGameLocalState().getPlayerType().equals(PlayerType.Backup)) {
            /**
             * Update game local state
             */
            game.getGameLocalState().setPlayerType(PlayerType.Primary);
            game.getGameLocalState().setPrimaryStub(game.getGameLocalState().getLocalStub());
            game.getGameLocalState().setBackupStub(null);
            /**
             * Update game global state
             */
            game.getGameGlobalState().updatePlayerType(game.getGameLocalState().getName(), PlayerType.Primary);
            return true;
        }
        return false;
    }

    /**
     * Player promotes itself to be backup server
     * @param game
     * @param gameGlobalState: updated global game state from primary server
     * @return promotion status
     */
    public static boolean setupSelfAsBackup(Game game, GameGlobalState gameGlobalState) {
        if(game.getGameLocalState().getPlayerType().equals(PlayerType.Standard)){
            game.getGameLocalState().setPlayerType(PlayerType.Backup);
            game.getGameLocalState().setBackupStub(game.getGameLocalState().getLocalStub());
            game.getGameGlobalState().resetAllStates(
                    gameGlobalState.getPlayersMap(),
                    gameGlobalState.getPlayerStubsMap(),
                    gameGlobalState.getTreasuresLocation()
            );
            return true;
        }
        if (game.getGameLocalState().getPlayerType().equals(PlayerType.Backup)){
            System.out.println("### " + game.getGameLocalState().getName() + " has been set up as back up");
            return true;
        }
        return false;
    }

    /**
     * Player set up as a standard player
     * @param game
     * @param gameGlobalState: updated global game state from primary server
     * @return promotion status
     */
    public static void setupSelfAsStandard(Game game, GameGlobalState gameGlobalState) {
        game.getGameLocalState().setPlayerType(PlayerType.Standard);
        game.getGameGlobalState().resetAllStates(
                gameGlobalState.getPlayersMap(),
                gameGlobalState.getPlayerStubsMap(),
                gameGlobalState.getTreasuresLocation()
        );
    }
    
    /**
     * Method for player to issue the user operation
     * @param request
     */
    public static void issueRequest(String request, Game game){
        System.out.printf("Issuing a request %s\n", request);
        Command playerCommand;
        try{
            playerCommand = Command.fromString(request);
        }catch (Exception e){
            playerCommand = Command.Invalid;
        }
        if(!playerCommand.equals(Command.Invalid)){
            try {
                if (game.getGameLocalState().getPlayerType().equals(PlayerType.Primary)) {
                    game.primaryExecuteRemoteRequest(game.getGameLocalState().getName(), request);
                    System.out.println("=========After call Primary Self Call================");
                    Map<String, Player> mm = game.getGameGlobalState().getPlayersMap();
                    for (Map.Entry<String, Player> entry : mm.entrySet()){
                        System.out.printf(entry.getKey()+" [" + entry.getValue().getCurrentPosition().getRow() + ", "+entry.getValue().getCurrentPosition().getColumn()+"]\n");
                    }
                    System.out.println("=======================================================");
                } else {
                    GameGlobalState updatedState = (GameGlobalState) game.getGameLocalState().getPrimaryStub().primaryExecuteRemoteRequest(game.getGameLocalState().getName(), request);
                    game.getGameGlobalState().resetAllStates(
                            updatedState.getPlayersMap(),
                            updatedState.getPlayerStubsMap(),
                            updatedState.getTreasuresLocation()
                    );

                    System.out.println("=========After call Primary Remote Call================");
                    Map<String, Player> mm = game.getGameGlobalState().getPlayersMap();
                    for (Map.Entry<String, Player> entry : mm.entrySet()){
                        System.out.printf(entry.getKey()+" [" + entry.getValue().getCurrentPosition().getRow() + ", "+entry.getValue().getCurrentPosition().getColumn()+"]\n");
                    }
                    System.out.println("=======================================================");
                }
            } catch (RemoteException e) {
                /**
                 * Primary server is offline
                 */
                System.out.printf("Primary server is offline when issuing request by %s\n", game.getGameLocalState().getName());
                if(game.getGameLocalState().getPlayerType().equals(PlayerType.Backup)){
                    game.primaryExecuteRemoteRequest(game.getGameLocalState().getName(), request);
                }else{
                    try {
                        System.out.println("Wait for 1 second for backup server promote");
                        TimeUnit.MILLISECONDS.sleep(Game.RETRY_WAITING_TIME);
                        if(!game.getGameLocalState().getPlayerType().equals(PlayerType.Backup)){
                            System.out.println("Current player is not backup");
                            List<GameInterface> serverList = game.getGameLocalState().getBackupStub().getPrimaryAndBackupStubs();
                            game.getGameLocalState().setPrimaryStub(serverList.get(0));
                            game.getGameLocalState().setBackupStub(serverList.get(1));
                        }
                        game.getGameLocalState().getPrimaryStub().primaryExecuteRemoteRequest(game.getGameLocalState().getName(), request);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        System.err.println("ISSUE REQUEST: Detect both primary server and backup server fail within 2 seconds");
                    }
                }
            }

            if(request.equals(Command.Exit.getValue())){
                System.exit(0);
            }
        }
    }
}
