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
        try {
            if(!pingMaster.isReachable()){
                try {
                    System.err.printf("Backup Ping Primary Fail: \n");
                    this.unavailableHandler.handle();
                } catch (Exception e) {
                    //System.err.printf("background single ping error %s \n", e.getMessage());
                }
            }
        } catch (Throwable t){
            System.err.println("Backup player found one player that is offline.");
        }
    }
}
