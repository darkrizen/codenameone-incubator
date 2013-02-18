package ca.weblite.codename1.tests;


import ca.weblite.codename1.js.JSFunction;
import ca.weblite.codename1.js.JSObject;
import ca.weblite.codename1.js.JavascriptContext;
import ca.weblite.codename1.maps.DirectionsRequest;
import ca.weblite.codename1.maps.DirectionsResult;
import ca.weblite.codename1.maps.DirectionsRouteListener;
import ca.weblite.codename1.maps.GoogleMap;
import com.codename1.components.WebBrowser;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.io.Log;
import com.codename1.ui.Container;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.List;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.events.DataChangedListener;
import com.codename1.ui.events.SelectionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.list.ListCellRenderer;
import com.codename1.ui.list.ListModel;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;



public class CodenameOneTests {

    private Form current;
    BaseTest test;

    public void init(Object context) {
        try{
            Resources theme = Resources.openLayered("/theme");
            UIManager.getInstance().setThemeProps(theme.getTheme(theme.getThemeResourceNames()[0]));
            UIManager.getInstance().setThemeProps(theme.getTheme("Theme 1"));
       }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public void start_hi_world(){
        Log.p("We are in the start method!!!");
        Form hi = new Form("Hi World");
        hi.setLayout(new BorderLayout());
        hi.addComponent(BorderLayout.CENTER, new Label("Test label"));
        hi.show();
    }
    
    public void start_complex(){
        try {
            startThrows();
        } catch (Throwable t){
            t.printStackTrace();
            final Form hi = new Form("Hi World");
            hi.addComponent(new Label("An error occurred "+t.getMessage()));
            hi.show();
        }
        
    }
    
    public void startThrows() throws Throwable{
        System.out.println("In CodenameOne startThrows");
        Log.p("In CodenameOneTests.startsThrows()");
        final WebBrowser b = new WebBrowser();
        final Form hi = new Form("Hi World"){

            @Override
            protected void onShowCompleted() {
                /*
                try {
                   Log.p("In onShowCompleted");
                    super.onShowCompleted();
                    b.setPage("<html><body>Hello World</body></html>", "file:///");
                    this.removeComponent(b);
                    this.addComponent(BorderLayout.CENTER, b);
                    this.revalidate();  
                    Log.p("Finished onShowCompleted");
                } catch ( Throwable t){
                    Log.p("Error occurred inOnShowCompleted: "+t.getMessage());
                    t.printStackTrace();
                }
                * */
            }
            
        };
        hi.setLayout(new BorderLayout());
        
        
        //final Label resultLabel = new Label("ResultPlaceholder");
        final TextArea resultLabel = new TextArea();
        resultLabel.setRows(5);
        
        resultLabel.setText("REsult placeholder");
        
        hi.addComponent(BorderLayout.NORTH, resultLabel);
        hi.addComponent(BorderLayout.CENTER, b);
        
        Button btn = new Button("Run Test");
        btn.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                Log.p("In btn.actionPerformed");
                try {
                    if ( BrowserComponent.isNativeBrowserSupported() ){
                        final BrowserComponent c = (BrowserComponent)b.getInternal();

                        String result = c.executeAndReturnString("'test string'");
                        resultLabel.setText(result);
                    } else {
                        resultLabel.setText("Native browser not supported");
                    }
                } catch ( Throwable t){
                    Log.p("CodenameOneTests Error "+t.getMessage());
                    t.printStackTrace();
                }
                
            }
            
        });
        
        Container south = new Container(new BoxLayout(BoxLayout.X_AXIS));
        
        hi.addComponent(BorderLayout.SOUTH, south);
        
        
        Button reload = new Button("Reload");
        reload.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                try {
                    hi.removeComponent(b);
                    hi.addComponent(BorderLayout.CENTER, b);
                    b.setPage("<html><body>Hello World</body></html>", "file:///");
                    
                    hi.revalidate();
                } catch (Throwable t){
                    t.printStackTrace();
                }
            }
            
        });
        south.addComponent(reload);
        south.addComponent(btn);
        
        hi.show();
        //b.setPage("<html><body>Hello World</body></html>", "file:///");
        //b.setURL("http://solutions.weblite.ca/index.html");
        
        
    }
    
    public void start_CameraExample(){
        final Form hi = new Form("Hi World");
        final WebBrowser b = new WebBrowser(){

            @Override
            public void onStart(String url) {
                super.onStart(url);
                Log.p("in onStart "+url);
            }
            
            @Override
            public void onLoad(String url) {
                // Placed on onLoad because we need to wait for page 
                // to load to interact with it.
                Log.p("Inside onLoad");
                final BrowserComponent c = (BrowserComponent)this.getInternal();
                
                // Create a Javascript context for this BrowserComponent
                final JavascriptContext ctx = new JavascriptContext(c);
                final JSObject camera = (JSObject)ctx.get("{}");
                camera.set("capture", new JSFunction(){

                    public void apply(JSObject self, final Object[] args) {
                        Log.p("About to capture photo");
                        Display.getInstance().capturePhoto(new ActionListener(){

                            public void actionPerformed(ActionEvent evt) {
                                Log.p("num args "+args.length);
                                c.revalidate();
                                // Get the image path from the taken image
                                String imagePath = (String)evt.getSource();
                                Log.p("Path is "+imagePath);
                                //if ( true ) return;
                                // Get the callback function that was provided
                                // from javascript
                                JSObject callback = (JSObject)args[0];
                                
                                
                                //Log.p(json);
                                //json = (String)ctx.get("console.log((function(a,b){return a+b}) )");
                                ctx.call(
                                        callback, // The function
                                        camera,   // The "this" object
                                        new Object[]{"file://"+imagePath}  // Parameters
                                );
                            }
                            
                        });
                    }
                    
                });
                
                
                ctx.set("window.camera", camera);
                
                
                //c.executeAndReturnString("camera.capture((function(url){document.body.innerHTML=url}));");
                
            } 
        };
        
        b.setURL("jar:///ca/weblite/codename1/tests/CameraExample.html");
        //b.setURL("http://solutions.weblite.ca/index.html");
        hi.setLayout(new BorderLayout());
        hi.addComponent(BorderLayout.CENTER, b);
        Button capture = new Button("Capture");
        capture.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                Display.getInstance().capturePhoto(new ActionListener(){

                    public void actionPerformed(ActionEvent evt) {
                        Log.p("Photo captured : "+evt.getSource());
                        try {
                            Label lbl = new Label(Image.createImage((String)evt.getSource()));
                            hi.addComponent(BorderLayout.CENTER, lbl);
                            hi.animate();
                        } catch (IOException ex) {
                            Log.e(ex);
                        }
                    }
                    
                });
            }
            
        });
        hi.addComponent(BorderLayout.SOUTH, capture);
        hi.show();
    }
    
    public void start12(){
        
        final Form hi = new Form("Hi World");
        final WebBrowser b = new WebBrowser(){
            @Override
            public void onLoad(String url) {
                // Placed on onLoad because we need to wait for page 
                // to load to interact with it.
                Log.p("Inside onLoad");
                final BrowserComponent c = (BrowserComponent)this.getInternal();
                
                // Create a Javascript context for this BrowserComponent
                final JavascriptContext ctx = new JavascriptContext(c);
                JSObject window = (JSObject)ctx.get("window");
                window.set("addAsync", new JSFunction(){

                    public void apply(JSObject self, final Object[] args) {
                        System.out.println("Params "+args[0]+", "+args[1]);
                        Double a = (Double)args[0];
                        Double b = (Double)args[1];
                        JSObject callback = (JSObject)args[2];
                        
                        double result = a.doubleValue() + b.doubleValue();
                        Log.p("Result is "+result);
                        callback.call(new Object[]{new Double(result)});
                        
                    }
                    
                });
                
                
                
                //c.executeAndReturnString("camera.capture((function(url){document.body.innerHTML=url}));");
                
            } 
        };
        
        //InputStream is = Display.getInstance().getResourceAsStream(this.getClass(), "AddAsync.html");
        
        
        //b.setPage("<html><body><button onclick=\"camera.capture(function(url){var div = document.createElement('div');div.innerHTML = '<img src=\\''+url+'\\'/>'; document.body.appendChild(div);})\">Capture Photo</button></html>", null);
        b.setURL("jar:///ca/weblite/codename1/tests/AddAsync.html");
        hi.setLayout(new BorderLayout());
        hi.addComponent(BorderLayout.CENTER, b);
        Button capture = new Button("Capture");
        capture.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                Display.getInstance().capturePhoto(new ActionListener(){

                    public void actionPerformed(ActionEvent evt) {
                        Log.p("Photo captured : "+evt.getSource());
                        try {
                            Label lbl = new Label(Image.createImage((String)evt.getSource()));
                            hi.addComponent(BorderLayout.CENTER, lbl);
                            hi.animate();
                        } catch (IOException ex) {
                            Log.e(ex);
                        }
                    }
                    
                });
            }
            
        });
        hi.addComponent(BorderLayout.SOUTH, capture);
        hi.show();
    }
    public void start10(){
        final Form hi = new Form("Hi World");
        final WebBrowser b = new WebBrowser();
        b.setPage("<html><body>Hello World!</body></html>", null);
        hi.setLayout(new BorderLayout());
        hi.addComponent(BorderLayout.CENTER, b);
        Button capture = new Button("Capture");
        capture.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                Display.getInstance().capturePhoto(new ActionListener(){

                    public void actionPerformed(ActionEvent evt) {
                        Log.p("Photo captured : "+evt.getSource());
                        
                    }
                    
                });
            }
            
        });
        hi.addComponent(BorderLayout.SOUTH, capture);
        hi.show();
    }
    
    public void start9(){
        
        final Form hi = new Form("Hi World");
        final WebBrowser b = new WebBrowser(){
            @Override
            public void onLoad(String url) {
                // Placed on onLoad because we need to wait for page 
                // to load to interact with it.
                Log.p("Inside onLoad");
                final BrowserComponent c = (BrowserComponent)this.getInternal();
                
                // Create a Javascript context for this BrowserComponent
                final JavascriptContext ctx = new JavascriptContext(c);
                final JSObject camera = (JSObject)ctx.get("{}");
                camera.set("capture", new JSFunction(){

                    public void apply(JSObject self, final Object[] args) {
                        Log.p("About to capture photo");
                        Display.getInstance().capturePhoto(new ActionListener(){

                            public void actionPerformed(ActionEvent evt) {
                                Log.p("num args "+args.length);
                                c.revalidate();
                                // Get the image path from the taken image
                                String imagePath = (String)evt.getSource();
                                Log.p("Path is "+imagePath);
                                //if ( true ) return;
                                // Get the callback function that was provided
                                // from javascript
                                JSObject callback = (JSObject)args[0];
                                
                                // Call the callback function
                                String sizeOfLookupTable = c.executeAndReturnString("ca_weblite_codename1_js_JavascriptContext_LOOKUP_TABLE0.length");
                                Log.p("lookup table size "+sizeOfLookupTable);
                                Log.p("About to call callback method "+callback.toJSPointer());
                                String type = (String)c.executeAndReturnString("typeof(ca_weblite_codename1_js_JavascriptContext_LOOKUP_TABLE0[1])");
                                Log.p("The type of it is "+type);
                                Log.p("The pointer is "+callback.toJSPointer());
                                c.executeAndReturnString(callback.toJSPointer()+"()");
                                
                                //Log.p(json);
                                //json = (String)ctx.get("console.log((function(a,b){return a+b}) )");
                                ctx.call(
                                        callback, // The function
                                        camera,   // The "this" object
                                        new Object[]{"file://"+imagePath}  // Parameters
                                );
                            }
                            
                        });
                    }
                    
                });
                
                
                ctx.set("window.camera", camera);
                
                
                //c.executeAndReturnString("camera.capture((function(url){document.body.innerHTML=url}));");
                
            } 
        };
        
        b.setPage("<html><body><button onclick=\"camera.capture(function(url){var div = document.createElement('div');div.innerHTML = '<img src=\\''+url+'\\'/>'; document.body.appendChild(div);})\">Capture Photo</button></html>", null);
        hi.setLayout(new BorderLayout());
        hi.addComponent(BorderLayout.CENTER, b);
        Button capture = new Button("Capture");
        capture.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                Display.getInstance().capturePhoto(new ActionListener(){

                    public void actionPerformed(ActionEvent evt) {
                        Log.p("Photo captured : "+evt.getSource());
                        try {
                            Label lbl = new Label(Image.createImage((String)evt.getSource()));
                            hi.addComponent(BorderLayout.CENTER, lbl);
                            hi.animate();
                        } catch (IOException ex) {
                            Log.e(ex);
                        }
                    }
                    
                });
            }
            
        });
        hi.addComponent(BorderLayout.SOUTH, capture);
        hi.show();
    }
    public void start8(){
        Form hi = new Form("Hi World");
        final WebBrowser b = new WebBrowser(){
            @Override
            public void onLoad(String url) {
                // Placed on onLoad because we need to wait for page 
                // to load to interact with it.
                
                BrowserComponent c = (BrowserComponent)this.getInternal();
                
                // Create a Javascript context for this BrowserComponent
                JavascriptContext ctx = new JavascriptContext(c);
                JSObject logger = (JSObject)ctx.get("{}");
                logger.set("log", new JSFunction(){

                    public void apply(JSObject self, Object[] args) {
                        String msg = (String)args[0];
                        Log.p("[Javascript Logger] "+msg);
                    }
                    
                });
                
                ctx.set("window.logger", logger);
                
                
                c.executeAndReturnString("logger.log('This is a test message');");
                
            } 
        };
        
        b.setPage("<html><body>Hello World</body></html>", null);
        hi.setLayout(new BorderLayout());
        hi.addComponent(BorderLayout.CENTER, b);
        hi.show();
        
    }
    
    public void start7(){
        Form hi = new Form("Hi World");
        final WebBrowser b = new WebBrowser(){
            @Override
            public void onLoad(String url) {
                // Placed on onLoad because we need to wait for page 
                // to load to interact with it.
                
                BrowserComponent c = (BrowserComponent)this.getInternal();
                
                // Create a Javascript context for this BrowserComponent
                JavascriptContext ctx = new JavascriptContext(c);
                JSObject steve = (JSObject)ctx.get("{}");
                steve.set("name", "Steve");
                steve.set("age", new Integer(34));
                ctx.set("window.steve", steve);
                
                c.executeAndReturnString("document.body.innerHTML='<p>'+steve.name+' is '+steve.age");
                
            } 
        };
        
        b.setPage("<html><body>Hello World</body></html>", null);
        hi.setLayout(new BorderLayout());
        hi.addComponent(BorderLayout.CENTER, b);
        hi.show();
    }
    
    public void start6(){
        Form hi = new Form("Hi World");
        final WebBrowser b = new WebBrowser(){
            @Override
            public void onLoad(String url) {
                // Placed on onLoad because we need to wait for page 
                // to load to interact with it.
                
                BrowserComponent c = (BrowserComponent)this.getInternal();
                
                // Create a Javascript context for this BrowserComponent
                JavascriptContext ctx = new JavascriptContext(c);
                
                ctx.set("window.location", "http://www.codenameone.com");
                
                //Dialog.show("Content", "Name is "+name+", age is "+age, "OK", "Cancel");
            } 
        };
        
        b.setPage("<html><body>Hello World</body></html>", null);
        hi.setLayout(new BorderLayout());
        hi.addComponent(BorderLayout.CENTER, b);
        hi.show();
    }
    
    public void start5(){
        Form hi = new Form("Hi World");
        final WebBrowser b = new WebBrowser(){
            @Override
            public void onLoad(String url) {
                // Placed on onLoad because we need to wait for page 
                // to load to interact with it.
                
                BrowserComponent c = (BrowserComponent)this.getInternal();
                
                // Create a Javascript context for this BrowserComponent
                JavascriptContext ctx = new JavascriptContext(c);
                
                // Now use the context to interact with Javascript environment....
                JSObject steve = (JSObject)ctx.get("{name : 'Steve', age: 34}");
                String name = (String)steve.get("name");
                Double age = (Double)steve.get("age");
                
                Dialog.show("Content", "Name is "+name+", age is "+age, "OK", "Cancel");
            } 
        };
        
        b.setPage("<html><body>Hello World</body></html>", null);
        hi.setLayout(new BorderLayout());
        hi.addComponent(BorderLayout.CENTER, b);
        hi.show();
    }
    
    
    public void start4(){
        Form hi = new Form("Hi World");
        final WebBrowser b = new WebBrowser(){
            @Override
            public void onLoad(String url) {
                // Placed on onLoad because we need to wait for page 
                // to load to interact with it.
                
                BrowserComponent c = (BrowserComponent)this.getInternal();
                
                // Create a Javascript context for this BrowserComponent
                JavascriptContext ctx = new JavascriptContext(c);
                
                // Now use the context to interact with Javascript environment....
                JSObject window = (JSObject)ctx.get("window");
                Double outerWidth = (Double)window.get("outerWidth");
                Double outerHeight = (Double)window.get("outerHeight");
                
                Dialog.show("Content", "Window size is "+outerWidth+"x"+outerHeight, "OK", "Cancel");
            } 
        };
        
        b.setPage("<html><body>Hello World</body></html>", null);
        hi.setLayout(new BorderLayout());
        hi.addComponent(BorderLayout.CENTER, b);
        hi.show();
    }
    public void start3(){
        Form hi = new Form("Hi World");
        final WebBrowser b = new WebBrowser(){
            @Override
            public void onLoad(String url) {
                // Placed on onLoad because we need to wait for page 
                // to load to interact with it.
                
                BrowserComponent c = (BrowserComponent)this.getInternal();
                
                // Create a Javascript context for this BrowserComponent
                JavascriptContext ctx = new JavascriptContext(c);
                
                // Now use the context to interact with Javascript environment....
                
                Double outerWidth = (Double)ctx.get("window.outerWidth");
                Double outerHeight = (Double)ctx.get("window.outerHeight");
                
                Dialog.show("Content", "Window size is "+outerWidth+"x"+outerHeight, "OK", "Cancel");
            } 
        };
        
        b.setPage("<html><body>Hello World</body></html>", null);
        hi.setLayout(new BorderLayout());
        hi.addComponent(BorderLayout.CENTER, b);
        hi.show();
    }
    
    public void start2(){
        Form hi = new Form("Hi World");
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
                
                Dialog.show("Content", pageContent, "OK", "Cancel");
            } 
        };
        
        b.setPage("<html><body>Hello World</body></html>", null);
        hi.setLayout(new BorderLayout());
        hi.addComponent(BorderLayout.CENTER, b);
        hi.show();
    }
    
    public void start0() {
        if(current != null){
            current.show();
            return;
        }
        Form hi = new Form("Hi World");
        //hi.addComponent(new Label("Hi World"));
        
        
        CookiesTest test = new CookiesTest(hi);
        test.run();
        hi.show();
    }
    
    public void start_bg_color(){
        Form hi = new Form("Hi World");
        hi.getStyle().setBgColor(0xFF0000);
        //hi.setLayout(new BorderLayout());
        //WebBrowser b = new WebBrowser();
        BrowserComponent b = new BrowserComponent();
        b.setNativeScrollingEnabled(false);
        //b.setPinchToZoomEnabled(true);
        //b.setOpaque(false);
        b.setHeight(100);
        //hi.addComponent(BorderLayout.CENTER, b);
        hi.addComponent(b);
        //b.setURL("http://google.com");
        b.setPage("<html><body><h1>Hello World</h1></body></html>", "http://localhost");
        hi.show();
    }
    
    public void start_gmap_web(){
        Form hi = new Form("Google Direction Service");
        
        WebBrowser b = new WebBrowser(){

            @Override
            public void onLoad(String url) {
                
            }
            
        };
        
        b.setURL("jar:///googlemaps.html");
        hi.setLayout(new BorderLayout());
        hi.addComponent(BorderLayout.CENTER, b);
        hi.show();
        
    }
    
    public void start(){
        Form hi = new Form("Google Direction Service");
        
        final GoogleMap map = new GoogleMap();
        hi.setLayout(new BorderLayout());
        
        final TextField start = new TextField();
        start.setHint("Start location");
        
        
        final TextField end = new TextField();
        end.setHint("Destination");
        
        Container form = new Container();
        form.setLayout(new BorderLayout());
        form.addComponent(BorderLayout.NORTH, start);
        form.addComponent(BorderLayout.SOUTH, end);
        
        ActionListener routeListener = new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                if ( !"".equals(start.getText()) && !"".equals(end.getText())){
                    DirectionsRequest req = new DirectionsRequest();
                    req.setTravelMode(DirectionsRequest.TRAVEL_MODE_DRIVING);
                    req.setOriginName(start.getText());
                    req.setDestinationName(end.getText());
                    map.route(req, new DirectionsRouteListener(){

                        public void routeCalculated(DirectionsResult result) {
                            System.out.println("Successfully mapped route");
                        }
                        
                    });
                }
            }
            
        };
        
        start.addActionListener(routeListener);
        end.addActionListener(routeListener);
        
        hi.addComponent(BorderLayout.NORTH, form);
        
        hi.addComponent(BorderLayout.CENTER, map);
        hi.show();
        
    }
    
    
    public void start_webbrowsers(){
        Form hi = new Form("Hi World");
        //hi.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        hi.setLayout(new BorderLayout());
        final int num = 1;
        //final WebBrowser[] b= new WebBrowser[num];
        final WebBrowser b = new WebBrowser();
        
        final TextField urlField = new TextField();
        urlField.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                for ( int i=0; i<num; i++){
                    b.setURL(urlField.getText());
                }
                
            }
            
        });
        
        hi.addComponent(BorderLayout.NORTH, urlField);
        
        for ( int i=0; i<num; i++){
            System.out.println("Adding web browser "+i);
            //b[i] = new WebBrowser();
            //b[i].setHeight(100);
            //((BrowserComponent)b.getInternal()).setOpaque(false);
            //((BrowserComponent)b.getInternal()).setNativeScrollingEnabled(false);
            hi.addComponent(BorderLayout.CENTER, b);
            //b.setURL("http://google.com");
        }
        
        
        
        
       
       
        
        
        hi.show();
       
        //b.setPage("<html><body style='background-color:transparent'><h1>Hello World</h1></body></html", "http://localhost/");
        //b1.setURL("jar:///ca/weblite/codename1/tests/testcss.html");
        //b2.setURL("jar:///ca/weblite/codename1/tests/testcss.html");
        
        
    }
    
    public void start_tests() {
        if(current != null){
            current.show();
            return;
        }
        
        
        final Form hi = new Form("Hi World");
        //hi.addComponent(new Label("Hi World"));
        
        
        test = new CookiesTest(hi);
        final JavascriptTests jsTest = new JavascriptTests();
        test.addTest(jsTest);
        
        Button showResults = new Button("Print results to console");
        showResults.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                test.run();
                test.printResults();
                final Vector results = new Vector();
                results.addAll(test.getPassed());
                results.addAll(test.getFailed());
                List passedList = new List(results);
                passedList.setRenderer(new ListCellRenderer(){
                    
                    TextArea passedLabel, failedLabel;
                    Label focus = new Label("");
                    
                    private TextArea getPassedLabel(){
                        if ( passedLabel == null ){
                            passedLabel = new TextArea();
                            passedLabel.setEditable(false);
                            
                            passedLabel.setUIID("MyLabel");
                        }
                        return passedLabel;
                    }
                    
                    private TextArea getFailedLabel(){
                        if ( failedLabel == null ){
                            failedLabel = new TextArea();
                            failedLabel.setEditable(false);
                            failedLabel.getStyle().setBgColor(0xFF0000);
                            failedLabel.setUIID("Label");
                        }
                        return failedLabel;
                    }

                    public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
                        
                        BaseTest.Result r = (BaseTest.Result)value;
                        if ( r.result ){
                            TextArea l = getPassedLabel();
                            l.setText(r.toString());
                            return l;
                        } else {
                            TextArea l = getFailedLabel();
                            l.setText(r.toString());
                            return l;
                        }
                        
                    }

                    public Component getListFocusComponent(List list) {
                        return focus;
                    }
                    
                });
                hi.addComponent(BorderLayout.CENTER, passedList);
                hi.repaint();
            }
            
        });
        hi.setLayout(new BorderLayout());
        hi.getContentPane().addComponent(BorderLayout.SOUTH, showResults);
        hi.show();
    }
    
    

    public void stop() {
        current = Display.getInstance().getCurrent();
    }
    
    public void destroy() {
    }

}
