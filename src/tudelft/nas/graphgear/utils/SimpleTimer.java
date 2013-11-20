/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tudelft.nas.graphgear.utils;

/**
 *
 * @author rvandebovenkamp
 * A simple timer that extends thread and can be polled to see whether the timer has
 * ticked.
 */
public class SimpleTimer extends Thread{
    public static volatile boolean tick = false;
    public volatile boolean stop = false;
    int sleep = 1000;
  public SimpleTimer(int _sleep) { 
    sleep = _sleep;
    setDaemon(true);
    
  }
 
  public void run() {    
    while (!stop) {
    try 
    {
        Thread.sleep(sleep);
    } 
    catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    tick = true;
    }
  }
}
