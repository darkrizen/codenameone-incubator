/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.js;

import com.codename1.io.Log;
import com.codename1.util.StringUtil;
import java.util.Vector;

/**
 *
 * @author shannah
 */
public class JSObject {
    JavascriptContext context;
    int objectId = 0;
    public static final String R1 = "ca_weblite_codename1_js_JSObject_R1";
    public static final String R2 = "ca_weblite_codename1_js_JSObject_R2";
    public static final String ID_KEY = "ca_weblite_codename1_js_JSObject_ID";
    
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
    
    public Object get(String key){
        return context.get(toJSPointer()+"."+key);
    }
    
    public void set(String key, Object js){
        context.set(toJSPointer()+"."+key, js);
    }

    public String toJSPointer() {
        return context.jsLookupTable+"["+objectId+"]";
    }
    
    public String exec(String js){
        return context.browser.executeAndReturnString(js);
    }
    
    public void addCallback(String key, final JSFunction func ){
        context.addCallback(this, key, func);
    }
    
    public void removeCallback(String key){
        context.removeCallback(this, key);
    }
    
    public Object call(String key, Object[] params){
        return context.call(toJSPointer()+"."+key, this, params);
        
    }
    
}
