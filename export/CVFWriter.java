// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   CVFWriter.java

package ru.iitp.gis.local.export;

import ru.iitp.gis.common.model.GeoLine;
import ru.iitp.gis.common.model.GeoPoint;
import ru.iitp.gis.common.model.MapLayer;
import ru.iitp.gis.common.util.PCOutputStream;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CVFWriter {

  private CVFWriter() {
  }

  private static int getSize(MapLayer ml) {
    int size = 50;
    switch (ml.getType()) {
      case 2: // '\002'
      case 4: // '\004'
      default:
        break;

      case 1: // '\001'
        size += ml.getObjectCount() * 10;
        break;

      case 3: // '\003'
      case 5: // '\005'
        for (int i = 0; i < ml.getObjectCount(); i++) {
          Polygon p[] = ((GeoLine) ml.getObject(i)).getParts();
          size += 18 + 2 * p.length;
          for (int j = 0; j < p.length; j++)
            size += p[j].npoints * 4;

        }

        break;
    }
    return size;
  }

  public static void save(String urlout, MapLayer ml)
    throws Exception {
    File fout = new File(urlout);
    PCOutputStream os = new PCOutputStream(new BufferedOutputStream(new FileOutputStream(urlout)));
    int code = 9994;
    os.writeInt(code);
    for (int i = 0; i < 5; i++)
      os.writeInt(0);

    os.writeInt(getSize(ml));
    int version = 1000;
    os.writePCInt(version);
    os.writePCInt(ml.getType());
    Rectangle bounds = ml.getBounds();
    os.writePCInt(bounds.x);
    os.writePCInt(bounds.y);
    os.writePCInt(bounds.x + bounds.width);
    os.writePCInt(bounds.y + bounds.height);
    for (int i = 0; i < 6; i++)
      os.writePCDouble(0.0D);

    switch (ml.getType()) {
      case 1: // '\001'
        savePoints(os, ml);
        break;

      case 3: // '\003'
      case 5: // '\005'
        savePolyLines(os, ml);
        break;
    }
    os.close();
  }

  private static void savePoints(PCOutputStream os, MapLayer ml)
    throws IOException {
    for (int i = 0; i < ml.getObjectCount(); i++) {
      os.writeInt(i);
      os.writeInt(6);
      os.writePCInt(ml.getType());
      GeoPoint gp = (GeoPoint) ml.getObject(i);
      os.writePCInt(gp.getX());
      os.writePCInt(gp.getY());
    }

  }

  public static void savePolyLines(PCOutputStream os, MapLayer ml)
    throws IOException {
    for (int i = 0; i < ml.getObjectCount(); i++) {
      os.writeInt(i);
      GeoLine gl = (GeoLine) ml.getObject(i);
      int numParts = gl.getParts().length;
      int numPoints = 0;
      int length = 14 + 2 * numParts;
      for (int j = 0; j < numParts; j++) {
        int np = gl.getParts()[j].npoints;
        length += 4 * np;
        numPoints += np;
      }

      os.writeInt(length);
      os.writePCInt(ml.getType());
      Rectangle bounds = gl.getBounds();
      os.writePCInt(bounds.x);
      os.writePCInt(bounds.y);
      os.writePCInt(bounds.x + bounds.width);
      os.writePCInt(bounds.y + bounds.height);
      os.writePCInt(numParts);
      os.writePCInt(numPoints);
      os.writePCInt(0);
      int ndx = 0;
      for (int j = 0; j < numParts - 1; j++) {
        ndx += gl.getParts()[j].npoints;
        os.writePCInt(ndx);
      }

      for (int j = 0; j < numParts; j++) {
        for (int k = 0; k < gl.getParts()[j].npoints; k++) {
          os.writePCInt(gl.getParts()[j].xpoints[k]);
          os.writePCInt(gl.getParts()[j].ypoints[k]);
        }

      }

    }

  }

  private static final int MAIN_HEADER_SIZE = 50;
  private static String wdir = System.getProperty("user.dir");

}
