package Game;
import Common.EndPoint;
import Common.Pair.NameEndPointPair;
import Game.Player.PlayerType;

import java.util.HashMap;
import java.util.Map;

public class GameLocalState {

    private PlayerType playerType;

    private NameEndPointPair localEndPoint;
    private NameEndPointPair primaryEndPoint;
    private NameEndPointPair backupEndPoint;
    private NameEndPointPair trackerEndPoint;

    private Map<String, EndPoint> playerEndPointsMap;

    public GameLocalState() {
        playerType = PlayerType.Standard;
        localEndPoint = new NameEndPointPair();
        primaryEndPoint = new NameEndPointPair();
        backupEndPoint = new NameEndPointPair();
        trackerEndPoint = new NameEndPointPair();
        this.playerEndPointsMap = new HashMap<>();
    }

    public String getPlayName() {
        return localEndPoint.getName();
    }

    public void setPlayName(String name) {
        localEndPoint.setName(name);
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
    }

    public NameEndPointPair getLocalEndPoint() {
        return localEndPoint;
    }

    public void setLocalEndPoint(NameEndPointPair localEndPoint) {
        this.localEndPoint = localEndPoint;
    }

    public NameEndPointPair getPrimaryEndPoint() {
        return primaryEndPoint;
    }

    public void setPrimaryEndPoint(NameEndPointPair primaryEndPoint) {
        this.primaryEndPoint = primaryEndPoint;
    }

    public NameEndPointPair getBackupEndPoint() {
        return backupEndPoint;
    }

    public void setBackupEndPoint(NameEndPointPair backupEndPoint) {
        this.backupEndPoint = backupEndPoint;
    }

    public NameEndPointPair getTrackerEndPoint() {
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
