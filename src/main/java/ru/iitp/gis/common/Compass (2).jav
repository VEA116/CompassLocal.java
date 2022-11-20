package ru.iitp.gis.common;

import ru.iitp.gis.common.control.*;
import ru.iitp.gis.common.control.AttrPanel;
import ru.iitp.gis.common.event.MapEvent;
import ru.iitp.gis.common.event.MapListener;
import ru.iitp.gis.common.factory.MapFactory;
import ru.iitp.gis.common.gui.OptionPane;
import ru.iitp.gis.common.gui.SplitPanel;
import ru.iitp.gis.common.model.Map;
import ru.iitp.gis.common.model.MapLayer;
import ru.iitp.gis.common.view.*;
import ru.iitp.gis.local.export.XMLWriter;
import ru.iitp.gis.local.gui.FileChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

public class Compass extends Panel
        implements ActionListener, ItemListener, MapListener {

    public Compass() {
        map = null;
        mapCanvas = null;
        mapControl = null;
        legendCanvas = null;
        btnShowRows = new Button("Show columns names");
        btnZoomIn = new Button(Config.getUIProperty("zoom.button.zoom-in"));
        btnZoomOut = new Button(Config.getUIProperty("zoom.button.zoom-out"));
        btnFullExtent = new Button(Config.getUIProperty("zoom.button.full-extent"));
        cgMouseMode = new CheckboxGroup();
        cbZoomMap = new Checkbox(Config.getUIProperty("zoom.radio.zoom"), true, cgMouseMode);
        cbPanMap = new Checkbox(Config.getUIProperty("zoom.radio.pan"), false, cgMouseMode);
        cbLabels = new Checkbox("Подписи регионов", true);
        btnExport = new Button(Config.getUIProperty("main.button.save-map"));
        cbLabels.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                setLabelsState(cbLabels.getState());
            }
        });
        sp = new ScrollPane(0);
        //tp1 = new TabPanel();
        //tp2 = new TabPanel();
        UIManager.put("TabbedPane.selected", Color.cyan);
        tp1 = new JTabbedPane();
        tp2 = new JTabbedPane();
        sp1 = new SplitPanel(true);
        p1 = new Panel();
        pZoom = new Panel();
        sp2 = new SplitPanel(false);
        p2 = new Panel();
        p3 = new Panel(new BorderLayout());
        Config.COMPASS = this;
        btnExport.addActionListener(this);
        btnZoomIn.addActionListener(this);
        btnZoomOut.addActionListener(this);
        btnFullExtent.addActionListener(this);
        cbZoomMap.addItemListener(this);
        cbPanMap.addItemListener(this);
        setLayout(new BorderLayout());
        p1.setBackground(SystemColor.control);
        p1.setLayout(new BorderLayout());
        pZoom.setLayout(new GridLayout(5, 1, 1, 1));
        pZoom.add(cbZoomMap);
        pZoom.add(cbPanMap);
        pZoom.add(cbLabels);
        pZoom.add(new JLabel(""));
        pZoom.add(btnZoomIn);
        pZoom.add(btnZoomOut);
        pZoom.add(btnFullExtent);
        pZoom.add(btnExport);
        p1.add("North", pZoom);
        p2.setLayout(new BorderLayout());
        long t1 = System.currentTimeMillis();
        try {
            MapFactory factory = MapFactory.getFactory(Config.FORMAT);
            try {
                mapDataPath = Config.PATH + Config.MAP;
                map = factory.createMap(mapDataPath);
            } catch (Exception exception) {
                mapDataPath = Config.MAP;
                map = factory.createMap(mapDataPath);
            }
            cbLabels.setState(getLabelsState());
            long t2 = System.currentTimeMillis();
            mapCanvas = new MapCanvas(map);
            long t3 = System.currentTimeMillis();
            mapControl = new MapControl(map);
            long t4 = System.currentTimeMillis();
            legendCanvas = new LegendCanvas(map);
            long t5 = System.currentTimeMillis();
            p2.add("South", new ObjectView(map));
//            p2.add("Center", new ObjectView(map));
            p2.add("North", p3);
            p2.add("Center", mapCanvas);
            //tp1.addComponent(Config.getUIProperty("main.window.layers"), mapControl);
            //tp1.addComponent(Config.getUIProperty("main.window.zoom"), p1);

 //my beg
            sp3 = new SplitPanel(false);//my
            pr2 = new AttrPanel(map,mapControl);
            sp3.addComponent(mapControl, 5F);
            sp3.addComponent(pr2, 3F);
            tp1.addTab(Config.getUIProperty("main.window.layers"), sp3);
    //my end
         //   tp1.addTab(Config.getUIProperty("main.window.layers"), mapControl);// my comment so was before
            tp1.addTab(Config.getUIProperty("main.window.zoom"), p1);
            sp.setBackground(Color.white);
            sp.add(legendCanvas);
            //tp1.addComponent(Config.getUIProperty("main.window.legend"), sp);
            tp1.addTab(Config.getUIProperty("main.window.legend"), sp);

            /*tp2.addComponent(Config.getUIProperty("main.window.explore"), new SearchPanel(map, mapCanvas, mapControl));
        tp2.addComponent(Config.getUIProperty("main.window.compare"), chartPanel);
        tp2.addComponent(Config.getUIProperty("main.window.object"), new ObjectPanel(map, mapControl));
        tp2.addComponent(Config.getUIProperty("main.window.compare-with-value"), new DistancePanel(map, mapControl));
        tp2.addComponent(Config.getUIProperty("main.window.grouping"), new ClassifyPanel(map, mapControl));
        tp2.addComponent(Config.getUIProperty("main.window.similarity"), new SimilarityPanel(map, mapControl));
        tp2.addComponent(Config.getUIProperty("main.window.membership"), new MembershipPanel(map, mapControl));
        tp2.addComponent(Config.getUIProperty("main.window.calculator"), new CalcPanel(map, this, mapControl));*/
            searchPanel = new SearchPanel(map, mapCanvas, mapControl);
            long t6 = System.currentTimeMillis();
            chartPanel = new ChartPanel(map);
            objectPanel = new ObjectPanel(map, mapControl);
            long t7 = System.currentTimeMillis();
            distancePanel = new DistancePanel(map, mapControl);
            classifyPanel = new ClassifyPanel(map, mapControl);
            long t8 = System.currentTimeMillis();
            similarityPanel = new SimilarityPanel(map, mapControl);
            membershipPanel = new MembershipPanel(map, mapControl);
            long t9 = System.currentTimeMillis();
            calcPanel = new CalcPanel(map, this, mapControl);
            reportsPanel = new ReportsPanel(map);
            long t10 = System.currentTimeMillis();
//        System.out.println(""+(t2-t1));
//        System.out.println(""+(t3-t2));
//        System.out.println(""+(t4-t3));
//        System.out.println(""+(t5-t4));
//        System.out.println(""+(t6-t5));
//        System.out.println(""+(t7-t6));
//        System.out.println(""+(t8-t7));
//        System.out.println(""+(t9-t8));
//        System.out.println(""+(t10-t9));

            tp2.addTab(Config.getUIProperty("main.window.explore"), searchPanel);
            tp2.addTab(Config.getUIProperty("main.window.compare"), chartPanel);
            tp2.addTab(Config.getUIProperty("main.window.object"), objectPanel);
            tp2.addTab(Config.getUIProperty("main.window.compare-with-value"), distancePanel);
            tp2.addTab(Config.getUIProperty("main.window.grouping"), classifyPanel);
            tp2.addTab(Config.getUIProperty("main.window.similarity"), similarityPanel);
            tp2.addTab(Config.getUIProperty("main.window.membership"), membershipPanel);
            tp2.addTab(Config.getUIProperty("main.window.calculator"), calcPanel);
//            tp2.addTab(Config.getUIProperty("main.window.reports"), reportsPanel);
            sp2.addComponent(p2, 5F);
            sp2.addComponent(tp2, 3F);
            sp1.addComponent(sp2, 4F);
            sp1.addComponent(tp1, 1.0F);
            add("Center", sp1);
            map.addMapListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Fatal error: " + e);
        }
    }

    private boolean getLabelsState() {
        for (MapLayer ml : map.getLayers())
            if (ml.getStyle().drawLabels)
                return true;
        return false;
    }

    private void setLabelsState(boolean state) {
        for (MapLayer ml : map.getLayers()) {
            ml.getStyle().drawLabels = state;
        }
        map.fireMapChanged();
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnZoomIn)
            mapCanvas.zoomIn();
        else if (src == btnZoomOut)
            mapCanvas.zoomOut();
        else if (src == btnFullExtent)
            mapCanvas.panToWindow();
        else if (src == btnExport)
            projectSave(Config.getCompass());
    }

    private void projectSave(final Compass compass) {
        //int value = 1;
        //hd.out("actionPerf_SAVE");
        //Frame frame = new Frame();
        FileChooser fc = new FileChooser();
        //JFileChooser fc = new JFileChooser();
        //frame.add(fc);
        //frame.show();
        //hd.out("actionPerf_SAVE 2");
        int value = OptionPane.showDialog(compass, fc,
                Config.getUIProperty("main.dialog-title.select-file"), 3);
        if (value == 1) {
            //final String fname = "D:"+File.separator+"STAS"+File.separator+"compass"+File.separator+"compass.xml";
            final String fname = fc.getSelectedFile();
            //final String fname = fc.getSelectedFile().getName();
            //hd.out("actionPerf_SAVE 3");
            final Dialog d = (new OptionPane("Saving map, please wait", 4)).createDialog(null, "title");
            //hd.out("actionPerf_SAVE 4");
            if (fname != null) {
                Thread t = new Thread() {
                    public void run() {
                        //hd.out("actionPerf_SAVE 5");
                        XMLWriter.saveMap(fname, compass.getMap());
                        //hd.out("actionPerf_SAVE 6");
                        d.dispose();
                    }
                };
                t.start();
                d.show();
            }
        }
    }

    public Image getChartImage() {
        return chartPanel.getImage();
    }

    public Image getLegendImage() {
        return legendCanvas.getImage();
    }

    public ColorRenderer getColorRenderer() {
        return legendCanvas.getColorRenderer();
    }

    public Hashtable setLegendHT(Hashtable ht) {
        return legendCanvas.ht = ht;
    }

    public Map getMap() {
        return map;
    }

    public MapControl getMapControl() {
        return mapControl;
    }

    public MapCanvas getMapCanvas() {
        return mapCanvas;
    }

    public Image getMapImage() {
        return mapCanvas.getImage();
    }

    public JTabbedPane getModePanel() {
        return tp2;
    }

    public void itemStateChanged(ItemEvent e) {
        Checkbox cbx = cgMouseMode.getSelectedCheckbox();
        if (cbx == cbZoomMap)
            mapCanvas.setMode(0);
        else if (cbx == cbPanMap)
            mapCanvas.setMode(1);
    }

    public void layerSelected(MapEvent mapevent) {
    }

    public void mapChanged(MapEvent e) {
        sp.doLayout();
    }

    public void setButtonPane(Component c) {
        p3.add("East", c);
    }


    public SearchPanel getSearchPanel() {
        return searchPanel;
    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }

    public ObjectPanel getObjectPanel() {
        return objectPanel;
    }

    public DistancePanel getDistancePanel() {
        return distancePanel;
    }

    public ClassifyPanel getClassifyPanel() {
        return classifyPanel;
    }

    public SimilarityPanel getSimilarityPanel() {
        return similarityPanel;
    }

    public MembershipPanel getMembershipPanel() {
        return membershipPanel;
    }

    public CalcPanel getCalcPanel() {
        return calcPanel;
    }

    public ReportsPanel getReportsPanel() {
        return reportsPanel;
    }

    public ExportPanel getExportPanel() {
        return exportPanel;
    }

    private Map map;
    private MapCanvas mapCanvas;
    private MapControl mapControl;
    private LegendCanvas legendCanvas;
    private Button btnShowRows;// my add
    private Button btnExport;
    private Button btnZoomIn;
    private Button btnZoomOut;
    private Button btnFullExtent;
    private CheckboxGroup cgMouseMode;
    private Checkbox cbZoomMap;
    private Checkbox cbPanMap;
    private Checkbox cbLabels;
    private ScrollPane sp;
    //private TabPanel tp1;
    private JTabbedPane tp1;
    //private TabPanel tp2;
    private JTabbedPane tp2;
    SplitPanel sp1;
    Panel p1;
    Panel pZoom;
    SplitPanel sp2;
    Panel p2;
    Panel p3;

    public String mapDataPath;

    private SearchPanel searchPanel;
    private ChartPanel chartPanel;
    private ObjectPanel objectPanel;
    private DistancePanel distancePanel;
    private ClassifyPanel classifyPanel;
    private SimilarityPanel similarityPanel;
    private MembershipPanel membershipPanel;
    private CalcPanel calcPanel;
    private ReportsPanel reportsPanel;
    private ExportPanel exportPanel;

    SplitPanel sp3 ;//my begin

    Panel pr1 ;
    ScrollPane pr2 ;// my end


}
