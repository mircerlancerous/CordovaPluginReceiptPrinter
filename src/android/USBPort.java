package com.offthebricks.cordova.printer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;

public class USBPort{
	
	private static final String TAG = "USBPrinterPort";
	private UsbManager mUsbManager;
	private UsbDevice mUsbDevice;
	private UsbDeviceConnection connection;
	private UsbInterface usbInterface;
	private UsbEndpoint inEndpoint;
	private UsbEndpoint outEndpoint;
	private Handler mHandler;
	private int mState;
	private Activity mActivity;
	private Context mContext;
	private static final String ACTION_USB_PERMISSION = "com.android.usb.USB_PERMISSION";
	private Thread mConnectThread;
	
	public USBPort(Activity activity, Handler handler)
	{
		mActivity = activity;
		mContext = activity.getApplicationContext();
		mUsbManager = ((UsbManager)mContext.getSystemService("usb"));
		mUsbDevice = findPrinter();
		mHandler = handler;
		mState = 103;
	}
	
	private UsbDevice findPrinter(){
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
	
	public void open()
	{
		Log.d(TAG, "connect to: " + mUsbDevice.getDeviceName());
		if (mState != 103) {
			close();
		}
		
		if (isUsbPrinter(mUsbDevice))
		{
			if (mUsbManager.hasPermission(mUsbDevice))
			{
				connect();
			}
			else {
				PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent("com.android.usb.USB_PERMISSION"), 0);
				IntentFilter filter = new IntentFilter("com.android.usb.USB_PERMISSION");
				mContext.registerReceiver(mUsbReceiver, filter);
				
				mUsbManager.requestPermission(mUsbDevice, pendingIntent);
			}
		} else {
			setState(102);
		}
	}
	
	private void connect() {
		mConnectThread = new Thread(connectThread);
		mConnectThread.start();
	}
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.w(TAG, "receiver action: " + action);
			
			if ("com.android.usb.USB_PERMISSION".equals(action))
				synchronized (this) {
					mContext.unregisterReceiver(mUsbReceiver);
					UsbDevice device = (UsbDevice)intent.getParcelableExtra("device");
					if ((intent.getBooleanExtra("permission", false)) && (mUsbDevice.equals(device))) {
						USBPort.this.connect();
					} else {
						USBPort.this.setState(102);
						Log.e(TAG, "permission denied for device " + device);
					}
				}
		}
	};
	
	private Runnable connectThread = new Runnable(){
		
		public void run() {
			boolean hasError = true;
			
			if (mUsbManager.hasPermission(mUsbDevice)) {
				try {
					usbInterface = mUsbDevice.getInterface(0);
					for (int i = 0; i < usbInterface.getEndpointCount(); i++)
					{
						UsbEndpoint ep = usbInterface.getEndpoint(i);
						if (ep.getType() == 2) {
							if (ep.getDirection() == 0) {
								outEndpoint = ep;
							} else {
								inEndpoint = ep;
							}
						}
					}
					
					connection = mUsbManager.openDevice(mUsbDevice);
					if ((connection != null) && 
						(connection.claimInterface(usbInterface, true))) {
						hasError = false;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			synchronized (this) {
				mConnectThread = null;
			}
			if (hasError) {
				USBPort.this.setState(102);
				close();
			} else {
				USBPort.this.setState(101);
			}
		}
	};
	
	public void close()
	{
		Log.w(TAG, "close()");
		if (connection != null) {
			connection.releaseInterface(usbInterface);
			connection.close();
			connection = null;
		}
		mConnectThread = null;
		if (mState != 102) {
			setState(103);
		}
	}
	
	public int write(byte[] data)
	{
		if (connection != null) {
			return connection.bulkTransfer(outEndpoint, data, data.length, 3000);
		}
		return -1;
	}
	


	public byte[] read()
	{
		if (connection != null) {
			byte[] retData = new byte[64];
			int readLen = connection.bulkTransfer(inEndpoint, retData, retData.length, 3000);
			Log.w(TAG, "read length:" + readLen);
			if (readLen > 0) {
				if (readLen == 64) {
					return retData;
				}
				byte[] realData = new byte[readLen];
				System.arraycopy(retData, 0, realData, 0, readLen);
				return realData;
			}
		}
		
		return null;
	}
	
	private boolean isUsbPrinter(UsbDevice device){
		int vendorId = device.getVendorId();
		int productId = device.getProductId();
		int classId = device.getDeviceClass();
		Log.w(TAG, "device name: " + device.getDeviceName());
		Log.w(TAG, "vid:" + vendorId + " pid:" + productId + " class:" + classId);
		//https://www.usb.org/defined-class-codes
		if(classId == 7){
			return true;
		}
		return false;
	}
	
	private synchronized void setState(int state)
	{
		Log.w(TAG, "setState() " + mState + " -> " + state);
		if (mState != state) {
			mState = state;
			if (mHandler != null) {
				mHandler.obtainMessage(mState).sendToTarget();
			}
		}
	}
	
	public int getState()
	{
		return mState;
	}
}
