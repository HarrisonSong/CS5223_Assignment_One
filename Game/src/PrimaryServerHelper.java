import java.rmi.RemoteException;
import java.util.Iterator;

public class PrimaryServerHelper {
    public static void updateTrackerStubMap(Game game){
        try {
            game.getGameLocalState().getTrackerStub().resetTrackerStubs(game.getGameGlobalState().getPlayerStubsMap());
        } catch (RemoteException e) {
            e.printStackTrace();
            System.err.println("Failed to contact Tracker METHOD: resetTrackerStubs");
            System.exit(0);
        }
    }

    /**
     * Assign player to be backup server
     */
    public static void assignBackupServer(Game game){
         Iterator<String> iterator = game.getGameGlobalState().getPlayersMap().keySet().iterator();
         while(iterator.hasNext()){
             String backupPlayerName = iterator.next();
             if(backupPlayerName.equals(game.getGameLocalState().getName())){
                 System.out.println("Primary cannot be backup");
                 continue;
             }
             try {
                 game.getGameGlobalState().updatePlayerType(backupPlayerName, PlayerType.Backup);
                 GameInterface newBackupStub = game.getGameGlobalState().getPlayerStubsMap().get(backupPlayerName);
                 game.getGameLocalState().setBackupStub(newBackupStub);
                 newBackupStub.playerPromoteAsBackup(game.getGameGlobalState(), game.getGameLocalState().getPrimaryStub());
                 System.out.printf("Successfully set %s to be backup.\n", backupPlayerName);
                 return;
             } catch (RemoteException e) {
                 game.getGameGlobalState().removePlayerByName(backupPlayerName);
             }
         }
         game.getGameLocalState().setBackupStub(null);
    }
}
