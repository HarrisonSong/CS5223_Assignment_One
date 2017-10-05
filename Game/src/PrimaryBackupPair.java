import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PrimaryBackupPair extends Pair<GameInterface, GameInterface> {

    private ReadWriteLock primaryStubLock;
    private ReadWriteLock backupStubLock;

    public PrimaryBackupPair() {
        super(null, null);
        this.primaryStubLock = new ReentrantReadWriteLock();
        this.backupStubLock = new ReentrantReadWriteLock();
    }

    public GameInterface getPrimaryStub() {
        primaryStubLock.readLock().lock();
        try {
            return getA();
        } finally {
            primaryStubLock.readLock().unlock();
        }
    }

    public void setPrimaryStub(GameInterface stub) {
        primaryStubLock.writeLock().lock();
        try {
            setA(stub);
        } finally {
            primaryStubLock.writeLock().unlock();
        }
    }

    public GameInterface getBackupStub() {
        backupStubLock.readLock().lock();
        try {
            return getB();
        } finally {
            backupStubLock.readLock().unlock();
        }
    }

    public void setBackupStub(GameInterface stub) {
        backupStubLock.writeLock().lock();
        try {
            setB(stub);
        } finally {
            backupStubLock.writeLock().unlock();
        }
    }

    public void setBackupStubLockFree(GameInterface stub) {
        setB(stub);
    }


    public ReadWriteLock getBackupStubLock(){
        return this.backupStubLock;
    }
}
