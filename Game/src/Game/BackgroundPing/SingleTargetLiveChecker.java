package Game.BackgroundPing;

import Game.Player.PlayerType;
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
        System.out.printf("%s Ping: \n", PlayerType.Backup);
        if(!pingMaster.isReachable()){
            try {
                System.err.printf("Backup Ping Fail: \n");
                this.unavailableHandler.handle();
            } catch (Exception e) {
                System.err.printf("background multiple ping error %s \n", e.getMessage());
            }
        }
    }
}
