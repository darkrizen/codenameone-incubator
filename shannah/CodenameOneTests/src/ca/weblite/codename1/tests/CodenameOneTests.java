package ca.weblite.codename1.tests;


import com.codename1.ui.Button;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import java.io.IOException;

public class CodenameOneTests {

    private Form current;

    public void init(Object context) {
        try{
            Resources theme = Resources.openLayered("/theme");
            UIManager.getInstance().setThemeProps(theme.getTheme(theme.getThemeResourceNames()[0]));
       }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public void start() {
        if(current != null){
            current.show();
            return;
        }
        Form hi = new Form("Hi World");
        //hi.addComponent(new Label("Hi World"));
        
        
        final CookiesTest test = new CookiesTest(hi);
        final JavascriptTests jsTest = new JavascriptTests();
        test.addTest(jsTest);
        
        Button showResults = new Button("Print results to console");
        showResults.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent evt) {
                test.run();
                test.printResults();
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
