package Game.State;

import Game.Player.PlayerType;
import Interface.GameInterface;
import Interface.TrackerInterface;

public class GameLocalState {

    private PlayerType playerType;
    private String name;

    private GameInterface localStub = null;
    private GameInterface primaryStub = null;
    private GameInterface backupStub = null;
    private TrackerInterface trackerStub = null;

    public GameLocalState() {
        playerType = PlayerType.Standard;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GameInterface getLocalStub() {
        return localStub;
    }

    public void setLocalStub(GameInterface stub){
        this.localStub = stub;
    }

    public GameInterface getPrimaryStub() {
        return primaryStub;
    }

    public void setPrimaryStub(GameInterface stub){
        this.primaryStub = stub;
    }

    public GameInterface getBackupStub() {
        return backupStub;
    }

    public void setBackupStub(GameInterface stub){
        this.backupStub = stub;
    }

    public TrackerInterface getTrackerStub() {
        return trackerStub;
    }

    public void setTrackerStub(TrackerInterface trackerStub){
        this.trackerStub = trackerStub;
    }
}
