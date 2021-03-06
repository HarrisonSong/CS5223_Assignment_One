import java.rmi.RemoteException;

public class PingMaster {
    private GameInterface endPointStub;

    public PingMaster(GameInterface stub) {
        this.endPointStub = stub;
    }

    public boolean isReachable() {
        try {
            this.endPointStub.isAlive();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
}
