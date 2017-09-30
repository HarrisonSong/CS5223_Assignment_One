package Game.BackgroundPing;

import Common.PrimaryBackupPair;

public class StandardLiveChecker implements Runnable {

    private PrimaryBackupPair primaryBackupPair;

    private HandlerInterface standardToPrimaryHandler;
    private HandlerInterface standardToBackupHandler;

    public StandardLiveChecker(PrimaryBackupPair primaryBackupPair,
                               HandlerInterface standardToPrimaryHandler,
                               HandlerInterface standardToBackupHandler) {
        this.primaryBackupPair = primaryBackupPair;
        this.standardToPrimaryHandler = standardToPrimaryHandler;
        this.standardToBackupHandler = standardToBackupHandler;
    }

    @Override
    public void run() {
        System.out.println("Standard Ping");
        try {
            if(!new PingMaster(this.primaryBackupPair.getPirmaryStub()).isReachable()){
                System.out.println("Standard Ping Primary Fail");
                this.standardToPrimaryHandler.handle();
            }else if(!new PingMaster(this.primaryBackupPair.getBackupStub()).isReachable()){
                System.out.println("Standard Ping Backup Fail");
                this.standardToBackupHandler.handle();
            }
        } catch (Throwable t){
            System.err.println("Standard player found one player that is offline.");
        }
    }
}
