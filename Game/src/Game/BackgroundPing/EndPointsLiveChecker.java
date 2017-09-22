package Game.BackgroundPing;

import Common.EndPoint;
import Common.Pair.NameTypePair;
import Game.Player.PlayerType;

import java.util.Map;

public class EndPointsLiveChecker implements Runnable {

    private Map<NameTypePair, EndPoint> endPoints;
    private StandardHandlerInterface standardHandler;
    private BackupHandlerInterface backupHandler;

    public EndPointsLiveChecker(Map<NameTypePair, EndPoint> endPoints, StandardHandlerInterface standardHandler, BackupHandlerInterface backupHandler) {
        this.endPoints = endPoints;
        this.standardHandler = standardHandler;
        this.backupHandler = backupHandler;
    }

    @Override
    public void run() {
        for(Map.Entry<NameTypePair, EndPoint> endPoint : this.endPoints.entrySet()){
            PingMaster pingMaster = new PingMaster(endPoint.getValue());
            if(!pingMaster.isReachable()){
                try {
                    if(endPoint.getKey().getPlayerType() == PlayerType.Backup){
                        this.backupHandler.handleBackupUnavailability();
                    }else{
                        this.standardHandler.handleStandardUnavailability(endPoint.getKey().getPlayerName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
