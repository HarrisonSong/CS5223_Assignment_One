package Game.State;

import Game.Player.PlayerType;
import Interface.GameInterface;
import Interface.TrackerInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameLocalState {

    private PlayerType playerType;
    private String name;

    private GameInterface localStub = null;
    private GameInterface primaryStub = null;
    private GameInterface backupStub = null;
    private TrackerInterface trackerStub = null;

    private Map<String, GameInterface> playerStubsMap;
    private ReadWriteLock playerStubsMapLock;

    public GameLocalState() {
        playerType = PlayerType.Standard;
        this.playerStubsMap = new HashMap<>();
        this.playerStubsMapLock = new ReentrantReadWriteLock();
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

    public Map<String, GameInterface> getPlayerStubsMap() {
        this.playerStubsMapLock.readLock().lock();
        try {
            return this.playerStubsMap;
        } finally {
            this.playerStubsMapLock.readLock().unlock();
        }
    }

    public String getPlayerNameByStub(GameInterface targetStub) {
        this.playerStubsMapLock.readLock().lock();
        try {
            for(Map.Entry<String, GameInterface> stub : this.playerStubsMap.entrySet()){
                if(stub.getValue().equals(targetStub)){
                    return stub.getKey();
                }
            }
            return "";
        } finally {
            this.playerStubsMapLock.readLock().unlock();
        }
    }

    public void setPlayerStubsMap(Map<String, GameInterface> playerStubsMap) {
        this.playerStubsMapLock.writeLock().lock();
        try {
            this.playerStubsMap = playerStubsMap;
        } finally {
            this.playerStubsMapLock.writeLock().unlock();
        }
    }

    public void addPlayerStub(String playName, GameInterface Stub){
        this.playerStubsMapLock.writeLock().lock();
        try {
            this.playerStubsMap.put(playName, Stub);
        } finally {
            this.playerStubsMapLock.writeLock().unlock();
        }
    }

    public void removePlayerStubByName(String playName){
        this.playerStubsMapLock.writeLock().lock();
        try {
            this.playerStubsMap.remove(playName);
        } finally {
            this.playerStubsMapLock.writeLock().unlock();
        }
    }
}
