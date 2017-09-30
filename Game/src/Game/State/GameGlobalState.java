package Game.State;

import Common.mazePair;
import Game.Player.Command;
import Game.Player.Player;
import Game.Player.PlayerType;
import Interface.GameInterface;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameGlobalState implements Serializable {
    public static final int LocationExplorerAttemptTime = 10;

    private int mazeSize;
    private int treasuresSize;

    private Map<String, Player> playersMap;
    private Map<String, GameInterface> playerStubsMap;
    private List<mazePair> treasuresLocation;

    private ReadWriteLock playersMapLock;
    private ReadWriteLock playerStubsMapLock;
    private ReadWriteLock treasuresLocationLock;

    PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public GameGlobalState(int mazeSize, int treasuresSize) {
        this.mazeSize = mazeSize;
        this.treasuresSize = treasuresSize;
        this.playersMap = new HashMap<>();
        this.playerStubsMap = new HashMap<>();
        this.treasuresLocation = new ArrayList<>(treasuresSize);
        for(int i = 0; i < treasuresSize; i++) {
            generateNewTreasures(i);
        }

        this.playersMapLock = new ReentrantReadWriteLock();
        this.playerStubsMapLock = new ReentrantReadWriteLock();
        this.treasuresLocationLock = new ReentrantReadWriteLock();
    }

    /**
     * Update player state when receiving movement request
     * @param command
     * @param playerName
     * @return
     *  true: target user is not found.
     *  false: target user is found and proceed movement.
     */
    public boolean makeMove(Command command, String playerName) {
        Map<String, Player> oldPlayerMap = this.playersMap;

        this.playersMapLock.writeLock().lock();
        this.treasuresLocationLock.writeLock().lock();
        try{
            Player targetPlayer = this.playersMap.get(playerName);
            mazePair currentLocation = targetPlayer.getCurrentPosition();
            if(targetPlayer == null) return false;
            switch (command){
                case West:
                    currentLocation.setColumn(currentLocation.getColumn() - 1);
                    break;
                case South:
                    currentLocation.setRow(currentLocation.getRow() - 1);
                    break;
                case East:
                    currentLocation.setColumn(currentLocation.getColumn() + 1);
                    break;
                case North:
                    currentLocation.setRow(currentLocation.getRow() + 1);
                    break;
                default:
                    break;
            }
            if(isLocationAccessible(currentLocation)){
                targetPlayer.setCurrentPosition(currentLocation);
                targetPlayer.showWhereIAm();
                int treasures = findTreasuresAtLocation(currentLocation);
                if(treasures!=-1){
                    targetPlayer.setScore(targetPlayer.getScore() + 1);
                    generateNewTreasures(treasures);
                }
            }
        } finally {
            this.treasuresLocationLock.writeLock().unlock();
            this.playersMapLock.writeLock().unlock();
        }

        changeSupport.firePropertyChange("PlayersMap", 1,2);
        return true;
    }

    public void resetAllStates(Map<String, Player> playersMap, Map<String, GameInterface> playerStubsMap, List<mazePair> treasuresLocation){

        System.out.println("Reset all states ----------------------");
        this.playersMapLock.writeLock().lock();
        this.playerStubsMapLock.writeLock().lock();
        this.treasuresLocationLock.writeLock().lock();
        try {
            this.playersMap = playersMap;
            this.playerStubsMap = playerStubsMap;
            this.treasuresLocation = treasuresLocation;
        } finally {
            this.playersMapLock.writeLock().unlock();
            this.playerStubsMapLock.writeLock().unlock();
            this.treasuresLocationLock.writeLock().unlock();
        }
        changeSupport.firePropertyChange("PlayersMap", 1, 2);
        //changeSupport.firePropertyChange("TreasureList", 1, 2);

    }

    /*** PlayerMap methods ***/

    public boolean updatePlayerType(String playerName, PlayerType type){
        Map<String, Player> oldPlayerMap = new HashMap<>(this.playersMap);


        this.playersMapLock.writeLock().lock();
        if(!this.playersMap.containsKey(playerName)) return false;
        try {
            this.playersMap.get(playerName).setType(type);
        } finally {
            this.playersMapLock.writeLock().unlock();
        }
        changeSupport.firePropertyChange("PlayersMap", 1, 2);
        return true;
    }

    public boolean addNewPlayerWithName(String playerName, PlayerType type){
        Map<String, Player> oldPlayerMap = new HashMap<>(this.playersMap);
        this.playersMapLock.writeLock().lock();
        if(this.playersMap.containsKey(playerName)) return false;
        try {
            while(true) {
                mazePair newLocation = new mazePair(this.mazeSize);
                if (!isLocationAccessible(newLocation) || hasTreasureLocatedAt(newLocation)) continue;
                Player newPlayer = new Player(playerName, newLocation, 0, type);
                newPlayer.showWhereIAm();
                this.playersMap.put(playerName, newPlayer);
                return true;
            }
        } finally {
            changeSupport.firePropertyChange("PlayersMap", 1, 2);
            this.playersMapLock.writeLock().unlock();
        }

    }

    public boolean removePlayerByName(String playerName){
        Map<String, Player> oldPlayerMap = new HashMap<>(this.playersMap);
        this.playersMapLock.writeLock().lock();
        if(!this.playersMap.containsKey(playerName)) return false;
        try {
            this.playersMap.remove(playerName);
        } finally {
            this.playersMapLock.writeLock().unlock();
        }
        changeSupport.firePropertyChange("PlayersMap", 1, 2);
        return true;
    }

    public Map<String, Player> getPlayersMap() {
        this.playersMapLock.readLock().lock();
        try {
            return playersMap;
        } finally {
            this.playersMapLock.readLock().unlock();
        }
    }

    /*** PlayerStubMap methods ***/

    public List<mazePair> getTreasuresLocation() {
        this.treasuresLocationLock.readLock().lock();
        try {
            return this.treasuresLocation;
        } finally {
            this.treasuresLocationLock.readLock().unlock();
        }
    }

    /*** PlayerStubMap methods ***/

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

    /*** Helper methods ***/

    private boolean isLocationAccessible(mazePair location) {
        return location.isValid() && !doesPlayerExistAtLocation(location);
    }

    private boolean hasTreasureLocatedAt(mazePair mp){
        for(int i = 0; i < this.treasuresLocation.size(); i++){
            if(mp.equals(this.treasuresLocation.get(i))){
                return true;
            }
        }
        return false;
    }

    private boolean doesPlayerExistAtLocation(mazePair location) {
        for(Player player : this.playersMap.values()){
            if(player.isAtCell(location)){
                return true;
            }
        }
        return false;
    }

    private int findTreasuresAtLocation(mazePair location) {
        int result = -1;
        for(int i = 0; i < this.treasuresSize; i++) {
            if(treasuresLocation.get(i).equals(location)) {
                result = i;
                break;
            }
        }
        return result;
    }

    private void generateNewTreasures(int index) {
        List<mazePair> oldTreasureList = this.treasuresLocation;

        mazePair mp;
        while(treasuresLocation.size()<= index) {
            treasuresLocation.add(new mazePair(this.mazeSize));
        }
        while(true){
            mp = new mazePair(this.mazeSize);
            if(!isLocationAccessible(mp) || hasTreasureLocatedAt(mp)) continue;
            this.treasuresLocation.set(index, mp);
            break;

        }

        changeSupport.firePropertyChange("TreasureList",1, 2);

    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }

    public void GameGlobalStateRefreshListener(){
        changeSupport.firePropertyChange("PlayersMap", 1, 2);
        //changeSupport.firePropertyChange("TreasureList", 1, 2);
        System.out.println("HEU");
    }
}

