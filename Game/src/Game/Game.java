package Game;
import Common.EndPoint;
import Game.Player.PlayerType;

import java.util.*;

public class Game implements GameInterface{
    //const variable
    public static int MazeSize = 15;
    public static int TreasureSize = 10;

    //game state binded to UI
    private GameState gameState = new GameState();

    //local layer data
    private String id;
    private PlayerType type = PlayerType.Standard;
    private EndPoint localIp;

    //primary server and backup server address
    private EndPoint primaryIp;
    private EndPoint backupIp;

    public static void main(String args[]) {

        //get local IP and ports
        //try contact Tracker
            //success, return with list of player Ip
                //try contact with Play Ip(from last one) to get primary & backup
                    //fail, no one in the game, become primary server, setup game
                    //success, try to contact primary to join game
                        //fail, exit
                        //success, update local game state
            //fail, exit

        //Get input from IO, loop
            // '9' observed, exit
            // non '9' observed, try makeMove
                //Case 1: Standard
                    //Try to call primary to makeMove
                        //timeout retry
                        //fail, send getPrimaryAndBackupIp to backup stored, update Ips, call (new)server to makeMove
                        //success, update local game state
                //Case 2: Primary
                    //update locally, try to call backup to updateBackupGameState
                        //fail, select an Standard player to be new backup, update BackupIp, gameState
                        //success, do nothing
                //Case 3: Backup
                    //Try to call primary to makeMove
                        //fail, updateToBeNewPrimary, makeMove locally, then select an Standard player to be new backup,

        //Ping (only do when no operation execute locally) (N-1)+ 1 + (N-1)*2 -> O(N)
        //Ping primary server every 0.5s, (primary no need ping itself)
            //fail,wait for 1s, send getPrimaryAndBackupIp to backup stored, update Ips
            //success do nothing
        //Ping Backup every 0.5s, (backup no need ping itself)
            //fail,wait for 1s, send getPrimaryAndBackupIp to primary stored, update Ips
            //success do nothing
        //(Primary Only) Ping standard player one by one
            //fail, remove from list, update gameState
            //success do nothing

    }

    //initallize game elements
    private void setupGame(){}

    //update gameState after get latest gameState from primary server
    private void updateGameState(){}

    private void updateToBeNewPrimary(boolean isFirstPlayer){
        if(isFirstPlayer || type == PlayerType.Backup)
        {
            type = PlayerType.Primary;
            primaryIp = localIp;
        }
    }

    private void updateToBeNewBackup(){
        if(type == PlayerType.Standard)
        {
            type = PlayerType.Backup;
            backupIp = localIp;
        }
    }


    public GameState makeMove() {return gameState;}

    public GameState join() {return gameState;}

    public void becomeBackup(GameState gs) {}

    public List<EndPoint> getPrimaryAndBackupIp(){
        List<EndPoint> result = new ArrayList<EndPoint>();
        result.add(primaryIp);
        result.add(backupIp);

        return result;
    }

    public void updateBackupGameState(GameState gs)
    {}
}
