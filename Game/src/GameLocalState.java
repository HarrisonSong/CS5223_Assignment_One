import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameLocalState {

    private String name;

    private PlayerType playerType;
    private ReadWriteLock playerTypeLock;

    private GameInterface localStub = null;
    private TrackerInterface trackerStub = null;

    private PrimaryBackupPair primaryBackupPair = new PrimaryBackupPair();

    public GameLocalState() {
        this.playerType = PlayerType.Standard;
        this.playerTypeLock = new ReentrantReadWriteLock();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlayerType getPlayerType() {
        this.playerTypeLock.readLock().lock();
        try {
            return this.playerType;
        } finally {
            this.playerTypeLock.readLock().unlock();
        }
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerTypeLock.writeLock().lock();
        try {
            this.playerType = playerType;
        } finally {
            this.playerTypeLock.writeLock().unlock();
        }
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
