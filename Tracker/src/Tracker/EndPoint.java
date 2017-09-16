package Tracker;

public class EndPoint {
    private String IPAddr = "";
    private int PortNum = 0;

    public EndPoint(){
        IPAddr = "";
        PortNum = 0;
    };

    public EndPoint(String IPAdd, int Port){
        IPAddr = IPAdd;
        PortNum = Port;
    }

    public EndPoint(String fullPath){
        String[] parts = fullPath.split(":");
        IPAddr = parts[0];
        PortNum = Integer.parseInt(parts[1]);
    }

    public int getPortNum(){
        return PortNum;
    }

    public String getIPAddr() {
        return IPAddr;
    }

    public void setIPAddr(String IPAddr) {
        this.IPAddr = IPAddr;
    }

    public void setPortNum(int portNum) {
        PortNum = portNum;
    }
}
