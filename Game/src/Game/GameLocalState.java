package Game;

import Common.EndPoint;
import Game.Player.PlayerType;

import java.util.HashMap;
import java.util.Map;

public class GameLocalState {

    private String playName;
    private PlayerType playerType;

    private EndPoint localEndPoint;
    private EndPoint primaryEndPoint;
    private EndPoint backupEndPoint;
    private EndPoint trackerEndPoint;

    private Map<String, EndPoint> playerEndPointsMap;

    public GameLocalState() {
        playName = new String(new char[Game.NAME_LENGTH]);
        playerType = PlayerType.Standard;
        localEndPoint = new EndPoint();
        primaryEndPoint = new EndPoint();
        backupEndPoint = new EndPoint();
        trackerEndPoint = new EndPoint();
        this.playerEndPointsMap = new HashMap<>();
    }

    public String getPlayName() {
        return playName;
    }

    public void setPlayName(String name) {
        this.playName = name;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
    }

    public EndPoint getLocalEndPoint() {
        return localEndPoint;
    }

    public void setLocalEndPoint(EndPoint localEndPoint) {
        this.localEndPoint = localEndPoint;
    }

    public EndPoint getPrimaryEndPoint() {
        return primaryEndPoint;
    }

    public void setPrimaryEndPoint(EndPoint primaryEndPoint) {
        this.primaryEndPoint = primaryEndPoint;
    }

    public EndPoint getBackupEndPoint() {
        return backupEndPoint;
    }

    public void setBackupEndPoint(EndPoint backupEndPoint) {
        this.backupEndPoint = backupEndPoint;
    }

    public EndPoint getTrackerEndPoint() {
        return trackerEndPoint;
    }

    public Map<String, EndPoint> getPlayerEndPointsMap() {
        return playerEndPointsMap;
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
