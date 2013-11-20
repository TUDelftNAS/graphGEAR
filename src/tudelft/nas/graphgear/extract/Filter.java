/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tudelft.nas.graphgear.extract;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import tudelft.nas.graphgear.linkset.LinkSet;
import tudelft.nas.graphgear.linkset.LinkSetNode;
import tudelft.nas.graphgear.linkset.TimedLinkSetNode;
import tudelft.nas.graphgear.utils.NodeColor;



/**
 *
 * @author Ruud van de Bovenkamp
 */

enum operation {OR,AND};
public class Filter {
    
    /**
     * Closes the log file
     */
    private void closeLog(){
        try {
            if(log_out != null)
            {
                log_out.flush();
                log_out.close();
                log_out = null;
            }
        } catch (IOException ex) {
            Logger.getLogger(Filter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Writes to the log file
     * @param s string to be written to the log
     */
    private void writeToLog(String s){
            try 
            {
                if(log_out == null)
                {
                    log_out = new BufferedWriter(new FileWriter(logfile,true));
                }
                log_out.write(new Date(System.currentTimeMillis()).toString() + ": " + s + "\n");
            } catch (IOException ex) {
                Logger.getLogger(Filter.class.getName()).log(Level.SEVERE, null, ex);
            }

    }
    
    /**
     * Prints all matches in the data file
     * @param dataFile data file
     */
    public void printMatches(String dataFile){
        MatchData M = new MatchData(dataFile);
        Match m;
        while((m = M.getNext())!= null)
        {
            System.out.println(m.toString());
        }
    }
    
    /**
     * Extracts links from a match by creating a link between every player
     * that played in this match
     * @param m the match from which the links have to be extracted
     */
    public void extractLinksMatch(Match m){
        if(!localConditionsMet(m,-1,-1))
            return;
        // Go over all the players in team scourge
        for(int s :m.scourge)
        {
            // create a link to all the other players in team scourge
            for(int d:m.scourge)
            {
                if(s != d && s != 0 && d != 0)
                {
                    L.addLink(new LinkSetNode(s, d));
                    globalLinkCounter++;
                }
            }
            // create a link to all the players in team sentinel
            for(int d:m.sentinel)
            {
                if(s != d && s != 0 && d != 0)
                {
                    L.addLink(new LinkSetNode(s, d));
                    globalLinkCounter++;
                }
            }
        }
        // Go over all the players in team sentinel
        for(int s :m.sentinel)
        {
            // create a link to all the players in team scourge
            for(int d:m.scourge)
            {
                if(s != d && s != 0 && d != 0)
                {
                    L.addLink(new LinkSetNode(s, d));
                    globalLinkCounter++;
                }
            }
            // create a link to all the other players in team sentinel
            for(int d:m.sentinel)
            {
                if(s != d && s != 0 && d != 0)
                {
                    L.addLink(new LinkSetNode(s, d));
                    globalLinkCounter++;
                }
            }
        }
    }
    
    /**
     * Orders a matchdata file in time
     * @param in input file
     * @param out output file
     */
    public void orderMatchDataInTime(String in, String out){
        MatchData M = new MatchData(in);
        Match m;
        M.loadMap(Integer.MAX_VALUE);
        try 
        {
            BufferedOutputStream o = new BufferedOutputStream(new FileOutputStream(out));
            while((m=M.getNextFromMap())!= null)
            {
                o.write(m.toBytArray());
            }
            o.flush();
            o.close();
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(Filter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     * Extracts links from a match by creating a link between every player
     * that played in this match
     * @param m the match from which the links have to be extracted
     */
    public ArrayList<LinkSetNode> getLinksMatch(Match m){
        ArrayList<LinkSetNode> res = new ArrayList<>();
        // Go over all the players in team scourge
        for(int s :m.scourge)
        {
            // create a link to all the other players in team scourge
            for(int d:m.scourge)
            {
                if(s != d && s != 0 && d != 0)
                {
                    res.add(new LinkSetNode(s, d));
                }
            }
            // create a link to all the players in team sentinel
            for(int d:m.sentinel)
            {
                if(s != d && s != 0 && d != 0)
                {
                    res.add(new LinkSetNode(s, d));
                }
            }
        }
        // Go over all the players in team sentinel
        for(int s :m.sentinel)
        {
            // create a link to all the players in team scourge
            for(int d:m.scourge)
            {
                if(s != d && s != 0 && d != 0)
                {
                    res.add(new LinkSetNode(s, d));
                }
            }
            // create a link to all the other players in team sentinel
            for(int d:m.sentinel)
            {
                if(s != d && s != 0 && d != 0)
                {
                    res.add(new LinkSetNode(s, d));
                }
            }
        }
        return res;
    }
    
    /**
     * Prints all matches for a certain player
     * @param file datafile 
     * @param player player id
     */
    public void printMatchesOfPlayer(String file, int player)
    {
        MatchData M = new MatchData(file);
        Match m;
        while((m = M.getNext()) != null)
        {
            if(m.matchValid(true))
            {
                if(m.playerIds.contains(player))
                {
                    System.out.println(m);
                }
            }
        }
    }
    
    
    /**
     * Extracts links from the match by creating a link between all all players
     * in opposing sides
     * @param m The match from which the links have to be extracted
     */
    public void extractLinksOpposingSide(Match m){
        /* create a flag to signal link specific conditions
         * If there are link specific conditions, these have to be checked for
         * every link. If the local conditions apply to the match only, they have
         * to be checked only once.
        */
        boolean linkSpecificConditions = false;
        for(String s: localconditions)
        {
            if(s.contains("link") || s.contains("rank"))
            {
                linkSpecificConditions = true;
            }
        }
        if(!localConditionsMet(m,-1,-1))
            return;
        // go over all the players in team scourge
        for(int s :m.scourge)
        {
            // add a link to all players in team sentinel
            for(int d:m.sentinel)
            {
                if(s != d && s != 0 && d != 0)
                {
                    if(linkSpecificConditions)
                    {
                        // Check link specific conditions, if there are any.
                        if(localConditionsMet(m,s,d))
                        {
                            L.addLink(new LinkSetNode(s, d));
                            globalLinkCounter++;
                        }
                    }
                    else
                    {
                        L.addLink(new LinkSetNode(s, d));
                        globalLinkCounter++;
                    }
                }
            }
        }
        // go over all the players in team sentinel
        for(int s :m.sentinel)
        {
            // add a link to all players in team scourge
            for(int d:m.scourge)
            {
                if(s != d && s != 0 && d != 0)
                {
                    if(linkSpecificConditions)
                    {
                        // Check link specific conditions, if there are any.
                        if(localConditionsMet(m,s,d))
                        {
                            L.addLink(new LinkSetNode(s, d));
                            globalLinkCounter++;
                        }
                    }
                    else
                    {
                        L.addLink(new LinkSetNode(s, d));
                        globalLinkCounter++;
                    }
                }
            }
        }
    }
    
    /**
     * Extracts links from the match by creating a link between all the players
     * that played on the same side.
     * @param m The match from which the links have to be extracted
     */
    public void extractLinksSameSide(Match m){
        // Check for the presence of link specific conditions, see comment with
        //  extractLinksOpposingSide(Match m)
        boolean linkSpecificConditions = false;
        for(String s: localconditions)
        {
            if(s.contains("link") || s.contains("rank"))
            {
                linkSpecificConditions = true;
            }
        }
        if(!localConditionsMet(m,-1,-1))
            return;
        // Go over all the players on team scourge
        for(int s :m.scourge)
        {
            // create a link to all the other players on team scourge
            for(int d:m.scourge)
            {
                if(s != d && s != 0 && d != 0)
                {
                    // Check link specific conditions, if there are any.
                    if(linkSpecificConditions)
                    {
                        if(localConditionsMet(m,s,d))
                        {
                            L.addLink(new LinkSetNode(s, d));
                            globalLinkCounter++;
                        }
                    }
                    else
                    {
                        L.addLink(new LinkSetNode(s, d));
                        globalLinkCounter++;
                    }
                }
            }
        }
        // Go over all the players on team sentinel
        for(int s :m.sentinel)
        {
            // create a link to all the other players on team sentinel
            for(int d:m.sentinel)
            {
                if(s != d && s != 0 && d != 0)
                {
                    // Check link specific conditions, if there are any.
                    if(linkSpecificConditions)
                    {
                        if(localConditionsMet(m,s,d))
                        {
                            L.addLink(new LinkSetNode(s, d));
                            globalLinkCounter++;
                        }
                    }
                    else
                    {
                        L.addLink(new LinkSetNode(s, d));
                        globalLinkCounter++;
                    }
                }
            }
        }
    }
    
    /**
     * Extracts a linkset with links between players that played on
     * the same side and won the match
     * @param dataFile datafile
     * @param outputFile linkset file
     */
    public void SimpleRunSameSideWon(String dataFile, String outputFile){
        ArrayList<String> conditions = new ArrayList<String>();
        conditions.add("*local conditions");
        conditions.add("([link-won])");
        processConfig(conditions);
        SimpleRunSameSideLinks(dataFile,outputFile);
    }
    
    /**
     * Extracts a linkset with links between players that played on
     * the same side and lost the match
     * @param dataFile datafile
     * @param outputFile linkset file
     */
    public void SimpleRunSameSideLost(String dataFile, String outputFile){
        ArrayList<String> conditions = new ArrayList<String>();
        conditions.add("*local conditions");
        conditions.add("([link-lost])");
        processConfig(conditions);
        SimpleRunSameSideLinks(dataFile,outputFile);
    }
    
    /**
     * Extracts links between players that played in the same match in
     * a certin interval
     * @param dataFile datafile
     * @param outputFile linkset file
     * @param interval interval 
     */
    public void SimpleRunSameGameInterval(String dataFile, String outputFile, int interval){
        globalLinkCounter = 0;
        L = new LinkSet();
        MatchData M = new MatchData(dataFile);
        Match m;
        int c = 0;
        
        ArrayList<LinkSetNode> links;
        long longinterval = (long)interval*1000l*60l*60l*24l;
        while((m = M.getNext()) != null)
        {
            if(m.getDuration() > 0 && m.winner != -2)
            {
                links = getLinksMatch(m);
                for(LinkSetNode ls : links)
                {
                    L.addLink(new TimedLinkSetNode(ls.s,ls.d,longinterval,m.gameStartMs));
                }
                c++;
                if(c%25000 ==0)
                {
                    System.out.println(c + " done. Size of tree: " + L.size());
                }
            }
        }
        System.out.println(c + " done. Size of tree: " + L.size());
        System.out.println("links added (including weight increments) " + globalLinkCounter);
        L.writePartFile(outputFile);
        L.clear();
    }
    
    /**
     * Extracts a linkset with a link between players that played
     * on the same side
     * @param dataFile datafile
     * @param outputFile linkset destination file
     */
    public void SimpleRunSameSideLinks(String dataFile, String outputFile){
        globalLinkCounter = 0;
        L = new LinkSet();
        if(localconditions == null)
        {
            localconditions = new String[0];
        }
        MatchData M = new MatchData(dataFile);
        Match m;
        int c = 0;
        int read = 0;
        while((m = M.getNext()) != null)
        {
            read++;
            if(m.getDuration() > 0 && m.matchValid(true))
            {
                extractLinksSameSide(m);
                c++;
                if(c%25000 ==0)
                {
                    System.out.println(c + " done. Size of tree: " + L.size());
                }
            }
            else
            {
                writeToLog("Match rejected: " + m.shortToString());
            }
                  
        }
        writeToLog("SimpleRunOpposingSideLinks, read: " +read + " used: " + c);
        writeToLog("Input: " + dataFile + " output: " + outputFile);
        System.out.println(c + " done. Size of tree: " + L.size());
        System.out.println("links added (including weight increments) " + globalLinkCounter);
        if(new File(outputFile).exists())
        {
            System.out.println("Warning, result file already exists. Results will be merged!");
            writeToLog("Result file exists. Results will be merged.");
        }
        L.writePartFile(outputFile);
        closeLog();
    } 
    
    /**
     * Processes the match data file and creates a link between two
     * players if they played on opposing sides
     * @param dataFile match data file
     * @param outputFile destination link set file
     */    
    public void SimpleRunOpposingSideLinks(String dataFile, String outputFile){
        globalLinkCounter = 0;
        L = new LinkSet();
        if(localconditions == null)
        {
            localconditions = new String[0];
        }
        MatchData M = new MatchData(dataFile);
        Match m;
        int c = 0;
        int read = 0;
        while((m = M.getNext()) != null )
        {
            read++;
            if(m.getDuration() > 0 && m.matchValid(true))
            {
                extractLinksOpposingSide(m);
                c++;
                if(c%25000 ==0)
                {
                    System.out.println(c + " done. Size of tree: " + L.size());
                }
            }
            else
            {
                writeToLog("Match rejected: " + m.shortToString());
            }
        }
        writeToLog("SimpleRunOpposingSideLinks, read: " +read + " used: " + c);
        writeToLog("Input: " + dataFile + " output: " + outputFile);
        System.out.println(c + " done. Size of tree: " + L.size());
        System.out.println("links added (including weight increments) " + globalLinkCounter);
        L.writePartFile(outputFile);
        closeLog();
    }
    
    /**
     * Sets a match requirement that it has to be played before or during the xth month
     * of the dataset. NOTE. A month equals 30 days.
     * @param month number of months that are included
     */
    public void setMonthLimit(int month, String dataFile, String additionalCond){
        long firstMatch = Long.MAX_VALUE;
        MatchData M = new MatchData(dataFile);
        Match m;
        int c = 0;
        while((m = M.getNext()) != null )
        {
            firstMatch = m.gameStartMs < firstMatch ? m.gameStartMs : firstMatch;
        }
        long d = (30l * 24l * 60l * 60l * 1000l * (long)month) + firstMatch;
        System.out.println("Games have to be played between " + new Date(firstMatch).toString() + " and " + new Date(d).toString());
        ArrayList<String> conditions = new ArrayList<String>();
        conditions.add("*local conditions");
        if(additionalCond == null)
        {
            conditions.add("([date <= " + d + "])");
        }
        else
        {
            conditions.add("([date <= " + d + "] " + additionalCond + ")");
        }
        processConfig(conditions);
        
    }
    
    /**
     * Counts the number of matches that pass the local criteria. It also
     * outputs the total number of players in the matches to the console
     * @param dataFile match data file
     * @return the number of matches that pass the local criteria
     */
    public int countMatches(String dataFile){
        if(localconditions == null)
        {
            localconditions = new String[0];
        }
        MatchData M = new MatchData(dataFile);
        Match m;
        int c = 0;
        Set<Integer> players = new HashSet<Integer>();
        while((m = M.getNext()) != null)
        {
            if(m.getDuration() > 0 && m.winner != -2)
            {
                if(localConditionsMet(m,-1,-1))
                {
                    c++;
                    for(int i : m.playerIds)
                    {
                        players.add(i);
                    }
                }
            }
        }
        System.out.println(players.size() + " players in " + c + " matches");
        M.close();
        return c;
    }
    
    /**
     * Processes the match data file and creates a link between two players
     * that played in the same game
     * @param dataFile match data file
     * @param outputFile file location for the link set file
     */
    public void SimpleRunSameGameLinks(String dataFile, String outputFile){
        if(localconditions == null)
        {
            localconditions = new String[0];
        }
        globalLinkCounter = 0;
        L = new LinkSet();
        MatchData M = new MatchData(dataFile);
        Match m;
        int c = 0;
        int read = 0;
        while((m = M.getNext()) != null)
        {
            read++;
            if(m.getDuration() > 0 && m.matchValid(true))
            {
                extractLinksMatch(m);
                c++;
                if(c%10000 ==0)
                {
                    System.out.println(c + " done. Size of tree: " + L.size());
                }
            }
            else
            {
                writeToLog("Match rejected: " + m.shortToString());
            }
        }
        writeToLog("SimpleRunOpposingSideLinks, read: " +read + " used: " + c);
        writeToLog("Input: " + dataFile + " output: " + outputFile);
        System.out.println(c + " done. Size of tree: " + L.size());
        System.out.println("links added (including weight increments) " + globalLinkCounter);
        L.writePartFile(outputFile);
        closeLog();
    }
    
    /**
     * Checks whether the value satisfies the requirements
     * @param req The requirement
     * @param value The value
     * @return True if the value satisfies the requirement <br> false otherwise
     */
    public boolean satisfies(String req, int value){
        String[] parts = req.trim().split(" ");
        char comp = 'x';
        int t = 0;
        int t2 = 0;
        if(parts.length == 2)
        {
            t = Integer.parseInt(parts[1]);
            if(parts[0].length() == 1)
            {
                comp = parts[0].charAt(0);
            }
            else
            {
                if(parts[0].equals(">="))
                {
                    comp = ')';
                }
                if(parts[0].equals("<="))
                {
                    comp = '(';
                }
            }
        }
        else
        {
            t = Integer.parseInt(parts[1]);
            t2 = Integer.parseInt(parts[3]);
            int v = 0;
            if(parts[0].equals(">"))
            {
                v = 1;
            }
            if(parts[0].equals(">="))
            {
                v = 2;
            }
            if(parts[1].equals("<"))
            {
                v++;
            }
            if(parts[2].equals("<="))
            {
                v += 2;
            }
            comp = (char)(v+96);
        }
        switch(comp)
        {
            case '>':
                if(value > t)
                {
                    return true;
                }
                break;
            case '<':
                if(value < t)
                {
                    return true;
                }
                break;
            case '=':
                if(value == t)
                {
                    return true;
                }
                break;
            case '(': // <=
                if(value >= t)
                {
                    return true;
                }
                break;
            case ')': // >=
                if(value >= t)
                {
                    return true;
                }
                break;
            case 'a': // t > x < t2
                if(value > t && value < t2)
                {
                    return true;
                }
                break;
            case 'b': // t > x <= t2
                if(value > t && value <= t2)
                {
                    return true;
                }
                break;
            case 'c': // t >= x < t2
                if(value >= t && value < t2)
                {
                    return true;
                }
                break;
            case 'd': // t >= x <= t2
                if(value >= t && value <= t2)
                {
                    return true;
                }
                break;
            default:
                System.out.println("Unknown operator: " + comp);
                break;
        }
        return false;
    }
    
    /**
     * Checks whether the value satisfies the requirements
     * @param req The requirement
     * @param value The value
     * @return True if the value satisfies the requirement <br> false otherwise
     */
    public boolean satisfies(String req, long value){
        String[] parts = req.trim().split(" ");
        char comp = 'x';
        long t = 0;
        long t2 = 0;
        if(parts.length == 2)
        {
            t = Long.parseLong(parts[1]);
            if(parts[0].length() == 1)
            {
                comp = parts[0].charAt(0);
            }
            else
            {
                if(parts[0].equals(">="))
                {
                    comp = ')';
                }
                if(parts[0].equals("<="))
                {
                    comp = '(';
                }
            }
        }
        else
        {
            t = Long.parseLong(parts[1]);
            t2 = Long.parseLong(parts[3]);
            int v = 0;
            if(parts[0].equals(">"))
            {
                v = 1;
            }
            if(parts[0].equals(">="))
            {
                v = 2;
            }
            if(parts[2].equals("<"))
            {
                v++;
            }
            if(parts[2].equals("<="))
            {
                v += 2;
            }
            comp = (char)(v+96);
        }
        switch(comp)
        {
            case '>':
                if(value > t)
                {
                    return true;
                }
                break;
            case '<':
                if(value < t)
                {
                    return true;
                }
                break;
            case '=':
                if(value == t)
                {
                    return true;
                }
                break;
            case '(': // <=
                if(value <= t)
                {
                    return true;
                }
                break;
            case ')': // >=
                if(value >= t)
                {
                    return true;
                }
                break;
            case 'a': // t > x < t2
                if(value > t && value < t2)
                {
                    return true;
                }
                break;
            case 'b': // t > x <= t2
                if(value > t && value <= t2)
                {
                    return true;
                }
                break;
            case 'c': // t >= x < t2
                if(value >= t && value < t2)
                {
                    return true;
                }
                break;
            case 'd': // t >= x <= t2
                if(value >= t && value <= t2)
                {
                    return true;
                }
                break;
            default:
                System.out.println("Unknown operator: " + comp);
                break;
        }
        return false;
    }
   
    /**
     * Compares two links and orders them numerically by which one has the lowest
     * id amongst the source nodes
     * @param sa Source node of link a
     * @param da Destination node of link a
     * @param sb Source node of link b
     * @param db Destination node of link b
     * @return 0: both links have the same source and destination<br>
     * 1: same source node link b has smaller destination or link b has smalle source node<br>
     * -1: same source node, link a has smaller destination or link a has smaller source node <br>
     */
    public int compareLinks(int sa, int da, int sb, int db){
        if(sb == sa)
        {
            if(db == da)
            {
                return 0;
            }
            if(db < da)
            {
                return 1;
            }
            return -1;
        }
        if(sb < sa)
        {
            return 1;
        }
        return -1;
    }
    
    /**
     * Writes a node to the output stream encoded as a byte array
     * @param out The output stream
     * @param b A buffer
     * @param n the node id
     * @throws IOException
     */
    private void writeNode(BufferedOutputStream out, byte[] b, int n) throws IOException{
        // convert int to byte array (in buffer)
        b[0] = (byte)(n >>> 24);
        b[1] = (byte)(n >>> 16);
        b[2] = (byte)(n >>> 8);
        b[3] = (byte)(n);
        // write buffer to stream
        out.write(b);
    }
    
    /**
     * Reads an int encoded as a byte array from the input stream
     * @param in The input stream
     * @param b A buffer
     * @return the int read from the input stream or -1 if none could be read
     * @throws IOException
     */
    private int readInt(BufferedInputStream in, byte[] b) throws IOException{
        // Reads from the input stream
        if(in.read(b) == -1)
        {
            // If nothing could be read, return -1
            return -1;
        }
        // shift the bytes into an int and return the value
        return ((b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF));
    }
    
    /**
     * processResults reads the adjacency lists that are the result of multiple link
     * forming criteria and combines them based on the given operations
     * @param dir The location of the adjacency lists for the various properties
     * @param prop the number of properties
     * @param operations the operations that have to be formed on the properties
     */
    public void processResults(String dir, int prop,operation[] operations){
        int propertyCounter = 1;
        try
        {
            // Create an input stream for adjacency list A
            BufferedInputStream inA = new BufferedInputStream(new FileInputStream(dir + "prop" + propertyCounter + ".tlst"));
            propertyCounter++;
            // Create an input stream for adjacency list B
            BufferedInputStream inB = new BufferedInputStream(new FileInputStream(dir + "prop" + propertyCounter + ".tlst"));
            // Create an output stream for the combined properties
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dir + "comb.lst"));
            // Create flags to signal end of files for both stream A and B
            boolean endofa = false;
            boolean endofb = false;
            // Declare variables
            String line;
            String[] parts;
            byte[] b = new byte[4];
            int sa,da,sb,db;
            // Read first link from A
            sa  = readInt(inA,b);
            da  = readInt(inA,b);
            if(da == -1)
            {
                endofa = true;
            }
            // Read first link from B
            sb = readInt(inB,b);
            db = readInt(inB,b);
            if(db == -1)
            {
                endofb = true;
            }
            // Continue reading from the list until they have both been processed
            while(!endofa && !endofb)
            {
                // Read from list A while the links in A are still of lower
                // order than those in list B. (Endpoints of links are ordered in ascending
                // order)
                while(compareLinks(sa,da,sb,db) < 0 && !endofa)
                {
                    // In the case of OR, no accompanying link has to be found
                    // in list B, so the links can be copied to the output
                    if(operations[0] == operation.OR)
                    {
                        writeNode(out,b,sa);
                        writeNode(out,b,da);
                    }
                    // Read next link from list A
                    sa  = readInt(inA,b);
                    da  = readInt(inA,b);
                    // If no next link can be found
                    if(da == -1)
                    {
                        // Set the nodes of link A to max val
                        // to ensure list B will always have smaller links
                        sa = Integer.MAX_VALUE;
                        da = Integer.MAX_VALUE;
                        // Signal end of file for list A
                        endofa = true;
                    }
                }
                // Read from both lists for as long as the links in
                // A and B are the same
                while(compareLinks(sa,da,sb,db) == 0 && !endofa && !endofb)
                {
                    // both for OR and AND the links are written to the output
                    writeNode(out,b,sa);
                    writeNode(out,b,da);

                    // Read next link from A, if there is one
                    // otherwise signal end of file for A
                    sa  = readInt(inA,b);
                    da  = readInt(inA,b);
                    if(da == -1)
                    {
                        sa = Integer.MAX_VALUE;
                        da = Integer.MAX_VALUE;
                        endofa = true;
                    }
                    // Read next link from B, if there is one
                    // otherwise signal end of file for B
                    sb = readInt(inB,b);
                    db = readInt(inB,b);
                    if(db == -1)
                    {
                        sb = Integer.MAX_VALUE;
                        db = Integer.MAX_VALUE;
                        endofb = true;
                    }
                    
                }
                // Read from link B for as long as the links are smaller
                // than the links in list A
                while(compareLinks(sa,da,sb,db) > 0 && !endofb)
                {
                    // In the case of OR, no accompanying link has to be found
                    // in list A, so the links can be copied to the output
                    if(operations[0] == operation.OR)
                    {
                        writeNode(out,b,sb);
                        writeNode(out,b,db);
                    }
                    // Read next link from B
                    sb = readInt(inB,b);
                    db = readInt(inB,b);
                    // Check for end of list, and signal if so
                    if(db == -1)
                    {
                        sb = Integer.MAX_VALUE;
                        db = Integer.MAX_VALUE;
                        endofb = true;
                    }
                }
            }
            // Close inputs
            inA.close();
            inB.close();
            // Flush and close output
            out.flush();
            out.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Checks whether the local conditions are met. The conditions are stored in
     * an array together with the operators in reversed polish notation. The operands
     * are the separate conditions that have to be met
     * @param m the match
     * @param s source node
     * @return true if the local conditions are met <br> false otherwise
     */
    public boolean localConditionsMet(Match m,int s, int d){
        if(localconditions.length == 0)
        {
            return true;
        }
        // declare variables
        boolean valid = true;
        String exp;
        Stack<Boolean> S = new Stack<Boolean>();
        boolean op1;
        boolean op2;
        String req = "";
        String cond = "";
        // Go over all the operands and operators
        for(int i = 0;i<localconditions.length;i++)
        {
            exp = localconditions[i];
            // If the next item in local conditions is an operand
            // check the conditions
            if("&|".indexOf(exp) == -1)
            {
                // If the expression contains spaces, it is not a simple
                // condition and the requirement is parsed.
                if(exp.indexOf(" ") != -1)
                {
                    cond = exp.substring(1,exp.indexOf(" "));
                    req = exp.substring(exp.indexOf(" ")+1,exp.length()-1);
                }
                else
                {
                    cond = exp.substring(1, exp.length()-1);
                }
                // Check all the conditions
                Boolean res = null;
                if(cond.equals("link-won"))
                {
                    res = m.playerWon(s);
                    if(s == -1)
                        res = true;
                }
                if(cond.equals("link-lost"))
                {
                    res = m.playerLost(s);
                    if(s == -1)
                        res = true;
                }
                if(cond.equals("rank-delta"))
                {
                    if(playerMap.containsKey(s) && playerMap.containsKey(d))
                    {   
                        res = satisfies(req,Math.abs(playerMap.get(s).getWinRatio()-playerMap.get(d).getWinRatio()));
                    }
                    else
                    {
                        res = false;
                    }
                    if(s == -1)
                        res = true;
                }
                if(cond.equals("sentinel-won"))
                {
                    res = (m.winner == 1);
                }
                if(cond.equals("scourge-won"))
                {
                    res = (m.winner == -1);
                }
                if(cond.equals("duration"))
                {
                    res = satisfies(req,m.getDuration());
                }
                if(cond.equals("date"))
                {
                    res = satisfies(req,m.gameStartMs);
                }
                if(res != null)
                {
                    // Push the result on the stack
                    S.push(res.booleanValue());
                }
                else
                {
                    System.out.println("Error processing conditions");
                }
            }
            else // if the next item is an operator
            {
                // pop the two results that are on the stack
                op2 = S.pop();
                op1 = S.pop();
                // perform the operation and push the result back on the stack
                if(exp.equals("&"))
                {
                    S.push((op1&&op2));
                }
                else
                {
                    S.push((op1||op2));
                }
            }
        }
        // pop the final result from the stack
        valid = S.pop();
        // and return the result
        return valid;
    }

    /**
     * Reads the config file and returns it in a String list
     * @param config location of the config file
     * @return string list containing the content of the config file
     */
    public ArrayList<String> readConfig(String config){
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(config));
            String line = null;
            ArrayList<String> lines = new ArrayList<String>();
            while((line = in.readLine()) != null)
            {
                lines.add(line);
            }
            in.close();
            return lines;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Reads the configuration file and fills the rules and local conditions
     * arrays. Rules are highlevel link forming rules such as 'on the same team'.
     * Local conditions are conditions on the single match.
     * @param lines The location of a configuration file. A configuration file
     * has the following format. Lines starting with # are comments. A line starting
     * with '*link rule' is followed by the link rule and a the line '*local conditions'
     * by the local conditions. Local conditions can be boolean expressions e.g.
     * ([link-won] & [duration >= 30 <= 300]), conditions should be placed within square
     * brackets.
     */
    public void processConfig(ArrayList<String> lines){
        String s;
        // Read the lines and find the link rule and local conditions
        //TODO add support for multiple link rules and local conditions
        for(int i=0;i<lines.size();i++)
        {
            // link rule found
            if(lines.get(i).contains("*link rule"))
            {
                s = lines.get(++i).trim();
                // So far two link rules are supported: same side and opposing side
                if(s.equals("same side"))
                {
                    rules = new int[]{0};
                }
                if(s.equals("opposing side"))
                {
                    rules = new int[]{1};
                }
            }
            // local conditions found
            if(lines.get(i).contains("*local conditions"))
            {
                System.out.println("Found local conditions");
                // an expression evaluator is used to convert the boolean expression
                // into a reversed polish notation to make it easier to check whether
                // the local conditions are met.
                conditionsEvaluator ev = new conditionsEvaluator();
                localconditions = ev.Evaluate(lines.get(++i));
                System.out.println("localconditions now read:");
                for(String str:localconditions)
                {
                    System.out.println(str);
                }
            }
        }
    }
    
    /**
     * Go implements the actual filtering based on a config file. It creates an
     * adjacency list for each high level rule that are then combined to give the
     * final adjacency list.
     * @param input the location of the match data
     * @param outputdir the output location
     */
    public void go(String input, String outputdir){
        // set location of config file
        String config = "d:/warcraft data/config.cfg";
        // Create data structure
        MatchData M = new MatchData(input);
        // Read the config file
        processConfig(readConfig(config));
        // Declare variables
        int c = 0;
        Match m = null;
        long start = System.currentTimeMillis();
        long last = System.currentTimeMillis();
        int lim = 100000;
        int propertyCounter = 0;
        int numMatches = 4000000;
        int rule;
        // Create an adjacency list file for every high level rule
        for(int i=0;i<rules.length;i++)
        {
            // Read rule
            rule = rules[i];
            // do some time keeping
            start = System.currentTimeMillis();
            c = 0;
            // reset the data structure
            M.reset();
            // Go over all the matches, until end of file or match limit is reached
            while((m = M.getNext()) != null && c < numMatches)
            {
                // extract the links based on the rule
                switch(rule)
                {
                    case 0:
                        extractLinksSameSide(m);
                        break;
                    case 1:
                        extractLinksOpposingSide(m);
                        break;
                }
                c++;
                if(c%lim==0) // output some info ever lim cycles
                {
                    System.out.println(c + " done. Last " + lim + " done in " + (System.currentTimeMillis()-last) + " ms.");
                    System.out.println("Total number of links added: " + L.size());
                    last = System.currentTimeMillis();
                }
            }

            System.out.println("Total number of links added: " + L.size());
            System.out.println("All files read in " + (System.currentTimeMillis()-start) + " ms.");
            start = System.currentTimeMillis();

            // Write the result of this rule to disk
            System.out.println("Writing links to disk...");
//            L.writeValidLinksToFile((outputdir + "prop" + (i+1) + ".tlst"),thresholds[i]);
            System.out.println("Done. That took me " + (System.currentTimeMillis()-start));
            start = System.currentTimeMillis();
            // Clear tree for next rule
            L.clear();
        }
        // Close the data set
        M.close();
        // Combine the lists
//        System.out.println("Combining results...");
        System.out.println("Done. That took me " + (System.currentTimeMillis()-start) + " ms.");
    }
    
    int days = 100;
    long interval = (long)days * 86400000l;
    // Create the LinkSet
    LinkSet L = new LinkSet(new LinkSetNode(0,0,NodeColor.BLACK,null,null));
    int[] rules;
    String[] localconditions;
    int[] internalProperties = new int[]{1,3};
    String[] thresholds = new String[]{">= 5 =< 10","> 10"};
    String[] localThresholds = new String[]{"",">= 1243980000000 <= 1244066340000"};
    operation[] operations = new operation[]{operation.AND};
    Map<Integer, Player> playerMap = new HashMap<Integer,Player>();
    int globalLinkCounter = 0;
    String logfile = "filterlog.log";
    BufferedWriter log_out;
}
