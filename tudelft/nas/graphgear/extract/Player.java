/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.extract;

/**
 *
 * @author rvandebovenkamp
 * The player class contains a player id and some play statistics
 */
public class Player {
    /**
     * Creates a new player with the specified id
     * @param _id player id
     */
    public Player(int _id){
        id = _id;
    }
    
    /**
     * Adds the result of a match to the player's statistics
     * @param result match result
     */
    public void addMatchResult(int result){
        matchesPlayed++;
        switch(result)
        {
            case 1:
                matchesWon++;
                break;
            case -1:
                matchesLost++;
                break;
            case 0:
                break;
            default:
                System.out.println("wrong result!");
                break;
        }
    }
    
    @Override
    public String toString(){
        return "Player " + id + ". Played: " + matchesPlayed + " won: " + matchesWon + " lost: " + matchesLost + " win perc:" + getWinRatio();
    }
    
    @Override
    public int hashCode(){
        return id;
    }
    
    @Override
    public boolean equals(Object o){
        if(o instanceof Player)
        {
            if(this.id == ((Player)o).id)
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get the number of matches played by this player
     * @return number of matches played by this player
     */
    public int getMatchesPlayed(){
        return matchesPlayed;
    }
    
    /**
     * Gets the win ratio of this player
     * @return the win ratio of this player
     */
    public int getWinRatio(){
        return Math.round(((float)matchesWon*100f)/(float)matchesPlayed);
    }
    
    private int id;
    private int matchesPlayed = 0;
    private int matchesWon = 0;
    private int matchesLost = 0;
    
}
