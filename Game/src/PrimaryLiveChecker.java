import java.util.Iterator;
import java.util.Map;

public class PrimaryLiveChecker implements Runnable {

    private PrimaryBackupPair primaryBackupPair;
    private Map<String, GameInterface> stubsMap;

    private HandlerInterface primaryToBackupHandler;
    private HandlerWithPlayerNameInterface primaryToStandardHandler;

    public PrimaryLiveChecker(
            PrimaryBackupPair primaryBackupPair,
            Map<String, GameInterface> stubsMap,
            HandlerInterface PrimaryToBackupHandler,
            HandlerWithPlayerNameInterface PrimaryToStandardHandler) {
        this.primaryBackupPair = primaryBackupPair;
        this.stubsMap = stubsMap;
        this.primaryToBackupHandler = PrimaryToBackupHandler;
        this.primaryToStandardHandler = PrimaryToStandardHandler;
    }

    @Override
    public void run() {
        Iterator<Map.Entry<String, GameInterface>> iterator = this.stubsMap.entrySet().iterator();
        boolean proceed = true;
        try {
            if(this.primaryBackupPair.getBackupStub() != null){
                if (!new PingMaster(this.primaryBackupPair.getBackupStub()).isReachable()) {
                    System.err.println("PRIMARY PING FAILURE: backup Name");
                    this.primaryToBackupHandler.handle();
                    proceed = false;
                }
            }
            while (iterator.hasNext() && proceed) {
                Map.Entry<String, GameInterface> nextStub = iterator.next();
                if (!nextStub.getValue().equals(this.primaryBackupPair.getPrimaryStub())) {
                    if (!new PingMaster(nextStub.getValue()).isReachable()) {
                        if (nextStub.getValue().equals(this.primaryBackupPair.getBackupStub())) {
                            System.err.printf("PRIMARY PING FAILURE: backup Name - %s\n", nextStub.getKey());
                            this.primaryToBackupHandler.handle();
                        } else {
                            System.err.printf("PRIMARY PING FAILURE: standard Name - %s\n", nextStub.getKey());
                            this.primaryToStandardHandler.handleWithPlayerName(nextStub.getKey());
                        }
                        proceed = false;
                    }
                }
            }

        } catch (Throwable t){
        }
    }
}
