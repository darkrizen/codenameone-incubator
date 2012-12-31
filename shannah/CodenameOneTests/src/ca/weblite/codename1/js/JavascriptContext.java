/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.js;

import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.Display;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.util.StringUtil;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author shannah
 */
public class JavascriptContext  {
    public static boolean DEBUG=false;
    BrowserComponent browser;
    private ActionListener urlListener, scriptMessageListener;
    String jsLookupTable;
    int objectId = 0;
    private Hashtable callbacks = new Hashtable();
    private static int contextId = 0;
    private static int callbackId = 0;
    
    public static final String DUMMY_VAR = "ca_weblite_codename1_js_JavascriptContext_DUMMY_VAR";
    public static final String RETURN_VAR = "ca_weblite_codename1_js_JavascriptContext_RETURN_VAR";
    public static final String LOOKUP_TABLE = "ca_weblite_codename1_js_JavascriptContext_LOOKUP_TABLE";
    public static final String STACK = "ca_weblite_codename1_js_JavascriptContext_STACK";
    
    
    public JavascriptContext(BrowserComponent c){
        jsLookupTable = LOOKUP_TABLE+(contextId++);
        this.urlListener = new URLListener();
        this.scriptMessageListener = new ScriptMessageListener();
        this.setBrowserComponent(c);
    }
    
    public final void setBrowserComponent(BrowserComponent c){
        if ( c != browser ){
            if ( browser != null ){
                this.uninstall();
            }
            browser = c;
            if ( browser != null ){
                this.install();
                
            }
        }
    }
    
    private synchronized String exec(String js){
        if ( DEBUG ){
            //Log.p("About to execute "+js);
        }
        return browser.executeAndReturnString(installCode()+"("+js+")");
    }
    
    private void uninstall(){
        browser.removeWebEventListener("shouldLoadURL", urlListener);
        browser.removeWebEventListener("scriptMessageReceived", scriptMessageListener);
    }
    private void install(){
        browser.addWebEventListener("shouldLoadURL", urlListener);
        browser.addWebEventListener("scriptMessageReceived", scriptMessageListener);
    }
    
    
    public synchronized Object get(String javascript){
        String js2 = RETURN_VAR+"=("+javascript+")";
        String res = exec(js2);
        String typeQuery = "typeof("+RETURN_VAR+")";
        String type = exec(typeQuery);
        try {
            if ( "string".equals(type)){
                return res;
            } else if ( "number".equals(type)){
                return Double.valueOf(res);
            } else if ( "boolean".equals(type)){
                return "true".equals(res)?Boolean.TRUE:Boolean.FALSE;
            } else if ( "object".equals(type) || "function".equals(type)){
                return new JSObject(this, RETURN_VAR);
            } else {
                return null;
            }
        } catch ( Exception ex){
            Log.e(new RuntimeException("Failed to get javascript "+js2+".  The error was "+ex.getMessage()+".  The result was "+res+".  The type result was "+type+"."));
            return null;
        }
    }
    
    public synchronized void set(String key, Object value){
        String lhs = key;
        String rhs = "undefined";
      
        if ( String.class.isInstance(value)){
            String escaped = StringUtil.replaceAll((String)value, "\\", "\\\\");
            escaped = StringUtil.replaceAll(escaped, "'", "\\'");
            rhs = "'"+escaped+"'";
        } else if ( value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double ){
            rhs =value.toString();
        } else if ( JSObject.class.isInstance(value)){
            rhs = ((JSObject)value).toJSPointer();
        } else if (value instanceof Boolean){
            rhs = ((Boolean)value).booleanValue()?"true":"false";
        } else {
            rhs = "null";
        }
        
        exec(lhs+"="+rhs);
    }
    
    
    
    public void dispatchCallback(final String request){
        Runnable r = new Runnable(){
            public void run(){
                String command = request.substring(request.indexOf(":")+1);
                // Get the callback id
                String objMethod = command.substring(0, command.indexOf("?"));
                command = command.substring(command.indexOf("?")+1);

                final String self = objMethod.substring(0, objMethod.indexOf("."));
                String method = objMethod.substring(objMethod.indexOf(".")+1);

                // Now let's get the parameters
                String[] keyValuePairs = Util.split(command, "&");
                //Vector params = new Vector();

                int len = keyValuePairs.length;
                Object[] params = new Object[len];
                for ( int i=0; i<len; i++){
                    String[] parts = Util.split(keyValuePairs[i], "=");
                    if ( parts.length < 2 ){
                        continue;
                    }
                    String ptype = Util.decode(parts[0], null, true);
                    String pval = Util.decode(parts[1], null, true);
                    if ( "object".equals(ptype) || "function".equals(ptype) ){
                        params[i] = new JSObject(JavascriptContext.this, pval);
                    } else if ( "number".equals(ptype) ){
                        params[i] = Double.valueOf(pval);
                    } else if ( "string".equals(ptype)){
                        params[i] = pval;
                    } else if ( "boolean".equals(ptype)){
                        params[i] = "true".equals(pval)?Boolean.TRUE:Boolean.FALSE;
                    } else {
                        params[i] = null;
                    }
                }
                JSObject selfObj = new JSObject(JavascriptContext.this, self);

                JavascriptEvent evt = new JavascriptEvent(selfObj, method, params);
                browser.fireWebEvent("scriptMessageReceived", evt);
            }
        };
        
        Display.getInstance().callSerially(r);
        
        
        
    }
    
    
    private class URLListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            //if ( true ) return;
            String url = (String)evt.getSource();
            if ( url.indexOf("cn1command:") == 0 ){
                //.. Handle the cn1 callbacks
                dispatchCallback(url);
                evt.consume();
            }
        }
    }
    
    private class ScriptMessageListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            JavascriptEvent jevt = (JavascriptEvent)evt;
            JSObject source = jevt.getSelf();
            String method = jevt.getMethod();
            String key = source.toJSPointer()+"."+method;
            JSFunction func = (JSFunction)callbacks.get(key);
            if ( func == null ){
                // No callback is registered for this method.
                return;
            }
            func.apply(source, jevt.getArgs());
            evt.consume();
        }
        
    }
    
    
    
    private String installCode(){
        return "if (typeof("+jsLookupTable+") != 'object'){"+jsLookupTable+"=[]}"+
            " if ( typeof("+STACK+") == 'undefined'){"+STACK+"=[]}";
    }
    
    
    public void addCallback(JSObject source, String method, JSFunction callback){
        String key = source.toJSPointer()+"."+method;
        callbacks.put(key, callback);
        
        String id = JSObject.ID_KEY;
        //String lookup = LOOKUP_TABLE;
        String self = source.toJSPointer();
        String js = self+"."+method+"=function(){"+
                "var len=arguments.length;var url='cn1command:"+self+"."+method+"?'; "+
                "for (var i=0; i<len; i++){"+
                    "var val = arguments[i];"+
                    "if ( typeof(arguments[i]) == 'object' ){ "+
                        "var id = arguments[i]."+id+"; "+
                        "if (typeof(id)=='undefined' || typeof("+jsLookupTable+"[id]) == 'undefined' || "+jsLookupTable+"[id]."+id+"!=id){"+
                            jsLookupTable+".push(arguments[i]); id="+jsLookupTable+".indexOf(arguments[i]); arguments[i]."+id+"=id;"+
                        "}"+
                        "val="+jsLookupTable+"[id]"+
                    "}"+
                    "url += encodeURIComponent(typeof(arguments[i]))+'='+encodeURIComponent(val);"+
                    "if (i < len-1){ url += '&';}"+
                "} window.location=url;"+
                //"} return 56;"+
                //"console.log('About to try to load '+url); var el = document.createElement('iframe'); el.setAttribute('src', url); document.body.appendChild(el); el.parentNode.removeChild(el); console.log(el); el = null"+
            "}";
        //String js2 = self+"."+method+"=function(){console.log('This is the alternate java native call method');}";
        exec(js);
           
        
    }
    
    public void removeCallback(JSObject source, String method){
        String key = source.toJSPointer()+"."+method;
        callbacks.remove(key);
        String js = "delete "+source.toJSPointer()+"."+method;
        exec(js);
    }
    
    public Object call(String jsFunc, JSObject self, Object[] params){
        String var = RETURN_VAR+"_call";
        String js = var+"=("+jsFunc+").call("+self.toJSPointer();
        int len = params.length;
        for ( int i=0; i<len; i++){
            Object param = params[i];
            js += ", ";
            
            if ( param instanceof Integer || param instanceof Long || param instanceof Double || param instanceof Float){
                js += param.toString();
            } else if ( param instanceof Boolean ){
                js += ((Boolean)param).booleanValue()?"true":"false";
            } else if ( param instanceof String ){
                js += "'"+StringUtil.replaceAll((String)param, "'", "\\'")+"'";
            } else if ( param instanceof JSObject ){
                js += ((JSObject)param).toJSPointer();
            } else {
                js += "null";
            }
            
        }
        js += ")";
        // We need to intialize the var to undefined in case the actual
        // javascript adjusts the window.location or doesn't cause a 
        // result for some reason.
        try {
            exec(var+"=undefined");
        } catch (Exception ex){
            Log.e(new RuntimeException("Failed to execute javascript "+var+"=undefined.  The error was "+ex.getMessage()));
            return null;
        }
        try {
            exec(js);
        } catch (Exception ex){
            Log.e(new RuntimeException("Failed to execute javascript "+js+".  The error was "+ex.getMessage()));
            return null;
        }
        try {
            return get(var);
        } catch (Exception ex){
            Log.e(new RuntimeException("Failed to get the javascript variable "+var+".  The error was "+ex.getMessage()));
            return null;
        }
    }
    
    
    
    
}
