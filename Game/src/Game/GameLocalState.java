package Game;
import Game.Player.PlayerType;
import Interface.GameInterface;
import Interface.TrackerInterface;

import java.util.HashMap;
import java.util.Map;

public class GameLocalState {

    private PlayerType playerType;
    private String name;

    private GameInterface localStub = null;
    private GameInterface primaryStub= null;
    private GameInterface backupStub= null;
    private TrackerInterface trackerStub= null;

    private Map<String, GameInterface> playerStubsMap;

    public GameLocalState() {
        playerType = PlayerType.Standard;
        this.playerStubsMap = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
    }

    public GameInterface getLocalStub() {
        return localStub;
    }

    public void setLocalStub(GameInterface localStub) {
        this.localStub = localStub;
    }

    public GameInterface getPrimaryStub() {
        return primaryStub;
    }

    public void setPrimaryStub(GameInterface primaryStub) {
        this.primaryStub = primaryStub;
    }

    public GameInterface getBackupStub() {
        return backupStub;
    }

    public void setBackupStub(GameInterface backupStub) {
        this.backupStub = backupStub;
    }

    public TrackerInterface getTrackerStub() {
        return trackerStub;
    }

    public void setTrackerStub(TrackerInterface trackerStub){
        this.trackerStub = trackerStub;
    }

    public Map<String, GameInterface> getPlayerStubsMap() {
        return playerStubsMap;
    }

    public String getPlayerByStub(GameInterface targetStub) {
        for(Map.Entry<String, GameInterface> Stub : this.playerStubsMap.entrySet()){
            if(Stub.equals(targetStub)){
                return Stub.getKey();
            }
        }
        return "";
    }

    public void setPlayerStubsMap(Map<String, GameInterface> playerStubsMap) {
        this.playerStubsMap = playerStubsMap;
    }

    public void addPlayerStub(String playName, GameInterface Stub){
        this.playerStubsMap.put(playName, Stub);
    }

    public void removePlayerStub(String playName){
        this.playerStubsMap.remove(playName);
    }
}
