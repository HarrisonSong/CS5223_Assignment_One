package Game.Utility;

import Game.State.GameGlobalState;
import Game.State.GameLocalState;
import Game.Player.PlayerType;
import Interface.GameInterface;

import java.rmi.RemoteException;
import java.util.Iterator;

public class PrimaryServerHelper {
    public static void updateTrackerStubMap(GameLocalState localState){
        try {
            localState.getTrackerStub().resetTrackerStubs(localState.getPlayerStubsMap());
        } catch (RemoteException e) {
            e.printStackTrace();
            System.err.println("Failed to contact Tracker METHOD: resetTrackerStubs");
            System.exit(0);
        }
    }

    /**
     * Assign player to be backup server
     */
    public static void assignBackupServer(GameLocalState gameLocalState, GameGlobalState gameGlobalState){
         Iterator<String> iterator = gameGlobalState.getPlayersMap().keySet().iterator();
         while(iterator.hasNext()){
             String backupPlayerName = iterator.next();
             if(backupPlayerName.equals(gameLocalState.getName())){
                 System.out.println("Primary cannot be backup");
                 continue;
             }
             try {
                 GameInterface newBackupStub = gameLocalState.getPlayerStubsMap().get(backupPlayerName);
                 newBackupStub.backupUpdateGameGlobalState(gameGlobalState);
                 gameLocalState.setBackupStub(newBackupStub);
                 setBackupServer(backupPlayerName, gameLocalState, gameGlobalState);
                 System.out.printf("Successfully set %s to be backup.\n", backupPlayerName);
                 break;
             } catch (RemoteException e) {
                 gameGlobalState.removePlayerByName(backupPlayerName);
             }
         }
    }

    private static void setBackupServer(String playerName, GameLocalState gameLocalState, GameGlobalState gameGlobalState){
        if(gameLocalState.getPlayerType() == PlayerType.Standard) {
            gameGlobalState.updatePlayerType(playerName, PlayerType.Backup);
            gameLocalState.setBackupStub(gameLocalState.getLocalStub());
        }
    }
}
