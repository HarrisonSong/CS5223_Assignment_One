import java.rmi.RemoteException;
import java.util.Iterator;

public class PrimaryServerHelper {
    public static void updateTrackerStubMap(Game game){
        try {
            game.getGameLocalState().getTrackerStub().resetTrackerStubs(
                    game.getGameGlobalState().getRemovedPlayers(),
                    game.getGameLocalState().getName()
            );
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
        /**
         * Confirm the old backup server is unavailable.
         * Release it.
         */
        System.out.println("Primary assigning new backup server");
        game.getGameLocalState().getPrimaryBackupPair().getBackupStubLock().writeLock().lock();
        try {
            game.getGameLocalState().setBackupStubLockFree(null);
            Iterator<String> iterator = game.getGameGlobalState().getPlayersMap().keySet().iterator();
            while(iterator.hasNext()){
                String backupPlayerName = iterator.next();
                if(backupPlayerName.equals(game.getGameLocalState().getName())){
                    System.out.println("Primary cannot be backup");
                    continue;
                }
                System.out.printf("Primary trying to assign %s as backup server\n", backupPlayerName);
                try {
                    game.getGameGlobalState().updatePlayerType(backupPlayerName, PlayerType.Backup);
                    GameInterface newBackupStub = game.getGameGlobalState().getPlayerStubsMap().get(backupPlayerName);
                    newBackupStub.playerPromoteAsBackup(game.getGameGlobalState(), game.getGameLocalState().getPrimaryStub());
                    game.getGameLocalState().setBackupStub(newBackupStub);
                    System.out.printf("Successfully set %s to be backup.\n", backupPlayerName);
                    return;
                } catch (RemoteException e) {
                    game.getGameGlobalState().removePlayerByName(backupPlayerName);
                }
            }
        } finally {
            game.getGameLocalState().getPrimaryBackupPair().getBackupStubLock().writeLock().unlock();
        }
    }
}
