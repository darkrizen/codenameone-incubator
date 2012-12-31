/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.js;

import com.codename1.ui.events.ActionEvent;


/**
 *
 * @author shannah
 */
public class JavascriptEvent extends ActionEvent {
    
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
