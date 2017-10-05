import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameGlobalState implements Serializable {

    private int mazeSize;
    private int treasuresSize;

    private Map<String, Player> playersMap;
    private Map<String, GameInterface> playerStubsMap;
    private List<MazePair> treasuresLocation;

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
        this.playersMapLock.writeLock().lock();
        this.treasuresLocationLock.writeLock().lock();
        try{
            Player targetPlayer = this.playersMap.get(playerName);
            MazePair currentLocation = targetPlayer.getCurrentPosition();
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
                if(treasures != -1){
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

    public void resetAllStates(Map<String, Player> playersMap, Map<String, GameInterface> playerStubsMap, List<MazePair> treasuresLocation){

        System.out.println("Reset all states ----------------------");
        this.playersMapLock.writeLock().lock();
        this.playerStubsMapLock.writeLock().lock();
        this.treasuresLocationLock.writeLock().lock();
        try {
            this.playersMap = playersMap;
            this.playerStubsMap = playerStubsMap;
            this.treasuresLocation = treasuresLocation;
        } finally {
            this.treasuresLocationLock.writeLock().unlock();
            this.playerStubsMapLock.writeLock().unlock();
            this.playersMapLock.writeLock().unlock();
        }
        changeSupport.firePropertyChange("PlayersMap", 1, 2);
    }

    public boolean addPlayer(String playerName, PlayerType type, GameInterface stub){
        this.playersMapLock.writeLock().lock();
        this.playerStubsMapLock.writeLock().lock();
        try {
            if(this.playersMap.containsKey(playerName)) return false;
            while(true) {
                MazePair newLocation = new MazePair(this.mazeSize);
                if (!isLocationAccessible(newLocation) || hasTreasureLocatedAt(newLocation)) continue;
                Player newPlayer = new Player(playerName, newLocation, 0, type);
                newPlayer.showWhereIAm();
                this.playersMap.put(playerName, newPlayer);
                this.playerStubsMap.put(playerName, stub);

                changeSupport.firePropertyChange("PlayersMap", 1, 2);
                return true;
            }
        } finally {
            this.playerStubsMapLock.writeLock().unlock();
            this.playersMapLock.writeLock().unlock();
        }
    }

    public boolean removePlayerByName(String playerName){
        this.playersMapLock.writeLock().lock();
        this.playerStubsMapLock.writeLock().lock();
        try {
            if(!this.playersMap.containsKey(playerName)) return false;

            this.playersMap.remove(playerName);
            this.playerStubsMap.remove(playerName);

            changeSupport.firePropertyChange("PlayersMap", 1, 2);
            return true;
        } finally {
            this.playerStubsMapLock.writeLock().unlock();
            this.playersMapLock.writeLock().unlock();
        }
    }

    /*** PlayerMap methods ***/

    public void updateServerPlayerType(GameInterface primaryStub, GameInterface backupStub){
        String updatedPrimaryName = this.getPlayerNameByStub(primaryStub);
        String updatedBackupName = this.getPlayerNameByStub(backupStub);
        this.updatePlayerType(updatedPrimaryName, PlayerType.Primary);
        this.updatePlayerType(updatedBackupName, PlayerType.Backup);
    }

    public boolean updatePlayerType(String playerName, PlayerType type){
        this.playersMapLock.writeLock().lock();
        try {
            if(!this.playersMap.containsKey(playerName)) return false;
            this.playersMap.get(playerName).setType(type);
            changeSupport.firePropertyChange("PlayersMap", 1, 2);
            return true;
        } finally {
            this.playersMapLock.writeLock().unlock();
        }
    }

    public Map<String, Player> getPlayersMap() {
        this.playersMapLock.readLock().lock();
        try {
            return playersMap;
        } finally {
            this.playersMapLock.readLock().unlock();
        }
    }

    public Map<String, Player> getPlayersMapCopy() {
        this.playersMapLock.readLock().lock();
        try {
            return new HashMap<>(playersMap);
        } finally {
            this.playersMapLock.readLock().unlock();
        }
    }

    public String getNameOfSpecialType(PlayerType type) {
        if(type.equals(PlayerType.Standard)){
            return null;
        }
        this.playersMapLock.readLock().lock();
        try {
            Iterator<Map.Entry<String, Player>> iterator = playersMap.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<String, Player> entry = iterator.next();
                if(entry.getValue().getType().equals(type)){
                    return entry.getKey();
                }
            }
            return null;
        } finally {
            this.playersMapLock.readLock().unlock();
        }
    }

    /*** treasures methods ***/

    public List<MazePair> getTreasuresLocation() {
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

    public void setPlayerStubsMap(Map<String, GameInterface> playerStubsMap) {
        this.playerStubsMapLock.writeLock().lock();
        try {
            this.playerStubsMap = playerStubsMap;
        } finally {
            this.playerStubsMapLock.writeLock().unlock();
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

    /*** Helper methods ***/

    private boolean isLocationAccessible(MazePair location) {
        return location.isValid() && !doesPlayerExistAtLocation(location);
    }

    private boolean hasTreasureLocatedAt(MazePair mazePair){
        for(int i = 0; i < this.treasuresLocation.size(); i++){
            if(mazePair.equals(this.treasuresLocation.get(i))){
                return true;
            }
        }
        return false;
    }

    private boolean doesPlayerExistAtLocation(MazePair location) {
        for(Player player : this.playersMap.values()){
            if(player.isAtCell(location)){
                return true;
            }
        }
        return false;
    }

    private int findTreasuresAtLocation(MazePair location) {
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
        MazePair mazePair;
        while(treasuresLocation.size()<= index) {
            treasuresLocation.add(new MazePair(this.mazeSize));
        }
        while(true){
            mazePair = new MazePair(this.mazeSize);
            if(!isLocationAccessible(mazePair) || hasTreasureLocatedAt(mazePair)) continue;
            this.treasuresLocation.set(index, mazePair);
            break;
        }

        this.changeSupport.firePropertyChange("TreasureList",1, 2);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }
}

