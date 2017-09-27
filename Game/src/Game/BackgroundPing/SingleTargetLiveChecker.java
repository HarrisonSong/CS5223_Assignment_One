package Game.BackgroundPing;

import Interface.GameInterface;

public class SingleTargetLiveChecker implements Runnable {

    private GameInterface playerStub;
    private PrimaryHandlerInterface unavailableHandler;

    public SingleTargetLiveChecker(GameInterface playerStub, PrimaryHandlerInterface handler) {
        this.playerStub = playerStub;
        this.unavailableHandler = handler;
    }

    @Override
    public void run() {
        PingMaster pingMaster = new PingMaster(this.playerStub);
        if(!pingMaster.isReachable()){
            try {
                this.unavailableHandler.handlePrimaryUnavailability();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
