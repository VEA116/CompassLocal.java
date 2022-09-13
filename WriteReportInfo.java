package ru.iitp.gis.local;

import ru.iitp.gis.applet.report.ReportInfo;
import ru.iitp.gis.applet.report.ReportAttributeBean;
import ru.iitp.gis.applet.report.ReportAttribute;

import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.beans.XMLEncoder;
import java.nio.charset.Charset;

/**
 * Created by IntelliJ IDEA.
 * User: STANISLAV ILYASOV
 * Date: 02.07.2006
 * Time: 14:22:56
 * To change this template use File | Settings | File Templates.
 */
public class WriteReportInfo {

    private static final String DEFAULT_ENCODING = "cp866";

    public static void main(String[] args){
        try{
            ReportInfo reportInfo = new ReportInfo();

            String plugin = null;
            String imageDir = null;
            String crystalPageURL = null;
            String encoding = null;

            for (int i = 0; i < args.length; i++){
                if (args[i].equalsIgnoreCase("-plugin") && i+1 < args.length && args[i+1].charAt(0) != '-')
                    plugin = args[++i];
                if (args[i].equalsIgnoreCase("-crystalPageURL") && i+1 < args.length && args[i+1].charAt(0) != '-')
                    crystalPageURL = args[++i];
                if (args[i].equalsIgnoreCase("-imageDirURL") && i+1 < args.length && args[i+1].charAt(0) != '-')
                    imageDir = args[++i];
                if (args[i].equalsIgnoreCase("-encoding") && i+1 < args.length && args[i+1].charAt(0) != '-')
                    encoding = args[++i];
            }

            if (encoding == null || !Charset.isSupported(encoding)){
                if (Charset.isSupported(DEFAULT_ENCODING)) encoding = DEFAULT_ENCODING;
                else encoding = Charset.defaultCharset().name();
            }

            InputStream in = System.in;
            PrintStream out = System.out;
            byte[] b = new byte[4096];
            int blen = b.length;
            int p;

            out.print("report user name = ");
            p = in.read(b,0,blen);
            reportInfo.setUserName(new String(b,0,p-2,encoding));

//            out.println("select report type:");
//            out.println("for "+ReportInfo.ALPHABET_REPORT+" press \""+ReportInfo.ALPHABET_REPORT_TYPE+"\"");
//            out.println("for "+ReportInfo.GROUP_REPORT+" press \""+ReportInfo.GROUP_REPORT_TYPE+"\"");
//            out.println("for "+ReportInfo.COLOR_REPORT+" press \""+ReportInfo.COLOR_REPORT_TYPE+"\"");
//            out.println("press "+ReportInfo.OTHER_REPORTS_TYPE+" for input yourself types in format type_1,...,type_n");
//            out.println("press other symbols for no report type defined");
//            p = in.read(b,0,blen);
//            int rtype = Integer.parseInt(new String(b,0,p-2,encoding));
//            if (rtype == ReportInfo.ALPHABET_REPORT_TYPE) reportInfo.setReportType(ReportInfo.ALPHABET_REPORT);
//            if (rtype == ReportInfo.GROUP_REPORT_TYPE) reportInfo.setReportType(ReportInfo.GROUP_REPORT);
//            if (rtype == ReportInfo.COLOR_REPORT_TYPE) reportInfo.setReportType(ReportInfo.COLOR_REPORT);
//            if (type == ReportInfo.OTHER_REPORTS_TYPE){
//                out.println("input types");
//                reportInfo.setReportType(ReportInfo.COLOR_REPORT);
//            }

            out.print("maximum map count = ");
            p = in.read(b,0,blen);
            reportInfo.setMaxMapCount(Integer.parseInt(new String(b,0,p-2,encoding)));
            //out.print('\n');

            out.print("info type = ");
            p = in.read(b,0,blen);
            int infoType = Integer.parseInt(new String(b,0,p-2,encoding));
            reportInfo.setInfoType(infoType);
            //out.print('\n');

            out.print("list title = ");
            p = in.read(b,0,blen);
            reportInfo.setListTitle(new String(b,0,p-2,encoding));

            out.print("book landscape (1 - book, 0 - letter) = ");
            p = in.read(b,0,blen);
            reportInfo.setBookLandscape(Integer.parseInt(new String(b,0,p-2,encoding)));

            out.print("image size = ");
            p = in.read(b,0,blen);
            reportInfo.setImageSize(Integer.parseInt(new String(b,0,p-2,encoding)));

            if (crystalPageURL == null){
                out.print("report page url = ");
                p = in.read(b,0,blen);
                crystalPageURL = new String(b,0,p-2,encoding);
                //out.print('\n');
            }
            reportInfo.setCrystalPageURL(crystalPageURL);

            /*out.print("to enter image file names press \"y\", else press \"n\": ");
            p = in.read(b,0,blen);
            String select = new String(b,0,p-2);
            if ("y".equalsIgnoreCase(select)){
                String[] imageFileNames = new String[reportInfo.getMapCount()];
                for (int i = 0; i < imageFileNames.length; i++){
                    out.print("image "+i+" file name = ");
                    p = in.read(b,0,blen);
                    imageFileNames[i] = new String(b,0,p-2);
                }
                reportInfo.setImageFileName(imageFileNames);
            }*/

            out.print("to enter top info caption press \"y\", else press \"n\": ");
            p = in.read(b,0,blen);
            String select = new String(b,0,p-2,encoding);
            if ("y".equalsIgnoreCase(select)){
                String[] topInfo = new String[reportInfo.getMaxMapCount()];
                for (int i = 0; i < topInfo.length; i++){
                    out.print("map "+i+" top info caption = ");
                    p = in.read(b,0,blen);
                    topInfo[i] = new String(b,0,p-2,encoding);
                }
                reportInfo.setMapInfoTopCaption(topInfo);
            }

            out.print("to enter bottom info caption press \"y\", else press \"n\": ");
            p = in.read(b,0,blen);
            select = new String(b,0,p-2,encoding);
            if ("y".equalsIgnoreCase(select)){
                String[] bottomInfo = new String[reportInfo.getMaxMapCount()];
                for (int i = 0; i < bottomInfo.length; i++){
                    out.print("map "+i+" bottom info caption = ");
                    p = in.read(b,0,blen);
                    bottomInfo[i] = new String(b,0,p-2,encoding);
                }
                reportInfo.setMapInfoBottomCaption(bottomInfo);
            }

            if (imageDir == null){
                out.print("image dir url = ");
                p = in.read(b,0,blen);
                imageDir = new String(b,0,p-2,encoding);
            }
            reportInfo.setImageDirURL(imageDir);

            if (plugin == null){
                out.print("plugin dir url = ");
                p = in.read(b,0,blen);
                plugin = new String(b,0,p-2,encoding);
                //out.print('\n');
            }

            out.print("report file name = ");
            p = in.read(b,0,blen);
            String reportName = new String(b,0,p-2,encoding);

            out.print("attribute count = ");
            p = in.read(b,0,blen);
            int attrCount = Integer.parseInt(new String(b,0,p-2,encoding));



            Map<String, ReportAttributeBean> attributesMap = new HashMap<String, ReportAttributeBean>();
            for (int i = 0; i < attrCount; i++){
                out.print("name"+i+" = ");
                p = in.read(b,0,blen);
                String key = new String(b,0,p-2,encoding);

                out.println("if attribute is "+ ReportAttribute.ARRAY_STR+" press "+ReportAttribute.ARRAY);
                out.println("else press other key");
                int attrType;
                p = in.read(b,0,blen);
                String attrTypeStr = new String(b,0,p-2,encoding);
                try{
                    attrType = Integer.parseInt(attrTypeStr);
                } catch(NumberFormatException nfe){
                    attrType = ReportAttribute.STRING;
                }
                if (attrType != ReportAttribute.ARRAY) attrType = ReportAttribute.STRING;
                ReportAttributeBean reportAttribute = new ReportAttributeBean();
                reportAttribute.setType(attrType);

                if (attrType == ReportAttribute.ARRAY){
                    out.println("if combo press "+ReportAttribute.COMBO);
                    out.println("if checkbox press "+ReportAttribute.CHECKBOX);
                    out.println("if radio button press "+ReportAttribute.RADIO);
                    p = in.read(b,0,blen);
                    int showType = ReportAttribute.COMBO;
                    String selectionType = new String(b,0,p-2,encoding);
                    try{
                        int st = Integer.parseInt(selectionType);
                        if (st == ReportAttribute.CHECKBOX || st == ReportAttribute.RADIO)  showType = st;
                    } catch(NumberFormatException nfe){}
                    reportAttribute.setShowType(showType);
                    out.println("enter attribute"+i+" values string ( .,...,.) :");
                } else {
                    out.println("if attribute is shown and editable press 1, else press other key");
                    p = in.read(b,0,blen);
                    boolean  isShown = false;
                    String isShownStr = new String(b,0,p-2,encoding);
                    try{
                        if (Integer.parseInt(isShownStr) == 1) isShown = true;
                    } catch(NumberFormatException nfe){}
                    reportAttribute.setShowInDialog(isShown);
                    out.print("enter attribute"+i+" value :");
                }


                p = in.read(b,0,blen);
                String value = new String(b,0,p-2,encoding);
                reportAttribute.setValue(value);
                attributesMap.put(key,reportAttribute);
            }
            reportInfo.setAttributesMap(attributesMap);

            Map<String,String> templates = new HashMap<String, String>();

            out.println("set templates (press \"y\" to begin and \"n\" to skip)");
            p = in.read(b,0,blen);
            select = new String(b,0,p-2,encoding);
            if ("y".equalsIgnoreCase(select)){
                out.println("\nfor grouping and distance\n");
                out.println("#S - scripts for intervals");
                out.println("#C - colors in intervals (LEGEND_COLOR)");
                out.println("#V - values of slider");
                out.println("#L - labels for slider's values");
                out.println("#s - min value");
                out.println("#b - max value");
                out.println("#v - slider's value in distance mode");
                out.println("\nfor compare and object\n");
                out.println("#D1 - data array of objects/attributes (OBJECT_VALUES)");
                out.println("#D2 - data array of objects of multyattribute (first values are the values of child attributes at the first object)");
                out.println("#C - colors in chart (LEGEND_COLOR)");
                out.println("#c1 - objects/attributes count (NUMBER_OF_OBJECT)");
                out.println("#L - names of objects/attributes (OBJECT_NAMES)");
                out.println("#c2 - child atributes count in multyattribute");
                out.println("#t - total objects/attributes sum (TOTAL_VALUE)");
                out.println("#tl - \"ALL\" label");
                out.println("#s - sum of selected objects/attributes");
                out.println("#sl - \"SELECTED\" label");

                String c;
                do{
                    out.print("type = ");
                    p = in.read(b,0,blen);
                    String type = new String(b,0,p-2,encoding);
                    out.print("template = ");
                    p = in.read(b,0,blen);
                    String template = new String(b,0,p-2,encoding);
                    out.print("press 1 for continue, other key for exit templates settings");
                    p = in.read(b,0,blen);
                    templates.put(type,template);
                    c = new String(b,0,p-2,encoding);
                } while(c.equals("1"));
                reportInfo.setTemplates(templates);
            }

            Map<String,String> replacer = new HashMap<String, String>();

            out.println("set replacers for inner strings (press \"y\" to begin and \"n\" to skip)");
            p = in.read(b,0,blen);
            select = new String(b,0,p-2,encoding);
            if ("y".equalsIgnoreCase(select)){
                String c;
                do{
                    out.print("char = ");
                    p = in.read(b,0,blen);
                    String key = new String(b,0,p-2,encoding);
                    out.print("replacement = ");
                    p = in.read(b,0,blen);
                    String replacement = new String(b,0,p-2,encoding);
                    out.print("press 1 for continue, other key for exit replacers settings");
                    p = in.read(b,0,blen);
                    replacer.put(key,replacement);
                    c = new String(b,0,p-2,encoding);
                } while(c.equals("1"));
                reportInfo.setReplacer(replacer);
            }


            XMLEncoder xmlEncoder = new XMLEncoder(new FileOutputStream(plugin+"/"+reportName+".xml"));
            xmlEncoder.writeObject(reportInfo);
            xmlEncoder.close();
        } catch(Exception e){e.printStackTrace();}
    }
}
