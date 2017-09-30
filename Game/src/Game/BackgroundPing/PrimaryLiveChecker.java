package Game.BackgroundPing;

import Common.PrimaryBackupPair;
import Interface.GameInterface;

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
        System.out.printf("Primary Ping: %d \n", this.stubsMap.size());
        for (Map.Entry<String, GameInterface> stubEntry : this.stubsMap.entrySet()) {
            System.out.printf("Primary Ping: Name - %s\n", stubEntry.getKey());
            if (!stubEntry.getValue().equals(this.primaryBackupPair.getPirmaryStub())) {
                if (!new PingMaster(stubEntry.getValue()).isReachable()) {
                    System.out.printf("Primary Ping Fail: Name - %s\n", stubEntry.getKey());
                    if (stubEntry.getValue().equals(this.primaryBackupPair.getBackupStub())) {
                        this.primaryToBackupHandler.handle();
                    } else {
                        this.primaryToStandardHandler.handleWithPlayerName(stubEntry.getKey());
                    }
                }
            }
        }
    }
}
