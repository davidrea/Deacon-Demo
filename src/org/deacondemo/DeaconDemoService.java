package org.deacondemo;

import org.deacon.Deacon;
import org.deacon.DeaconError;
import org.deacon.DeaconObserver;
import org.deacon.DeaconResponse;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class DeaconDemoService extends Service implements DeaconObserver {
	
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
		if(mActivity != null) {
			mActivity.onPush(response);
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
