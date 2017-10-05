public class BackupLiveChecker implements Runnable {

    private PrimaryBackupPair primaryBackupPair;
    private GameInterface localStub;
    private HandlerInterface unavailableHandler;

    public BackupLiveChecker(PrimaryBackupPair primaryBackupPair, GameInterface localStub, HandlerInterface handler) {
        this.primaryBackupPair = primaryBackupPair;
        this.localStub = localStub;
        this.unavailableHandler = handler;
    }

    @Override
    public void run() {
        if(!localStub.equals(this.primaryBackupPair.getPrimaryStub())){
            PingMaster pingMaster = new PingMaster(this.primaryBackupPair.getPrimaryStub());
            try {
                if(!pingMaster.isReachable()){
                    try {
                        System.err.printf("BACKUP PING FAILURE: primary \n");
                        this.unavailableHandler.handle();
                    } catch (Exception e) {
                    }
                }
            } catch (Throwable t){
            }
        }
    }
}
