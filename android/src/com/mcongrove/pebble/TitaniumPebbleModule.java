/**
 * Copyright 2014 Matthew Congrove
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mcongrove.pebble;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.KrollProxyListener;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.titanium.TiApplication;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

@Kroll.module(name="TitaniumPebble", id="com.mcongrove.pebble")
public class TitaniumPebbleModule extends KrollModule
{
	private static final String LCAT = "Pebble";
	private static UUID uuid;
	private int connectedCount = 0;
	private static boolean isListeningToPebble = false;
	
	private BroadcastReceiver connectedReceiver = null;
	private BroadcastReceiver disconnectedReceiver = null;
	private PebbleKit.PebbleDataReceiver dataReceiver = null;
	private PebbleKit.PebbleAckReceiver ackReceiver = null;
	private PebbleKit.PebbleNackReceiver nackReceiver = null;

	public TitaniumPebbleModule()
	{
		super();
	}
	
	public TiApplication getApplicationContext()
	{
		return TiApplication.getInstance();
	}

	// Lifecycle Events
	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		
	}
	
	@Override
	public void onPause(Activity activity)
	{
		super.onPause(activity);
		
		if(connectedReceiver != null)
		{
			getApplicationContext().unregisterReceiver(connectedReceiver);
			connectedReceiver = null;
		}
		
		if(disconnectedReceiver != null)
		{
			getApplicationContext().unregisterReceiver(disconnectedReceiver);
			disconnectedReceiver = null;
		}
		
		if(dataReceiver != null)
		{
			getApplicationContext().unregisterReceiver(dataReceiver);
			dataReceiver = null;
		}
		
		if(ackReceiver != null)
		{
			getApplicationContext().unregisterReceiver(ackReceiver);
			ackReceiver = null;
		}
		
		if(nackReceiver != null)
		{
			getApplicationContext().unregisterReceiver(nackReceiver);
			nackReceiver = null;
		}
	}
	
	@Override
	public void onResume(Activity activity)
	{
		super.onResume(activity);
		
		if(isListeningToPebble)
		{
			addReceivers();
		}
	}
	
	@Override
	public void onDestroy(Activity activity) 
	{
		super.onDestroy(activity);
	}

	// Methods
	@Kroll.method
	private void listenToConnectedWatch()
	{
		Log.d(LCAT, "listenToConnectedWatch: Listening");
		
		if(!checkWatchConnected())
		{
			Log.w(LCAT, "listenToConnectedWatch: No watch connected");
			
			return;
		}
		
		if(!isListeningToPebble)
		{
			isListeningToPebble = true;
		}
		
		addReceivers();
	}
	
	@Kroll.method
	private void addReceivers()
	{
		if(isListeningToPebble)
		{
			if(connectedReceiver == null)
			{
				connectedReceiver = new BroadcastReceiver()
				{
					@Override
					public void onReceive(Context context, Intent intent)
					{
						Log.d(LCAT, "watchDidConnect");

						setConnectedCount(0);
						
						fireEvent("watchConnected", new Object[] {});
					}
				};
				
				PebbleKit.registerPebbleConnectedReceiver(getApplicationContext(), connectedReceiver);
			}
			
			if(disconnectedReceiver == null)
			{
				disconnectedReceiver = new BroadcastReceiver()
				{
					@Override
					public void onReceive(Context context, Intent intent)
					{
						Log.d(LCAT, "watchDidDisconnect");
						
						setConnectedCount(0);
						
						fireEvent("watchDisconnected", new Object[] {});
					}
				};
				
				PebbleKit.registerPebbleDisconnectedReceiver(getApplicationContext(), disconnectedReceiver);
			}
			
			if(dataReceiver == null)
			{
				dataReceiver = new PebbleKit.PebbleDataReceiver(uuid)
				{
					@Override
					public void receiveData(final Context context, final int transactionId, final PebbleDictionary data)
					{
						if(!data.contains(0))
						{
							Log.e(LCAT, "listenToConnectedWatch: Received message, data corrupt");
							
							PebbleKit.sendNackToPebble(context, transactionId);
							
							return;
						}

						PebbleKit.sendAckToPebble(context, transactionId);
						
						try
						{
							JSONArray jsonArray = new JSONArray(data.toJsonString());
							
							if(jsonArray.length() > 0)
							{
								JSONObject jsonObject = jsonArray.getJSONObject(0);
								
								if(jsonObject.has("value"))
								{
									Log.i(LCAT, "listenToConnectedWatch: Received message");
									
									HashMap message = new HashMap();
									message.put("message", jsonObject.getString("value"));
									
									fireEvent("update", message);
								}
							}
						} catch(Throwable e) {
							Log.e(LCAT, "listenToConnectedWatch: Received message, data corrupt");
						}
					}
				};
				
				PebbleKit.registerReceivedDataHandler(getApplicationContext(), dataReceiver);
			}
			
			if(ackReceiver == null)
			{
				ackReceiver = new PebbleKit.PebbleAckReceiver(uuid)
				{
					@Override
					public void receiveAck(Context context, int transactionId)
					{
						Log.i(LCAT, "Received ACK");
					}
				};
				
				PebbleKit.registerReceivedAckHandler(getApplicationContext(), ackReceiver);
			}
			
			if(nackReceiver == null)
			{
				nackReceiver = new PebbleKit.PebbleNackReceiver(uuid)
				{
					@Override
					public void receiveNack(Context context, int transactionId)
					{
						Log.i(LCAT, "Received NACK");
					}
				};
				
				PebbleKit.registerReceivedNackHandler(getApplicationContext(), nackReceiver);
			}
		}
	}

	@Kroll.method
	public void setAppUUID(String uuidString)
	{
		uuid = UUID.fromString(uuidString);
	}

	@Kroll.method
	public boolean checkWatchConnected()
	{
		try
		{
			boolean connected = PebbleKit.isWatchConnected(getApplicationContext());
			
			if(!connected)
			{
				Log.w(LCAT, "checkWatchConnected: No watch connected");
			}
			
			return connected;
		} catch(SecurityException e) {
			return false;
		}
	}
	
	@Kroll.getProperty @Kroll.method
	public int getConnectedCount()
	{
		return connectedCount;
	}
	
	@Kroll.setProperty @Kroll.method
	public void setConnectedCount(int ignore)
	{
		if(checkWatchConnected())
		{
			connectedCount = 1;
		} else {
			connectedCount = 0;
		}
	}
	
	@Kroll.method
	public void connect(HashMap args)
	{
		Log.d(LCAT, "connect");
		
		final KrollFunction successCallback = (KrollFunction)args.get("success");
		final KrollFunction errorCallback = (KrollFunction)args.get("error");
		
		if(!PebbleKit.areAppMessagesSupported(getApplicationContext()))
		{
			Log.e(LCAT, "connect: Watch does not support messages");
			
			if(errorCallback != null)
			{
				errorCallback.call(getKrollObject(), new Object[] {});
			}
			
			return;
		}
		
		setConnectedCount(0);
		listenToConnectedWatch();
		
		Log.d(LCAT, "connect: Messages supported");
		
		if(successCallback != null)
		{
			successCallback.call(getKrollObject(), new Object[] {});
		}
	}
	
	@Kroll.method
	public void getVersionInfo(HashMap args)
	{
		Log.d(LCAT, "getVersionInfo");
		
		final KrollFunction successCallback = (KrollFunction)args.get("success");
		final KrollFunction errorCallback = (KrollFunction)args.get("error");
		
		int majorVersion;
		int minorVersion;
		
		try
		{
			PebbleKit.FirmwareVersionInfo versionInfo = PebbleKit.getWatchFWVersion(getApplicationContext());
			
			majorVersion = versionInfo.getMajor();
			minorVersion = versionInfo.getMinor();
		} catch(Exception e) {
			Log.w(LCAT, "Could not retrieve version info from Pebble");
			
			HashMap event = new HashMap();
			event.put("message", "Could not retrieve version info from Pebble");
			
			errorCallback.call(getKrollObject(), event);
			
			return;
		}

		Log.d(LCAT, "Pebble FW Major " + majorVersion);
		Log.d(LCAT, "Pebble FW Minor " + minorVersion);
		
		if(successCallback != null)
		{
			HashMap versionInfoHash = new HashMap();
			versionInfoHash.put("major", majorVersion);
			versionInfoHash.put("minor", minorVersion);
			
			successCallback.call(getKrollObject(), versionInfoHash);
		}
	}
	
	@Kroll.method
	public void launchApp(HashMap args)
	{
		Log.d(LCAT, "launchApp");
		
		if(!checkWatchConnected())
		{
			Log.w(LCAT, "launchApp: No watch connected");
			
			return;
		}
		
		final KrollFunction successCallback = (KrollFunction)args.get("success");
		final KrollFunction errorCallback = (KrollFunction)args.get("error");
		
		try
		{
			PebbleKit.startAppOnPebble(getApplicationContext(), uuid);
			
			setConnectedCount(0);
			listenToConnectedWatch();
			
			Log.d(LCAT, "launchApp: Success");
			
			if(successCallback != null)
			{
				HashMap event = new HashMap();
				event.put("message", "Successfully launched app");
				
				successCallback.call(getKrollObject(), event);
			}
		} catch(IllegalArgumentException e) {
			Log.e(LCAT, "launchApp: Error");
			
			if(errorCallback != null)
			{
				errorCallback.call(getKrollObject(), new Object[] {});
			}
			
			return;
		}
	}
	
	@Kroll.method
	public void killApp(HashMap args)
	{
		Log.d(LCAT, "killApp");
		
		if(!checkWatchConnected())
		{
			Log.w(LCAT, "killApp: No watch connected");
			
			return;
		}
		
		final KrollFunction successCallback = (KrollFunction)args.get("success");
		final KrollFunction errorCallback = (KrollFunction)args.get("error");
		
		try
		{
			PebbleKit.closeAppOnPebble(getApplicationContext(), uuid);
			
			Log.d(LCAT, "killApp: Success");
			
			if(successCallback != null)
			{
				HashMap event = new HashMap();
				event.put("message", "Successfully killed app");
				
				successCallback.call(getKrollObject(), event);
			}
		} catch(IllegalArgumentException e) {
			Log.e(LCAT, "killApp: Error");
			
			if(errorCallback != null)
			{
				errorCallback.call(getKrollObject(), new Object[] {});
			}
			
			return;
		}
	}
	
	@Kroll.method
	public void sendMessage(HashMap args)
	{
		Log.d(LCAT, "sendMessage");
		
		if(!checkWatchConnected())
		{
			Log.w(LCAT, "sendMessage: No watch connected");
			
			return;
		}
		
		final KrollFunction successCallback = (KrollFunction)args.get("success");
		final KrollFunction errorCallback = (KrollFunction)args.get("error");
		final Object message = args.get("message");
		
		Map<Integer, Object> messageHash = (HashMap<Integer, Object>) message;
		Iterator<Map.Entry<Integer, Object>> entries = messageHash.entrySet().iterator();
		
		PebbleDictionary data = new PebbleDictionary();
		
		while(entries.hasNext())
		{
			Map.Entry<Integer, Object> entry = entries.next();
			
			if(entry.getValue() instanceof Integer)
			{
				data.addInt32((Integer) entry.getKey(), (Integer) entry.getValue());
			} else if(entry.getValue() instanceof String) {
				data.addString((Integer) entry.getKey(), (String) entry.getValue());
			}
		}
		
		PebbleKit.sendDataToPebble(getApplicationContext(), uuid, data);
	}
}

/*
	TODO:
		- Map ACK/NACK handler to sendMessage so it can fire success/error callback for each message
		- Queue messages in sendMessage to avoid overflowing Pebble channel
*/