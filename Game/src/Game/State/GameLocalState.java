package Game.State;
import Game.Game;
import Game.Player.PlayerType;
import Interface.GameInterface;
import Interface.TrackerInterface;

import java.util.HashMap;
import java.util.Map;

public class GameLocalState {

    private PlayerType playerType;
    private String name;

    private GameInterface localStub = null;
    private GameInterface primaryStub = null;
    private GameInterface backupStub = null;
    private TrackerInterface trackerStub = null;

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

//    public void setLocalStub(String playerName, GameInterface localStub) {
//        this.localStub = new NameStubPair(playerName, localStub);
//    }

    public void setLocalStub(GameInterface stub){
        this.localStub = stub;
    }

    public GameInterface getPrimaryStub() {
        return primaryStub;
    }

//    public void setPrimaryStub(String playerName, GameInterface primaryStub) {
//        this.primaryStub = new NameStubPair(playerName, primaryStub);
//    }

    public void setPrimaryStub(GameInterface stub){
        this.primaryStub = stub;
    }

    public GameInterface getBackupStub() {
        return backupStub;
    }

//    public void setBackupStub(String playerName, GameInterface backupStub) {
//        this.backupStub = new NameStubPair(playerName, backupStub);
//    }

    public void setBackupStub(GameInterface stub){
        this.backupStub = stub;
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

    public String getPlayerNameByStub(GameInterface targetStub) {
        for(Map.Entry<String, GameInterface> stub : this.playerStubsMap.entrySet()){
            if(stub.getValue().equals(targetStub)){
                return stub.getKey();
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

    public void removePlayerStubByName(String playName){
        this.playerStubsMap.remove(playName);
    }
}
