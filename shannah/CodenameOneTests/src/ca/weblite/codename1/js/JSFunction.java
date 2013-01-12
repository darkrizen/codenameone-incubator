/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.js;

/**
 * A callback function that can be registered with a Javascript context to allow
 * Javascript to call into Java.
 * 
 * @see JavascriptContext.registerCallback()
 * @see JSObject.registerCallback()
 * @author shannah
 */
public interface JSFunction {
    public void apply(JSObject self, Object[] args);
}
