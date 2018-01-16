package com.crowdblink.cordova.printer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import android.os.Handler;
import android.os.Message;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.graphics.Color;
import android.graphics.Matrix;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.print.sdk.PrinterType;
import com.android.print.sdk.PrinterConstants.BarcodeType;
import com.android.print.sdk.PrinterConstants.Command;
import com.android.print.sdk.PrinterConstants.Connect;
import com.android.print.sdk.PrinterInstance;
import com.android.print.sdk.Barcode;
//import com.android.print.sdk.FontProperty;

public class SGT88iVPrinter extends CordovaPlugin{
	private static boolean isConnected;
	private UsbOperation myOperation;
	private PrinterInstance mPrinter;
	
	//used for asynchronous callbacks to the plugin
	private CallbackContext callback = null;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			boolean success = true;
			switch(msg.what){
				case Connect.SUCCESS:		//101
					isConnected = true;
					mPrinter = myOperation.getPrinter();
					break;
				case Connect.FAILED:		//102
					success = false;
					isConnected = false;
					break;
				case Connect.CLOSED:		//103
					isConnected = false;
					break;
				default:
					success = false;
					break;
			}
			if(callback != null){
				PluginResult result = new PluginResult(PluginResult.Status.OK,"handler success: "+msg.what);
				if(!success){
					result = new PluginResult(PluginResult.Status.ERROR,"error connecting/disconnecting: "+msg.what);
				}
				callback.sendPluginResult(result);
				callback = null;
			}
		}
	};

	private void openConn(boolean connect){
		if(connect){
			myOperation = new UsbOperation(cordova.getActivity(), mHandler);
			boolean success = myOperation.open();
			if(!success){
				PluginResult result = new PluginResult(PluginResult.Status.ERROR,"can't find printer");
				callback.sendPluginResult(result);
				callback = null;
			}
		}
		else{
			myOperation.close();
			myOperation = null;
			mPrinter = null;
		}
	}
	
/**************************************************************************************/

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		isConnected = false;
	}
    
	@Override
	public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
		boolean found = true;
		try{
			if(action.equalsIgnoreCase("connect")){
				callback = callbackContext;
				openConn(true);
			}
			else if(action.equalsIgnoreCase("disconnect")){
				callback = callbackContext;
				openConn(false);
			}
			else if(action.equalsIgnoreCase("isConnected")){
				JSisConnected(callbackContext);
			}
			else if(action.equalsIgnoreCase("printBarcode")){
				JSprintBarcode(callbackContext, data);
			}
			else if(action.equalsIgnoreCase("printText")){
				JSprintText(callbackContext, data);
			}
			else if(action.equalsIgnoreCase("cutPaper")){
				cutPaper();
			}
			else if(action.equalsIgnoreCase("openCashBox")){
				openCashBox();
			}
			else if(action.equalsIgnoreCase("getUSBDevices")){
				JSgetUSBDevices(callbackContext);
			}
			else if(action.equalsIgnoreCase("openByProductId")){
				JSopenByProductId(callbackContext, data);
			}
			else{
				found = false;
			}
		}
		catch(Exception e){
			PluginResult result = new PluginResult(PluginResult.Status.ERROR,"Action Error: "+e);
			callbackContext.sendPluginResult(result);
		}
		return found;
	}

/**************************************************************************************/

	private void JSisConnected(CallbackContext callbackContext){
		PluginResult result = new PluginResult(PluginResult.Status.OK,"true");
		if(!isConnected){
			result = new PluginResult(PluginResult.Status.OK,"false");
		}
		callbackContext.sendPluginResult(result);
	}
	
	private void JSprintBarcode(CallbackContext callbackContext, JSONArray data){
		PluginResult result = new PluginResult(PluginResult.Status.OK,"");
		String type = "", value = "";
	    boolean success = false;
		try{
			type = data.getString(0);
			value = data.getString(1);
			success = true;
		}
		catch(JSONException e){
			result = new PluginResult(PluginResult.Status.ERROR,"JSON:"+e.getMessage());
		}
		if(success){
			printBarcode(type,value);
		}
		callbackContext.sendPluginResult(result);
	}
	
	private void JSprintText(CallbackContext callbackContext, JSONArray data){
		PluginResult result = new PluginResult(PluginResult.Status.OK,"");
		String value = "";
	    boolean success = false;
		try{
			value = data.getString(0);
			success = true;
		}
		catch(JSONException e){
			result = new PluginResult(PluginResult.Status.ERROR,"JSON:"+e.getMessage());
		}
		if(success){
			printText(value);
		}
		callbackContext.sendPluginResult(result);
	}
	
	private void JSgetUSBDevices(CallbackContext callbackContext){
		String json = myOperation.getUSBDevices();
		PluginResult result = new PluginResult(PluginResult.Status.OK,json);
		callbackContext.sendPluginResult(result);
	}
	
	private void JSopenByProductId(CallbackContext callbackContext, JSONArray data){
		int ProductId = 0;
		try{
			ProductId = data.getInt(0);
		}
		catch(JSONException e){
			PluginResult result = new PluginResult(PluginResult.Status.ERROR,"JSON:"+e.getMessage());
			callbackContext.sendPluginResult(result);
			return;
		}
		boolean success = myOperation.setDevice(ProductId);
		if(!success){
			PluginResult result = new PluginResult(PluginResult.Status.ERROR,"error setting printer");
			if(ProductId == 0){
				result = new PluginResult(PluginResult.Status.OK,"printer unset");
			}
			callbackContext.sendPluginResult(result);
			return;
		}
		
		callback = callbackContext;
		openConn(true);
	}

/**************************************************************************************/
	
	private void printText(String value){
		mPrinter.init();
		mPrinter.printText(value);
		//mPrinter.setPrinter(Command.PRINT_AND_NEWLINE);
		mPrinter.setPrinter(Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2);
	}
	
	private void cutPaper(){
		mPrinter.cutPaper();
	}
	
	private void openCashBox(){
		mPrinter.openCashbox(true,true);
	}
	
	private void printBarcode(String type, String value){
		mPrinter.init();
		mPrinter.setCharacterMultiple(0, 0);
		mPrinter.setLeftMargin(15, 0);
		Barcode barcode = null;
		if(type == "CODE39"){
			barcode = new Barcode(BarcodeType.CODE39, 2, 150, 2, value);
		}
		else if(type ==	"Code128"){
			barcode = new Barcode(BarcodeType.CODE128, 2, 150, 2, value);
		}
		else if(type == "PDF417"){
			barcode = new Barcode(BarcodeType.PDF417, 2, 3, 6, value);
		}
		else if(type == "QRCODE"){
			barcode = new Barcode(BarcodeType.QRCODE, 2, 3, 6, value);
		}
		if(barcode != null){
			mPrinter.printBarCode(barcode);
			mPrinter.setPrinter(Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1);
		}
	}
	
	private void printImage(Bitmap bitmap, boolean convertToBW){
		if(convertToBW){
			bitmap = convertToBlackWhite(bitmap);
		}
		mPrinter.init();
		mPrinter.printImage(bitmap);
		mPrinter.setPrinter(Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2);
	}
	
	private Bitmap convertToBlackWhite(Bitmap bmp) {
		int width = bmp.getWidth(); // ??????
		int height = bmp.getHeight(); // ??????
		int[] pixels = new int[width * height]; // ??????????????

		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		int alpha = 0xFF << 24;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int grey = pixels[width * i + j];

				int red = ((grey & 0x00FF0000) >> 16);
				int green = ((grey & 0x0000FF00) >> 8);
				int blue = (grey & 0x000000FF);

				grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
				grey = alpha | (grey << 16) | (grey << 8) | grey;
				pixels[width * i + j] = grey;
			}
		}
		Bitmap newBmp = Bitmap.createBitmap(width, height, Config.RGB_565);

		newBmp.setPixels(pixels, 0, width, 0, 0, width, height);

		Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, 380, 460);
		return resizeBmp;
	}
}