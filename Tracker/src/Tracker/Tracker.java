package Tracker;

//import Common.*;

import java.rmi.Remote;
import java.util.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Semaphore;

public class Tracker implements TrackerInterface {

    private int N = 0;
    private int K = 0;
    private List<EndPoint> EndPointList;
    private Semaphore semaphore = new Semaphore(1);


    public Tracker(){
        EndPointList = new List<EndPoint>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public Iterator<EndPoint> iterator() {
                return null;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return null;
            }

            @Override
            public boolean add(EndPoint endPoint) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends EndPoint> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends EndPoint> c) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public EndPoint get(int index) {
                return null;
            }

            @Override
            public EndPoint set(int index, EndPoint element) {
                return null;
            }

            @Override
            public void add(int index, EndPoint element) {

            }

            @Override
            public EndPoint remove(int index) {
                return null;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @Override
            public ListIterator<EndPoint> listIterator() {
                return null;
            }

            @Override
            public ListIterator<EndPoint> listIterator(int index) {
                return null;
            }

            @Override
            public List<EndPoint> subList(int fromIndex, int toIndex) {
                return null;
            }
        };
    }

    public Tracker(int t_k, int t_n){
        K = t_k;
        N = t_n;
        EndPointList = new List<EndPoint>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public Iterator<EndPoint> iterator() {
                return null;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return null;
            }

            @Override
            public boolean add(EndPoint endPoint) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends EndPoint> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends EndPoint> c) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public EndPoint get(int index) {
                return null;
            }

            @Override
            public EndPoint set(int index, EndPoint element) {
                return null;
            }

            @Override
            public void add(int index, EndPoint element) {

            }

            @Override
            public EndPoint remove(int index) {
                return null;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @Override
            public ListIterator<EndPoint> listIterator() {
                return null;
            }

            @Override
            public ListIterator<EndPoint> listIterator(int index) {
                return null;
            }

            @Override
            public List<EndPoint> subList(int fromIndex, int toIndex) {
                return null;
            }
        };
    }

    public List<EndPoint> registerNewPlayer(String IP, int Port){

        try {
            semaphore.acquire();
            try {
                EndPoint newOne = new EndPoint(IP, Port);
                EndPointList.add(newOne);
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return EndPointList;
    }

    public boolean resetTrackerList(List<EndPoint> updatedList){
        boolean result = true;

        try {
            semaphore.acquire();

            try {
                EndPointList.clear();
                EndPointList = updatedList;
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException e){
            result = false;
            e.printStackTrace();
        }

        return result;
    }

    public int getK() {
        return K;
    }

    public int getN() {
        return N;
    }

    public List<EndPoint> getEndPointList() {
        List<EndPoint> temp = null;
        try {
            semaphore.acquire();
            try {
                temp = EndPointList;
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return temp;
    }

    public void setK(int t_k){
        K = t_k;
    }

    public void setN(int t_n){
        N = t_n;
    }

    public static void main(String args[]) {
        Remote stub = null;
        Registry registry = null;
        int p = 0;
        int N = 0;
        int K = 0;

        if (args.length == 3) {
            try {
                p = Integer.parseInt(args[0]);
                N = Integer.parseInt(args[1]);
                K = Integer.parseInt(args[2]);

            } catch (NumberFormatException e) {
                return ;
            }
        }

        try {
            Tracker obj = new Tracker(N, K);

            stub = (TrackerInterface) UnicastRemoteObject.exportObject(obj, p);
            registry = LocateRegistry.getRegistry();
            registry.bind("Tracker", stub);

            System.err.println("Tracker ready");
        } catch (Exception e) {
            try{
                registry.unbind("Tracker");
                registry.bind("Tracker",stub);
                System.err.println("Server ready");
            }catch(Exception ee){
                System.err.println("Server exception: " + ee.toString());
                ee.printStackTrace();
            }
        }

        return ;
    }
}

