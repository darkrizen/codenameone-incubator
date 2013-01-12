/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.js;

import com.codename1.io.Log;
import com.codename1.util.StringUtil;
import java.util.Vector;

/**
 * A Java Wrapper around a Javascript object. In Javascript there are only a few
 * different types:  Number, String, Boolean, Null, Undefined, and Object.
 * 
 * Arrays and functions are objects also.
 * 
 * <p>A JSObject is associated with a particular JavascriptContex and it is backed
 * by a Javascript object inside the javascript context.  This object acts as a 
 * proxy to call methods and access properties of the actual javascript object.</p>
 * 
 * <p>All return values for Javascript calls in the JavascriptContext will be 
 * converted to the appropriate Java type.  Javascript objects will automatically
 * be wrapped in a JSObject proxy.</p>
 * @author shannah
 */
public class JSObject {
    
    /**
     * The Javascript context that this object belongs to.
     */
    JavascriptContext context;
    
    /**
     * The ID of this object.  This is the ID within the Javascript lookup table
     * that stores a reference to the actual Javascript object.
     */
    int objectId = 0;
    
    /**
     * Javascript register variable 1.
     */
    public static final String R1 = "ca_weblite_codename1_js_JSObject_R1";
    
    /**
     * Javascript register variable 2.
     */
    public static final String R2 = "ca_weblite_codename1_js_JSObject_R2";
    
    /**
     * The key, within a Javascript object that stores the ID of an object. 
     * When a JSObject is initially created for a javascript object, an ID 
     * is generated and used to store a reference to the javascript object
     * in the Javascript lookup table - and this ID is stored in the object
     * itself.  This "ID_KEY" variable is the name of the property that stores
     * this ID within the object.
     */
    public static final String ID_KEY = "ca_weblite_codename1_js_JSObject_ID";
    
    /**
     * Constructor for a JSObject.
     * @param context The javascript context in which this object is being created.
     * 
     * @param expr A javascript expression that resolves to an Javascript Object.
     */
    public JSObject(JavascriptContext context, String expr) {
        this.context = context;
        synchronized (context){
            String escaped = StringUtil.replaceAll(expr, "\\", "\\\\");
            escaped = StringUtil.replaceAll(escaped, "'", "\\'");
            exec(R1+"=eval('"+escaped+"')");
            String type = exec("typeof("+R1+")");
            if ( !"object".equals(type) && !"function".equals(type)){
                throw new JSException("Attempt to create JSObject for expression "+expr+" which does not evaluate to an object.");
            }
            
            String lt = context.jsLookupTable;
            String js = "var id = "+R1+"."+ID_KEY+"; "+
                        "if (typeof(id)=='undefined' || typeof("+lt+"[id]) == 'undefined' || "+lt+"[id]."+ID_KEY+"!=id){"+
                            lt+".push("+R1+"); id="+lt+".indexOf("+R1+"); "+R1+"."+ID_KEY+"=id;"+
                        "} id";
            
           
            String id = exec(js);
            this.objectId = Integer.parseInt(id);
            
        }
    }
    
    /**
     * Returns a member variable of the Javascript object.  
     * 
     * <p>E.g., suppose you have a Javascript object myCar whose JSON representation
     * is </p>
     * <code>
     * { make : 'Ford', model : 'Escort', 'year' : 1989}
     * </code>
     * 
     * <p>Then the JSObject proxy for this object could call:</p>
     * <code>
     * String model = (String)myCar.get("model");
     * </code>
     * 
     * <p>And this would return "Ford".</p>
     * 
     * 
     * @param key The name of the property to retrieve on this object.
     * @return The value of the property specified converted to the appropriate 
     * Java value.  See @ref JavascriptContext.get() for a table of the Javascript
     * to Java type conversions.
     */
    public Object get(String key){
        if ( key.indexOf("'") == 0 ){
            return context.get(toJSPointer()+"["+key+"]");
        } else {
            return context.get(toJSPointer()+"."+key);
        }
    }
    
    public Object get(int index){
        return context.get(toJSPointer()+"["+index+"]");
    }
    
    
    /**
     * Sets a property on the underlying Javascript object.
     * @param key The name of the property to set on the current object.
     * @param js The value of the property.  This value should be provided as a 
     * Java value and it will be converted to the appropriate Javascript value.
     * See @ref JavascriptContext.set() for a conversion table of the Java to
     * Javascript type conversions.
     */
    public void set(String key, Object js){
        if ( key.indexOf("'") == 0 ){
            context.set(toJSPointer()+"["+key+"]", js);
        } else {
            context.set(toJSPointer()+"."+key, js);
        }
    }
    
    public void set(int index, Object js){
        context.set(toJSPointer()+"["+index+"]", js);
    }

    /**
     * Returns a Javascript variable name for the underlying Javascript object.  This
     * refers to the object inside the JavascriptContext's lookup table.
     * @return 
     */
    public String toJSPointer() {
        return context.jsLookupTable+"["+objectId+"]";
    }
    
    /**
     * Convenience method.  A wrapper around BrowserComponent.executeAndReturnString()
     * @param js
     * @return 
     */
    private String exec(String js){
        return context.browser.executeAndReturnString(js);
    }
    
    /**
     * Registers a JSFunction with the Javascript context so that it can handle
     * calls from Javascript.  This installs a Javascript proxy method that 
     * sends a message, via the BrowserNavigationCallback mechanism to the
     * JavascriptContext object so that the actual Java code in the JSFunction
     * will be called.
     * @param key The name of the property on the underlying Javascript object
     * where the method proxy will be added in Javascript.
     * @param func A JSFunction callback that will be called when the generated
     * Javascript proxy method is called.
     */
    public void addCallback(String key, JSFunction func ){
        context.addCallback(this, key, func);
    }
    
    /**
     * Removes a previously added JSFunction callback from the object.
     * @param key The name of the property on the underlying Javascript object
     * that is to be deleted.
     */
    public void removeCallback(String key){
        context.removeCallback(this, key);
    }
    
    /**
     * Calls a method on the underlying Javascript object.
     * @param key The name of the method to call.
     * @param params Array of parameters to pass to the method.  These will be 
     * converted to corresponding Javascript types according to the translation 
     * table specified in @ref JavascriptContext.set()
     * @return The result of calling the method.  Javascript return values will
     * be converted to corresponding Java types according to the rules described
     * in @ref JavascriptContext.get()
     */
    public Object call(String key, Object[] params){
        return context.call(toJSPointer()+"."+key, this, params);
        
    }
    
   
    
}
