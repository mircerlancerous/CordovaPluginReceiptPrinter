package com.crowdblink.cordova.printer;

import android.app.Activity;
import android.app.PendingIntent;
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
import java.util.Iterator;

import com.android.print.sdk.PrinterInstance;

public class UsbOperation{
	private final static String TAG = "UsbOperation";
	private static final String ACTION_USB_PERMISSION = "com.android.usb.USB_PERMISSION";
	private Context mContext;
	private Handler mHandler;
	private PrinterInstance mPrinter;
	private UsbManager mUsbManager;
	private UsbDevice mDevice;
	private IntentFilter filter;
	private Activity mActivity;
	private PendingIntent mPermissionIntent;

	public UsbOperation(Activity activity, Handler handler) {
		mActivity = activity;
		mContext = activity.getApplicationContext();
		mHandler = handler;
		mUsbManager = (UsbManager)activity.getSystemService(Context.USB_SERVICE);

		filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		
        mPermissionIntent = PendingIntent.getBroadcast(mActivity, 0, new Intent(ACTION_USB_PERMISSION), 0);
	}
	
	private boolean hasUSBPermission(){
    	if(mDevice == null){
    		return false;
    	}
    	if(!mUsbManager.hasPermission(mDevice)){
    		return false;
    	}
    	return true;
    }
	
	private boolean getUSBPermission(){
        mUsbManager.requestPermission(mDevice,mPermissionIntent);
        return hasUSBPermission();
    }
	
	public String getUSBDevices(){
		String json = "[";
		int count = 0;
		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
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
			json += "\"isPrinter\":\""+String.valueOf(isUsbPrinter(device))+"\"}";
		}
		json += "]";
		return json;
    }
    
    public boolean setDevice(int ProductId){
    	if(ProductId == 0){
    		mDevice = null;
    		return false;
    	}
    	HashMap<String, UsbDevice> devices = mUsbManager.getDeviceList();
    	for(UsbDevice device : devices.values()){
    		if(device.getProductId() == ProductId){
    			Log.i(TAG, "set printer: " + device.getDeviceName());
    			mDevice = device;
    			return true;
			}
		}
		return false;
    }
	
	private UsbDevice doDiscovery(){
    	HashMap<String, UsbDevice> devices = mUsbManager.getDeviceList();
    	for(UsbDevice device : devices.values()){
    		Log.i(TAG, "list device: " + device.getDeviceName());
    		if(isUsbPrinter(device)){
    			Log.i(TAG, "found printer: " + device.getDeviceName());
    			return device;
			}
		}
		return null;
    }

	public boolean open() {
		if(mDevice == null){
			mDevice = doDiscovery();
		}
		if(mDevice == null){
			return false;
		}
		mActivity.registerReceiver(myReceiver, filter);
		if(!getUSBPermission()){
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
		mContext.unregisterReceiver(myReceiver);
	}

	private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i(TAG, "receiver is: " + action);
			if (ACTION_USB_PERMISSION.equals(action)){
                synchronized(this){
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
						//permission granted
                        
                    } else {
						//permission denied
						
                    }
                }
            }
			else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				// xxxxx
			}
			else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				UsbDevice device = (UsbDevice) intent
						.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				if (device != null && mPrinter != null
						&& mPrinter.isConnected() && device.equals(mDevice)) {
					close();
				}
			}
		}
	};

	public PrinterInstance getPrinter(){
		return mPrinter;
	}
	
	private boolean isUsbPrinter(UsbDevice device){
		int vendorId = device.getVendorId();
		int productId = device.getProductId();
		Log.i(TAG, "device name: " + device.getDeviceName());
		Log.i(TAG, "vid:" + vendorId + " pid:" + productId);
		if((1155 == vendorId && 22304 == productId) ||
			(1659 == vendorId && 8965 == productId) ||
			(4070 == vendorId && 33054 == productId)){
			return true;
		}
		return false;
	}
}
