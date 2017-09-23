package Game;
import Common.EndPoint;
import Common.Pair.IdEndPointPair;
import Game.Player.PlayerType;

import java.util.HashMap;
import java.util.Map;

public class GameLocalState {

    private PlayerType playerType;

    private IdEndPointPair localEndPoint;
    private IdEndPointPair primaryEndPoint;
    private IdEndPointPair backupEndPoint;
    private IdEndPointPair trackerEndPoint;

    private Map<String, EndPoint> playerEndPointsMap;

    public GameLocalState() {
        playerType = PlayerType.Standard;
        localEndPoint = new IdEndPointPair();
        primaryEndPoint = new IdEndPointPair();
        backupEndPoint = new IdEndPointPair();
        trackerEndPoint = new IdEndPointPair();
        this.playerEndPointsMap = new HashMap<>();
    }

    public String getPlayName() {
        return localEndPoint.getId();
    }

    public void setPlayName(String name) {
        localEndPoint.setId(name);
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
    }

    public IdEndPointPair getLocalEndPoint() {
        return localEndPoint;
    }

    public void setLocalEndPoint(IdEndPointPair localEndPoint) {
        this.localEndPoint = localEndPoint;
    }

    public IdEndPointPair getPrimaryEndPoint() {
        return primaryEndPoint;
    }

    public void setPrimaryEndPoint(IdEndPointPair primaryEndPoint) {
        this.primaryEndPoint = primaryEndPoint;
    }

    public IdEndPointPair getBackupEndPoint() {
        return backupEndPoint;
    }

    public void setBackupEndPoint(IdEndPointPair backupEndPoint) {
        this.backupEndPoint = backupEndPoint;
    }

    public IdEndPointPair getTrackerEndPoint() {
        return trackerEndPoint;
    }

    public Map<String, EndPoint> getPlayerEndPointsMap() {
        return playerEndPointsMap;
    }

    public String getPlayerByEndPoint(EndPoint targetEndpoint) {
        for(Map.Entry<String, EndPoint> endpoint : this.playerEndPointsMap.entrySet()){
            if(endpoint.equals(targetEndpoint)){
                return endpoint.getKey();
            }
        }
        return "";
    }


    public void setPlayerEndPointsMap(Map<String, EndPoint> playerEndPointsMap) {
        this.playerEndPointsMap = playerEndPointsMap;
    }

    public void addPlayerEndPoint(String playName, EndPoint endPoint){
        this.playerEndPointsMap.put(playName, endPoint);
    }

    public void removePlayerEndPoint(String playName){
        this.playerEndPointsMap.remove(playName);
    }
}
