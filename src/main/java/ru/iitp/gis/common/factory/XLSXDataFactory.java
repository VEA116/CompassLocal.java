package ru.iitp.gis.common.factory;

import org.apache.poi.ss.usermodel.Cell;
import ru.iitp.gis.common.model.DataTable;
import ru.iitp.gis.common.model.DataRecord;
import ru.iitp.gis.common.model.Attribute;
import ru.iitp.gis.common.model.DataType;
import ru.iitp.gis.common.Config;
import ru.iitp.gis.common.util.PCInputStream;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javax.swing.*;

import static javax.print.attribute.standard.ReferenceUriSchemesSupported.HTTP;

public class XLSXDataFactory extends DataFactory implements DataType {
    public URL convertToURLEscapingIllegalCharacters(String string) {
        try {
            String decodedURL = URLDecoder.decode(string, "ISO-8859-1");
            URL url = new URL(decodedURL);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            return new URL(uri.toASCIIString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void createRegNamesTable(DataSource rnsrc, Hashtable rntable) throws FactoryException {
        try {
            PCInputStream in;
            try {
                String p = Config.PATH + rnsrc.getSrc();
                URL url = convertToURLEscapingIllegalCharacters(p);
                in = new PCInputStream(new BufferedInputStream(url.openConnection().getInputStream()));
            } catch (FileNotFoundException fnfe) {
                System.out.println(fnfe.getMessage());
                fnfe.printStackTrace();
                in = new PCInputStream(new BufferedInputStream(
                        new URL(rnsrc.getSrc()).openConnection().getInputStream()));
            }
            String pref = rnsrc.getSrc().substring(0, rnsrc.getSrc().lastIndexOf('.')) + "_";//my chan;
            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(in);
            String[] attributes = null;

            XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(0);
            int column_num = sheet.getRow(2).getLastCellNum();
            int row_num = sheet.getLastRowNum();
            attributes = new String[column_num];
            ArrayList<String> subattr = new ArrayList<String>();
            Hashtable htsubatr = new Hashtable();
            int ireg = 0;
            XSSFRow row = (XSSFRow) sheet.getRow(0);
            for (int i = 0; i < column_num; i++) {
                if (row.getCell(i) == null) {
                    if (i == 0)
                        attributes[i] = rnsrc.getIdAttr();
                    continue;
                } else
                    try {
                        attributes[i] = row.getCell(i).getRichStringCellValue().getString();
                        attributes[i] = row.getCell(i).getStringCellValue();//getRichStringCellValue().getString();

                    } catch (Exception e) {
                        if (row.getCell(i).getNumericCellValue() == (int) row.getCell(i).getNumericCellValue())
                            attributes[i] = String.valueOf((int) row.getCell(i).getNumericCellValue());
                        else
                            attributes[i] = String.valueOf(row.getCell(i).getNumericCellValue());
                    }
                if (attributes[i].equals(rnsrc.getIdAttr())) ireg = i;
            }
            for (int i = 1; i <= row_num; i++) {
                row = (XSSFRow) sheet.getRow(i);
                if (row == null)
                    continue;
//                DataRecord drec = new DataRecord(dtab);
                XSSFCell cell = (XSSFCell) row.getCell(ireg);
                if (cell == null || cell.getRawValue() == null)
                    continue;
            }
        }
        catch (Exception e) {
        }
    }

    public void createTable(DataSource dsrc, DataTable dtab) throws FactoryException {
        try {
            if(dtab.getRegNameSource() != null) {
                Hashtable rntable = new Hashtable();
                createRegNamesTable(dtab.getRegNameSource(), rntable);
            }

            PCInputStream in;
            try {
                String p = Config.PATH + dsrc.getSrc();
                URL url = convertToURLEscapingIllegalCharacters(p);
                in = new PCInputStream(new BufferedInputStream(url.openConnection().getInputStream()));
            } catch (FileNotFoundException fnfe) {
                System.out.println(fnfe.getMessage());
                fnfe.printStackTrace();
                in = new PCInputStream(new BufferedInputStream(
                        new URL(dsrc.getSrc()).openConnection().getInputStream()));
            }
            String pref = dsrc.getSrc().substring(0,dsrc.getSrc().lastIndexOf('.'))+"_";//my chan;
            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(in);
            String[] attributes = null;
            for(int nsh = 0; nsh<workbook.getNumberOfSheets(); nsh++) {
                XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(nsh);
                int column_num = sheet.getRow(2).getLastCellNum();
                int row_num = sheet.getLastRowNum();
                attributes = new String[column_num];
                ArrayList<String> subattr = new ArrayList<String>();
                Hashtable htsubatr = new Hashtable();

                XSSFRow row = (XSSFRow) sheet.getRow(2);
                Attribute attrShN = dtab.getAttribute(pref+sheet.getSheetName());

                boolean isdat = false;// все, что после RFEGION, - данные
                for (int i = 0; i < column_num; i++) {
                    if(row.getCell(i) == null){
                        if(i == 0)
                    {
                        attributes[i] = dsrc.getIdAttr();
                        isdat = true;}
                        continue;}
                     else
                    try {
                        attributes[i] = row.getCell(i).getRichStringCellValue().getString();
                        attributes[i] = row.getCell(i).getStringCellValue();//getRichStringCellValue().getString();

                    } catch (Exception e) {
                        if(row.getCell(i).getNumericCellValue() ==(int)row.getCell(i).getNumericCellValue())
                        attributes[i] = String.valueOf((int)row.getCell(i).getNumericCellValue());
                        else
                            attributes[i] = String.valueOf(row.getCell(i).getNumericCellValue());
                    }
                    if (attributes[i]=="" && i==0) {
                        attributes[i] = dsrc.getIdAttr();
                        isdat = true;
                        continue;
                    }
                    if (attributes[i].equals(dsrc.getIdAttr())) {
                        isdat = true;
                        continue;
                    }
                    if (attrShN != null && isdat) {
                        subattr.add(attributes[i]);
                        htsubatr.put(attributes[i], pref+sheet.getSheetName() + "_" + attributes[i]);
                        Attribute attr111 = dtab.getAttribute(attributes[i]);
                        if (attr111 == null) {
                            attr111 = new Attribute(pref+sheet.getSheetName() + "_" + attributes[i], attributes[i], attrShN.getType());
                            attrShN.addAttribute(attr111);
                            attr111.setParent(attrShN);
                            dtab.addAttribute(attr111, new Vector(1), attrShN);
                        }
                    }
                }
                dtab.removeAttribute(attrShN);
                String[] ss = new String[subattr.size()];
                dtab.addSubatr(pref+sheet.getSheetName(), htsubatr);
                dtab.addSubatrList(pref+sheet.getSheetName(), subattr.toArray((new String[subattr.size()])));
            }

                //               XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(0);//21.09.2022
//++++++++++++++++++++++++++++++
            String recId = null;
            for(int nsh = 0; nsh<workbook.getNumberOfSheets(); nsh++) {
                XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(nsh);
                int column_num = sheet.getRow(2).getLastCellNum();
                int row_num = sheet.getLastRowNum();
                XSSFRow row = (XSSFRow) sheet.getRow(2);
                attributes = new String[column_num];
                for (int i = 0; i < column_num; i++) {
                    if(row.getCell(i) == null)
                        attributes[i] ="";
                     else
                        try {
                            attributes[i] = row.getCell(i).getRichStringCellValue().getString();
                        } catch (Exception e) {
 //                           attributes[i] = String.valueOf(row.getCell(i).getNumericCellValue());
                            if(row.getCell(i).getNumericCellValue() ==(int)row.getCell(i).getNumericCellValue())
                                attributes[i] = String.valueOf((int)row.getCell(i).getNumericCellValue());
                            else
                                attributes[i] = String.valueOf(row.getCell(i).getNumericCellValue());

                        }
                    if (attributes[i] ==""&& i == 0)
                        attributes[i] = dsrc.getIdAttr();
                }
                //++++++++++++++++++++++++
                for (int i = 1 + 2; i <= row_num; i++) {
                    row = (XSSFRow) sheet.getRow(i);
                    if (row == null)
                        continue;
                    DataRecord drec = new DataRecord(dtab);
                    boolean isdat = false;// все, что после RFEGION, - данные
                    for (int j = 0; j < column_num; j++) {//21.09.2022
                        XSSFCell cell = (XSSFCell) row.getCell(j);
                        if (cell == null || cell.getRawValue() == null)
                            continue;

                        if (attributes[j].equals(dsrc.getIdAttr())) {
                            String value = cell == null ? "" : cell.getRichStringCellValue().getString();
                            // my beg
                            drec =dtab.getRecord(value);
                            if(drec == null)
                                drec = new DataRecord(dtab);
                            recId = value;
                            // my end
                            drec.setId(value != null ? value : "" + i);
                            continue;
                        }
                        if (attributes[j].equals(dsrc.getNameAttr())) {
                            String value = cell == null ? "" : cell.getRichStringCellValue().getString();
                            drec.setName(value);
                            isdat = true;
                            continue;
                        }
                        Attribute attr = dtab.getAttribute(pref+sheet.getSheetName()+"_"+attributes[j]);//my chan pref+
                        if (attr == null) continue;
                        Object val = null;
                        String value = "";
                        switch (attr.getType()) {
                            case BOOLEAN:
                                value = cell == null ? "" : cell.getRichStringCellValue().getString();
                                if (value.equalsIgnoreCase("Y") || value.equalsIgnoreCase("T"))
                                    val = new Boolean(true);
                                else if (value.equalsIgnoreCase("N") || value.equalsIgnoreCase("F"))
                                    val = new Boolean(false);
                                break;
                            case INTEGER:
                                val = cell.getCellType() != Cell.CELL_TYPE_NUMERIC ? null : (int) cell.getNumericCellValue();
                                break;
                            case FLOAT:
//                          try{
/*                            try{
                                new Float((float) cell.getNumericCellValue());
                            } catch (Exception e) {
                                System.out.println();
                            }*/
                                val = cell.getCellType() != Cell.CELL_TYPE_NUMERIC ? null : (float) cell.getNumericCellValue();
//                          }catch (Exception e) {
//                              System.out.println("sda");
//                          }
                                break;
                            case URL:
                                value = cell == null ? "" : cell.getRichStringCellValue().getString();
                                if (value.length() == 0) break;
                                if (!value.startsWith("http:") && !value.startsWith("ftp:"))
                                    value = Config.PATH + value;
                                try {
                                    val = new URL(value);
                                } catch (Exception mue) {
                                }
                                break;
                            default:
                                val = cell == null ? "" : cell.getRichStringCellValue().getString();
                        }
                        drec.setData(val, pref+sheet.getSheetName() + "_" +attributes[j]);//my chan pref+
                    }
                    if(dtab.getRecord(recId) == null && dtab.getRecordCount()<=84)
                    dtab.addRecord(drec);
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new FactoryException(e.toString());
        }
    }
    public void createTable1(DataSource dsrc, DataTable dtab) throws FactoryException {
        try {
            PCInputStream in;
            try {
                String p = Config.PATH + dsrc.getSrc();
                URL url = convertToURLEscapingIllegalCharacters(p);
                in = new PCInputStream(new BufferedInputStream(url.openConnection().getInputStream()));
            } catch (FileNotFoundException fnfe) {
                System.out.println(fnfe.getMessage());
                fnfe.printStackTrace();
                in = new PCInputStream(new BufferedInputStream(
                        new URL(dsrc.getSrc()).openConnection().getInputStream()));
            }

            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(in);
            for(int nsh = 0; nsh<1; nsh++) {
 //               for(int nsh = 0; nsh<workbook.getNumberOfSheets(); nsh++) {
//                XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(0);//21.09.2022
                XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(nsh);
                int column_num = sheet.getRow(2).getLastCellNum();
                int row_num = sheet.getLastRowNum();
                String[] attributes = new String[column_num];
                XSSFRow row = (XSSFRow) sheet.getRow(2);
                for (int i = 0; i < column_num; i++) {
                    //                    if (i == 0) attributes[i] = row.getCell(i).getRichStringCellValue().getString();
                    //else {
                        try {
                            attributes[i] = row.getCell(i).getRichStringCellValue().getString();
                        } catch (Exception e) {
                            attributes[i] = String.valueOf(row.getCell(i).getNumericCellValue());
                        }
                    //}
                    //------------------------del
 /*                  if (i == 2) {
                        Attribute attr1 = dtab.getAttribute(attributes[i]);
                        Attribute attr111 = new Attribute(attributes[i], attributes[i], attr1.getType());
                        attr1.addAttribute(attr111);
                        dtab.addAttribute(attr111, new Vector(1), attr1);
                    }*/
                }
                //------------------------

//                attributes[i] = sheet.getSheetName()+"_"+row.getCell(i).getRichStringCellValue().getString();
//                attributes[1] = sheet.getSheetName();//21.09.2022
                int i = -1, j = -1;
                for (i = 1 + 2; i <= row_num; i++) {
                    row = (XSSFRow) sheet.getRow(i);
                    if (row == null)
                        continue;
                    DataRecord drec = new DataRecord(dtab);
                    if(nsh == 0) j = 0;//21.09.2022
                    else j = 1;//21.09.2022
                    for (; j < column_num; j++) {//21.09.2022
                        //                   for (j = 0; j < column_num; j++) {//21.09.2022
                        XSSFCell cell = (XSSFCell) row.getCell(j);
                        if (cell == null || cell.getRawValue() == null)
                            continue;
                        if (attributes[j].equals(dsrc.getIdAttr())) {
                            String value = cell == null ? "" : cell.getRichStringCellValue().getString();
                            drec.setId(value != null ? value : "" + i);
                            continue;
                        }
                        if (attributes[j].equals(dsrc.getNameAttr())) {
                            String value = cell == null ? "" : cell.getRichStringCellValue().getString();
                            drec.setName(value);
                            continue;
                        }
                        Attribute attr = dtab.getAttribute(attributes[j]);
                        if (attr == null) continue;

                        Object val = null;
                        String value = "";
                        switch (attr.getType()) {
                            case BOOLEAN:
                                value = cell == null ? "" : cell.getRichStringCellValue().getString();
                                if (value.equalsIgnoreCase("Y") || value.equalsIgnoreCase("T"))
                                    val = new Boolean(true);
                                else if (value.equalsIgnoreCase("N") || value.equalsIgnoreCase("F"))
                                    val = new Boolean(false);
                                break;
                            case INTEGER:
                                val = cell.getCellType() != Cell.CELL_TYPE_NUMERIC ? null : (int) cell.getNumericCellValue();
                                break;
                            case FLOAT:
//                          try{
/*                            try{
                                new Float((float) cell.getNumericCellValue());
                            } catch (Exception e) {
                                System.out.println();
                            }*/
                                val = cell.getCellType() != Cell.CELL_TYPE_NUMERIC ? null : (float) cell.getNumericCellValue();
//                          }catch (Exception e) {
//                              System.out.println("sda");
//                          }
                                break;
                            case URL:
                                value = cell == null ? "" : cell.getRichStringCellValue().getString();
                                if (value.length() == 0) break;
                                if (!value.startsWith("http:") && !value.startsWith("ftp:"))
                                    value = Config.PATH + value;
                                try {
                                    val = new URL(value);
                                } catch (Exception mue) {
                                }
                                break;
                            default:
                                val = cell == null ? "" : cell.getRichStringCellValue().getString();
                        }
                        drec.setData(val, attributes[j]);
                    }
                    dtab.addRecord(drec);
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new FactoryException(e.toString());
        }
    }

    static int p = 0;

    public static void out(DataTable dt) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Data");
        XSSFRow row = sheet.createRow(0);
        XSSFCell cell = row.createCell(0);
        cell.setCellValue("ID");
        cell = row.createCell(1);
        cell.setCellValue("NAME");
        for (int i = 0; i < dt.getAttrCount(); i++) {
            cell = row.createCell(i + 2);
            cell.setCellValue(dt.getAttribute(i).getId());
        }
        for (int i = 0; i < dt.getRecordCount(); i++) {
            DataRecord dr = dt.getRecord(i);
            row = sheet.createRow(i + 1);
            cell = row.createCell(0);
            cell.setCellValue(dr.getId());
            cell = row.createCell(1);
            cell.setCellValue(dr.getName());
            for (int j = 0; j < dt.getAttrCount(); j++) {
                cell = row.createCell(j + 2);
                Object o = dr.getData(j);
                if (o != null)
                    cell.setCellValue(o.toString());
            }
        }
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        long m = runtime.maxMemory();
        long w = runtime.freeMemory();
        System.out.println(m + " " + w);
        dt.clear();
        System.gc();
        m = runtime.maxMemory();
        w = runtime.freeMemory();
        System.out.println(m + " " + w);
        try {
            wb.write(new FileOutputStream("c:\\test" + (p++) + ".xlsx"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
