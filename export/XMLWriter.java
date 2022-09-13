// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   XMLWriter.java

package ru.iitp.gis.local.export;

import ru.iitp.gis.common.factory.XMLMapFactory;
import ru.iitp.gis.common.factory.DataSource;
import ru.iitp.gis.common.model.*;
import ru.iitp.gis.common.util.TextUtils;
import ru.iitp.gis.common.view.Style;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;


// Referenced classes of package ru.iitp.gis.local.export:
//            CVFWriter, DBFWriter

public class XMLWriter {
  private static final String PATTERN_XML = "<?xml version=\"1.0\" encoding=\"windows-1251\"?>\n\n";
  private static final String PATTERN_MAP = "<map name=\"{0}\" active-layer-id=\"{1}\">\n";
  private static final String PATTERN_LAYER = "  <layer id=\"{0}\" visible=\"{1}\">\n";
  private static final String PATTERN_GEOMETRY = "    <geometry src=\"{0}\" format=\"{1}\"/>\n";
  private static final String PATTERN_DATABASE = "    <database src=\"{0}\" format=\"{1}\" id-attr=\"{2}\" name-attr=\"{3}\">\n";
  private static final String PATTERN_ATTR_LIST_1 = "<attr-list name=\"{0}\">\n";
  private static final String PATTERN_ATTR_LIST_2 = "<attr-list id=\"{0}\" name=\"{1}\" type=\"{2}\"/>\n";
  private static final String PATTERN_ATTRIBUTE = "<attr id=\"{0}\" name=\"{1}\" type=\"{2}\"/>\n";
  private static final String ATTR_LIST = "</attr-list>\n";

  private static MessageFormat mf;
  private static StringBuffer sb;
  private static String fullPath;
  //private static String absolutPath;
  private static String briefPath;


    public XMLWriter() {
  }

  public static String colorToString(Color c) {
    return "#" + Integer.toHexString(c.getRGB()).substring(2);
  }

  private static String getTypeName(int type) {
    Hashtable types = XMLMapFactory.types;
    for (Enumeration e = types.keys(); e.hasMoreElements();) {
      String name = (String) e.nextElement();
      if (((Integer) types.get(name)).intValue() == type)
        return name;
    }

    return "unknown";
  }

  public static String pointStyleToString(int s) {
    switch (s) {
      case 1: // '\001'
        return "square";
    }
    return "circle";
  }

  private static void saveDatabase(MapLayer ml, int i) {
    String fname;
      //fname = "layer" + (i + 1) + ".dbf";
      //fname = ml.getId()+".dbf";
      fname = ml.getDatabase_file_name();
      String format;
      String id;
      String name;
      String encoding;
      DataSource datasource = new DataSource();
      ru.iitp.gis.common.model.DataSupplier ds = ml.getDataSupplier();
      /*if (ds instanceof DataTable){
          datasource = ((DataTable)ds).getDataSource();
      }*/
      datasource = ml.getDatasource();
      if (datasource == null) datasource = new DataSource();
      format = datasource.getFormat();
      if (format == null) format = "dbf";
      id = datasource.getIdAttr();
      if (id == null) id = "ID";
      name = datasource.getNameAttr();
      if (name == null && id.equals("ID")) name = "NAME";
      if (name == null) name = id+"_name";
      encoding = datasource.getEncoding();
      if (encoding == null) encoding = System.getProperty("encoding");

      if (fname == null || fname.length() <= 0) fname = ml.getId()+".dbf";
    DBFWriter.save(fullPath + File.separator + fname, ml.getDataSupplier(), id, name);
    mf.applyPattern("    <database src=\"{0}\" format=\"{1}\" id-attr=\"{2}\" name-attr=\"{3}\" encoding=\"{4}\">\n");
    sb.append(mf.format(((Object) (new Object[]{
      "file:"+File.separator+fullPath + File.separator + fname, format, id, name, encoding
    }))));
    Attribute root = ds.getRoot();
    if (root != null)
      for (int j = 0; j < root.getAttrCount(); j++)
        saveAttribute(root.getAttribute(j), 3);
    sb.append("    </database>\n");
  }

    private static void saveDatacomp(MapLayer ml) {
      String src;
        src = ml.getDatacomp().getSource().replace('/','\\');
      //DBFWriter.save(fullPath + File.separator + fname, ml.getDataSupplier());
      mf.applyPattern("    <datacomp src=\"{0}\" name=\"{1}\"/>\n");
      sb.append(mf.format(((Object) (new Object[]{
        src, "NAME"
      }))));
    }


  private static void saveAttribute(Attribute attr, int level) {
    int ac = attr.getAttrCount();
    if (ac == 0) {
      mf.applyPattern(PATTERN_ATTRIBUTE);
      makeIndent(level, 2);
      sb.append(mf.format(new Object[]{
        attr.getId(), attr.getName(), getTypeName(attr.getType())}));
    } else {
      makeIndent(level, 2);
      if (attr.getId() == null) {
        mf.applyPattern(PATTERN_ATTR_LIST_1);
        sb.append(mf.format(new Object[]{attr.getName()}));
      } else {
        mf.applyPattern(PATTERN_ATTR_LIST_2);
        sb.append(mf.format(new Object[]{
          attr.getId(), attr.getName(), getTypeName(attr.getType())}));
      }
      for (int i = 0; i < attr.getAttrCount(); i++)
        saveAttribute(attr.getAttribute(i), level + 1);
      makeIndent(level, 2);
      sb.append(ATTR_LIST);
    }
  }

  private static void makeIndent(int level, int size) {
    for (int i = 0; i < level; i++)
      for (int j = 0; j < size; j++)
        sb.append(' ');
  }

  private static void saveGeometry(MapLayer ml, int i) {
    Double scale;
      scale = new Double(ml.getGeometry_scale());  
    String fname;// = "layer" + (i + 1) + ".cvf";
      //fname = ml.getId()+".cvf";
      fname = ml.getGeometry_file_name();
      if (fname == null || fname.length() <= 0) fname = ml.getId()+".cvf";
      String format = "cvf";
      int pos = fname.lastIndexOf('.');
      if (pos >= 0) fname = fname.substring(0,pos+1)+format;
    mf.applyPattern("    <geometry src=\"{0}\" format=\"{1}\" scale=\"{2}\"/>\n");
    sb.append(mf.format(((Object) (new Object[]{
      "file:"+File.separator+fullPath + File.separator + fname, format, scale
    }))));
    try {
      CVFWriter.save(fullPath + File.separator + fname, ml);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void saveLayer(Map map, int i) {
    MapLayer ml = map.getLayer(i);
    mf.applyPattern("  <layer id=\"{0}\" name=\"{1}\" visible=\"{2}\">\n");
    sb.append(mf.format(((Object) (new Object[]{
      ml.getId(), ml.getName(), ml.isVisible() ? "true" : "false"
    }))));
    saveDatacomp(ml);
    saveGeometry(ml, i);
    if (ml.getDataSupplier() != null) saveDatabase(ml, i);
    saveStyle(ml, sb);
    sb.append("  </layer>\n");
  }

  public static void saveMap(String url, Map map) {
    //absolutPath = url+"_files";
    fullPath = TextUtils.trimExtension(url) + "_files";
    briefPath = fullPath.substring(fullPath.lastIndexOf(File.separator) + 1);
    (new File(fullPath)).mkdir();
    mf = new MessageFormat("<map name=\"{0}\" active-layer-id=\"{1}\">\n");
    sb = new StringBuffer();
    sb.append("<?xml version=\"1.0\" encoding=\"windows-1251\"?>\n\n");
    sb.append(mf.format(new String[]{
      map.getName(), map.getActiveLayer().getId()
    }));
    for (int i = 0; i < map.getLayerCount(); i++)
      saveLayer(map, map.getLayerCount()-i-1);

    sb.append("</map>\n");
    saveToFile(url, sb);
  }

  private static void saveStyle(MapLayer ml, StringBuffer sb) {
    Style style = ml.getStyle();
    sb.append("    <style\n");
    if (style.lineColor != null)
      sb.append("      line-color=\"" + colorToString(style.lineColor) + "\"\n");
    if (style.fillColor != null)
      sb.append("      fill-color=\"" + colorToString(style.fillColor) + "\"\n");
    if (style.textColor != null)
      sb.append("      text-color=\"" + colorToString(style.textColor) + "\"\n");
    if (style.palette != null)
      sb.append("      palette=\"" + colorToString(style.palette[0]) + " " + colorToString(style.palette[1]) + "\"\n");
    sb.append("      font-size=\"" + style.fontSize + "\"\n");
    sb.append("      draw-labels=\"" + style.drawLabels + "\"\n");
    sb.append("      draw-shadow=\"" + style.drawShadow + "\"\n");
    sb.append("      min-size=\"" + style.minSize + "\"\n");
    sb.append("      max-size=\"" + style.maxSize + "\"\n");
    sb.append("      point-style=\"" + pointStyleToString(style.pointStyle) + "\"\n");
    sb.append("    />\n");
  }

  private static void saveToFile(String fname, StringBuffer sb) {
    try {
      Writer fout = new OutputStreamWriter(new FileOutputStream(fname), "Cp1251");
      fout.write(sb.toString());
      fout.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
