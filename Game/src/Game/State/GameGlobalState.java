package Game.State;

import Common.mazePair;
import Game.Player.Command;
import Game.Player.Player;
import Game.Player.PlayerType;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameGlobalState implements Serializable {
    public static final int LocationExplorerAttemptTime = 10;

    private int mazeSize;
    private int treasuresSize;

    private Map<String, Player> playersMap;
    private List<mazePair> treasuresLocation;
    private Stack<String> activePlayerQueue;

    private ReadWriteLock playersMapLock;
    private ReadWriteLock treasuresLocationLock;
    private ReadWriteLock activePlayerQueueLock;

    public GameGlobalState(int mazeSize, int treasuresSize) {
        this.mazeSize = mazeSize;
        this.treasuresSize = treasuresSize;
        this.playersMap = new HashMap<>();
        this.treasuresLocation = new ArrayList<>(treasuresSize);
        for(int i = 0; i < treasuresSize; i++) {
            treasuresLocation.add(new mazePair(mazeSize));
        }
        this.activePlayerQueue = new Stack<>();

        this.playersMapLock = new ReentrantReadWriteLock();
        this.treasuresLocationLock = new ReentrantReadWriteLock();
        this.activePlayerQueueLock = new ReentrantReadWriteLock();
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
        this.playersMapLock.writeLock().lock();
        this.treasuresLocationLock.writeLock().lock();
        this.activePlayerQueueLock.writeLock().lock();
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
                List<Integer> treasures = findTreasuresAtLocation(currentLocation);
                if(!treasures.isEmpty()){
                    targetPlayer.setScore(targetPlayer.getScore() + treasures.size());
                    generateNewTreasures(treasures);
                }

                /**
                 * pop up the player to be latest active
                 */
                this.activePlayerQueue.removeElement(playerName);
                this.activePlayerQueue.push(playerName);
            }
        } finally {
            this.treasuresLocationLock.writeLock().unlock();
            this.activePlayerQueueLock.writeLock().unlock();
            this.playersMapLock.writeLock().unlock();
        }

        return true;
    }

    public void resetAllStates(Map<String, Player> playersMap, List<mazePair> treasuresLocation, Stack<String> activePlayerQueue){
        this.playersMapLock.writeLock().lock();
        this.treasuresLocationLock.writeLock().lock();
        this.activePlayerQueueLock.writeLock().lock();
        try {
            this.playersMap = playersMap;
            this.treasuresLocation = treasuresLocation;
            this.activePlayerQueue = activePlayerQueue;
        } finally {
            this.playersMapLock.writeLock().unlock();
            this.treasuresLocationLock.writeLock().unlock();
            this.activePlayerQueueLock.writeLock().unlock();
        }
    }

    public boolean updatePlayerType(String playerName, PlayerType type){
        this.playersMapLock.writeLock().lock();
        if(!this.playersMap.containsKey(playerName)) return false;
        try {
            this.playersMap.get(playerName).setType(type);
        } finally {
            this.playersMapLock.writeLock().unlock();
        }
        return true;
    }

    public boolean addNewPlayerWithName(String playerName, PlayerType type){
        this.playersMapLock.writeLock().lock();
        if(!this.playersMap.containsKey(playerName)) return false;
        try {
            for(int i = 0; i < LocationExplorerAttemptTime; i++) {
                mazePair newLocation = new mazePair(this.mazeSize);
                if(!doesPlayerExistAtLocation(newLocation)){
                    this.playersMap.put(playerName, new Player(playerName, newLocation, 0, type));
                    this.activePlayerQueueLock.writeLock().lock();
                    try {
                        activePlayerQueue.push(playerName);
                    } finally {
                        this.activePlayerQueueLock.writeLock().unlock();
                    }
                    return true;
                }
            }
            return false;
        } finally {
          this.playersMapLock.writeLock().unlock();
        }
    }

    public boolean removePlayerByName(String playerName){
        this.playersMapLock.writeLock().lock();
        if(!this.playersMap.containsKey(playerName)) return false;
        this.activePlayerQueueLock.writeLock().lock();
        try {
            activePlayerQueue.removeElement(playerName);
            this.playersMap.remove(playerName);
        } finally {
            this.playersMapLock.writeLock().unlock();
            this.activePlayerQueueLock.writeLock().unlock();
        }
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

    public List<mazePair> getTreasuresLocation() {
        this.treasuresLocationLock.readLock().lock();
        try {
            return this.treasuresLocation;
        } finally {
            this.treasuresLocationLock.readLock().unlock();
        }
    }

    public Stack<String> getActivePlayerQueue() {
        this.activePlayerQueueLock.readLock().lock();
        try {
            return this.activePlayerQueue;
        } finally {
            this.activePlayerQueueLock.readLock().unlock();
        }
    }

    public String findNextActivePlayerName() {
        this.activePlayerQueueLock.writeLock().lock();
        try {
            return this.activePlayerQueue.size() == 0 ? "" : this.activePlayerQueue.pop();
        } finally {
            this.activePlayerQueueLock.writeLock().unlock();
        }
    }

    /*** Helper methods ***/

    private boolean isLocationAccessible(mazePair location) {
        return location.isValid() && !doesPlayerExistAtLocation(location);
    }

    private boolean doesPlayerExistAtLocation(mazePair location) {
        for(Player player : this.playersMap.values()){
            if(player.isAtCell(location)){
                return true;
            }
        }
        return false;
    }

    private List<Integer> findTreasuresAtLocation(mazePair location) {
        List<Integer> indexList = new ArrayList<>();
        for(int i = 0; i < this.treasuresSize; i++) {
            if(treasuresLocation.get(i).equals(location)) {
                indexList.add(i);
            }
        }
        return indexList;
    }

    private void generateNewTreasures(List<Integer> list) {
        for(int i = 0; i < list.size(); i++){
            this.treasuresLocation.set(list.get(i).intValue(), new mazePair(this.mazeSize));
        }
    }
}

