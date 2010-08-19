package org.deacondemo;

import java.util.Calendar;

import org.deacon.Deacon;
import org.deacon.DeaconError;
import org.deacon.DeaconObserver;
import org.deacon.DeaconResponse;
import org.deacondemo.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class DeaconDemo extends Activity implements DeaconObserver {
	
	private Deacon   deacon;
	private TextView textbox;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.textbox = (TextView) findViewById(R.id.textBox);
		
		try {
			this.deacon = new Deacon("home.daverea.com",4670, this);
			deacon.joinChannel("1min", 0);
			deacon.catchUpTimeOut(60);
			deacon.register(this);
			deacon.start();
		} catch (Exception e) {
			System.out.println("Problem while creating/starting Deacon");
			e.printStackTrace();
		}
	}

	@Override
	public void onPush(DeaconResponse response) {
		String currentText = (String) this.textbox.getText();
		Calendar now = Calendar.getInstance();
		this.textbox.setText("Push@"+
				String.format("%02d", now.get(Calendar.HOUR_OF_DAY)) + ":" + 
				String.format("%02d", now.get(Calendar.MINUTE)) + 
				String.format("%02d", now.get(Calendar.SECOND)) +
				", Channel="+response.getChannel() + 
				", Payload="+response.getPayload() + "\n" + currentText);
	}

	@Override
	public void onError(DeaconError err) {
	}

	@Override
	public void onReconnect() {
	}

}
