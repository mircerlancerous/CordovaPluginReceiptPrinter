package com.crowdblink.cordova.printer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;

import com.crowdblink.cordova.printer.USBPort;
import com.crowdblink.cordova.printer.Utils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public class PrinterInstance implements Serializable
{
  private static final long serialVersionUID = 1L;
  private static String TAG = "PrinterInstance";
  private USBPort myPrinter;
  private String charsetName = "gbk";
  private final String SDK_VERSION = "3.0";
  

  public PrinterInstance(Activity activity, Handler handler)
  {
    myPrinter = new USBPort(activity, handler);
  }
  
  public String getEncoding() {
    return charsetName;
  }
  
  public void setEncoding(String charsetName)
  {
    this.charsetName = charsetName;
  }
  
  public String getSDK_Vesion() {
    return "3.0";
  }
  
  public boolean isConnected() {
    return myPrinter.getState() == 101;
  }
  
  public void openConnection() {
    myPrinter.open();
  }
  
  public void closeConnection() {
    myPrinter.close();
  }
  
  public int printText(String content)
  {
    byte[] data = null;
    try
    {
      if (charsetName != "") {
        data = content.getBytes(charsetName);
      } else {
        data = content.getBytes();
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return sendByteData(data);
  }
  
  public int sendByteData(byte[] data)
  {
    if (data != null) {
      return myPrinter.write(data);
    }
    return -1;
  }
  
  public int printImage(Bitmap bitmap) {
    return sendByteData(Utils.bitmap2PrinterBytes(bitmap, 0));
  }
  
  public int printImage(Bitmap bitmap, int left) {
    return sendByteData(Utils.bitmap2PrinterBytes(bitmap, left));
  }
  
  public int printImageStylus(Bitmap bitmap, int multiple) {
    return sendByteData(Utils.bitmap2PrinterBytes_stylus(bitmap, multiple, 0));
  }
  
  public int printImageStylus(Bitmap bitmap, int multiple, int left) {
    return sendByteData(Utils.bitmap2PrinterBytes_stylus(bitmap, multiple, left));
  }
  
  public int printTable(Table table) {
    return printText(table.getTableText());
  }
  
  public int printBarCode(Barcode barcode) {
    return sendByteData(barcode.getBarcodeData());
  }
  
  public void init() {
    setPrinter(0);
  }
  
  public byte[] read() {
    return myPrinter.read();
  }
  
  public boolean setPrinter(int command)
  {
    byte[] arrayOfByte = null;
    switch (command) {
    	//ESC @
	    case 0: 
	      arrayOfByte = new byte[2];
	      arrayOfByte[0] = 27;
	      arrayOfByte[1] = 64;
	      break;
	    //NULL
	    case 1: 
	      arrayOfByte = new byte[1];
	      arrayOfByte[0] = 0;
	      break;
	    
	    case 2: 
	      arrayOfByte = new byte[1];
	      arrayOfByte[0] = 12;
	      break;
	    case 3: 
	      arrayOfByte = new byte[1];
	      arrayOfByte[0] = 10;
	      break;
	    case 4: 
	      arrayOfByte = new byte[1];
	      arrayOfByte[0] = 13;
	      break;
	    case 5: 
	      arrayOfByte = new byte[1];
	      arrayOfByte[0] = 9;
	      break;
	    case 6: 
	      arrayOfByte = new byte[2];
	      arrayOfByte[0] = 27;
	      arrayOfByte[1] = 50;
    }
    
    sendByteData(arrayOfByte);
    return true;
  }
  
  public boolean setPrinter(int command, int value)
  {
    byte[] arrayOfByte = new byte[3];
    switch (command) {
    case 0: 
      arrayOfByte[0] = 27;
      arrayOfByte[1] = 74;
      break;
    case 1: 
      arrayOfByte[0] = 27;
      arrayOfByte[1] = 100;
      break;
    case 2: 
      arrayOfByte[0] = 27;
      arrayOfByte[1] = 33;
      break;
    case 3: 
      arrayOfByte[0] = 27;
      arrayOfByte[1] = 85;
      break;
    
    case 4: 
      arrayOfByte[0] = 27;
      arrayOfByte[1] = 86;
      break;
    case 5: 
      arrayOfByte[0] = 27;
      arrayOfByte[1] = 87;
      break;
    case 6: 
      arrayOfByte[0] = 27;
      arrayOfByte[1] = 45;
      break;
    case 7: 
      arrayOfByte[0] = 27;
      arrayOfByte[1] = 43;
      break;
    case 8: 
      arrayOfByte[0] = 27;
      arrayOfByte[1] = 105;
      break;
    case 9: 
      arrayOfByte[0] = 27;
      arrayOfByte[1] = 99;
      break;
    case 10: 
      arrayOfByte[0] = 27;
      arrayOfByte[1] = 51;
      break;
    case 11: 
      arrayOfByte[0] = 27;
      arrayOfByte[1] = 32;
    case 12: 
      arrayOfByte[0] = 28;
      arrayOfByte[1] = 80;
    case 13: 
      arrayOfByte[0] = 27;
      arrayOfByte[1] = 97;
      if ((value > 2) || (value < 0))
        return false;
      break;
    }
    arrayOfByte[2] = ((byte)value);
    sendByteData(arrayOfByte);
    return true;
  }
  
  public void setCharacterMultiple(int x, int y)
  {
    byte[] arrayOfByte = new byte[3];
    arrayOfByte[0] = 29;
    arrayOfByte[1] = 33;
    
    if ((x >= 0) && (x <= 7) && (y >= 0) && (y <= 7)) {
      arrayOfByte[2] = ((byte)(x * 16 + y));
      sendByteData(arrayOfByte);
    }
  }
  
  public void setLeftMargin(int nL, int nH)
  {
    byte[] arrayOfByte = new byte[4];
    arrayOfByte[0] = 29;
    arrayOfByte[1] = 76;
    
    arrayOfByte[2] = ((byte)nL);
    arrayOfByte[3] = ((byte)nH);
    sendByteData(arrayOfByte);
  }
  
  public void cutPaper()
  {
    byte[] cutCommand = new byte[4];
    cutCommand[0] = 29;
    cutCommand[1] = 86;
    cutCommand[2] = 66;
    cutCommand[3] = 0;
    sendByteData(cutCommand);
  }
  
  public void ringBuzzer(byte time)
  {
    byte[] buzzerCommand = new byte[3];
    buzzerCommand[0] = 29;
    buzzerCommand[1] = 105;
    buzzerCommand[2] = time;
    sendByteData(buzzerCommand);
  }
  
  public void openCashbox(boolean cashbox1, boolean cashbox2)
  {
    if (cashbox1) {
      byte[] drawCommand = new byte[5];
      drawCommand[0] = 27;
      drawCommand[1] = 112;
      drawCommand[2] = 0;
      drawCommand[3] = 50;
      drawCommand[4] = 50;
      sendByteData(drawCommand);
    }
    
    if (cashbox2) {
      byte[] drawCommand = new byte[5];
      drawCommand[0] = 27;
      drawCommand[1] = 112;
      drawCommand[2] = 1;
      drawCommand[3] = 50;
      drawCommand[4] = 50;
      sendByteData(drawCommand);
    }
  }
}