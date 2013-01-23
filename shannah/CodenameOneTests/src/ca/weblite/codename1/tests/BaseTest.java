/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.tests;

import com.codename1.io.Log;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author shannah
 */
public abstract class BaseTest {
    public Hashtable results = new Hashtable();
    public Vector subtests = new Vector();
    
    public static class Result {
        public String msg;
        public boolean result;
        public Result(String msg, boolean result){
            this.msg = msg;
            this.result = result;
        }
    
        public String toString(){
            return msg;
        }
        
    }
    
    public static class Failed extends Result {
        public Failed(String msg){
            super(msg, false);
        }
        
        public String toString(){
            return "Failed :"+msg;
        }
    }
    
    public static class Passed extends Result {
        public Passed(String msg){
            super(msg, true);
        }
        public String toString(){
            return "Passed :"+msg;
        }
    }
    
    public void assertTrue(boolean expr, String msg){
        if ( expr ) results.put(msg, new Passed(msg));
        else results.put(msg, new Failed(msg));
    }
    
    public void assertEquals(Object expected, Object actual, String label){
        if ( (expected != null && expected.equals(actual)) || ( expected == null && actual == null) ){
            results.put(label, new Passed(label));
        } else {
            results.put(label, new Failed(label+": expected ["+expected+"] but actual was ["+actual+"]"));
        }
    }
    
    protected void printResultLog(boolean result){
        Enumeration e = results.elements();
        
        while ( e.hasMoreElements() ){
            Result r = (Result)e.nextElement();
            if ( r.result == result ){
                Log.p(r.toString());
            }
        }
        
        e = subtests.elements();
        while ( e.hasMoreElements() ){
            ((BaseTest)e.nextElement()).printResultLog(result);
        }
    }
    
    public void printPassedLog(){
        printResultLog(true);
    }
    
    public void printFailedLog(){
        printResultLog(false);
    }
    
    public int numWithResult(boolean result){
        int numPassed = 0;
        Enumeration e = results.elements();
        while ( e.hasMoreElements() ){
            Result r = (Result)e.nextElement();
            if ( r.result == result ){
                numPassed++;
            }
        }
        
        
        e = subtests.elements();
        while ( e.hasMoreElements() ){
            numPassed += ((BaseTest)e.nextElement()).numWithResult(result);
        }
        return numPassed;
        
    }
    
    public int numPassed(){
        return numWithResult(true);
    }
    
    public int numFailed(){
        return numWithResult(false);
        
    }
    
    public Vector getResults(boolean result){
        Vector out = new Vector();
        Enumeration e = results.elements();
        while ( e.hasMoreElements() ){
            Result r = (Result)e.nextElement();
            if ( r.result == result ){
                out.addElement(r);
            }
        }
        e = subtests.elements();
        while ( e.hasMoreElements() ){
            out.addAll(((BaseTest)e.nextElement()).getResults(result));
        }
        return out;
    }
    
    public Vector getPassed(){
        return getResults(true);
    }
    
    public Vector getFailed(){
        return getResults(false);
    }
    
    public void printResults(){
        printPassedLog();
        
        printFailedLog();
        
        Log.p(numPassed()+" passed; "+numFailed()+" failed.");
    }
    
    public void addTest(BaseTest test){
        subtests.addElement(test);
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
