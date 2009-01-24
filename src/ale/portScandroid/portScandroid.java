package ale.portScandroid;

import java.util.Observable;
import java.util.Observer;

import jmap.Scan;
import jmap.ScanningException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class portScandroid extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final Handler mHandler = new Handler();
    	
        final Button scanButton = (Button) findViewById(R.id.scan);
        scanButton.setOnClickListener(new View.OnClickListener(){
            Scanning sh;
        	
        	public void onClick(View v) {
        		//Scan button clicked.. scan that shit!
        		
        		//Button already clicked.. kill scan, reset.
        		if(scanButton.getText() == "Stop"){
        				sh.destroy();
        				scanButton.setText("Scan");
        				return;
        		}
        		
        		int fromval;
        		int toval;
        	
        		//Get the elements from the form
        		final EditText hostname = (EditText) findViewById(R.id.hostname);
        		String host = hostname.getText().toString();
        		final EditText from = (EditText) findViewById(R.id.from);
        		String froom = from.getText().toString();
        		if (froom.length() != 0) { 
        			fromval = new Integer(from.getText().toString()).intValue();}
        		else{
        			return;
        		}
        		final EditText to = (EditText) findViewById(R.id.to);
        		String tooo = to.getText().toString();
        		if (tooo.length() != 0) { 
        			toval = new Integer(to.getText().toString()).intValue();}
        		else{
        			return;
        		}
        		final CheckBox udp = (CheckBox) findViewById(R.id.udp);
        		boolean udpmode = udp.isChecked();
        		final TextView result = (TextView) findViewById(R.id.result);
        		
        		//Runnable to update the GUI as scan runs.
                final Runnable mUpdateTimeTask = new Runnable() {
             	   public void run() {
             	       //long millis = System.currentTimeMillis();
             	       result.setText(sh.getResult());
             	       
             	       if(sh.getDone()){
           				sh.destroy();
        				scanButton.setText("Scan");
        				return;
             	       }
             	       
             	      mHandler.postDelayed(this, 500);
             	   }
             	};
             	mHandler.removeCallbacks(mUpdateTimeTask);
             	mHandler.postDelayed(mUpdateTimeTask, 100);
        		
        		//Validate input
        		if(fromval<=0){
        			//TODO: Toast
        			return;	
        		}
        		if((toval<=0) || (toval<fromval)) {
        			//TODO: Toast
        			return;
        		}
        		if(host.length()==0){
        			//TODO: Toast
        			return;
        		}
        		
				try {
					scanButton.setText("Stop");
					sh = new Scanning(host, fromval, toval, udpmode);
					sh.start();
					
				} catch (ScanningException e) {
					result.setText(e.toString());
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
        	}
        });
    }
    
    class Scanning extends Thread implements Observer{
    	
    private Scan scan;
    private int progress=0;
    private boolean udp;
    private boolean done;
    
    private String result = "Nothing scanned yet..";

	Scanning(String host, int from, int to, boolean udpp) throws ScanningException{
		//Make the Scan
		scan = new Scan(host, from, to);
		
		//add this as one of the observers of scan
		scan.addObserver(this);
		udp=udpp;

	}
	
	public void run(){
		//Scan the Scan!
		
		if (scan == null){
			result = "Scan has not been properly configured.";
		}
		//This is where the magic happens
		if (udp) {
			result = scan.scan(false, true);
			System.out.println(result);
		}
		else{
			result = scan.scan(true, false);
			System.out.println(result);
		}
		
		done = true;
		
	}

	public void update(Observable o, Object arg){
		//Update the progress
		
		if (arg instanceof Integer){
			progress += ((Integer) arg).intValue();
			result = "Scanned " + progress + " ports.";
			
		}
		else{
			//increment the progress bar's value
			++progress;        		
			result = "Scanned " + progress + " ports.";
		}
		
	}

	public void destroy(){
		//Bitch get out of my car I don't love you
		scan.stop();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		done = true;
	}
	
	public String getResult(){
		//To prevent threads breaking GUI widgets
		return result;
	}
	
	public boolean getDone(){
		//To prevent threads breaking GUI widgets
		return done;
	}
	
}
}