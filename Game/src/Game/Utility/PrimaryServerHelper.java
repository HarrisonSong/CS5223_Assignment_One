package Game.Utility;

import Game.State.GameGlobalState;
import Game.State.GameLocalState;
import Game.Player.PlayerType;
import Interface.GameInterface;

import java.rmi.RemoteException;

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
        while(true){
            int latestActivePlayerIndex = gameGlobalState.findNextActivePlayerIndex();
            if(latestActivePlayerIndex == -1){
                break;
            }
            String backupPlayerName = gameGlobalState.getPlayerList().get(latestActivePlayerIndex).getName();
            GameInterface newBackupStub = gameLocalState.getPlayerStubsMap().get(backupPlayerName);
            gameLocalState.setBackupStub(newBackupStub);
            if(gameLocalState.getBackupStub()!= null){
                setBackupServer(latestActivePlayerIndex, gameLocalState, gameGlobalState);
                break;
            }
        }
    }

    private static void setBackupServer(int playerIndex,GameLocalState gameLocalState, GameGlobalState gameGlobalState){
        if(gameLocalState.getPlayerType() == PlayerType.Standard) {
            gameGlobalState.getPlayerList().get(playerIndex).setType(PlayerType.Backup);
            gameLocalState.setBackupStub(gameLocalState.getLocalStub());
        }
    }
}
