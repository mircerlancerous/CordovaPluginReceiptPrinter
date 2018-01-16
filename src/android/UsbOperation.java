package com.crowdblink.cordova.printer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.print.sdk.PrinterInstance;
import com.android.print.sdk.usb.USBPort;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class UsbOperation{
	private final static String TAG = "UsbOpertion";
	private Context mContext;
	private Handler mHandler;
	private PrinterInstance mPrinter;
	private UsbDevice mDevice;
	private boolean hasRegDisconnectReceiver;
	private IntentFilter filter;

	public UsbOperation(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		hasRegDisconnectReceiver = false;

		filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
	}
	
	public String getUSBDevices(Activity activity){
		String json = "[";
		int count = 0;
		UsbManager usbManager = (UsbManager)activity.getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		while(deviceIterator.hasNext()){
			if(count > 0){
				json += ",";
			}
			count++;
			UsbDevice device = deviceIterator.next();
			json += "{\"Model\":\""+device.getDeviceName()+"\",";
			json += "\"DeviceID\":\""+device.getDeviceId()+"\",";
			json += "\"VendorID\":\""+device.getVendorId()+"\",";
			json += "\"ProductID\":\""+device.getProductId()+"\",";
			json += "\"Class\":\""+device.getDeviceClass()+"\",";
			json += "\"Subclass\":\""+device.getDeviceSubclass()+"\",";
			json += "\"isPrinter\":\""+String.valueOf(USBPort.isUsbPrinter(device))+"\"}";
		}
		json += "]";
		return json;
    }
	
	private UsbDevice doDiscovery(Activity activity){
    	UsbManager manager = (UsbManager)activity.getSystemService(Context.USB_SERVICE);
    	HashMap<String, UsbDevice> devices = manager.getDeviceList();
    	for(UsbDevice device : devices.values()){
    		Log.i(TAG, "list device: " + device.getDeviceName());
    		if(USBPort.isUsbPrinter(device)){
    			Log.i(TAG, "found printer: " + device.getDeviceName());
    			return device;
			}
		}
		return null;
    }

	public boolean open(Activity activity) {
		mDevice = doDiscovery(activity);
		if(mDevice == null){
			return false;
		}
		mPrinter = new PrinterInstance(mContext, mDevice, mHandler);
		// default is gbk...
		// mPrinter.setEncoding("gbk");
		mPrinter.openConnection();
		return true;
	}

	public void close() {
		if (mPrinter != null) {
			mPrinter.closeConnection();
			mPrinter = null;
		}
		if (hasRegDisconnectReceiver) {
			mContext.unregisterReceiver(myReceiver);
			hasRegDisconnectReceiver = false;
		}
	}

	private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i(TAG, "receiver is: " + action);
			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				// xxxxx
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				UsbDevice device = (UsbDevice) intent
						.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				if (device != null && mPrinter != null
						&& mPrinter.isConnected() && device.equals(mDevice)) {
					close();
				}
			}
		}
	};

	public PrinterInstance getPrinter() {
		if (mPrinter != null && mPrinter.isConnected()) {
			if (!hasRegDisconnectReceiver) {
				mContext.registerReceiver(myReceiver, filter);
				hasRegDisconnectReceiver = true;
			}
		}
		return mPrinter;
	}
}
