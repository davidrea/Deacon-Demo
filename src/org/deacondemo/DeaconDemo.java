package org.deacondemo;

import java.util.Calendar;

import org.deacon.Deacon;
import org.deacon.DeaconError;
import org.deacon.DeaconObserver;
import org.deacon.DeaconResponse;
import org.deacondemo.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DeaconDemo extends Activity implements OnClickListener, OnItemSelectedListener {
	
	private Spinner  		  spinner;
	private TextView		  textbox, deaconStatusText, networkStatusText;
	private Button			  startStopButton;
	private ImageView		  serviceImage, networkImage;
	private DeaconDemoService mBoundDeaconDemoService;
	private boolean 		  mIsBound = false;
	private DeaconDemo		  mThisDemo = this;
	
	private ServiceConnection DeaconDemoConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mBoundDeaconDemoService = ((DeaconDemoService.DeaconDemoServiceBinder)service).getService();
			if(mBoundDeaconDemoService != null) {
				mIsBound = true;
				Log.d("DeaconDemoConnection", "Service bound: " + mBoundDeaconDemoService.toString());
				mBoundDeaconDemoService.register(mThisDemo);
			}
		}

		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			mBoundDeaconDemoService = null;
			mIsBound = false;
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		networkImage = (ImageView) findViewById(R.id.image_connection_status);
		serviceImage = (ImageView) findViewById(R.id.image_deacon_status);
		this.textbox = (TextView)  findViewById(R.id.textBox);
		this.deaconStatusText = (TextView) findViewById(R.id.text_deacon_status);
		this.networkStatusText = (TextView) findViewById(R.id.text_connection_status);
		this.spinner = (Spinner)   findViewById(R.id.spinner_channel_selector);
		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, 
																					R.array.meteor_channels_array, 
																					android.R.layout.simple_spinner_item);

		startStopButton = (Button) findViewById(R.id.button_service_startstop);
		startStopButton.setOnClickListener(this);
		
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerAdapter);
		spinner.setOnItemSelectedListener(this);
		
		// Start up the DeaconDemoService service
		startService(new Intent(this, DeaconDemoService.class));
		bindService(new Intent(DeaconDemo.super, DeaconDemoService.class), DeaconDemoConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onPause() {
		if(mIsBound) {
			super.onPause();
			mBoundDeaconDemoService.unregister();
		}
	}

	public void onPush(DeaconResponse response) {
		setNetworkState(true);
		setDeaconState(true);
		String currentText = (String) this.textbox.getText();
		Calendar now = Calendar.getInstance();
		this.textbox.setText("Push@"+
				String.format("%02d", now.get(Calendar.HOUR_OF_DAY)) + ":" + 
				String.format("%02d", now.get(Calendar.MINUTE)) + 
				String.format("%02d", now.get(Calendar.SECOND)) +
				", Channel="+response.getChannel() + 
				", Payload="+response.getPayload() + "\n" + currentText);
	}
	
	// Button method

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.button_service_startstop:
			if(mIsBound && startStopButton.getText().equals("Start")) {
				mBoundDeaconDemoService.startDeacon();
			}
			else {
				mBoundDeaconDemoService.stopDeacon();
			}
			break;
		}
	}
	
	// Utility methods
	
	public void setNetworkState(boolean up) {
		if(up) {			
			networkImage.setImageResource(R.drawable.network_connected);
			this.networkStatusText.setText("Network connected");
		}
		else {
			networkImage.setImageResource(R.drawable.network_disconnected);
			this.networkStatusText.setText("Network disconnected");
		}
	}
	
	public void setDeaconState(boolean up) {
		if(up){
			serviceImage.setImageResource(R.drawable.running);
			this.deaconStatusText.setText("Deacon running");
			startStopButton.setText("Stop");
		}
		else {
			serviceImage.setImageResource(R.drawable.not_running);
			this.deaconStatusText.setText("Deacon not running");
			startStopButton.setText("Start");
		}
	}
	
	// Spinner methods

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		Log.d("DeaconDemo", "Got spinner item selected: " + parent.getItemAtPosition(pos).toString());
		mBoundDeaconDemoService.changeChannel(parent.getItemAtPosition(pos).toString());
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// Do nothing
	}

}
