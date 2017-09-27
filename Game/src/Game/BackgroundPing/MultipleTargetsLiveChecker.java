package Game.BackgroundPing;

import Common.NameTypePair;
import Game.Player.PlayerType;
import Interface.GameInterface;

import java.util.Map;

public class MultipleTargetsLiveChecker implements Runnable {

    private Map<NameTypePair, GameInterface> stubsMap;
    private StandardHandlerInterface standardHandler;
    private BackupHandlerInterface backupHandler;

    public MultipleTargetsLiveChecker(Map<NameTypePair, GameInterface> stubsMap, StandardHandlerInterface standardHandler, BackupHandlerInterface backupHandler) {
        this.stubsMap = stubsMap;
        this.standardHandler = standardHandler;
        this.backupHandler = backupHandler;
    }

    @Override
    public void run() {
        for(Map.Entry<NameTypePair, GameInterface> stubEntry : this.stubsMap.entrySet()){
            PingMaster pingMaster = new PingMaster(stubEntry.getValue());
            if(!pingMaster.isReachable()){
                try {
                    if(stubEntry.getKey().getPlayerType() == PlayerType.Backup){
                        this.backupHandler.handleBackupUnavailability();
                    }else{
                        this.standardHandler.handleStandardUnavailability(stubEntry.getKey().getPlayerName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
