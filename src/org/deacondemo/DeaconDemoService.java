package org.deacondemo;

import org.deacon.Deacon;
import org.deacon.DeaconError;
import org.deacon.DeaconObserver;
import org.deacon.DeaconResponse;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class DeaconDemoService extends Service implements DeaconObserver {
	
	private static final int HELLO_ID = 1;
	
	private Deacon deacon = null;
	private final IBinder mBinder = new DeaconDemoServiceBinder();
	private String channel = null;
	private DeaconDemo mActivity = null;

	public class DeaconDemoServiceBinder extends Binder {
		DeaconDemoService getService() {
			return DeaconDemoService.this;
		}
	}

	@Override
	public void onCreate() {
		// Instantiate and configure the Deacon object
		try {
			this.deacon = new Deacon("home.daverea.com",4670, this);
			deacon.catchUpTimeOut(60);
			deacon.register(this);
		} catch (Exception e) {
			System.out.println("Problem while creating Deacon");
			e.printStackTrace();
		}
	}
	
	// Interface methods for use by Activity
	
	public void register(DeaconDemo activity) {
		Log.d("DeaconDemoService", "Got register request from " + activity.toString());
		mActivity = activity;
	}
	
	public void unregister() {
		mActivity = null;
	}
	
	public void changeChannel(String chan) {
		Log.d("DeaconDemoService", "Got channel change request: " + chan);
		if(channel != null) {
			deacon.leaveChannel(channel);
		}
		deacon.joinChannel(chan, 0);
		channel = chan;
	}
	
	public boolean isRunning() {
		return deacon.isRunning();
	}
	
	public void startDeacon() {
		Log.d("DeaconDemoService", "Got startDeacon request");
		try {
			deacon.start();
			if(mActivity != null) {
				mActivity.setDeaconState(true);
			}
		} catch (Exception e) {
			Toast.makeText(this, "Service: Could not start Deacon", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void stopDeacon() {
		Log.d("DeaconDemoService", "Got stopDeacon request");
		deacon.stop();
		if(mActivity != null) {
			mActivity.setDeaconState(false);
		}
	}
	
	// Overridden superclass/interface methods
	
	@Override
	public void onPush(DeaconResponse response) {
		// Notify the activity if it is running
		if(mActivity != null) {
			mActivity.onPush(response);
		}
		else {
			// Otherwise, create a notification if the pushed integer is prime
			Integer payload = new Integer(Integer.parseInt(response.getPayload().trim()));
			boolean isPrime = true;
			for(int mod = 2; mod < payload; mod++) {
				if(payload % mod == 0) isPrime = false;
			}
			String primestring = "";
			if(isPrime) primestring = "is prime";
			else primestring = "is not prime";
			Log.d("DeaconDemoService", "payload = " + payload.toString() + ", which " + primestring);
			if(isPrime) {
				String ns = Context.NOTIFICATION_SERVICE;
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
				Notification notification = new Notification(R.drawable.running, (CharSequence) ("Deacon Push: " + payload), System.currentTimeMillis());
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				notification.defaults |= Notification.DEFAULT_SOUND;
				Intent notificationIntent = new Intent(this, DeaconDemo.class);
				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
				notification.setLatestEventInfo(getApplicationContext(), (CharSequence) "DeaconDemo", (CharSequence) ("Got Push: " + payload.toString()), contentIntent);
				mNotificationManager.notify(HELLO_ID, notification);
			}
		}
	}

	@Override
	public void onError(DeaconError err) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onReconnect() {
		Log.d("DeaconDemoService", "Got onReconnect");
		if(mActivity != null) mActivity.setNetworkState(true);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.deacon.stop();
	}

	@Override
	public void onDisconnect(DeaconError error) {
		Log.d("DeaconDemoService", "Got onDisconnect with message: " + error.getErrorMsg());
		if(this.mActivity != null) mActivity.setNetworkState(false);
	}

}
