package Tracker;

import java.rmi.Remote;
import java.util.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Semaphore;

public class Tracker implements TrackerInterface {

    private int N;
    private int K;
    private Map<String, EndPoint> endPointsMap;
    private Semaphore semaphore = new Semaphore(1);

    public Tracker(){
        this.N = 0;
        this.K = 0;
        this.endPointsMap = new HashMap<String, EndPoint>();
    }

    public Tracker(int treasureNumber, int mazeDimension){
        this.K = treasureNumber;
        this.N = mazeDimension;
        this.endPointsMap = new HashMap<String, EndPoint>();
    }

    public static void main(String args[]) {
        Remote stub = null;
        Registry registry = null;
        int port = 0;
        int N = 0;
        int K = 0;
        if(args.length < 3){
            System.err.println("Not enough parameters.");
            System.exit(0);
            return;
        }

        if (args.length == 3) {
            try {
                port = Integer.parseInt(args[0]);
                N = Integer.parseInt(args[1]);
                K = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.err.println("Failed to get required parameters.");
                System.exit(0);
                return;
            }
        }

        try {
            Tracker tracker = new Tracker(N, K);
            stub = (TrackerInterface) UnicastRemoteObject.exportObject(tracker, port);
            registry = LocateRegistry.getRegistry();
            registry.bind("Tracker", stub);
            System.out.println("Tracker ready");
        } catch (Exception e) {
            try {
                registry.unbind("Tracker");
                registry.bind("Tracker", stub);
                System.out.println("Tracker ready");
            }catch(Exception ee){
                System.err.println("Tracker exception: " + ee.toString());
                ee.printStackTrace();
            }
        }
    }

    /**
     * TRACKER INTERFACE IMPLEMENTATION
     */

    public boolean registerNewPlayer(String IP, int port, String playName){
        boolean isSuccessfullyRegistered = false;
        try {
            semaphore.acquire();
            try {
                if(!isPlayerNameUsed(playName)){
                    this.endPointsMap.put(playName, new EndPoint(IP, port));
                    isSuccessfullyRegistered = true;
                }
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return isSuccessfullyRegistered;
    }

    public boolean resetTrackerEndPointsMap(Map updatedMap){
        boolean isSuccessful = true;
        try {
            semaphore.acquire();
            try {
                this.endPointsMap.clear();
                this.endPointsMap = updatedMap;
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException e){
            isSuccessful = false;
            e.printStackTrace();
        }

        return isSuccessful;
    }

    public Map<String, EndPoint> retrieveEndPointsMap() {
        return this.endPointsMap;
    }

    public int getK() {
        return this.K;
    }

    public int getN() {
        return this.N;
    }

    /**
     * helper method
     */

    private boolean isPlayerNameUsed(String newPlayerName){
        for(String name : this.endPointsMap.keySet()){
            if(name.equals(newPlayerName)){
                return true;
            }
        }
        return false;
    }
}

