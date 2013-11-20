/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.extract;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The MatchData class functions as a container for matches. Matches can either
 * be offered sorted in time or as they appear in the dataset.<br>
 * The methods getNext() and getNextFromMap() return the next map in the dataset
 * and the next match ordered in time, respectively.
 * @author Ruud van de Bovenkamp
 */
public class MatchData {
    
    /**
     * Creates an instance of MatchData based on the data in file.
     * @param file The file containing the matchdata.
     */
    public MatchData(String file){
        try
        {
            in = new BufferedInputStream(new FileInputStream(file));
            currentFile = file;
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(MatchData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Resets the state of the Data stream.
     */
    public void reset(){
        try
        {
            in.close();
            in = new BufferedInputStream(new FileInputStream(currentFile));
        }
        catch (Exception ex)
        {
            Logger.getLogger(MatchData.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /**
     * Gets the next valid Match in the data file.
     * @return the next match or null if no more bytes can be read from the stream
     */
    public Match getNext(){
        
        try
        {
            // read size of match
            int r = 0;
            byte[] b = new byte[4];
            r = in.read(b);
            if(r == -1)
            {
                return null;
            }
            ByteBuffer bb = ByteBuffer.wrap(b);
            int size = bb.getInt();
            // create byte array to hold the match data
            b = new byte[size];
            Match m;
            // read data into byte array
            r = in.read(b);
            if(r == -1)
            {
                return null;
            }
            // construct match from byte array
            m = new Match(b);
            // check whether the match is valid (ignoring duration)
            // and if the match is not valid, read the next match until
            // a valid match is found.
            while(!m.matchValid(false))
            {
                b = new byte[4];
                r = in.read(b);
                if(r == -1)
                {
                    return null;
                }
                bb = ByteBuffer.wrap(b);
                size = bb.getInt();
                
                b = new byte[size];
                r = in.read(b);
                if(r == -1)
                {
                    return null;
                }
                m = new Match(b);
            }
            return m;
        }
        catch (IOException ex)
        {
            Logger.getLogger(MatchData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * Gets the next valid match sorted in time
     * @return  the next valid match sorted in time or null if no more
     * matches are present.
     */
    public Match getNextFromMap(){
        // Checks the global iterator over the sorted map to see whether
        // there actually is a next match
        if(it.hasNext())
        {
            return validGames.get(it.next());
        }
        return null;
    }
    
    /**
     * Fills the map with game active time mapped to matches and initialises the global
     * iterator over the sorted indices.
     * @param lim limits the number of matches that are read from the data file. <br>
     * NOTE that the limit does NOT check the whole data file and gives the first lim number
     * of matches. It only reads the first lim number of valid matches from the data file
     */
    public void loadMap(int lim){
        reset();
        int c = 0;
        Match m;
        // All valid games are stored in a map sorted by their active time
        validGames = new TreeMap<Long,Match>();
        // Get the next valid game, when still under the limit
        int overwritten = 0;
        while((m = getNext()) != null && c < lim)
        {
            if(m.gameStartMs == 0l)
                continue;
            if(m.gameStartMs == 943916400000l && m.gameEndMs != 943916400000l && m.gameActiveMs != 943916400000l)
            {
                m.gameStartMs = m.gameActiveMs;
                System.out.println("game start time modified");
                System.out.println("game now reads: ");
                System.out.println(m);
            }
            while(validGames.containsKey(m.gameStartMs) && m.gameStartMs != 943916400000l)
            {
                m.gameStartMs += 1l;
            }
            if(m.gameStartMs != 943916400000l)
            {
                validGames.put(m.gameStartMs, m);
                c++;
            }
            if(c % 10000 == 0)
            {
                System.out.println("Map contains: " + validGames.size() + " matches");
            }
        }
        System.out.println("Map contains: " + validGames.size() + " matches");
        // Get the map's keyset and get the iterator
        Set<Long> t = validGames.keySet();
        validTimestamps = new TreeSet<Long>();
        for(long l:t)
        {
            validTimestamps.add(l);
        }
        it = validTimestamps.iterator();
    }
    
    /**
     * Closes the input file.
     */
    public void close(){
        if(in != null)
        {
            try
            {
                in.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(MatchData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    String currentFile;
    public BufferedInputStream in;
    Iterator it;
    TreeSet<Long> validTimestamps;
    Map<Long,Match> validGames;
}
