/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.tests;

import com.codename1.io.Log;
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author shannah
 */
public abstract class BaseTest {
    public Vector passed = new Vector();
    public Vector failed = new Vector();
    public Vector subtests = new Vector();
    
    public void assertTrue(boolean expr, String msg){
        if ( expr ) passed.add(msg);
        else failed.add(msg);
    }
    
    public void assertEquals(Object expected, Object actual, String label){
        if ( (expected != null && expected.equals(actual)) || ( expected == null && actual == null) ){
            passed.add(label);
        } else {
            failed.add(label+": expected ["+expected+"] but actual was ["+actual+"]");
        }
    }
    
    public void printPassedLog(){
        Enumeration e = passed.elements();
        
        while ( e.hasMoreElements() ){
            Log.p(e.nextElement()+" Passed");
        }
        
        e = subtests.elements();
        while ( e.hasMoreElements() ){
            ((BaseTest)e.nextElement()).printPassedLog();
        }
    }
    
    public void printFailedLog(){
        Enumeration e = failed.elements();
        while ( e.hasMoreElements() ){
            Log.p(e.nextElement()+" Failed");
        }
        e = subtests.elements();
        while ( e.hasMoreElements() ){
            ((BaseTest)e.nextElement()).printFailedLog();
        }
    }
    
    public int numPassed(){
        int numPassed = passed.size();
        
        Enumeration e = subtests.elements();
        while ( e.hasMoreElements() ){
            numPassed += ((BaseTest)e.nextElement()).numPassed();
        }
        return numPassed;
        
    }
    
    public int numFailed(){
        int numFailed = failed.size();
        
        Enumeration e = subtests.elements();
        while ( e.hasMoreElements() ){
            numFailed += ((BaseTest)e.nextElement()).numFailed();
        }
        return numFailed;
        
    }
    
    public void printResults(){
        printPassedLog();
        
        printFailedLog();
        
        Log.p(numPassed()+" passed; "+numFailed()+" failed.");
    }
    
    public void addTest(BaseTest test){
        subtests.add(test);
    }
    
    public void run(){
        _run();
        Enumeration e = subtests.elements();
        while ( e.hasMoreElements() ){
            ((BaseTest)e.nextElement()).run();
        }
    }
    protected abstract void _run();
    
}
