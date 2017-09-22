package Game.BackgroundPing;

import Common.EndPoint;

public class SingleEndPointLiveChecker implements Runnable {

    private EndPoint endPoint;
    private PrimaryHandlerInterface unavailableHandler;

    public SingleEndPointLiveChecker(EndPoint endPoint, PrimaryHandlerInterface handler) {
        this.endPoint = endPoint;
        this.unavailableHandler = handler;
    }

    @Override
    public void run() {
        PingMaster pingMaster = new PingMaster(this.endPoint);
        if(!pingMaster.isReachable()){
            try {
                this.unavailableHandler.handlePrimaryUnavailability();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
