package ru.iitp.gis.common.factory;

import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import ru.iitp.gis.common.Config;
import ru.iitp.gis.common.util.datacomp.Datacomp;
import ru.iitp.gis.common.model.*;
import ru.iitp.gis.common.view.ColorRenderer;
import ru.iitp.gis.common.view.Style;

import java.awt.Color;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.Stack;
import java.util.StringTokenizer;

public class XMLMapFactory extends MapFactory
        implements DocumentHandler, ErrorHandler, DataType {

    private static final int MAP = 0;
    private static final int LAYER = 1;
    private static final int GEOMETRY = 2;
    private static final int DATABASE = 3;
    private static final int ATTR_LIST = 4;
    private static final int ATTR = 5;
    private static final int STYLE = 6;
    private static final int DATACOMP = 7;

    private static final int ATTR_GROUP = 8;
    private static final int MAPPING = 9;
    private static final int OBJECTS = 10;
    private static final int OBJECT = 11;
    private static final int REGNAMES = 12;
    private static Hashtable tags = new Hashtable();
    public static Hashtable types = new Hashtable();


    static {
        tags.put("map", new Integer(MAP));
        tags.put("layer", new Integer(LAYER));
        tags.put("geometry", new Integer(GEOMETRY));
        tags.put("regnames", new Integer(REGNAMES));
        tags.put("database", new Integer(DATABASE));
        tags.put("attr-list", new Integer(ATTR_LIST));
        tags.put("attr", new Integer(ATTR));
        tags.put("style", new Integer(STYLE));
        tags.put("datacomp", new Integer(DATACOMP));
        tags.put("attr-group", new Integer(ATTR_GROUP));
        tags.put("mapping", new Integer(MAPPING));
        tags.put("objects", new Integer(OBJECTS));
        tags.put("object", new Integer(OBJECT));
        types.put("boolean", new Integer(BOOLEAN));
        types.put("integer", new Integer(INTEGER));
        types.put("float", new Integer(FLOAT));
        types.put("string", new Integer(STRING));
        types.put("date", new Integer(DATE));
        types.put("url", new Integer(URL));
        types.put("raster", new Integer(RASTER));
    }

    private Stack stack = new Stack();
    //private DataSource dsrc;
    //private DataTable dtab;

    Map map;
    String activeLayerId;
    Attribute root;
    DataTable dtab1 = null;

    public Map createMap(String url) throws Exception {
        Parser parser = //ParserFactory.makeParser(ru.iitp.gis.Config.PARSER);
                new com.microstar.xml.SAXDriver();
        parser.setDocumentHandler(this);
        parser.setErrorHandler(this);
        InputSource is = new InputSource(
                new InputStreamReader(new URL(url).openStream(),
                        Config.ENCODING));
        parser.parse(is);
        return map;
    }

    private int getTag(String tagName) {
        Integer t = (Integer) tags.get(tagName);
        return t != null ? t.intValue() : -1;
    }

    private int getType(String typeName) {
        if (typeName == null) return NULL;
        Integer t = (Integer) types.get(typeName);
        return t != null ? t.intValue() : NULL;
    }

    private void showMessage(String msg) {
        System.err.println(msg);
    }
    private String pref;
    //org.xml.sax.DocumentHandler implementation

    public void startElement(String name, AttributeList attrs) {
        switch (getTag(name)) {
            case MAP:
                map = new Map();
                map.setName(attrs.getValue("name"));
                activeLayerId = attrs.getValue("active-layer-id");
                stack.push(map);
                break;
            case LAYER:
                MapLayer layer = new MapLayer((Map) stack.peek());
                String layerId = attrs.getValue("id");
                String layerName = attrs.getValue("name");
                layer.setId(layerId);
                layer.setName(layerName);
                String visible = attrs.getValue("visible");
                if (visible != null && visible.equals("false")) layer.setVisible(false);
                stack.push(layer);
                showMessage("Load map layer: " + attrs.getValue("id"));
                break;
            case GEOMETRY:
                String src = attrs.getValue("src");
                String format = attrs.getValue("format");
                String scl = attrs.getValue("scale");
                String file_name_geo;
                if (src.lastIndexOf('\\') >= 0) file_name_geo = src.substring(src.lastIndexOf('\\') + 1, src.length());
                else
                if (src.lastIndexOf('/') >= 0) file_name_geo = src.substring(src.lastIndexOf('/') + 1, src.length());
                else file_name_geo = src;
                double scale;
                boolean bind = false;
                if (scl == null) {
                    scale = 1;
                    bind = true;
                } else {
                    try {
                        scale = Double.valueOf(scl).doubleValue();
                    } catch (NumberFormatException e) {
                        scale = 1;
                    }
                }
                /*try{
                    bind = "true".equals(attrs.getValue("geo"));
                } catch (RuntimeException e){
                    bind = false;
                }*/

                showMessage("  Load geometry: " + src);
                try {
                    layer = (MapLayer) stack.peek();
                    if (src != null && src.length() > 0 && layer != null) layer.setGeometry_file_name(file_name_geo);
                    if (layer != null) layer.setGeometry_scale(scale);
                    GeoFactory gf = GeoFactory.getFactory(format);
                    if (gf instanceof SHPGeoFactory) ((SHPGeoFactory) gf).setScale(scale, bind);
                    gf.createGeometry(src, layer);
                    map.addLayer(layer);
                } catch (Exception e) {
                    showMessage(e.toString());
                    break;
                }
                break;
                case REGNAMES :
                    //String src = attrs.getValue("src");
                    //String format = attrs.getValue("format");
                   layer = (MapLayer) stack.peek();
                    String srcRN = attrs.getValue("src");
                    String file_name_rn;
                    if (srcRN.lastIndexOf('\\') >= 0)
                        file_name_rn = srcRN.substring(srcRN.lastIndexOf('\\') + 1, srcRN.length());
                    else if (srcRN.lastIndexOf('/') >= 0)
                        file_name_rn = srcRN.substring(srcRN.lastIndexOf('/') + 1, srcRN.length());
                    else file_name_rn = srcRN;
               //     if (srcRN != null && srcRN.length() > 0 && layer != null) layer.setRegionNames_file_name(file_name_rn);

                    String formatrn = attrs.getValue("format");
                    String idrn = attrs.getValue("id-attr");
                    if (formatrn == null)
                        formatrn = "xlsx";
                    if (idrn == null)
                        formatrn = "REGION";

                    RegionNamesSource rsrc = new RegionNamesSource();
                    rsrc.setSrc(srcRN);
                    rsrc.setFormat(attrs.getValue("format"));
                    rsrc.setIdAttr(attrs.getValue("id-attr"));
                    if (rsrc.getSrc() != null && layer != null) {
                        String str = new String(rsrc.getSrc());
                        int ind = str.lastIndexOf('/');
                        if (ind == -1) ind = str.lastIndexOf('\\');
                        if (ind == -1) str = "";
                        else str = str.substring(0, ind + 1);
                        Config.COMMENT_DIRECTORIES.put(layer.getId(), str + "Comment");
                    }
//                    if (layer != null) layer.setRNsource(rsrc);
                    //this.dsrc = (DataSource)dsrc.clone();
 //my                   stack.push(dsrc);
//                    DataTable rnm = new DataTable();
                    //this.dtab = new DataTable();
//                    dtab.setRegionNamesSource(rsrc);

                    break;
            case DATABASE:
                layer = (MapLayer) stack.peek();
                String srcDB = attrs.getValue("src");
                String file_name_db;
                if (srcDB.lastIndexOf('\\') >= 0)
                    file_name_db = srcDB.substring(srcDB.lastIndexOf('\\') + 1, srcDB.length());
                else if (srcDB.lastIndexOf('/') >= 0)
                    file_name_db = srcDB.substring(srcDB.lastIndexOf('/') + 1, srcDB.length());
                else file_name_db = srcDB;
                if (srcDB != null && srcDB.length() > 0 && layer != null) layer.setDatabase_file_name(file_name_db);
                DataSource dsrc = new DataSource();
                dsrc.setSrc(srcDB);
                dsrc.setTable(attrs.getValue("table"));
                dsrc.setFormat(attrs.getValue("format"));
                dsrc.setIdAttr(attrs.getValue("id-attr"));
                dsrc.setNameAttr(attrs.getValue("name-attr"));
                dsrc.setEncoding(attrs.getValue("encoding"));
                if (dsrc.getSrc() != null && layer != null) {
                    String str = new String(dsrc.getSrc());
                    int ind = str.lastIndexOf('/');
                    if (ind == -1) ind = str.lastIndexOf('\\');
                    if (ind == -1) str = "";
                    else str = str.substring(0, ind + 1);
                    Config.COMMENT_DIRECTORIES.put(layer.getId(), str + "Comment");
                }
                if (layer != null) layer.setDatasource(dsrc);
                //this.dsrc = (DataSource)dsrc.clone();
                pref = dsrc.getSrc().substring(0,dsrc.getSrc().lastIndexOf('.'))+"_";//my chan
                stack.push(dsrc);
                DataTable dtab = new DataTable();
                if(dtab1!=null)//my ch
                    dtab = dtab1;// my ch
                //this.dtab = new DataTable();
                root = dtab.getRoot();
                //this.dtab.setRoot(root);
                //this.dtab.setDataSource(this.dsrc);
                dtab.setDataSource(dsrc);
                stack.push(dtab);
                break;
            case OBJECT:
                DataTable dt = (DataTable) stack.peek();
                dt.names.put(attrs.getValue("id"), attrs.getValue("value"));
                break;
            case ATTR_GROUP:
                Attribute group = new Attribute();
                group.setId(attrs.getValue("attr"));
                group.setGrouped();

                dtab = (DataTable) stack.peek();
                if(dtab.getAttribute(group.getId())==null)
                    dtab.addAttribute(group);

                if (root != null)
                    root.addAttribute(group);
                root = group;
                break;
            case MAPPING:
                boolean mapAll = "all".equalsIgnoreCase(attrs.getValue("type"));
                Mapping mapping = new Mapping(attrs.getValue("name"),
                        attrs.getValue("value"), attrs.getValue("id"), mapAll);
                root.addMapping(mapping);
                break;
            case ATTR_LIST:
                Attribute node = new Attribute();
                node.setName(attrs.getValue("name"));
                node.setId(attrs.getValue("id"));
//        System.out.println("<attr-list name=" + node.getName());
                root.addAttribute(node);
                root = node;
                break;
            case ATTR:
                String id = attrs.getValue("id");
                if (id == null) break;

                dtab = (DataTable) stack.peek();

                Attribute attr = dtab.getAttribute(id);
                if(attr==null) {
                    attr = new Attribute(pref+id, attrs.getValue("name"), getType(attrs.getValue("type")));//my chan pref+
                    dtab.addAttribute(attr);
                }

                if (root != null)
                    root.addAttribute(attr);
                break;
            case STYLE:
                layer = (MapLayer) stack.peek();
                Style style = new Style();
                layer.setStyle(style);
                Color fc = ColorRenderer.parseColor(attrs.getValue("fill-color"));
                Color lc = ColorRenderer.parseColor(attrs.getValue("line-color"));
                Color tc = ColorRenderer.parseColor(attrs.getValue("text-color"));
                if (fc != null) style.fillColor = fc;
                if (lc != null) style.lineColor = lc;
                if (tc != null) style.textColor = tc;
                try {
                    style.minSize = Integer.parseInt(attrs.getValue("min-size"));
                } catch (Exception e) {
                }
                try {
                    style.maxSize = Integer.parseInt(attrs.getValue("max-size"));
                } catch (Exception e) {
                }
                try {
                    style.fontSize = Integer.parseInt(attrs.getValue("font-size"));
                } catch (Exception e) {
                }
                String ps = attrs.getValue("point-style");
                if (ps != null) {
                    if (ps.equals("circle"))
                        style.pointStyle = Style.CIRCLE;
                    else if (ps.equals("square")) style.pointStyle = Style.SQUARE;
                }
                String cs = attrs.getValue("chart-style");
                if (cs != null) {
                    if (cs.equals("pie-chart"))
                        style.chartStyle = Style.PIE_CHART;
                    else if (cs.equals("bar-chart")) style.chartStyle = Style.BAR_CHART;
                }
                style.drawLabels = new Boolean(attrs.getValue("draw-labels")).booleanValue();
                style.drawShadow = new Boolean(attrs.getValue("draw-shadow")).booleanValue();
                String palette = attrs.getValue("palette");
                if (palette == null) break;
                StringTokenizer st = new StringTokenizer(palette, " ");
                Color c[] = new Color[st.countTokens()];
                if (c.length != 2) break;
                c[0] = ColorRenderer.parseColor(st.nextToken());
                c[1] = ColorRenderer.parseColor(st.nextToken());
                if (c[0] != null && c[1] != null) style.palette = c;
                break;
            case DATACOMP:
                String source = attrs.getValue("src");
                String levelName = attrs.getValue("name");
                String dcformat = attrs.getValue("format");
                //String encoding = attrs.getValue("encoding");
                //this.dsrc.setSrc(source);
                //this.dsrc.setFormat(dcformat);
                //showMessage("  Load levels: " + this.dsrc.getSrc());
                Datacomp d = new Datacomp(source, levelName, dcformat);
                /*try{
                    //DataFactory.getFactory(this.dsrc.getFormat()).createTable(this.dsrc, this.dtab);
                    //d = new Datacomp(this.dtab);
                }
                catch (Exception e) {
                    d = new Datacomp(source, levelName);
                }*/
                ((MapLayer) stack.peek()).setDatacomp(d);

            default:
                break;
        }
    }

    public void endElement(String name) {
        switch (getTag(name)) {
            case MAP:
                map.setActiveLayer(activeLayerId);
                break;
            case LAYER:
                stack.pop();
                break;

            case REGNAMES :
            break;

            case DATABASE:
                DataTable dtab = (DataTable) stack.pop();
                DataSource dsrc = (DataSource) stack.pop();
                dtab1 = dtab;//my ch
                showMessage("  Load database: " + dsrc.getSrc());
                try {
                    long t = System.currentTimeMillis();
                    DataFactory.getFactory(dsrc.getFormat()).createTable(dsrc, dtab);
                    long t2 = System.currentTimeMillis();
                    if(false) //my change
                    dtab = dtab.reformat();
//                    XLSXDataFactory.out(dtab);
                    if(false) //my change
                    dtab  = dtab.cleaning();
                    long t3 = System.currentTimeMillis();
                    System.out.println("Time of creating: "+(t2-t));
                    System.out.println("Time of formating: "+(t3-t2));
                    MapLayer layer = (MapLayer) stack.peek();
                    int rc = dtab.getRecordCount();
                    int oc = layer.getObjectCount();
                    if (oc != rc)
                        throw new FactoryException("Number of data records must be equals"
                                + " number of geo objects: " + dsrc.getSrc() + " " + rc + "!=" + oc);
                    layer.setDataSupplier(dtab);
                } catch (Exception e) {
                    showMessage(e.toString());
                    ((MapLayer) stack.peek()).setDataSupplier(null);
                    break;
                }
                break;
            case ATTR_GROUP:
            case ATTR_LIST:
                if (root.getParent() != null)
                    root = root.getParent();
                break;
            default:
                break;
        }
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

    public void processingInstruction(String target, String data) throws SAXException {
    }

    public void setDocumentLocator(Locator locator) {
    }

    //org.xml.sax.ErrorHandler implementation

    public void error(SAXParseException e) throws SAXException {
        showMessage(e.toString());
    }

    public void fatalError(SAXParseException e) throws SAXException {
        showMessage(e.toString());
    }

    public void warning(SAXParseException e) throws SAXException {
        showMessage(e.toString());
    }

}
