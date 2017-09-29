package Game.BackgroundPing;

import Interface.GameInterface;

public class SingleTargetLiveChecker implements Runnable {

    private GameInterface playerStub;
    private HandlerInterface unavailableHandler;

    public SingleTargetLiveChecker(GameInterface playerStub, HandlerInterface handler) {
        this.playerStub = playerStub;
        this.unavailableHandler = handler;
    }

    @Override
    public void run() {
        PingMaster pingMaster = new PingMaster(this.playerStub);
        if(!pingMaster.isReachable()){
            try {
                this.unavailableHandler.handle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
