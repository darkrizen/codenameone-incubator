/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.tests;

import com.codename1.components.WebBrowser;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.NetworkManager;
import com.codename1.io.NetworkEvent;

import com.codename1.io.Log;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.Form;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shannah
 */
public class CookiesTest extends BaseTest implements Runnable  {
    
    String url = "http://dev.weblite.ca/codenameone/tests/testcookie.php";
    Form form = null;
    
    public CookiesTest(Form form){
        this.form = form;
    }

    public void _run() {
        
        
        
        WebBrowser browser = new WebBrowser(){

            @Override
            public void onLoad(String url) {
                super.onLoad(url);
                Log.p("The page "+url+" was loaded in the browser");
                
                BrowserComponent bc = (BrowserComponent)this.getInternal();
                String result = bc.executeAndReturnString("document.body.innerHTML");
                Log.p("Result is "+result);
                
                ConnectionRequest req = new ConnectionRequest(){

                    @Override
                    protected void readResponse(InputStream input) throws IOException {
                        JSONParser p = new JSONParser();
                        Hashtable h = (Hashtable)p.parse(new InputStreamReader(input));
                        assertEquals("testvalue", h.get("testcookie"), "Cookie shared between browser and connectionrequest");
                    }
                    
                };
                req.setUrl(CookiesTest.this.url);
                req.addResponseCodeListener(new ActionListener(){

                    public void actionPerformed(ActionEvent evt) {
                        throw new RuntimeException("Failed to connect to test cookie script.");
                    }
                    
                });
                
                req.setPost(false);
                req.setHttpMethod("GET");
                NetworkManager.getInstance().addToQueue(req);
                
            }

            
            
            
            
        };
        
        String addr = url + "?name=testcookie&value=testvalue";
        //String addr = "androidurl://myurl";
        
        Log.p("About to set URL");
        
        //browser.setPreferredSize(new Dimension(200,200));
        //form.getContentPane().addComponent(BorderLayout.CENTER, browser);
        
        //form.setShouldCalcPreferredSize(true);
        //form.animateLayout(1000);
        //form.show();
        browser.setURL(addr+"&--format=html");
        
        
        
        // Now let's do something different
        // We'll set a cookie using a regular network request and see if it correctly adds
        // the cookie to the webview.
        
        addr = url+"?name=testcookie2&value=testvalue2";
        ConnectionRequest req = new ConnectionRequest();
        req.addResponseListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                
                // Now let's connect with WebView
                final WebBrowser b = new WebBrowser(){

                    @Override
                    public void onLoad(String url) {
                        try {
                            super.onLoad(url);
                            Log.p("The page "+url+" was loaded in the browser 2");
                            
                            BrowserComponent bc = (BrowserComponent)this.getInternal();
                            
                            //bc.execute("document.write('hello world')");
                            //bc.execute("document.write(foo.doSomething())");
                            
                            String documentBody = bc.executeAndReturnString("document.body.innerHTML");
                            JSONParser p = new JSONParser();
                            Hashtable res = (Hashtable)p.parse(new StringReader(documentBody));
                            assertEquals("testvalue2", res.get("testcookie2"), "Cookie set in network request loaded in browser");
                        } catch (IOException ex) {
                            assertTrue(false, "Failed to parse JSON response from web browser");
                        }
                    }
                    
                    
                    
                    public void setPageContents(){
                        Log.p("In setPageContents");
                    }
                    
                    
                };
                Object foo = new Object(){

                    public String doSomething(){
                        Log.p("Doing something");
                        return "Doing something";
                    }
                };
                //((BrowserComponent)b.getInternal()).exposeInJavaScript(foo, "foo");
                b.setURL(url+"?--format=html");
                form.getContentPane().addComponent(BorderLayout.CENTER, b);
                form.revalidate();
                
            }
            
        });
        req.setUrl(addr);
        req.setPost(false);
                req.setHttpMethod("GET");
                NetworkManager.getInstance().addToQueue(req);
        
        
    }
    
}
