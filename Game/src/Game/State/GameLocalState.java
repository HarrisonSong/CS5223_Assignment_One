package Game.State;

import Common.PrimaryBackupPair;
import Game.Player.PlayerType;
import Interface.GameInterface;
import Interface.TrackerInterface;

public class GameLocalState {

    private PlayerType playerType;
    private String name;

    private GameInterface localStub = null;
    private TrackerInterface trackerStub = null;

    private PrimaryBackupPair primaryBackupPair = new PrimaryBackupPair();

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

    public TrackerInterface getTrackerStub() {
        return trackerStub;
    }

    public void setTrackerStub(TrackerInterface trackerStub){
        this.trackerStub = trackerStub;
    }

    public PrimaryBackupPair getPrimaryBackupPair() {
        return primaryBackupPair;
    }

    public GameInterface getPrimaryStub() {
        return this.primaryBackupPair.getPirmaryStub();
    }

    public void setPrimaryStub(GameInterface stub){
        this.primaryBackupPair.setPrimaryStub(stub);
    }

    public GameInterface getBackupStub() {
        return this.primaryBackupPair.getBackupStub();
    }

    public void setBackupStub(GameInterface stub){
        this.primaryBackupPair.setBackupStub(stub);
    }


}
