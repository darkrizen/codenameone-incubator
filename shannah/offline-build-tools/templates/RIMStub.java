package rimapp;

public class RIMStub extends com.codename1.impl.blackberry.CodenameOneUiApplication implements Runnable {
  private #MAIN_CLASS# appInstance;
  public void run() {
  		try {
			appInstance = new #MAIN_CLASS#();
			appInstance.init(this);
			appInstance.start();
        } catch (Throwable t){
			System.out.println("Error in run(): "+t.getMessage());
			t.printStackTrace();
		}
    }
    public static void main(String[] arg) {
    	try {
			RIMStub m = new RIMStub();
			com.codename1.ui.Display.init(m);
			com.codename1.ui.Display.getInstance().callSerially(m);
		} catch (Throwable t){
			System.out.println("Error in main: "+t.getMessage());
			t.printStackTrace();
		}
    }
}