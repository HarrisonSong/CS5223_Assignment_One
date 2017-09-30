package Game.Utility;

import Game.BackgroundPing.PingMaster;
import Game.Game;
import Game.Player.Player;
import Game.State.GameGlobalState;
import Game.Player.Command;
import Game.Player.PlayerType;
import Interface.GameInterface;

import java.rmi.RemoteException;
import java.util.List;
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
                } else {
                    GameGlobalState updatedState = (GameGlobalState) game.getGameLocalState().getPrimaryStub().primaryExecuteRemoteRequest(game.getGameLocalState().getName(), request);
                    game.getGameGlobalState().resetAllStates(
                            updatedState.getPlayersMap(),
                            updatedState.getPlayerStubsMap(),
                            updatedState.getTreasuresLocation()
                    );
                }
            } catch (RemoteException e) {
                /**
                 * Primary server is offline
                 */
                if(game.getGameLocalState().getPlayerType().equals(PlayerType.Backup)){
                    game.primaryExecuteRemoteRequest(game.getGameLocalState().getName(), request);
                }else{
                    try {
                        TimeUnit.MILLISECONDS.sleep(1300);
                        if(!game.getGameLocalState().getPlayerType().equals(PlayerType.Backup)){
                            List<GameInterface> serverList = game.getGameLocalState().getBackupStub().getPrimaryAndBackupStubs();
                            game.getGameLocalState().setPrimaryStub(serverList.get(0));
                            game.getGameLocalState().setBackupStub(serverList.get(1));
                        }
                        game.getGameLocalState().getPrimaryStub().primaryExecuteRemoteRequest(game.getGameLocalState().getName(), request);
                    } catch (Exception e1) {
                        System.err.println("Detect 2 player failures within 2 seconds");
                    }
                }
            }

            if(request.equals(Command.Exit.getValue())){
                System.exit(0);
            }

            //game.updateGUI();
        }
    }
}
