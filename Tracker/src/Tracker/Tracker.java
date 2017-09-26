package Tracker;

import Interface.TrackerInterface;

import java.util.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;


public class Tracker implements TrackerInterface {

    public static final int NAME_LENGTH = 2;

    private int N;
    private int K;
    private StubsMap stubsMap;

    public Tracker(int treasureNumber, int mazeDimension){
        this.K = treasureNumber;
        this.N = mazeDimension;
        this.stubsMap = new Tracker.StubsMap();
    }

    public static void main(String args[]) {
        int port = 0;
        int N = 0;
        int K = 0;
        Registry registry = null;
        TrackerInterface stub = null;

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
        if(System.getSecurityManager() == null){
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Tracker tracker = new Tracker(N, K);
            stub = (TrackerInterface) UnicastRemoteObject.exportObject(tracker, port);
            registry = LocateRegistry.createRegistry(port);

            registry.bind("Tracker", stub);
            System.out.println("Tracker ready");
        } catch (Exception e) {
            try{
                /**
                 * Release the potential existing process
                 * and rebind.
                 */
                e.printStackTrace();
                registry.unbind("Tracker");
                registry.bind("Tracker", stub);
                System.err.println("Server ready");
            }catch(Exception ee){
                System.err.println("Server exception: " + ee.toString());
                ee.printStackTrace();
            }
        }
    }

    /**
     * TRACKER INTERFACE IMPLEMENTATION
     */

    public <T> boolean registerNewPlayer(String IP, int port, String playName, T stub){
        if(!this.stubsMap.isPlayerNameUsed(playName) && playName.length() == NAME_LENGTH){
            System.out.println("register success.");
            return this.stubsMap.addNewEndPoint(playName, IP, port);
        }
        System.out.println("register fail.");
        return false;
    }

    public <T> boolean resetTrackerEndPointsMap(Map<String, T> updatedStubs){
        return this.stubsMap.updateEndPointsMap(updatedStubs);
    }

    public Map retrieveStubs() {
        return this.stubsMap.retrieveEndPointsMap();
    }

    public int getK() {
        return this.K;
    }

    public int getN() {
        return this.N;
    }
}

