package org.deacontest;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Calendar;

import org.deacon.Deacon;
import org.deacon.DeaconError;
import org.deacon.DeaconResponse;
import org.deacon.interfaces.DeaconServiceObserver;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class Testing extends Activity implements DeaconServiceObserver {
	
	private Deacon   deacon;
	private TextView textbox;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.textbox = (TextView) findViewById(R.id.textBox);
		
		try {
			this.deacon = new Deacon("home.daverea.com",4670);
//			deacon.joinChannel("1min", 0);
			deacon.joinChannel("10sec", 0);
			deacon.register(this);
			deacon.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onPush(DeaconResponse response) {
		Log.d("Deacon", "Got DeaconResponse with channel="+response.getChannel()+" and payload="+response.getPayload());
		String currentText = (String) this.textbox.getText();
		Calendar now = Calendar.getInstance();
		this.textbox.setText("Push@"+String.format("%02d", now.get(Calendar.HOUR_OF_DAY))+":"+String.format("%02d", now.get(Calendar.MINUTE))+", Channel="+response.getChannel()+", Payload="+response.getPayload()+"\n" + currentText);
	}
//	
//	@Override
//	public void onPause() {
//		super.onPause();
//		this.deacon.stop();
//	}

	@Override
	public void onError(DeaconError err) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReconnect() {
		// TODO Auto-generated method stub
		
	}

}
