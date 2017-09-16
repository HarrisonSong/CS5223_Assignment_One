package Tracker;

import Common.EndPoint;

import java.rmi.Remote;
import java.util.*;

public class tracker implements Remote {

    private int N = 0;
    private int K = 0;
    private int myPortNum = 0;
    private List<EndPoint> EndPointList;

    public tracker(){}

    public tracker(int Port){
        myPortNum = Port;
    }

    public void addEndPoint(String IP, int Port){
        EndPoint newOne = new EndPoint(IP, Port);
        EndPointList.add(newOne);
    }

    public void resetEndPointList(List<EndPoint> updatedList){
        EndPointList.clear();
        EndPointList = updatedList;
    }

    public int getK() {
        return K;
    }

    public int getN() {
        return N;
    }

    public List<EndPoint> getEndPointList() {
        return EndPointList;
    }

    public static void main(String args[]) {

    }
}
