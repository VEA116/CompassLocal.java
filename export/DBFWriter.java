// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   DBFWriter.java

package ru.iitp.gis.local.export;

import ru.iitp.gis.common.model.Attribute;
import ru.iitp.gis.common.model.DataSupplier;
import ru.iitp.gis.common.util.PCOutputStream;
import ru.iitp.gis.common.util.TextUtils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class DBFWriter {

  public DBFWriter() {
  }

  private static int[] getPrec(DataSupplier dtab) {
    int prec[] = new int[dtab.getAttrCount() + 2];
    for (int i = 0; i < prec.length - 2; i++) {
      Attribute a = dtab.getAttribute(i);
      String id = a.getId();
      prec[i + 2] = 0;
      switch (a.getType()) {
        case 8: // '\b'
          for (int j = 0; j < dtab.getRecordCount(); j++)
            try {
              String s = dtab.getRecord(j).getData(id).toString();
              int p = s.length() - s.indexOf(".") - 1;
              prec[i + 2] = Math.max(p, prec[i + 2]);
            } catch (NullPointerException _ex) {
            }

          break;

        default:
          prec[i + 2] = 0;
          break;
      }
    }

    return prec;
  }

  private static int getRecordLength(int size[]) {
    int reclen = 1;
    for (int i = 0; i < size.length; i++)
      reclen += size[i];

    return reclen;
  }

  private static int[] getSize(DataSupplier dtab) {
    int size[] = new int[dtab.getAttrCount() + 2];
    for (int j = 0; j < dtab.getRecordCount(); j++) {
      try {
        size[0] = Math.max(dtab.getRecord(j).getId().length(), size[0]);
      } catch (NullPointerException _ex) {
      }
      try {
        size[1] = Math.max(dtab.getRecord(j).getName().length(), size[1]);
      } catch (NullPointerException _ex) {
      }
    }

    for (int i = 0; i < size.length - 2; i++) {
      Attribute a = dtab.getAttribute(i);
      String id = a.getId();
      size[i + 2] = 0;
      int j;
      switch (a.getType()) {
        case 2: // '\002'
          size[i + 2] = 1;
          // fall through

        default:
          j = 0;
          break;
      }
      for (; j < dtab.getRecordCount(); j++)
        try {
          size[i + 2] = Math.max(dtab.getRecord(j).getData(id).toString().length(), size[i + 2]);
        } catch (NullPointerException _ex) {
        }

    }

    return size;
  }

  private static char getType(Attribute a) {
    switch (a.getType()) {
      case 2: // '\002'
        return 'L';

      case 4: // '\004'
      case 8: // '\b'
        return 'N';

      case 3: // '\003'
      case 5: // '\005'
      case 6: // '\006'
      case 7: // '\007'
      default:
        return 'C';
    }
  }

  private static String getValue(Object o, int type) {
    switch (type) {
      case 2: // '\002'
        try {
          boolean b = ((Boolean) o).booleanValue();
          return b ? "T" : "F";
        } catch (Exception _ex) {
          return "F";
        }

      case 16: // '\020'
        try {
          return o.toString();//TextUtils.fromUnicode(o.toString());
        } catch (Exception _ex) {
          return "";
        }
    }
    try {
      return o.toString();
    } catch (Exception _ex) {
      return "";
    }
  }

  public static void save(String sout, DataSupplier dtab, String id, String name) {
    try {
      int cols = dtab.getAttrCount();
      int rows = dtab.getRecordCount();
      int size[] = getSize(dtab);
      int prec[] = getPrec(dtab);
      int reclen = getRecordLength(size);
      PCOutputStream out = new PCOutputStream(new BufferedOutputStream(new FileOutputStream(sout)));
      out.writeByte(3);
      out.write(new byte[3]);
      out.writePCInt(rows);
      out.writePCShort((cols + 2) * 32 + 33);
      out.writePCShort(reclen);
      out.write(new byte[20]);
      out.write(toByteArray(id, 11));
      out.writeByte(67);
      out.write(new byte[4]);
      out.writeByte(size[0]);
      out.writeByte(prec[0]);
      out.write(new byte[14]);
      out.write(toByteArray(name, 11));
      out.writeByte(67);
      out.write(new byte[4]);
      out.writeByte(size[1]);
      out.writeByte(prec[1]);
      out.write(new byte[14]);
      for (int i = 0; i < cols; i++) {
        Attribute attr = dtab.getAttribute(i);
        out.write(toByteArray(attr.getId(), 11));
        out.writeByte(getType(attr));
        out.write(new byte[4]);
        out.writeByte(size[i + 2]);
        out.writeByte(prec[i + 2]);
        out.write(new byte[14]);
      }

      out.writeByte(13);
      for (int i = 0; i < rows; i++) {
        out.writeByte(32);
        out.write(toByteArray(getValue(dtab.getRecord(i).getId(), 16), size[0]));
        out.write(toByteArray(getValue(dtab.getRecord(i).getName(), 16), size[1]));
        for (int j = 0; j < cols; j++)
          out.write(toByteArray(getValue(dtab.getRecord(i).getData(j), dtab.getAttribute(j).getType()), size[j + 2]));

      }

      out.close();
    } catch (Exception fe) {
      fe.printStackTrace();
    }
  }

  private static byte[] toByteArray(Object s, int len) {
    byte b[] = new byte[len];
    for (int i = 0; i < b.length; i++)
      b[i] = 32;

    try {
      byte sb[] = s.toString().getBytes();
      System.arraycopy(sb, 0, b, 0, sb.length);
    } catch (Exception _ex) {
    }
    return b;
  }
}
