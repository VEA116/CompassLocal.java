// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   SearchPanel.java

package ru.iitp.gis.common.control;

import org.zaval.lw.event.LwKeyEvent;
import org.zaval.lw.event.LwSelectionEvent;
import org.zaval.lw.event.LwSelectionListener;
import ru.iitp.gis.common.Config;
import ru.iitp.gis.common.event.*;
import ru.iitp.gis.common.gui.ScrollPanel;
import ru.iitp.gis.common.model.*;
import ru.iitp.gis.common.util.QuickSort;
import ru.iitp.gis.common.util.StringComparator;
import ru.iitp.gis.common.view.LayerControl;
import ru.iitp.gis.common.view.MapCanvas;
import ru.iitp.gis.common.view.MapContext;
import ru.iitp.gis.common.view.MapControl;
import ru.iitp.gis.common.view.selector.ExploreSelector;
import ru.iitp.gis.common.view.table.AttrColumn;
import ru.iitp.gis.common.view.table.AttrTableModel;
import ru.iitp.gis.common.view.table.Header;
import ru.iitp.gis.common.view.table.Table;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.Vector;

public class SearchPanel extends Panel
        implements ActionListener, ItemListener, DataListener, MapListener, KeyListener, AttrDeleted {

    private boolean inSelection = false;

    public SearchPanel(final Map map, MapCanvas mapCanvas, MapControl mapControl) {
        jt = new Table(map.getActiveLayer().getDataSupplier(), this);
        ch = new Choice();
        tf = new TextField("", 10);
        tf.addKeyListener(this);
        btn = new Button(Config.getUIProperty("explore.button.find"));
        btn.setActionCommand("search_attribute");
        index = new Hashtable();
        this.map = map;
        this.mapCanvas = mapCanvas;
        this.mapControl = mapControl;
        map.addMapListener(this);
        setBackground(SystemColor.control);
        setLayout(new BorderLayout());

        cbpan = new Panel(new FlowLayout());
        attrBtn = new Button(Config.getUIProperty("explore.button.add"));
        editBtn = new Button(Config.getUIProperty("explore.button.allow_edit"));
        cbg = new CheckboxGroup();
        cb1 = new Checkbox(Config.getUIProperty("explore.checkbox.find"), false, cbg);
        cb2 = new Checkbox(Config.getUIProperty("explore.checkbox.table"), true, cbg);
        cb1.addItemListener(this);
        cb2.addItemListener(this);
        attrBtn.addActionListener(this);
        attrBtn.setActionCommand("create_attribute");
        editBtn.addActionListener(this);
        editBtn.setActionCommand("table_editable");
        cbpan.add(cb1);
        cbpan.add(cb2);
        if (cb2.getState()) {
            cbpan.add(attrBtn);
            cbpan.add(editBtn);
        }
        add("North", cbpan);


        Panel p = new Panel();
        GridBagLayout gb = new GridBagLayout();
        p.setLayout(gb);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = 1;
        c.insets = new Insets(1, 1, 1, 1);
        c.weightx = 4D;
        gb.setConstraints(ch, c);
        p.add(ch);
        c.weightx = 4D;
        gb.setConstraints(tf, c);
        p.add(tf);
        c.gridwidth = 0;
        c.weightx = 2D;
        gb.setConstraints(btn, c);
        p.add(btn);

        sp = new ScrollPanel(map);
        sp.setBackground(Color.white);

        panel = new Panel(new BorderLayout());
        panel.add("North", p);
        panel.add("Center", sp);
        add("Center", panel);


        createTable();


        enableEvents(1L);
        ch.addItemListener(this);
        btn.addActionListener(this);
        map.getDataSample().addDataListener(this);
        selectLayer();
        sp.doLayout();
        sp.invalidate();

        //layerControl = this.mapControl.getLayerControl();
        createAttrListener();
        this.mapControl.getLayerControl().getAttrTree().setAttrDelListener(this);
        //createDeleteListener();
    }

    /*private void createDeleteListener(){
        LayerControl lc = this.mapControl.getLayerControl();
        final LwTree lwt = (LwTree)lc.getAttrTree();
        final LwKeyListener kl = (LwKeyListener)lc;
        if (kl != null) addKeyListener(new KeyListener(){

            public void keyTyped(KeyEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void keyPressed(KeyEvent e) {
                kl.keyPressed(new LwKeyEvent(lwt, e.getID(), e.getKeyCode(), e.getKeyChar(), 0));
            }

            public void keyReleased(KeyEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

    }*/

    private void createAttrListener() {
        this.mapControl.getLayerControl().getAttrTree().addSelectionListener(new LwSelectionListener() {

            public void selected(LwSelectionEvent e) {
                Attribute attr = map.getActiveLayer().getDataSupplier().getSelectedAttr();
                if (attr == null)
                    return;
                String id = attr.getId();
                sp.selectAttr(id);
                int pos;
                if (((AttrTableModel) jt.getModel()).transposed) {
                    pos = jt.getHPositionID(id);
                    sb.setValue(pos);
                } else {
                    pos = jt.getVPositionID(id);
                    sb2.setValue(pos);
                }
            }

            public void deselected(LwSelectionEvent e) {
            }

        });
    }

    private void createTable() {
        /*table*/
        ppp = new Panel(new BorderLayout());
        pp = new Panel(new BorderLayout());

        //jsp2 = new ScrollPane();
        jsp1 = new JScrollPane();
        jsp3 = new JScrollPane();
        jsp1.getViewport().add(jt.getTableHeader());
        jsp1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jsp3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jsp3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        final JScrollBar jsb = jsp1.getHorizontalScrollBar();
        final JScrollBar jsb3 = jsp3.getVerticalScrollBar();
        jsb.setBlockIncrement(sb.getBlockIncrement());
        jsb.setMaximum(sb.getMaximum());
        jsb.setMinimum(sb.getMinimum());
        sb.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                jsb.setValue(sb.getValue());
            }

        });
        jsb3.setBlockIncrement(sb2.getBlockIncrement());
        jsb3.setMaximum(sb2.getMaximum());
        jsb3.setMinimum(sb2.getMinimum());
        sb2.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                jsb3.setValue(sb2.getValue());
            }

        });
        pp.add("North", jsp1);
        jt.setSize(jt.tablewidth, jt.getRowHeight() * jt.getRowCount());
        jt.setMinimumSize(jt.getSize());
        jt.setPreferredScrollableViewportSize(jt.getSize());
        jt.setPreferredSize(jt.getSize());
        jsp2.add(jt);
        pp.add("Center", jsp2);

        createColumnHeader();
        /*ac.addMouseListener(new MouseAdapter(){
            public void mouseExited(MouseEvent e){
                jpm.setVisible(false);
            }
        });*/
        pppp = new Panel(new BorderLayout());
        header = new Header(jt.getRowHeight(), ac.getW(), jt.getTableHeader().getBackground());
        header.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
/*                jt.transpose();
                jt.setSize(jt.tablewidth, jt.getRowHeight() * jt.getRowCount());
                jt.setMinimumSize(jt.getSize());
                jt.setPreferredScrollableViewportSize(jt.getSize());
                jt.setPreferredSize(jt.getSize());
                createColumnHeader();
                header.setWidth(ac.getW());
                header.transpose();
                pppp.setBounds(pppp.getBounds().x, pppp.getBounds().y, ac.getW(), pppp.getBounds().height);
                SearchPanel.this.validate();
                createAttrListener();       */
                /*cb1.setState(true);
                if (SearchPanel.this.getComponent(1) != panel){
                    panel.setBounds(SearchPanel.this.getComponent(1).getBounds());
                    SearchPanel.this.remove(SearchPanel.this.getComponent(1));
                    SearchPanel.this.add("Center",panel);
                    panel.validate();
                }*/
                ((AttrTableModel) jt.getModel()).sort();
            }
        });
        createRowHeaderListener();
        pppp.add("North", header);
        pppp.add("Center", jsp3);
        ppp.add("West", pppp);
        ppp.add("Center", pp);
        //add("Center",ppp);
        /*table*/

    }

    private void createColumnHeader() {

//////////////////////////////

        ac = new AttrColumn((AttrTableModel) jt.getModel(), jt.getRowHeight(), jt.getTableHeader().getBackground());
        jsp3.getViewport().add(ac);
        this.validate();
        //final JPopupMenu jpm = new JPopupMenu();
        /*ac.addMouseMotionListener(new MouseMotionAdapter(){
            int pos;
            JPopupMenu jpm = new JPopupMenu();
            {
                jpm.addMouseListener(new MouseAdapter(){
                    public void mouseExited(MouseEvent e) {
                        jpm.setVisible(false);
                    }

                });
            }
            public void mouseMoved(MouseEvent e) {
                int y = e.getPoint().y;
                int rowHeight = jt.getRowHeight();
                int newpos = y/rowHeight;
                if (pos == newpos){pos = newpos; return;}
                else pos = newpos;
                if (pos >= ac.getRowNames().length) return;
                String str = ac.getRowNames()[pos];
                if (ac.getSize().width >= ac.getFontMetrics(ac.getFont()).stringWidth(str)){jpm.setVisible(false); return;}
                jpm = new JPopupMenu(ac.getRowNames()[pos]);
                //jpm.setLocation(0,rowHeight*pos);
                //jpm.show();
                //jpm.setVisible(true);
                jpm.removeAll();
                jpm.add(str);
                jpm.show(ac,0,rowHeight*pos);
            }
        });*/
        ac.addMouseListener(new MouseAdapter() {
            int pos;
            JPopupMenu jpm = new JPopupMenu();

            public void mousePressed(MouseEvent e) {
                int y = e.getPoint().y;
                int rowHeight = jt.getRowHeight();
                int newpos = y / rowHeight;
                if (pos == newpos) {
                    pos = newpos;
                    jpm.setVisible(true);
                    return;
                } else pos = newpos;
                if (pos >= ac.getRowNames().length) return;
                String str = ac.getRowNames()[pos];
                //======================

                Vector<String> v = map.getDataSample().getHighlightedObjects();
                v.add(str);
                //map.getDataSample().setHighlightedObjects(v);
                setHighlightedObjects(v);
          //          Graphics g = getGraphics();
           //         Rectangle r = new Rectangle(getSize());
            //        MapContext mc = new MapContext(g, map.getBounds(), r, bind, scaleFactor * zoomFactor);
             //       String id = v.lastElement();
                   // GeoObject activeObject = map.getActiveLayer().getObject(str);
                   // mapCanvas.onMouseClick(e);//objectHighlighted(e);activeObject.
                    //repaint();
               //==================
                if (ac.getSize().width >= ac.getFontMetrics(ac.getFont()).stringWidth(str)) {
                    jpm.setVisible(false);
                    return;
                }
                jpm = new JPopupMenu(ac.getRowNames()[pos]);
                //jpm.setLocation(0,rowHeight*pos);
                //jpm.show();
                //jpm.setVisible(true);
                jpm.removeAll();
                jpm.add(str);
                jpm.show(ac, 0, rowHeight * pos);
            }

            public void mouseReleased(MouseEvent e) {
                jpm.setVisible(false);
            }
        });
    }

    private static String getNameForAttr(Attribute a, Attribute parent) {
        if (a == null)
            return "";
        String name = a.getName();
        while (a.getParent() != null && a.getParent().getParent() != null && a.getParent() != parent) {
            a = a.getParent();
            name = a.getName() + ". " + name;
        }
        return name;
    }

    private void createRowHeaderListener() {
        jt.getTableHeader().addMouseMotionListener(new MouseMotionAdapter(){
            int pos;
            JPopupMenu jpm = new JPopupMenu();

            public void mouseMoved(MouseEvent e) {
                TableColumnModel cm = jt.getTableHeader().getColumnModel();
                int count = cm.getColumnCount();
                int x = e.getPoint().x;
                int newpos = 0, sumwidth = 0;
                for (int i = 0; i < count; i++){
                    if (sumwidth <= x && x < sumwidth + cm.getColumn(i).getWidth()) break;
                    sumwidth += cm.getColumn(i).getWidth();
                    newpos++;
                }

                if (pos == newpos){pos = newpos; return;}
                else pos = newpos;
                if (pos >= count) return;
                Attribute a = ((AttrTableModel)jt.getModel()).getAttr(pos);
                String str = getNameForAttr(a, null);
                
//                String str = ((AttrTableModel)jt.getModel()).getColumnNames()[pos];
                if (cm.getColumn(pos).getWidth() >= jt.getTableHeader().getFontMetrics(jt.getTableHeader().getFont()).stringWidth(str)){jpm.setVisible(false); return;}
                jpm = new JPopupMenu(str);
                jpm.addMouseListener(new MouseAdapter() {
                    public void mouseExited(MouseEvent e) {
                        jpm.setVisible(false);
                    }
                });
                jpm.removeAll();
                jpm.add(str);
                jpm.show(jt.getTableHeader(),sumwidth,-6);
            }
        });
//        jt.setAutoCreateRowSorter(true);
        jt.getTableHeader().addMouseListener(new MouseAdapter() {
            int pos;
            JPopupMenu jpm = new JPopupMenu();

            public void mousePressed(MouseEvent e) {
                TableColumnModel cm = jt.getTableHeader().getColumnModel();
                int count = cm.getColumnCount();
                int x = e.getPoint().x;
                int newpos = 0, sumwidth = 0;
                for (int i = 0; i < count; i++) {
                    if (sumwidth <= x && x < sumwidth + cm.getColumn(i).getWidth()) break;
                    sumwidth += cm.getColumn(i).getWidth();
                    newpos++;
                }
                pos = newpos;
                if (pos >= count) return;
                ((AttrTableModel) jt.getModel()).sort(pos);
                if (pos == newpos) {
                    jpm.setVisible(true);
                    return;
                }
                String str = ((AttrTableModel) jt.getModel()).getColumnNames()[pos];
                if (cm.getColumn(pos).getWidth() >= jt.getTableHeader().getFontMetrics(jt.getTableHeader().getFont()).stringWidth(str)) {
                    jpm.setVisible(false);
                    return;
                }
                jpm = new JPopupMenu(str);
                jpm.removeAll();
                jpm.add(str);
                jpm.show(jt.getTableHeader(), sumwidth, -6);
            }

            public void mouseReleased(MouseEvent e) {
                jpm.setVisible(false);
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("search_attribute")) {
            String src = tf.getText().trim().toLowerCase();
            String req;
            //DataSupplier ds = map.getActiveLayer().getDataSupplier();
            int objCount = map.getActiveLayer().getObjectCount();
            if (src.equals("")) {
                mapCanvas.releaseSelection();
            } else {
                for (int i = 0; i < objCount; i++) {
                    req = map.getActiveLayer().getObject(i).getId();
                    if ((req.toLowerCase()).startsWith(src)) {
                        tf.setText(req);
                        map.getDataSample().highlightObject(req);
                        mapCanvas.fixSelection();
                        return;
                    }
                }
                mapCanvas.releaseSelection();
                tf.setText("");
            }
        }
        if (e.getActionCommand().equals("create_attribute")) {
            //if (jt.getAttrTableModel().transposed)
            //    JOptionPane.showMessageDialog(this,"Transpose table and try again!");
            DataSupplier ds = map.getActiveLayer().getDataSupplier();
            Vector data = new Vector(ds.getRecordCount());
            String attrName = JOptionPane.showInputDialog(this, Config.getUIProperty("main.dialog-title.enter-attribute-name"), "create attribute", JOptionPane.PLAIN_MESSAGE);
            if (attrName == null) return;
            Attribute newAttr = new Attribute("ATTR" + ds.getAttrCount(), attrName, 8);
            Attribute root = ds.getSelectedAttr().getParent();
            if (ds instanceof DataTable) ((DataTable) ds).addAttribute(newAttr, data, root);

            ds.setSelectedAttr(newAttr.getId());
            LayerControl lc = mapControl.getLayerControl();
            if (lc != null) lc.addAttribute(newAttr);
            map.fireMapChanged();
            jt = new Table(ds, this);
            resetTable();

            cb2.setState(true);
            if (this.getComponent(1) != ppp) {
                ppp.setBounds(this.getComponent(1).getBounds());
                this.remove(this.getComponent(1));
                this.add("Center", ppp);
                ppp.validate();
            }

            AttrTableModel model = jt.getAttrTableModel();
            int row = model.getRowNumberFromName(attrName);
            int column = 0;
            model.setRowEditable(row);
            model.setCellEditable(column);
            /*int value = 10;

            jt.getColumnModel().getColumn(column).setCellEditor(new DefaultCellEditor(new JTextField("jjhfhf")));

            jt.setEditingRow(row);
            jt.setEditingColumn(column);
            jt.editCellAt(row,column);
            TableCellEditor editor = jt.getCellEditor(row,column);
            JTextField c = (JTextField)editor.getTableCellEditorComponent(jt,"dfhdh",true,row,column);
            c.setEditable(true);
            c.setText("test");



            boolean editable;
            editable = jt.isCellEditable(row,column);
            jt.setValueAt(new Integer(value),row,column);
            System.out.print(c.getText());*/

            //if (editable) return;
            //System.load("C:\\Program Files\\Microsoft Office\\OFFICE11\\winword.exe");
        }
        if (e.getActionCommand().equals("table_editable")) {
            AttrTableModel model = jt.getAttrTableModel();
            if (model.isAllCellsEditable()) {
                model.setAllCellsEditable(false);
                editBtn.setLabel(Config.getUIProperty("explore.button.allow_edit"));
            } else {
                model.setAllCellsEditable(true);
                editBtn.setLabel(Config.getUIProperty("explore.button.deny_edit"));
            }
        }
        /*int x,y,h,w;
        x = 0;
        y = this.getComponent(1).getHeight();
        h = jsp2.getVerticalScrollBar().getWidth();
        y -= h;
        w = this.getComponent(1).getWidth();
        jsp2.getHorizontalScrollBar().setBounds(x,y,h,w);*/
        //jt.setPreferredScrollableViewportSize(new Dimension(700,200));
        //jt.setSize(jt.tablewidth,jt.getSize().height);
        //jt.setLocation(-50,0);

        /*String id = (String) index.get(tf.getText().trim());
      if (id != null)
        map.getDataSample().highlightObject(id);*/
        //jt.setBounds(0,0,jt.getMinimumSize().width,jt.getMinimumSize().height);
    }

    public void attrSelected(DataEvent dataevent) {
    }

    public void dataChanged(DataEvent dataevent) {
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == ch) {
            String id = (String) index.get(ch.getSelectedItem());
            DataSample dsmp = map.getDataSample();
            dsmp.clearHighlightedList();
            if (id != null) {
                dsmp.highlightObject(id);
                mapCanvas.fixSelection();
            } else mapCanvas.releaseSelection();
        }
        if (e.getSource() == cb1) {
            if (cbpan.getComponentCount() == 4) {
                cbpan.remove(editBtn);
                cbpan.remove(attrBtn);
//                cbpan.validate();
                sp.doLayout();
                panel.validate();

            }
            if (this.getComponent(1) == ppp) {
                this.remove(ppp);
                panel.setBounds(ppp.getBounds());
                this.add("Center", panel);
                panel.validate();
            }
        }
        if (e.getSource() == cb2) {
            if (cbpan.getComponentCount() == 2) {
                cbpan.add(attrBtn);
                cbpan.add(editBtn);
                jt.getAttrTableModel().setAllCellsEditable(false);
                editBtn.setLabel(Config.getUIProperty("explore.button.allow_edit"));
                cbpan.validate();
            }
            if (this.getComponent(1) == panel) {
                //this.remove(cbpan);
                this.remove(panel);
                //this.add("North",cbpan);
                ppp.setBounds(panel.getBounds());
                this.add("Center", ppp);
                ppp.validate();
            }
        }

    }

    public void layerSelected(MapEvent e) {
        selectLayer();
        sp.doLayout();
        sp.invalidate();
    }

    public void mapChanged(MapEvent mapevent) {
    }

    public void objectHighlighted(DataEvent e) {
        if(inSelection)
            return;
        Vector v = map.getDataSample().getHighlightedObjects();
        if (v == null || v.size() == 0) {
            if (ch.getSelectedIndex() > 0)
                ch.select(0);
            return;
        }
        String id = (String) v.elementAt(v.size() - 1);
        DataSupplier ds = map.getActiveLayer().getDataSupplier();
        if (ds == null)
            return;

        String str = ds.getRecord(id).getName();
        ch.select(str);
        sp.selectObject(id);
        int pos;
        if (((AttrTableModel) jt.getModel()).transposed) {
            pos = jt.getVPositionID(id);
            sb2.setValue(pos);
        } else {
            pos = jt.getHPositionID(id);
            sb.setValue(pos);
        }
    }

    public void objectSelected(DataEvent dataevent) {
    }

    @Override
    protected void processComponentEvent(ComponentEvent e) {
        switch (e.getID()) {
            case 102 -> { // 'f'
                resetTable();
                map.setMode(Map.EXPLORE);
                MapLayer layer = map.getActiveLayer();
                DataSupplier ds = layer.getDataSupplier();
                if (ds == null) {
                    hide();
                    Config.getCompass().getMapCanvas().setSelector(null);
                    return;
                }
                show();
                layer.setDataSupplier(new DataTable(ds));
                Config.getCompass().getMapCanvas().setSelector(new ExploreSelector());
                Config.getCompass().getMapCanvas().reset();
                map.fireMapChanged();
            }
            case 103 -> // 'g'
                    mapCanvas.releaseSelection();
        }
    }

    private void selectLayer() {
        MapLayer layer = map.getActiveLayer();

        resetTable();
        ch.setVisible(false);
        ch.setEnabled(false);
        tf.setEnabled(false);
        btn.setEnabled(false);
        ch.removeAll();
        tf.setText("");
        DataSupplier ds = layer.getDataSupplier();
        if (ds == null) {
            ch.setVisible(true);
            return;
        }
        String s[] = new String[ds.getRecordCount()];
        index.clear();
        for (int i = 0; i < ds.getRecordCount(); i++) {
            DataRecord r = ds.getRecord(i);
            s[i] = r.getName();
            index.put(s[i], r.getId());
        }

        QuickSort.sort(new StringComparator(), s);
        ch.addItem("");
        for (int i = 0; i < s.length; i++)
            ch.addItem(s[i]);

        ch.setVisible(true);
        ch.setEnabled(true);
        tf.setEnabled(true);
        btn.setEnabled(true);
    }

    private void resetTable() {
        jt = new Table(map.getActiveLayer().getDataSupplier(), this);
        createTable();
        createAttrListener();
        cb2.setState(true);
        if (this.getComponent(1) != ppp) {
            ppp.setBounds(this.getComponent(1).getBounds());
            this.remove(this.getComponent(1));
            this.add("Center", ppp);
            ppp.validate();
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        if (KeyEvent.VK_ENTER != e.getKeyCode()) {
            if (KeyEvent.VK_CANCEL <= e.getKeyCode() && e.getKeyCode() < KeyEvent.VK_COMMA) return;
            if (KeyEvent.VK_BACK_SPACE == e.getKeyCode() && e.getKeyCode() == KeyEvent.VK_TAB) return;
        }
        String src = tf.getText().trim().toLowerCase();
        String req;
        int objCount = map.getActiveLayer().getObjectCount();

        for (int i = 0; i < objCount; i++) {
            req = map.getActiveLayer().getObject(i).getId();
            if (!src.equals("") && (req.toLowerCase()).startsWith(src)) {
                tf.setText(req);
                tf.setCaretPosition(src.length());
                tf.setSelectionEnd(req.length());
                tf.setSelectionStart(src.length());
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    map.getDataSample().highlightObject(req);
                    mapCanvas.fixSelection();
                }
                return;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            mapCanvas.releaseSelection();
            tf.setText("");
        }
    }

    private Map map;
    private MapCanvas mapCanvas;
    private MapControl mapControl;
    private LayerControl layerControl;
    public Choice ch;
    private TextField tf;
    private Button btn;
    private Hashtable index;
    private ScrollPanel sp;

    private ScrollPane jsp2 = new ScrollPane();
    private JScrollPane jsp1, jsp3;
    private Table jt;
    private Header header;
    private AttrColumn ac;

    private Checkbox cb1, cb2;
    private CheckboxGroup cbg;

    private Panel pp, ppp, pppp, panel, cbpan;

    private Button attrBtn;
    private Button editBtn;

    private final Adjustable sb = jsp2.getHAdjustable();
    private final Adjustable sb2 = jsp2.getVAdjustable();

    public void deleted(LwKeyEvent e) {
        resetTable();
    }

    public void resetColumns() {
        ac.paint(ac.getGraphics());
    }

    public void setHighlightedObjects(Vector<String> selectedIds) {
        inSelection = true;
        DataSample dsmp = map.getDataSample();
        dsmp.clearHighlightedList();
        if (selectedIds != null && selectedIds.size()>0) {
            dsmp.setHighlightedObjects(selectedIds);
        } else
            mapCanvas.releaseSelection();
        inSelection = false;
    }
}
