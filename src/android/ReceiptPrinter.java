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

import com.crowdblink.cordova.printer.PrinterInstance;
import com.crowdblink.cordova.printer.PrinterConstants.BarcodeType;
import com.crowdblink.cordova.printer.PrinterConstants.Command;
import com.crowdblink.cordova.printer.PrinterConstants.Connect;
import com.crowdblink.cordova.printer.Barcode;

import android.util.Log;

public class ReceiptPrinter extends CordovaPlugin{
	private static boolean isConnected;
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
			mPrinter = new PrinterInstance(cordova.getActivity(), mHandler);
			mPrinter.openConnection();
		}
		else if(mPrinter != null){
			mPrinter.closeConnection();
			mPrinter = null;
		}
		else if(callback != null){
			PluginResult result = new PluginResult(PluginResult.Status.OK,"already disconnected");
			callback.sendPluginResult(result);
			callback = null;
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
				JSprintText(callbackContext, data, false);
			}
			else if(action.equalsIgnoreCase("command")){
				JSprintText(callbackContext, data, true);
			}
			else if(action.equalsIgnoreCase("cutPaper")){
				cutPaper();
			}
			else if(action.equalsIgnoreCase("openCashBox")){
				openCashBox();
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
			if(!printBarcode(type,value)){
				result = new PluginResult(PluginResult.Status.ERROR,"error printing barcode: "+type);
			}
		}
		callbackContext.sendPluginResult(result);
	}
	
	private void JSprintText(CallbackContext callbackContext, JSONArray data, boolean isCommand){
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
			if(!isCommand){
				printText(value);
			}
			else{
				command(value);
			}
		}
		callbackContext.sendPluginResult(result);
	}

/**************************************************************************************/
	
	private void command(String value){
		mPrinter.printText(value);
	}
	
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
	
	private boolean printBarcode(String type, String value){
		mPrinter.init();
		mPrinter.setCharacterMultiple(0, 0);
		mPrinter.setLeftMargin(15, 0);
		Log.w("Plugin",type+": "+value);
		Barcode barcode = null;
		if(type.equalsIgnoreCase("CODE39")){
			barcode = new Barcode(BarcodeType.CODE39, 2, 150, 2, value);
		}
		else if(type.equalsIgnoreCase("CODE128")){
			barcode = new Barcode(BarcodeType.CODE128, 2, 150, 2, value);
		}
		else if(type.equalsIgnoreCase("PDF417")){
			barcode = new Barcode(BarcodeType.PDF417, 2, 3, 6, value);
		}
		else if(type.equalsIgnoreCase("QRCODE")){
			barcode = new Barcode(BarcodeType.QRCODE, 2, 3, 6, value);
		}
		if(barcode != null){
			mPrinter.printBarCode(barcode);
			mPrinter.setPrinter(Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1);
			return true;
		}
		return false;
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
