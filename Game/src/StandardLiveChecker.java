public class StandardLiveChecker implements Runnable {

    private PrimaryBackupPair primaryBackupPair;
    private GameInterface localStub;

    private HandlerInterface standardToPrimaryHandler;
    private HandlerInterface standardToBackupHandler;

    public StandardLiveChecker(PrimaryBackupPair primaryBackupPair,
                               GameInterface localStub,
                               HandlerInterface standardToPrimaryHandler,
                               HandlerInterface standardToBackupHandler){
        this.primaryBackupPair = primaryBackupPair;
        this.localStub = localStub;
        this.standardToPrimaryHandler = standardToPrimaryHandler;
        this.standardToBackupHandler = standardToBackupHandler;
    }

    @Override
    public void run() {
        try {
            if(!new PingMaster(this.primaryBackupPair.getPirmaryStub()).isReachable()){
                System.out.println("STANDARD PING FAILURE: primary");
                this.standardToPrimaryHandler.handle();
            }else if(!localStub.equals(this.primaryBackupPair.getBackupStub()) &&
                    !new PingMaster(this.primaryBackupPair.getBackupStub()).isReachable()){
                System.out.println("STANDARD PING FAILURE: backup");
                this.standardToBackupHandler.handle();
            }
        } catch (Throwable t){
        }
    }
}
