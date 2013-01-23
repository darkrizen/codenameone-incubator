/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.tests;

import ca.weblite.codename1.js.JSFunction;
import ca.weblite.codename1.js.JSObject;
import ca.weblite.codename1.js.JavascriptContext;
import com.codename1.components.WebBrowser;
import com.codename1.io.Log;
import com.codename1.ui.BrowserComponent;
import java.util.Vector;

/**
 *
 * @author shannah
 */
public class JavascriptTests extends BaseTest {
    
    JavascriptContext ctx;
    
    public void anotherTest(){
        final WebBrowser b = new WebBrowser(){
            @Override
            public void onLoad(String url) {
                // Placed on onLoad because we need to wait for page 
                // to load to interact with it.
                
                BrowserComponent c = (BrowserComponent)this.getInternal();
                
                // Create a Javascript context for this BrowserComponent
                JavascriptContext ctx = new JavascriptContext(c);
                
                // Now use the context to interact with Javascript environment....
                
                // ...
            } 
        };  
    }
    
    public void loadingPrimitives(){
        final WebBrowser b = new WebBrowser(){
            @Override
            public void onLoad(String url) {
                // Placed on onLoad because we need to wait for page 
                // to load to interact with it.
                
                BrowserComponent c = (BrowserComponent)this.getInternal();
                
                // Create a Javascript context for this BrowserComponent
                JavascriptContext ctx = new JavascriptContext(c);
                
                // Now use the context to interact with Javascript environment....
                
                String pageContent = (String)ctx.get("document.body.innerHTML");
               
            } 
        };  
    }
    
    public void testExecute(){
        
        final WebBrowser b = new WebBrowser(){
            @Override
            public void onLoad(String url) {
                Log.p("Starting onLoad for "+url);
                super.onLoad(url);
                ctx.DEBUG = false;
                ctx.set("window.myvar", "Hello World");
                String val = (String)ctx.get("window.myvar");
                assertEquals("Hello World", val, "Setting getting value in Javascript context");
                
                JSObject newObject = (JSObject)ctx.get("{val1:'testvalue'}");
                String val1 = (String)newObject.get("val1");
                assertEquals("testvalue", val1, "Value in new object in Javascript context");
                //Log.p("The value of val1 is "+val1);
                
                newObject.set("val2", "Another Value");
                String val2 = (String)newObject.get("val2");
                assertEquals("Another Value", val2, "Value in new object setting from Java");
                
                newObject.set("intmember", new Integer(5));
                Double intmember = (Double)newObject.get("intmember");
                assertEquals(new Double(5), intmember, "A JS numerical member value");
                
                String callResult = (String)ctx.call("f=function(){ return 'foo';}", newObject, new Object[0]);
                assertEquals("foo", callResult, "Result of javascript call");
                
                JSObject newFunc = (JSObject)ctx.get("function(){return 'bar';}");
                newObject.set("newFunc", newFunc);
                
                callResult = (String)newObject.call("newFunc", new Object[0]);
                assertEquals("bar", callResult, "JSObject Object method result");
                
                // Now let's test some parameters
                
                JSObject add = (JSObject)ctx.get("function(a,b){return a+b}");
                newObject.set("add", add);
                
                Double addResult = (Double)newObject.call("add", new Object[]{ new Double(5), new Double(4)});
                assertEquals(new Double(9), addResult, "JSObject addition Method");
                
                
                // Now let's try to pass an object as a parameter
                JSObject recAdd = (JSObject)ctx.get("(function(a,b,obj){ return a+b+obj.add(a,b);})");
                newObject.set("recAdd", recAdd);
                
                // We will pass the same object as the third parameter so we can make sure that
                // its add() method can be called within the recAdd() method.
                Double recAddResult = (Double)newObject.call("recAdd", new Object[]{ new Double(5), new Double(4), newObject});
                assertEquals(new Double(18), recAddResult, "JSObject rec-addition method");
                
                
                // Try passing strings to the same "add" method.
                String addStringResult = (String)newObject.call("add", new Object[]{"Foo", "bar"});
                assertEquals("Foobar", addStringResult, "JSObject add strings");
                
                JSFunction callback = new JSFunction(){

                    public void apply(final JSObject self, final Object[] args) {
                        Thread t = new Thread(new Runnable(){

                            public void run() {
                                Double intmember = (Double)self.get("intmember");
                                assertEquals(new Double(5), intmember, "Getting int member inside Java callback inside new thread");
                            }
                            
                        });
                        t.start();
                        
                        Double intmember = (Double)self.get("intmember");
                        assertEquals(new Double(5), intmember, "Getting int member inside Java callback on EDT");
                    }
                    
                };
                
                newObject.set("javaCallback", callback);
                
                newObject.call("javaCallback", new Object[0]);
                
            }
            
        };
        ctx = new JavascriptContext((BrowserComponent)b.getInternal());
        b.setURL("http://solutions.weblite.ca/index.html");
        
    }
    
    public void _run(){
        testExecute();
    }
}
