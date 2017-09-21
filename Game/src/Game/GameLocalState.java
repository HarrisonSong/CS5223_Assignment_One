package Game;

import Common.EndPoint;
import Game.Player.PlayerType;

public class GameLocalState {

    private String playName;
    private PlayerType playerType;

    private EndPoint localEndPoint;
    private EndPoint primaryEndPoint;
    private EndPoint backupEndPoint;
    private EndPoint trackerEndPoint;

    public GameLocalState() {
        playName = new String(new char[Game.NameLength]);
        playerType = PlayerType.Standard;
        localEndPoint = new EndPoint();
        primaryEndPoint = new EndPoint();
        backupEndPoint = new EndPoint();
        trackerEndPoint = new EndPoint();
    }

    public GameLocalState(String playName, PlayerType playerType, EndPoint localEndPoint, EndPoint primaryEndPoint, EndPoint backupEndPoint, EndPoint trackerEndPoint) {
        this.playName = playName;
        this.playerType = playerType;
        this.localEndPoint = localEndPoint;
        this.primaryEndPoint = primaryEndPoint;
        this.backupEndPoint = backupEndPoint;
        this.trackerEndPoint = trackerEndPoint;
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
}
