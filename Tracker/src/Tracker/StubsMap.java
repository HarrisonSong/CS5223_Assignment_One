package Tracker;

import Common.EndPoint;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StubsMap {
    private Map<String, EndPoint> endPoints;
    private ReadWriteLock readWritelock = new ReentrantReadWriteLock();

    public StubsMap() {
        this.endPoints = new HashMap<String, EndPoint>();
    }

    public StubsMap(HashMap<String, EndPoint> endPoints) {
        this.endPoints = endPoints;
    }

    public boolean addNewEndPoint(String playName, String IP, int port){
        boolean isSuccessfullyAdded = false;
        readWritelock.writeLock().lock();
        try {
            this.endPoints.put(playName, new EndPoint(IP, port));
            isSuccessfullyAdded = true;
        } finally {
            readWritelock.writeLock().unlock();
        }
        return isSuccessfullyAdded;
    }

    public boolean updateEndPointsMap(Map<String, EndPoint> newMap){
        boolean isSuccessfullyUpdated = false;
        readWritelock.writeLock().lock();
        try {
            this.endPoints.clear();
            this.endPoints = newMap;
            isSuccessfullyUpdated = true;
        } finally {
            readWritelock.writeLock().unlock();
        }
        return isSuccessfullyUpdated;
    }

    public Map<String, EndPoint> retrieveEndPointsMap(){
        Map<String, EndPoint> endPointMap;
        readWritelock.readLock().lock();
        try {
            endPointMap = this.endPoints;
        } finally {
            readWritelock.readLock().unlock();
        }
        return endPointMap;
    }

    public boolean isPlayerNameUsed(String playName){
        boolean isUsed = false;
        readWritelock.readLock().lock();
        try {
            for(String name : this.endPoints.keySet()){
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
