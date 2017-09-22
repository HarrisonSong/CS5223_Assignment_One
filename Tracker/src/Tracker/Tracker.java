package Tracker;

import java.util.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class Tracker implements TrackerInterface {

    public static final int NAME_LENGTH = 2;

    private int N;
    private int K;
    private EndPointsMap endPointsMap;

    public Tracker(int treasureNumber, int mazeDimension){
        this.K = treasureNumber;
        this.N = mazeDimension;
        this.endPointsMap = new EndPointsMap();
    }

    public static void main(String args[]) {
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
            TrackerInterface stub = (TrackerInterface) UnicastRemoteObject.exportObject(tracker, port);
            Registry registry = LocateRegistry.getRegistry();

            /**
             * Release the potential existing process
             * and rebind.
             */
            registry.unbind("Tracker");
            registry.bind("Tracker", stub);

            System.out.println("Tracker ready");
        } catch (Exception e) {
            System.err.println("Tracker exception: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * TRACKER INTERFACE IMPLEMENTATION
     */

    public boolean registerNewPlayer(String IP, int port, String playName){
        if(!this.endPointsMap.isPlayerNameUsed(playName) && playName.length() == NAME_LENGTH){
            return this.endPointsMap.addNewEndPoint(playName, IP, port);
        }
        return false;
    }

    public boolean resetTrackerEndPointsMap(Map updatedMap){
        return this.endPointsMap.updateEndPointsMap(updatedMap);
    }

    public Map<String, EndPoint> retrieveEndPointsMap() {
        return this.endPointsMap.retrieveEndPointsMap();
    }

    public int getK() {
        return this.K;
    }

    public int getN() {
        return this.N;
    }
}

