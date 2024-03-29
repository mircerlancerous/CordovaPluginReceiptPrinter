package com.offthebricks.cordova.printer;

import com.offthebricks.cordova.printer.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class Table
{
  private List<String> tableRows;
  private String tableReg;
  private HashMap<Integer, String> unPrintColumnMap;
  private int[] tableColWidth;
  private boolean alignRight;
  
  public Table(String column, String regularExpression, int[] columnWidth)
  {
    unPrintColumnMap = new HashMap();
    tableRows = new ArrayList();
    tableRows.add(column);
    tableReg = regularExpression;
    if (columnWidth != null)
    {
      tableColWidth = columnWidth;
    }
    else {
      tableColWidth = new int[column.split(regularExpression).length];
      for (int i = 0; i < tableColWidth.length; i++)
      {
        tableColWidth[i] = 8;
      }
    }
  }
  




  public void setColumnAlignRight(boolean right)
  {
    alignRight = right;
  }
  
  public void addRow(String row)
  {
    if (tableRows != null)
    {
      tableRows.add(row);
    }
  }
  
  public String getTableText()
  {
    StringBuffer sb = new StringBuffer();
    for (int m = 0; m < tableRows.size(); m++)
    {
      String[] tableLine = ((String)tableRows.get(m)).split(tableReg);
      sb.append(printTableLine(tableLine));
      sb.append("\n");
    }
    
    return sb.toString();
  }
  
  private String printTableLine(String[] tableLine)
  {
    StringBuffer sb = new StringBuffer();
    int sub_length = 0;
    String[] line = tableLine;
    
    for (int i = 0; i < line.length; i++) {
      line[i] = line[i].trim();
      
      int index = line[i].indexOf("\n");
      if (index != -1) {
        unPrintColumnMap.put(Integer.valueOf(i), line[i].substring(index + 1));
        
        line[i] = line[i].substring(0, index);
        sb.append(printTableLine(line));
        
        sb.append(printNewLine(line));
        return sb.toString();
      }
      
      int length = Utils.getStringCharacterLength(line[i]);
      int col_length = tableColWidth.length;
      int col_width = 8;
      if (i < col_length) {
        col_width = tableColWidth[i];
      }
      
      if ((length > col_width) && (i != line.length - 1)) {
        sub_length = Utils.getSubLength(line[i], col_width);
        unPrintColumnMap.put(Integer.valueOf(i), line[i].substring(sub_length, line[i].length()));
        
        line[i] = line[i].substring(0, sub_length);
        sb = new StringBuffer();
        sb.append(printTableLine(line));
        sb.append(printNewLine(line));
        return sb.toString();
      }
      if (i == 0) {
        sb.append(line[i]);
        for (int j = 0; j < col_width - length; j++) {
          sb.append(" ");
        }
      }
      else if (alignRight) {
        for (int j = 0; j < col_width - length; j++) {
          sb.append(" ");
        }
        sb.append(line[i]);
      } else {
        sb.append(line[i]);
        for (int j = 0; j < col_width - length; j++)
        {
          if (i != line.length - 1) {
            sb.append(" ");
          }
        }
      }
    }
    


    return sb.toString();
  }
  
  private String printNewLine(String[] oldLine)
  {
    if (!unPrintColumnMap.isEmpty()) {
      StringBuffer sb = new StringBuffer();
      String[] newLine = new String[oldLine.length];
      Iterator<Map.Entry<Integer, String>> iterator = unPrintColumnMap.entrySet().iterator();
      
      while (iterator.hasNext()) {
        Map.Entry<Integer, String> entry = (Map.Entry)iterator.next();
        Integer key = (Integer)entry.getKey();
        String value = (String)entry.getValue();
        if (key.intValue() < oldLine.length) {
          newLine[key.intValue()] = value;
        }
      }
      unPrintColumnMap.clear();
      
      for (int i = 0; i < newLine.length; i++) {
        if ((newLine[i] == null) || (newLine[i] == "")) {
          newLine[i] = " ";
        }
      }
      
      sb.append(printTableLine(newLine));
      return "\n" + sb.toString();
    }
    return "";
  }
}