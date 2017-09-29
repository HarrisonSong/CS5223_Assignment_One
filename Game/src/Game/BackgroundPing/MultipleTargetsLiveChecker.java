package Game.BackgroundPing;

import Common.NameTypePair;
import Game.Player.Player;
import Game.Player.PlayerType;
import Interface.GameInterface;

import java.util.Map;

public class MultipleTargetsLiveChecker implements Runnable {

    private PlayerType proposerType;
    private Map<NameTypePair, GameInterface> stubsMap;

    private HandlerInterface primaryToBackupHandler;
    private HandlerWithPlayerNameInterface primaryToStandardHandler;

    private  HandlerInterface standardToPrimaryHandler;
    private  HandlerInterface standardToBackupHandler;

    public MultipleTargetsLiveChecker(
            PlayerType proposerType,
            Map<NameTypePair, GameInterface> stubsMap,
            HandlerInterface PrimaryToBackupHandler,
            HandlerWithPlayerNameInterface PrimaryToStandardHandler,
            HandlerInterface StandardToPrimaryHandler,
            HandlerInterface StandardToBackupHandler) {
        this.proposerType = proposerType;
        this.stubsMap = stubsMap;
        this.primaryToBackupHandler = PrimaryToBackupHandler;
        this.primaryToStandardHandler = PrimaryToStandardHandler;
        this.standardToPrimaryHandler = StandardToPrimaryHandler;
        this.standardToBackupHandler = StandardToBackupHandler;
    }

    @Override
    public void run() {
        for(Map.Entry<NameTypePair, GameInterface> stubEntry : this.stubsMap.entrySet()){
            PingMaster pingMaster = new PingMaster(stubEntry.getValue());
            System.out.printf("%s Ping: \n Name - %s\n", this.proposerType, stubEntry.getKey());
            if(!pingMaster.isReachable()){
                try {
                    if(this.proposerType.equals(PlayerType.Primary)){
                        System.out.printf(
                                "Primary Ping Fail: \n" +
                                "Name - %s\n", stubEntry.getKey());
                        if(stubEntry.getKey().getPlayerType().equals(PlayerType.Backup)){
                            this.primaryToBackupHandler.handle();
                        }else{
                            this.primaryToStandardHandler.handleWithPlayerName(stubEntry.getKey().getPlayerName());
                        }
                    }else if(this.proposerType.equals(PlayerType.Standard)){
                        System.out.printf(
                                "Standard Ping Fail: \n" + "Name - %s\n", stubEntry.getKey());
                        if(stubEntry.getKey().getPlayerType().equals(PlayerType.Backup)){
                            this.standardToBackupHandler.handle();
                        }else{
                            this.standardToPrimaryHandler.handle();
                        }
                    }
                } catch (Exception e) {
                    System.err.printf("background multiple ping error %s \n", e.getMessage());
                }
            }
        }
    }
}
