package ca.weblite.codename1.tests;


import ca.weblite.codename1.js.JavascriptContext;
import com.codename1.components.WebBrowser;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Dialog;
import com.codename1.ui.Label;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import java.io.IOException;

public class TestApp {

    private Form current;

    public void init(Object context) {
        try{
            Resources theme = Resources.openLayered("/theme");
            UIManager.getInstance().setThemeProps(theme.getTheme(theme.getThemeResourceNames()[0]));
       }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public void start(){
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

    public void stop() {
        current = Display.getInstance().getCurrent();
    }
    
    public void destroy() {
    }

}
