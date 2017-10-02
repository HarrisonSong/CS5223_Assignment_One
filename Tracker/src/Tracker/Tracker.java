package Tracker;

import Interface.GameInterface;
import Interface.TrackerInterface;

import java.util.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;


public class Tracker implements TrackerInterface {

    public static final int NAME_LENGTH = 2;

    private int N;
    private int K;

    private StubsManager stubsManager;

    public Tracker(int treasureNumber, int mazeDimension){
        this.N = mazeDimension;
        this.K = treasureNumber;
        this.stubsManager = new StubsManager();
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
            Tracker tracker = new Tracker(K, N);
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

    public boolean registerNewPlayer(String playName, GameInterface stub){
        if(!this.stubsManager.isPlayerNameUsed(playName) && playName.length() == NAME_LENGTH){
            System.out.println("register success.");
            return this.stubsManager.addNewStub(playName, stub);
        }
        System.out.println("register fail.");
        return false;
    }

    public boolean resetTrackerStubs(Map<String, GameInterface> updatedStubs){
        System.out.println("Successfully update tracker stub map");
        return this.stubsManager.updateStubsMap(updatedStubs);
    }

    public Map serveStubs() {
        return this.stubsManager.getStubsMap();
    }

    public int getK() {
        return this.K;
    }

    public int getN() {
        return this.N;
    }

    /*** End of TrackerInterface Implementation ***/
}

