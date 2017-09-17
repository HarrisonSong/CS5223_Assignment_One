package Game;
import Common.*;
import Game.Player.Command;
import Game.Player.Player;
import Game.Player.PlayerType;
import com.sun.xml.internal.bind.v2.model.core.ID;

import java.util.*;

public class Game implements GameInterface{
    //const variable
    public static int IdLength = 2;
    public static int MazeSize = 15;
    public static int TreasureSize = 10;

    //game state binded to UI
    private GameState gameState = new GameState();

    //local layer data
    private char[] id = new char[2];
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

    //initallize game elements at first player join
    private boolean setupGame(String inputId){
        if(setLocalPlayerID(inputId))//set ID
        {
            type = PlayerType.Primary; //set type
            primaryIp = localIp;//set primary IP
            Player player = new Player(id, new Pair(Game.MazeSize-1),0, type);
            gameState.initialize(player);//initialize game state with one player
        }
        else
        {
            return false;
        }
        return true;
    }

    //update gameState after get latest gameState from primary server
    private void updateGameState(GameState gs){
        this.gameState.setPlayerList(gs.getPlayerList());
        this.gameState.setTreasureLocation(gs.getTreasureLocation());
    }

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

    public GameState makeMove(Command cmd, char[] id) {
        if(type == PlayerType.Primary){
            if(!gameState.makeMove(cmd,id)) return null;
        }
        return gameState;
    }

    public GameState join(EndPoint Ip, char[] id){
        if(gameState.getPlayerList().size() == 0){
            if(!gameState.addNewPlayerById(id, PlayerType.Primary)){return null;}
        }
        if(gameState.getPlayerList().size() == 1){
            if(!gameState.addNewPlayerById(id, PlayerType.Backup)){return null;}
        }
        else {
            if(!gameState.addNewPlayerById(id, PlayerType.Standard)){return null;}
        }
        return gameState;
    }

    //Standard ans Backup user true, else false
    public boolean becomeBackup(GameState gs) {
        if(type == PlayerType.Standard || type == PlayerType.Backup){
            type = PlayerType.Backup;
            backupIp = localIp;
            updateGameState(gs);
            return true;
        }
        else{return false;}

    }

    public List<EndPoint> getPrimaryAndBackupIp(){
        List<EndPoint> result = new ArrayList<EndPoint>();
        result.add(primaryIp);
        result.add(backupIp);

        return result;
    }

    //Backup user return true, others return false
    public boolean updateBackupGameState(GameState gs){
        if(type != PlayerType.Backup){return false;}
        else{
            updateGameState(gs);
            return true;
        }
    }

    //Helper function
    //convert input String Id to char[] , and store to ip
    private boolean setLocalPlayerID(String inputID ) {
        if(inputID.length() != IdLength){return false;}
        for(int i=0; i<IdLength; i++)
        {
            id[i] = inputID.charAt(i);
        }
        return true;
    }

    private Command charToCommandConvert(char inputChar){

        Command result = Command.Invlid;
        switch (inputChar){
            case '0':
                result = Command.Refresh;
                break;
            case '1':
                result = Command.West;
                break;
            case '2':
                result = Command.South;
                break;
            case '3':
                result = Command.East;
                break;
            case '4':
                result = Command.North;
                break;
            case '9':
                result = Command.Exit;
                break;
            default: break;
        }
        return result;
    }


}
