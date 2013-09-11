package com.omf.resourcecontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;

public class StartUpActivity extends Activity {
	
	public static final String TAG = "StartUpActivity";
	
	
	private ToggleButton toggleService = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_up);
		
		toggleService = (ToggleButton)findViewById(R.id.ToggleBtnServ);
		toggleService.setOnClickListener(toggleListener);
	}

	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_start_up, menu);
		return true;
	}*/

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		
		if(BackgroundService.isServiceRunning(getApplicationContext(), ".BackgroundService")){
			toggleService.setChecked(true);
		}else{
			toggleService.setChecked(false);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG,"onStop");
	}
	
	/**
	 * LISTENER FUNCTIONS
	 */
	View.OnClickListener toggleListener = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
			
			Intent intent = new Intent(StartUpActivity.this, BackgroundService.class);		
			
			if(toggleService.isChecked() && !BackgroundService.isServiceRunning(StartUpActivity.this.getApplicationContext(),
																				".BackgroundService")) {
				
				Log.i(TAG, "Start Service");
				startService(intent);
			}else {
				Log.i(TAG, "Stop Service");
				stopService(intent);
			}
			
		}
	};
	
	

}
