package com.codename1.impl.android;

import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.impl.android.CodenameOneActivity;
public class AndroidStub extends CodenameOneActivity implements Runnable {
  private #MAIN_CLASS# i;
  private boolean firstTime = true;
  private Form currentForm;
  protected void onResume() {
    super.onResume();
    if(!Display.isInitialized()) {
         Display.init(this);
         i = new #MAIN_CLASS#();
         Display.getInstance().callSerially(this);
    }
  }
  protected void onPause() {
     super.onPause();
     Display.deinitialize();
     currentForm = Display.getInstance().getCurrent();
  }
  public void run() {
    if(firstTime) {
       firstTime = false;
       i.init(this);
       i.start();
    } else {
    if(currentForm != null) {
      currentForm.show();
     }
   }
  }
  protected void onStop() {
    super.onStop();
    i.stop();
  }
  protected void onDestroy() {
    super.onDestroy();
    i.destroy();
  }
}