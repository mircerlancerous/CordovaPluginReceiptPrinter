package com.crowdblink.cordova.printer;

import com.crowdblink.cordova.printer.Utils;
import java.io.UnsupportedEncodingException;


public class Barcode
{
  private static final String TAG = "Barcode";
  private byte barcodeType;
  private int param1;
  private int param2;
  private int param3;
  private String content;
  private String charsetName = "gbk";
  
  public Barcode(byte barcodeType) {
    this.barcodeType = barcodeType;
  }
  
  public Barcode(byte barcodeType, int param1, int param2, int param3) {
    this.barcodeType = barcodeType;
    this.param1 = param1;
    this.param2 = param2;
    this.param3 = param3;
  }
  
  public Barcode(byte barcodeType, int param1, int param2, int param3, String content)
  {
    this.barcodeType = barcodeType;
    this.param1 = param1;
    this.param2 = param2;
    this.param3 = param3;
    this.content = content;
  }
  
  public void setBarcodeParam(byte param1, byte param2, byte param3)
  {
    this.param1 = param1;
    this.param2 = param2;
    this.param3 = param3;
  }
  
  public void setBarcodeContent(String content) {
    this.content = content;
  }
  
  public void setBarcodeContent(String content, String charsetName) {
    this.content = content;
    this.charsetName = charsetName; }
  
  public byte[] getBarcodeData() {
    byte[] realCommand;
    switch (barcodeType)
    {
    case 100: 
    case 101: 
    case 102: 
      realCommand = getBarcodeCommand2(content, barcodeType, param1, param2, param3);
      break;
    case 73: 
      byte[] tempCommand = new byte['Ѐ'];
      int index = 0;
      int strLength = content.length();
      int tempLength = strLength;
      char[] charArray = content.toCharArray();
      boolean preHasCodeA = false;
      boolean preHasCodeB = false;
      boolean preHasCodeC = false;
      boolean needCodeC = false;
      for (int i = 0; i < strLength; i++) {
        byte a = (byte)charArray[i];
        if ((a >= 0) && (a <= 31))
        {
          if ((i == 0) || (!preHasCodeA)) {
            tempCommand[(index++)] = 123;
            tempCommand[(index++)] = 65;
            preHasCodeA = true;
            preHasCodeB = false;
            preHasCodeC = false;
            tempLength += 2;
          }
          tempCommand[(index++)] = a;
        }
        else {
          if ((a >= 48) && (a <= 57)) {
            if (!preHasCodeC)
            {

              for (int m = 1; m < 9; m++)
              {
                if ((i + m != strLength) && 
                  (Utils.isNum((byte)charArray[(i + m)])))
                {
                  if (m == 8) {
                    needCodeC = true;
                  }
                }
                else {
                  needCodeC = false;
                  break;
                }
              }
            }
            
            if (needCodeC) {
              if (!preHasCodeC) {
                tempCommand[(index++)] = 123;
                tempCommand[(index++)] = 67;
                preHasCodeA = false;
                preHasCodeB = false;
                preHasCodeC = true;
                tempLength += 2;
              }
              
              if (i != strLength - 1) {
                byte b = (byte)charArray[(i + 1)];
                
                if (Utils.isNum(b)) {
                  tempCommand[(index++)] = ((byte)((a - 48) * 10 + (b - 48)));
                  tempLength--;
                  i++;
                  continue;
                }
              }
            }
          }
          
          if (!preHasCodeB) {
            tempCommand[(index++)] = 123;
            tempCommand[(index++)] = 66;
            preHasCodeA = false;
            preHasCodeB = true;
            preHasCodeC = false;
            tempLength += 2;
          }
          tempCommand[(index++)] = a;
        }
      }
      
      realCommand = getBarcodeCommand1(new String(tempCommand, 0, tempLength), new byte[] { barcodeType, (byte)tempLength });
      break;
    

    case 72: 
      realCommand = getBarcodeCommand1(content, new byte[] { barcodeType, (byte)content.length() });
      break;
    

    default: 
      realCommand = getBarcodeCommand1(content, new byte[] { barcodeType });
    }
    
    return realCommand;
  }
  
  private byte[] getBarcodeCommand1(String content, byte... byteArray) {
    int index = 0;
    byte[] tmpByte = new byte[0];
    try {
      if (charsetName != "") {
        tmpByte = content.getBytes(charsetName);
      } else
        tmpByte = content.getBytes();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
    byte[] command = new byte[tmpByte.length + 13];
    
    command[(index++)] = 29;
    command[(index++)] = 119;
    if ((param1 >= 2) && (param1 <= 6))
    {
      command[(index++)] = ((byte)param1);
    }
    else
    {
      command[(index++)] = 2;
    }
    

    command[(index++)] = 29;
    command[(index++)] = 104;
    if ((param2 >= 1) && (param2 <= 255))
    {
      command[(index++)] = ((byte)param2);
    }
    else
    {
      command[(index++)] = -94;
    }
    

    command[(index++)] = 29;
    command[(index++)] = 72;
    if ((param3 >= 0) && (param3 <= 3))
    {
      command[(index++)] = ((byte)param3);
    }
    else
    {
      command[(index++)] = 0;
    }
    

    command[(index++)] = 29;
    command[(index++)] = 107;
    for (int i = 0; i < byteArray.length; i++)
    {
      command[(index++)] = byteArray[i];
    }
    


    for (int j = 0; j < tmpByte.length; j++)
    {
      command[(index++)] = tmpByte[j];
    }
    
    return command;
  }
  
  private byte[] getBarcodeCommand2(String content, byte barcodeType, int param1, int param2, int param3)
  {
    byte[] tmpByte = new byte[0];
    try {
      if (charsetName != "") {
        tmpByte = content.getBytes(charsetName);
      } else {
        tmpByte = content.getBytes();
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
    byte[] command = new byte[tmpByte.length + 10];
    command[0] = 29;
    command[1] = 90;
    command[2] = ((byte)(barcodeType - 100));
    command[3] = 27;
    command[4] = 90;
    command[5] = ((byte)param1);
    command[6] = ((byte)param2);
    command[7] = ((byte)param3);
    command[8] = ((byte)(tmpByte.length % 256));
    command[9] = ((byte)(tmpByte.length / 256));
    System.arraycopy(tmpByte, 0, command, 10, tmpByte.length);
    
    return command;
  }
}
