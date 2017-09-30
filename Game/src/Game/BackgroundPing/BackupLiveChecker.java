package Game.BackgroundPing;

import Common.PrimaryBackupPair;
import Game.Player.PlayerType;
import Interface.GameInterface;

public class BackupLiveChecker implements Runnable {

    private PrimaryBackupPair primaryBackupPair;
    private HandlerInterface unavailableHandler;

    public BackupLiveChecker(PrimaryBackupPair primaryBackupPair, HandlerInterface handler) {
        this.primaryBackupPair = primaryBackupPair;
        this.unavailableHandler = handler;
    }

    @Override
    public void run() {
        PingMaster pingMaster = new PingMaster(this.primaryBackupPair.getPirmaryStub());
        System.out.println("Backup Ping");
        if(!pingMaster.isReachable()){
            try {
                System.err.printf("Backup Ping Fail: \n");
                this.unavailableHandler.handle();
            } catch (Exception e) {
                System.err.printf("background multiple ping error %s \n", e.getMessage());
            }
        }
    }
}
