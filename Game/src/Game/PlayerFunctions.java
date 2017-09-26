package Game;

import Common.EndPoint;
import Common.Pair.NameEndPointPair;
import Common.Pair.NameTypePair;
import Game.BackgroundPing.EndPointsLiveChecker;
import Game.Player.Command;
import Game.Player.PlayerType;

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

        this.gameLocalState.setLocalEndPoint(new NameEndPointPair(playName, new EndPoint(localIPAddress, DEFAULT_PLAYER_ACCESS_PORT)));
        //this.gameGlobalState.addNewPlayerByName(playName, type);

    }

    /**
     * Method for player to issue the user operation
     * @param request
     */
    public void issueRequest(String request){
        Command playerCommand = classifyPlayerInput(request);
        if(!playerCommand.equals(Command.Invalid)){
            try {
                primaryServer.primaryExecuteRemoteRequest(this.gameLocalState.getPlayName(), playerCommand);
            } catch (RemoteException e) {

                try {
                    TimeUnit.MILLISECONDS.sleep(1300);


                    List<NameEndPointPair> t_list = backupServer.getPrimaryAndBackupStubs();
                    gameLocalState.setPrimaryEndPoint(t_list.get(0));
                    gameLocalState.setBackupEndPoint(t_list.get(1));
                    primaryServer = contactGameEndPoint(t_list.get(0));
                    backupServer = contactGameEndPoint(t_list.get(1));

                    primaryServer.primaryExecuteRemoteRequest(this.gameLocalState.getPlayName(), playerCommand);

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
}
