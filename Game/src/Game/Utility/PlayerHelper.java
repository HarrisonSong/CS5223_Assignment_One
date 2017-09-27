package Game.Utility;

import Common.NameTypePair;
import Game.BackgroundPing.PingMaster;
import Game.Game;
import Game.State.GameGlobalState;
import Game.State.GameLocalState;
import Game.Player.Command;
import Game.Player.PlayerType;
import Interface.GameInterface;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayerHelper {

    /**
     * promote current game to be primary server
     * @param isTheOnlyPlayer
     */
    public static boolean setupSelfAsPrimary(Game game, boolean isTheOnlyPlayer){
        if(isTheOnlyPlayer || game.getGameLocalState().getPlayerType() == PlayerType.Backup) {
            /**
             * Update game local state
             */
            game.getGameLocalState().setPlayerType(PlayerType.Primary);
            game.getGameLocalState().setPrimaryStub(game.getGameLocalState().getLocalStub());

            /**
             * Update game global state
             */
            game.getGameGlobalState().getPlayerList().get(
                    game.getGameGlobalState().getIndexOfPlayerByName(game.getGameLocalState().getName())
            ).setType(PlayerType.Primary);
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
        if(game.getGameLocalState().getPlayerType() == PlayerType.Standard){
            game.getGameLocalState().setPlayerType(PlayerType.Backup);
            game.getGameLocalState().setBackupStub(game.getGameLocalState().getLocalStub());
            updateGameGlobalState(game.getGameGlobalState(), gameGlobalState);
            return true;
        }
        return false;
    }

    /**
     * Method for player to issue the user operation
     * @param request
     */
    public static void issueRequest(String request, Game game){
        Command playerCommand;
        try{
            playerCommand = Command.fromString(request);
        }catch (Exception e){
            playerCommand = Command.Invalid;
        }
        if(!playerCommand.equals(Command.Invalid)){
            try {
                GameInterface p = game.getGameLocalState().getPrimaryStub();
                p.primaryExecuteRemoteRequest(game.getGameLocalState().getName(), request);
            } catch (RemoteException e) {

                /**
                 * Primary server is offline
                 */
                try {
                    TimeUnit.MILLISECONDS.sleep(1300);
                    List<GameInterface> serverList = game.getGameLocalState().getBackupStub().getPrimaryAndBackupStubs();
                    game.getGameLocalState().setPrimaryStub(serverList.get(0));
                    game.getGameLocalState().setBackupStub(serverList.get(1));
                    game.getGameLocalState().getPrimaryStub().primaryExecuteRemoteRequest(game.getGameLocalState().getName(), request);
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
     * Method to retrieve player name/type mapping with endpoint
     * @return
     */
    public static Map retrieveEnhancedStubsMap(GameLocalState gameLocalState){
        Map<NameTypePair, GameInterface> enhancedEndPointsMap = new HashMap<>();
        for(Map.Entry<String, GameInterface> stub : gameLocalState.getPlayerStubsMap().entrySet()){
            if(!stub.getValue().equals(gameLocalState.getPrimaryStub())){
                if(stub.getValue().equals(gameLocalState.getBackupStub())){
                    enhancedEndPointsMap.put(new NameTypePair(stub.getKey(), PlayerType.Backup), stub.getValue());
                }else{
                    enhancedEndPointsMap.put(new NameTypePair(stub.getKey(), PlayerType.Standard), stub.getValue());
                }
            }
        }
        return enhancedEndPointsMap;
    }

    /**
     * Update game global state
     * server response
     * @param targetState
     * @param gameState
     */
    public static void updateGameGlobalState(GameGlobalState targetState, GameGlobalState gameState){
        targetState.setPlayerList(gameState.getPlayerList());
        targetState.setTreasureLocation(gameState.getTreasureLocation());
    }

    public static boolean isTargetAlive(GameInterface stub){
        return new PingMaster(stub).isReachable();
    }

}
