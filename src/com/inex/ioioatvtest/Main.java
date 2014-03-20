package com.inex.ioioatvtest;

import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/* 
 * 
 ********************************************
 *    Port    |    VR   | Button  |   LED   |
 ********************************************
 * IOIO Board | Port 31 | Port 20 | Port 0  |
 ********************************************
 * 
 */

public class Main extends IOIOActivity {
	// Create object for widget
	ProgressBar progressBar;
	TextView txtVal;
	ImageView imgDI;
	ToggleButton btnLED;

	// onCreate function that will be do first when application is startup
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("Check", "aaa");
		// Set format of image that will be use in this application
        getWindow().setFormat(PixelFormat.RGBA_8888);
        
		// Application no notification bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // Set application use layout from main.xml
		setContentView(R.layout.main);
		
		// Assign object to widget 
		txtVal = (TextView) findViewById(R.id.txtVal);
		imgDI = (ImageView)findViewById(R.id.imgDI);
		btnLED = (ToggleButton) findViewById(R.id.btnLED);
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		
		// Set event listener to button
		// This code use for custom toggle button's background 
		btnLED.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked) {
					btnLED.setBackgroundDrawable(getResources().getDrawable(R.drawable.digitalinputon));
				} else {
					btnLED.setBackgroundDrawable(getResources().getDrawable(R.drawable.digitalinputoff));
				}
			}
		});

		// Custom Progressive Bar Code
		// You can skip this code for Default Progressive Bar
		final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
		ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		pgDrawable.getPaint().setColor(Color.parseColor("#F5F5F5"));
		ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBar.setProgressDrawable(progress);  
	}
	
	// On resume function
	@Override
    public void onResume() {
        super.onResume();
	}
	
	// On pause function
	@Override
    public void onPause() {
        super.onPause();
    }
	
	// On destroy function
	@Override
    public void onDestroy() {
        super.onDestroy();
    }

	// This class is thread for ioio board
	// You can control ioio board through this class 
	class Looper extends BaseIOIOLooper {
		// Create object for assigned to digital output port 
		private DigitalOutput led;
		
		// Create object for assigned to digital input port 
		private DigitalInput in;

		// Create object for assigned to analog input port 
		private AnalogInput ain;
		
		// This function will do when application is startup 
		// Like onCreate function but use with ioio board
		@Override
		public void setup() throws ConnectionLostException {
			// Assigned eacth object to each port
			led = ioio_.openDigitalOutput(0, true);
			ain = ioio_.openAnalogInput(31);
			in = ioio_.openDigitalInput(20);

			// if we use any command which not ioio command 
			// in any ioio board's function program will force close
			// then we could use runnable to avoid force close
			/*runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// When device connected with ioio board 
					// Toast will show "Connected!"
					Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_SHORT).show();
				}		
			});*/Log.e("Check", "Connteced");
		}

		// This function will always running when device connect with ioio board
		// It use for control ioio board
		@Override
		public void loop() throws ConnectionLostException {
			try {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							// Set led state by reading boolean from toggle button state 
							led.write(!btnLED.isChecked());
							
							// Reading button state and show on image view
							if(in.read() == true) {
								imgDI.setImageDrawable(getResources().getDrawable(R.drawable.digitalinputoff));
							} else {
								imgDI.setImageDrawable(getResources().getDrawable(R.drawable.digitalinputon));
							}
					
							// Create string and read voltage from analog input port
							String str;
							str = String.valueOf(ain.getVoltage());
							
							// Set progress bar by reference from analog input port
							progressBar.setProgress((int)((float)(ain.getVoltage() * 100 / 3.3)));
							
							// Trim decimal to 2 digit 
							// If string longer than 4 letters
							// Then show result to text view
							if(str.length() > 4){
								txtVal.setText(str.substring(0,4));
							} else{
								txtVal.setText(str);
							}
						} catch (ConnectionLostException e) {
						} catch (InterruptedException e) { }
					}		
				});
				
				// Delay time 50 milliseconds
				Thread.sleep(50);
			} catch (InterruptedException e) {	}
		}
		
		public void disconnected() {/*
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// When device connected with ioio board 
					// Toast will show "Connected!" and close application
					Main.this.finish();
					Toast.makeText(getApplicationContext(), "Disconnected!", Toast.LENGTH_SHORT).show();
				}		
			});	*/Log.e("Check", "Disconnected");
		}

		public void incompatible() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// When device connected with ioio board 
					// Toast will show "Connected!"
					Toast.makeText(getApplicationContext(), "Incompatible!", Toast.LENGTH_SHORT).show();
				}		
			});	
		}
	}

	@Override
    protected IOIOLooper createIOIOLooper() {
        return new Looper();
    }
}

