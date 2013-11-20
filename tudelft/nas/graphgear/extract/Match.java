package tudelft.nas.graphgear.extract;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Vector;

public class Match implements Comparable<Match>{
    public Match(){};

    
    /**
     * Reads the match from a byte array
     * @param in The byte array containing the match data
     */
    public Match(byte[] in){
        
        ByteBuffer bb = ByteBuffer.wrap(in);
    	this.matchId = bb.getInt();
    	this.gameActiveMs = bb.getLong();
    	this.gameStartMs = bb.getLong();
    	this.gameEndMs = bb.getLong();
    	this.seScore = bb.getShort();
    	this.scScore = bb.getShort();
    	this.seKill = bb.getShort();
    	this.seDeath = bb.getShort();
    	this.scKill = bb.getShort();
    	this.scDeath = bb.getShort();
    	this.winner = bb.get();
        
    	this.sentinel = new int[bb.get()];
    	for(int i=0;i<this.sentinel.length;i++)
        {
         	sentinel[i] = bb.getInt(); // allows for variable team sizes
                playerIds.add(sentinel[i]);
        }
        
        this.scourge = new int[bb.get()]; // allows for variable team sizes
        for(int i=0;i<scourge.length;i++)
        {
               scourge[i] = bb.getInt();
               playerIds.add(scourge[i]);
        }
    }
   
    /**
     * Returns the total number of players in the match
     * @return total number of players
     */
    public int getNumberOfPlayers(){
        if(playerIds.isEmpty())
        {
            for(int i : sentinel)
            {
                if(i != 0)
                {
                    playerIds.add(i);
                }
            }
            for(int i : scourge)
            {
                if(i != 0)
                {
                    playerIds.add(i);
                }
            }
        }
        int c = 0;
        for(int i : playerIds)
        {
            if(i != 0)
            {
                c++;
            }
        }
        return c;
    }
    
    /**
     * Returns whether the players in the array are on the same team. The assumption
     * is that a player cannot be in both teams
     * @param pl array with player ids
     * @return true if the players are non the same team, false otherwise
     */
    public boolean playersOnSameTeam(int[] pl)
    {
        return (playersInSentinel(pl) || playersInScourge(pl));
    }
    
    /**
     * Checks whether all players in the offered array are on team sentinel
     * @param pl array containing player ids
     * @return true if all players are on team sentinel, false otherwise
     */
    public boolean playersInSentinel(int[] pl)
    {
        boolean found = false;
        for(int p:pl)
        {
            found = false;
            for(int s : sentinel)
            {
                if(s==p)
                {
                    found = true;
                }
            }
            if(!found)
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns a short string representation of the match. Only contains the matchId
     * the game start and game end times
     * @return short string representation of the match
     */
    public String shortToString(){
        return (matchId + " " + new Date(gameStartMs) + "-" + new Date(gameEndMs));
    }
    
    /**
     * Checks whether all players in the offered array are on team scourge
     * @param pl array containing player ids
     * @return true if all players are on team scourge, false otherwise
     */
    public boolean playersInScourge(int[] pl)
    {
        boolean found = false;
        for(int p:pl)
        {
            found = false;
            for(int s : scourge)
            {
                if(s==p)
                {
                    found = true;
                }
            }
            if(!found)
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Checks whether the match contained a certain player
     * @param pl the player
     * @return true if the player is present in the match, false otherwise
     */
    public boolean containsPlayer(int pl){
        for(int p : sentinel)
        {
            if(p == pl)
                return true;
        }
        for(int p : scourge)
        {
            if(p == pl)
                return true;
        }
        return false;
    }
    
    /**
     * Checks whether a set of players are present in the match
     * @param pl array with player ids
     * @return true if all players are present in the match, false otherwise
     */
    public boolean containsPlayers(int[] pl){
        boolean found = false;
        for(int p:pl)
        {
            found = false;
            for(int s : sentinel)
            {
                if(s==p)
                {
                    found = true;
                }
            }
            for(int s : scourge)
            {
                if(s==p)
                {
                    found = true;
                }
            }
            if(!found)
                return false;
        }
        return true;
    }
    
    /**
     * Checks if team sentinel won the match
     * @return true if team sentinel won, false otherwise (includes draws and canceled matches etc.)
     */
    public boolean sentinelWon(){
        return (winner == 1);
    }
    
    /**
     * Checks if team scourge won the match
     * @return true if team scourge won, false otherwise (includes draws and canceled matches etc.)
     */
    public boolean scourgeWon(){
        return (winner == -1);
    }
    
    @Override
    public String toString(){
            Date d = new Date();
            d.setTime(gameActiveMs);
            String res = "";
            res += "Game: " + gameName + "\n";
            res += "Id: " + matchId + "\n";
            res += "Mode: " + gameMode + "\n";
            res += "Active: " + d.toString() + "\n";
            d.setTime(gameStartMs);
            res += "Start: " + d.toString() + "\n";
            d.setTime(gameEndMs);
            res += "End: " + d.toString() + "\n";
            res += "Length: " + gameLength + "\n";
            res += "dotaVersion: " + dotaVersion + "\n";
            res += "Points: Sentinel: " + seScore + " Scourge: " + scScore + "\n";
            res += "KD: Sentinel: " + seKill + " / " + seDeath + " Scourge: " + scKill + " / " + scDeath + "\n";
            res += "winner: ";
            switch(winner)
            {
                case 0:
                    res += "draw\n";
                    break;
                case 1:
                    res += "sentinel\n";
                    break;
                case -1:
                    res += "scourge\n";
                    break;
                case -2:
                    res += "game aborted\n";
                    break;
                case -3:
                    res +=  "no winner information\n";
                    break;
                default:
                    res += "invalid winner value\n";
                    break;
            }
            res += "Sentinel\n";
            for(int s : sentinel)
            {
                res += " " + s + "\n";
            }
            res += "Scourge\n";
            for(int s : scourge)
            {
                res += " " + s + "\n";
            }
            res += "Number of players " + playerIds.size();
            return res;

        }
    /**
     * Stores this game as a byte array of the following format:<br>
     * 0-3 match id <br>
     * 4-11 game active <br>
     * 12-19 game start time <br>
     * 20-27 game end time <br>
     * 28-29 score sentinel <br>
     * 30-31 score scourge <br>
     * 32-33 kill sentinel <br>
     * 34-35 death sentinel <br>
     * 36-37 kill scourge <br>
     * 38-39 death scourge <br>
     * 40 winner <br>
     * 41 size team sentinel <br>
     * 42 - 42 + (41)*4 team sentinel <br>
     * 42 + (41)*4 + 1 size time scourge
     * 42 + (41)*4 + 2 - x team scourge <br>
     * @return byte array of this match
     */
public byte[] toBytArray(){
        int size = 41 + 2 + sentinel.length*4 + scourge.length*4 + 4;
        ByteBuffer bb = ByteBuffer.allocate(size); 
        bb.putInt(size-4);
        bb.putInt(matchId); //a integer is 4 bytes
        bb.putLong(gameActiveMs); //a long in java is 8 bytes;
        bb.putLong(gameStartMs);  
        bb.putLong(gameEndMs);  //28 bytes now
        bb.putShort(seScore); //seScore is a short value, only two bytes;
        bb.putShort(scScore);
        bb.putShort(seKill);
        bb.putShort(seDeath);
        bb.putShort(scKill);
        bb.putShort(scDeath);
        bb.put(winner);
        bb.put((byte)sentinel.length);
        // Team Sentinel
        for(int i=0;i<sentinel.length;i++)
        {
        	bb.putInt(sentinel[i]); // sentinel[i] is integer value which takes up 4 bytes;
        }
        bb.put((byte)scourge.length);
        // Team Scourge
        for(int i=0;i<scourge.length;i++)
        {
        	bb.putInt(scourge[i]); // scourge[i] is integer value which takes up 4 bytes;
        }
        byte[] res = bb.array();
        return res;
    }

    /**
     * Checks whether the game has a valid active time (after jan 1st 2002)
     * @return true if the game has a valid active time
     */
    public boolean activeValid(){
        return (gameActiveMs > 1009877540019l);
    }
    
    /**
     * Checks whether the game has a valid start time (non zero)
     * @return true if this game has a valid (non zero) start time
     */
    public boolean startValid(){
        //return (gameStartMs != 943916400000l);
        // return whether the start time is after jan 1st 2002
        return (gameStartMs > 1009877540019l);
    }

    /**
     * Checks whether the game has non-zero teams
     * @return true if the game has at least one player <br>
     * false otherwise.
     */
    public boolean nonZeroTeams(){
        int sent = 0;
        int scou = 0;
        for(int i=0;i<sentinel.length;i++)
        {
            sent += sentinel[i];
        }
        for(int i=0;i<scourge.length;i++)
        {
            scou += scourge[i];
        }
        return (sent != 0) && (scou != 0);
    }
    
    /**
     * Checks whether the match is valid either with or without a time information.
     * A valid match has nonzero teams and may or may not have a valid game start time.
     * @param includeTime true if the time information has to be checked. Time
     * information means that the start of the games has to be valid. (Not Active)
     * @return true if the match is valid <br> false otherwise
     */
    public boolean matchValid(boolean includeTime){
        if(winner == -2)
        {
            return false;
        }
        if(includeTime)
        {
            return (nonZeroTeams() & startValid());
        }
        return nonZeroTeams();
    }
    
    /**
     * Gets the duration of the game (in minutes)
     * @return duration of the game in minutes
     */
    public int getDuration(){
        if(gameStartMs == 943916400000l && gameEndMs != 943916400000l && gameActiveMs != 943916400000l)
        {
            gameStartMs = gameActiveMs;
        }
        return (int) ((gameEndMs-gameStartMs)/60000l);
    }
    
    /**
     * Gets the start time in the system standard string representation of
     * a Date object
     * @return Game start time
     */
    public String getStartTime(){
        Date d = new Date();
        d.setTime(gameStartMs);
        return d.toString();
    }
    
    /**
     * Gets the end time in the system standard string representation of
     * a Date object
     * @return Game end time
     */
    public String getEndTime(){
        Date d = new Date();
        d.setTime(gameEndMs);
        return d.toString();
    }
    
    /**
     * Gets the active time in the system standard string representation of
     * a Date object
     * @return Game active time
     */
    public String getActiveTime(){
        Date d = new Date();
        d.setTime(gameActiveMs);
        return d.toString();
    }
    
    /**
     * Returns a string containing the id and start and end time of the match and all the players
     * @return string containing the id and start and end time of the match and all the players
     */
    public String timeAndPlayers(){
        String res = shortToString();
        res += ". sen: ";
        for(int i : sentinel)
        {
            res += (i + " ");
        }
        res += " scr: ";
        for(int i : scourge)
        {
            res += (i + " ");
        }
        return res;
    }
    
    /**
     * Checks if a certain player was on the losing team
     * @param p player id
     * @return true if player lost, false otherwise
     */
    public boolean playerLost(int p){
        // If sentinel won
        if(winner == 1)
        {
            // Check whether player was on team sentinel
            for(int s:scourge)
            {
                if(s == p)
                    return true;
            }
        }
        // If scourge won
        if(winner == -1)
        {
            // Check whether player was on team scourge
            for(int s:sentinel)
            {
                if(s == p)
                    return true;
            }
        }
        // If no one won, or the player was not found on the winning team
        // the result is false.
        return false;
    }
    
    /**
     * Checks whether a given player was on the team that won this match.
     * @param p player id
     * @return true if the player won, false if the player did not win. Note that
     * the player does not have to lose in order not to win.
     */
    public boolean playerWon(int p){
        // If sentinel won
        if(winner == 1)
        {
            // Check whether player was on team sentinel
            for(int s:sentinel)
            {
                if(s == p)
                    return true;
            }
        }
        // If scourge won
        if(winner == -1)
        {
            // Check whether player was on team scourge
            for(int s:scourge)
            {
                if(s == p)
                    return true;
            }
        }
        // If no one won, or the player was not found on the winning team
        // the result is false.
        return false;
    }
    
    /**
     * Compares the duration of a match to the duration of this match
     * @param o match to compare this match to
     * @return 0 if both games where active for the same duration <br>
     * -1 if the comparing match lasted longer <br>
     * 1 if the comparing match lasted shorter
     */
    @Override
    public int compareTo(Match o) {
        if(this.gameStartMs > o.gameStartMs)
                return 1;
        else if(this.gameStartMs < o.gameStartMs)
                return -1;
        return 0;
    }

    /**
     * Gives the number of players on team scourge
     * @return the number of players on team scourge
     */
    public int playersInScourge(){
        int c = 0;
        for(int i : scourge)
        {
            if(i!=0)
                c++;
        }
        return c;
    }
    
    /**
     * Gives the number of players on team sentinel
     * @return the number of players on team sentinel
     */
    public int playersInSentinel(){
        int c = 0;
        for(int i : sentinel)
        {
            if(i!=0)
                c++;
        }
        return c;
    }

    @Override
    public int hashCode(){
        return matchId;
    }
    
    /**
     * Tests for equality between an object and this object. A match is considered
     * to be the same as another match of the two matches have the same match id and
     * if the matches were of equal duration. 
     * @param o object to be tested for equality
     * @return true if the object is the same as this object <br>
     * false if it is not the same object
     */
    @Override
    public boolean equals(Object o){
        if(o instanceof Match)
        {
            Match m = (Match)o;
            if(m.matchId == this.matchId && m.gameActiveMs == this.gameActiveMs)
            {
                return true;
            }
        }
        return false;
    }
    public byte winner;
    public short seScore;
    public short scScore;
    public short seKill;
    public short scKill;
    public short seDeath;
    public short scDeath;
    public int matchId = -1;
    public String gameName = "";
    public String gameMode = "";
    public String gameActive = "";
    public String gameStart = "";
    public String gameEnd = "";
    public String gameLength = "";
    public String dotaVersion = "";
    public long gameActiveMs;
    public long gameStartMs;
    public long gameEndMs;
    public int sentinel[] = new int[5]; // 5 players' id
    public int scourge[] = new int[5]; // 5 players' id, Dota allows at most 10 players play.
    public Vector<Integer> playerIds = new Vector<>();
}
