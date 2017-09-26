package Game;

import Common.Pair.NameTypePair;
import Game.Player.Command;
import Game.Player.PlayerType;
import Interface.GameInterface;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayerFunctions {

    /**
     * promote current game to be primary server
     * @param isTheOnlyPlayer
     */
    public static boolean promoteToBePrimary(boolean isTheOnlyPlayer, GameLocalState gameLocalState, GameGlobalState gameGlobalState){
        if(isTheOnlyPlayer || gameLocalState.getPlayerType() == PlayerType.Backup) {
            gameLocalState.setPlayerType(PlayerType.Primary);
            gameGlobalState.getPlayerList().get(gameGlobalState.getIndexOfPlayerByName(gameLocalState.getName())).setType(PlayerType.Primary);
            gameLocalState.setPrimaryStub(gameLocalState.getLocalStub());

            return true;
            }
        return false;
    }

    /**
     *
     * COMMON PLAYER METHODS
     *
     */

    public static void setupGame(String playName, GameInterface stub, PlayerType type, GameLocalState gameLocalState){
        gameLocalState.setName(playName);
        gameLocalState.setPlayerType(type);
        gameLocalState.setLocalStub(stub);

    }

    /**
     * Method for player to issue the user operation
     * @param request
     */
    public static void issueRequest(String request, GameLocalState gameLocalState){
        Command playerCommand;
        try{
             playerCommand= Command.valueOf(request);
        }catch (Exception e){
            playerCommand = Command.Invalid;
        }
        if(!playerCommand.equals(Command.Invalid)){
            try {
                gameLocalState.getPrimaryStub().primaryExecuteRemoteRequest(gameLocalState.getName(), request);
            } catch (RemoteException e) {

                try {
                    TimeUnit.MILLISECONDS.sleep(1300);
                    List<GameInterface> t_list = gameLocalState.getBackupStub().getPrimaryAndBackupStubs();
                    gameLocalState.setPrimaryStub(t_list.get(0));
                    gameLocalState.setBackupStub(t_list.get(1));

                    gameLocalState.getPrimaryStub().primaryExecuteRemoteRequest(gameLocalState.getName(), request);

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
    public static Map retrieveEnhancedEndPointsMap(GameLocalState gameLocalState){
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
}
