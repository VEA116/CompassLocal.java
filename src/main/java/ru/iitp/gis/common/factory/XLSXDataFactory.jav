package ru.iitp.gis.common.factory;

import org.apache.poi.ss.usermodel.Cell;
import ru.iitp.gis.common.model.DataTable;
import ru.iitp.gis.common.model.DataRecord;
import ru.iitp.gis.common.model.Attribute;
import ru.iitp.gis.common.model.DataType;
import ru.iitp.gis.common.Config;
import ru.iitp.gis.common.util.PCInputStream;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.ss.usermodel.WorkbookFactory;

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

    public void createTable(DataSource dsrc, DataTable dtab) throws FactoryException {
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
            XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(0);
            int column_num = sheet.getRow(0).getLastCellNum();
            int row_num = sheet.getLastRowNum();
            String[] attributes = new String[column_num];
            XSSFRow row = (XSSFRow) sheet.getRow(0);
            for (int i = 0; i < column_num; i++)
                attributes[i] = row.getCell(i).getRichStringCellValue().getString();
            int i = -1, j = -1;
            for (i = 1; i <= row_num; i++) {
                row = (XSSFRow) sheet.getRow(i);
                if (row == null)
                    continue;
                DataRecord drec = new DataRecord(dtab);
                for (j = 0; j < column_num; j++) {
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
