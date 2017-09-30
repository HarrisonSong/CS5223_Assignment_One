package Game.BackgroundPing;

import Game.Player.PlayerType;
import Interface.GameInterface;

import java.util.Map;

public class MultipleTargetsLiveChecker implements Runnable {

    private PlayerType proposerType;
    private GameInterface primaryStub;
    private GameInterface backupStub;
    private Map<String, GameInterface> stubsMap;

    private HandlerInterface primaryToBackupHandler;
    private HandlerWithPlayerNameInterface primaryToStandardHandler;

    private HandlerInterface standardToPrimaryHandler;
    private HandlerInterface standardToBackupHandler;

    public MultipleTargetsLiveChecker(
            PlayerType proposerType,
            GameInterface primaryStub,
            GameInterface backupStub,
            Map<String, GameInterface> stubsMap,
            HandlerInterface PrimaryToBackupHandler,
            HandlerWithPlayerNameInterface PrimaryToStandardHandler,
            HandlerInterface StandardToPrimaryHandler,
            HandlerInterface StandardToBackupHandler) {
        this.proposerType = proposerType;
        this.primaryStub = primaryStub;
        this.backupStub = backupStub;
        this.stubsMap = stubsMap;
        this.primaryToBackupHandler = PrimaryToBackupHandler;
        this.primaryToStandardHandler = PrimaryToStandardHandler;
        this.standardToPrimaryHandler = StandardToPrimaryHandler;
        this.standardToBackupHandler = StandardToBackupHandler;
    }

    @Override
    public void run() {
        System.out.printf("%s Ping: %d \n", this.proposerType, this.stubsMap.size());
        if (this.proposerType.equals(PlayerType.Primary)) {
            for (Map.Entry<String, GameInterface> stubEntry : this.stubsMap.entrySet()) {
                System.out.printf("Primary Ping: Name - %s\n", stubEntry.getKey());
                if (!stubEntry.getValue().equals(this.primaryStub)) {
                    if (!new PingMaster(stubEntry.getValue()).isReachable()) {
                        System.out.printf("Primary Ping Fail: Name - %s\n", stubEntry.getKey());
                        if (stubEntry.getValue().equals(this.backupStub)) {
                            this.primaryToBackupHandler.handle();
                        } else {
                            this.primaryToStandardHandler.handleWithPlayerName(stubEntry.getKey());
                        }
                    }
                }
            }
        } else if (this.proposerType.equals(PlayerType.Standard)) {
            for (Map.Entry<String, GameInterface> stubEntry : this.stubsMap.entrySet()) {
                System.out.printf("Standard Ping: Name - %s\n", stubEntry.getKey());
                if (stubEntry.getValue().equals(this.primaryStub)) {
                    if (!new PingMaster(stubEntry.getValue()).isReachable()) {
                        System.out.printf("Standard Ping Fail: Name - %s\n", stubEntry.getKey());
                        this.standardToPrimaryHandler.handle();
                    }
                } else if (stubEntry.getValue().equals(this.backupStub)) {
                    if (!new PingMaster(stubEntry.getValue()).isReachable()) {
                        System.out.printf("Standard Ping Fail: Name - %s\n", stubEntry.getKey());
                        this.standardToBackupHandler.handle();
                    }
                }
            }
        }
    }
}
