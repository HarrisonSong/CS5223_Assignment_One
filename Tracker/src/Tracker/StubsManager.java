package Tracker;

import Interface.GameInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StubsManager {
    private Map<String, GameInterface> stubsMap;
    private ReadWriteLock readWritelock = new ReentrantReadWriteLock();

    public StubsManager() {
        this.stubsMap = new HashMap<>();
    }

    public boolean addNewStub(String playName, GameInterface stub){
        boolean isSuccessfullyAdded;
        readWritelock.writeLock().lock();
        try {
            this.stubsMap.put(playName, stub);
            isSuccessfullyAdded = true;
        } finally {
            readWritelock.writeLock().unlock();
        }
        return isSuccessfullyAdded;
    }

    public boolean updateStubsMap(Map<String, GameInterface> newMap){
        boolean isSuccessfullyUpdated;
        readWritelock.writeLock().lock();
        try {
            this.stubsMap.clear();
            this.stubsMap = newMap;
            isSuccessfullyUpdated = true;
        } finally {
            readWritelock.writeLock().unlock();
        }
        return isSuccessfullyUpdated;
    }

    public Map<String, GameInterface> getStubsMap(){
        Map<String, GameInterface> endPointMap;
        readWritelock.readLock().lock();
        try {
            endPointMap = this.stubsMap;
        } finally {
            readWritelock.readLock().unlock();
        }
        return endPointMap;
    }

    public boolean isPlayerNameUsed(String playName){
        boolean isUsed = false;
        readWritelock.readLock().lock();
        try {
            for(String name : this.stubsMap.keySet()){
                if(name.equals(playName)){
                    isUsed = true;
                    break;
                }
            }
        } finally {
            readWritelock.readLock().unlock();
        }
        return isUsed;
    }
}
