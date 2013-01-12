/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.js;

import com.codename1.ui.events.ActionEvent;


/**
 * An event that encapsulates a Javascript method call.  When commands are
 * received in a JavascriptContext via the BrowserNavigationCallback mechanism,
 * the requests are parsed and wrapped in a JavascriptEvent, which is then fired
 * and ultimately handled by another event handler to actually call the method.
 * @author shannah
 */
class JavascriptEvent extends ActionEvent {
    
    Object[] args;
    String method;
    public JavascriptEvent(JSObject source, String method, Object[] args){
        super(source);
        this.args = args;
        this.method = method;
    }
    
    public Object[] getArgs(){
        return args;
    }
    
    public String getMethod(){
        return method;
    }
    
    public JSObject getSelf(){
        return (JSObject)this.getSource();
    }
}
