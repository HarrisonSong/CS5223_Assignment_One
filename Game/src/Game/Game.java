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

    private EndPoint TrackerIp;

    public static void main(String args[]) {
        // Args: [Tracker IP], [Tracker Port], [Player ID]

        /* Init Server */
        // Set up Tracker Connection

        // Get local IP and ports

        // Try to contact Tracker
            // Success, return with list of player Ip
                // If the list is empty, become primary server, setup game (No one in the list) -- END
                // Else,
                    // For LOOP: try contact with Play Ip(starting at the last one)
                        // All IPs in the list fail to contact, then become primary server, setup game (No alive one in the list) -- END
                        // Any one success, get Primary & backup, break the for loop
                    // While LOOP: while (Primary IP is not null):
                        // If contact primary server to join game
                            // Fail[primary crash],
                                // If backup IP is null, become primary server, setup game (No alive one in the list) -- END
                                // Else, send getPrimaryAndBackupIp to backup server, update IPs, get new primary and backup
                                    // Success,
                                    // Fail, no server alive, setup itself as primary, and setup game -- END
                            // Success, break the while loop: return game state and current Primary & Backup. If backup == itself, setup its self to backup -- END
            // Fail, exit with error


        /* Play Mode */
        // Get input from IO, loop
            // '9' observed, exit
            // non '9' observed,
                // Case 1: Standard
                    //Try to call primary to makeMove
                        //timeout retry
                        //fail, send getPrimaryAndBackupIp to backup stored, update Ips, call (new)server to makeMove (while loop?)
                        //success, update local game state
                // Case 2: Primary
                    //Update locally, try to call backup to updateBackupGameState
                        // Fail, select an Standard player to be new backup, update BackupIp, gameState (while loop?)
                        // Success, do nothing
                // Case 3: Backup
                    //Try to call primary to makeMove
                        // fail, updateToBeNewPrimary, makeMove locally, then select an Standard player to be new backup and (while loop?)
                        // update backup's game state
                        // Success





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

    // Primary Only
    // Initialize game elements as Primary Server
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

    // Client function
    // Update gameState after get latest gameState from primary server
    private void updateGameState(GameState gs){
        this.gameState.setPlayerList(gs.getPlayerList());
        this.gameState.setTreasureLocation(gs.getTreasureLocation());
    }

    // Server function
    private void updateToBeNewPrimary(boolean isFirstPlayer){
        if(isFirstPlayer || type == PlayerType.Backup)
        {
            type = PlayerType.Primary;
            primaryIp = localIp;
        }
    }

    //Server function
    private void updateToBeNewBackup(){
        if(type == PlayerType.Standard)
        {
            type = PlayerType.Backup;
            backupIp = localIp;
        }
    }

    // Primary only
    public GameState makeMove(Command cmd, char[] id) {
        if(type == PlayerType.Primary){
            if(!gameState.makeMove(cmd,id)) return null;
        }
        return gameState;
    }

    // Primary Only
    // Return game state and current primary IP and backup IP
    public GameState join(EndPoint Ip, char[] id){
        if (backupIp == null){
            backupIp = Ip;
            if(!gameState.addNewPlayerById(id, PlayerType.Backup)){return null;}
        }
        else {
            if(!gameState.addNewPlayerById(id, PlayerType.Standard)){return null;}
        }
//        if(gameState.getPlayerList().size() == 0){
//            if(!gameState.addNewPlayerById(id, PlayerType.Primary)){return null;}
//        }
//        if(gameState.getPlayerList().size() == 1){
//            if(!gameState.addNewPlayerById(id, PlayerType.Backup)){return null;}
//        }
//        else {
//            if(!gameState.addNewPlayerById(id, PlayerType.Standard)){return null;}
//        }
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
