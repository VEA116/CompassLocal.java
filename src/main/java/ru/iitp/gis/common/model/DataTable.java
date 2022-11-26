package ru.iitp.gis.common.model;

import ru.iitp.gis.common.event.DataEvent;
import ru.iitp.gis.common.event.DataListener;
import ru.iitp.gis.common.factory.DataSource;
import ru.iitp.gis.common.util.StringComparator;

import java.awt.*;
import java.awt.List;
import java.util.*;


public class DataTable implements DataSupplier, Cloneable {
    protected Attribute root;
    protected java.util.Vector attrs;
    protected java.util.Vector records;
    protected java.util.Hashtable rindex;
    protected java.util.Hashtable aindex;
    protected java.util.Hashtable subattrs;
    protected java.util.Hashtable subattrList;
    protected int selectedAttrIndex;
    private int selectedAttrIndexes[];
    protected DataSample sample;
    protected Statistics stat;
    protected java.util.Vector listeners;
    protected DataSource dataSource;
    protected DataSource rnSource;

    public HashMap names = new HashMap();

    public DataTable() {
        root = new Attribute("R00T", "R00T", Attribute.STRING);
        attrs = new java.util.Vector();
        records = new java.util.Vector();
        rindex = new java.util.Hashtable();
        aindex = new java.util.Hashtable();
        subattrs = new java.util.Hashtable();
        subattrList = new java.util.Hashtable();
        selectedAttrIndex = 0;
        listeners = new java.util.Vector();
    }

    public DataTable(DataSupplier ds) {
        attrs = new java.util.Vector();
        records = new java.util.Vector();
        rindex = new java.util.Hashtable();
        aindex = new java.util.Hashtable();
        subattrs = new java.util.Hashtable();
        subattrList = new java.util.Hashtable();
        selectedAttrIndex = 0;
        listeners = new java.util.Vector();
        DataTable dtab = (DataTable) ds;
        root = dtab.root;
        attrs = dtab.attrs;
        records = dtab.records;
        rindex = dtab.rindex;
        aindex = dtab.aindex;
        subattrs = dtab.subattrs;
        subattrList = dtab.subattrList;
        selectedAttrIndex = dtab.selectedAttrIndex;
        sample = dtab.sample;
        listeners = dtab.listeners;
        stat = new Statistics(this);
    }

    public Attribute getRoot() {
        return root;
    }

    public void setRoot(Attribute root) {
        this.root = root;
    }

    public void addAttribute(Attribute a) {
        addAttribute(a, new java.util.Vector());
    }

    public void addAttribute(Attribute a, java.util.Vector data) {
        addAttribute(a, data, null);
    }

    public void addAttribute(Attribute a, java.util.Vector data, Attribute root) {
        attrs.addElement(a);
        aindex.put(a.getId(), a);
        if (records.size() == 0) return;
        for (; data.size() < records.size(); data.addElement(null)) ;
        for (int i = 0; i < records.size(); i++)
            getRecord(i).addData(data.elementAt(i));
        if (root != null) root.addAttribute(a);
    }

/*    public String[] getSubatr(String id) {
        String [] sss = null;
        if(subattrs.containsKey(id))
            sss = (String[]) subattrs.get(id);
        return (sss) ;
    }*/
public Hashtable getSubatr(String id) {
    Hashtable sss = null;
    if(subattrs.containsKey(id))
        sss = (Hashtable) subattrs.get(id);
    return (sss) ;
}
    public String[] getSubatrList(String id) {
        String[] sss = null;
        if(subattrList.containsKey(id))
            sss = (String[]) subattrList.get(id);
        return (sss) ;
    }

    public void addSubatr(Attribute a, java.util.Vector data) {
        addAttribute(a, data, null);
    }
    public void addSubatrList(String a,String [] data) {
        subattrList.put(a,data);
    }
    public void addSubatr(String a,Hashtable data) {
        subattrs.put(a,data);
    }

    public void addDataListener(DataListener l) {
        if (listeners.contains(l)) {
            return;
        } else {
            listeners.addElement(l);
            return;
        }
    }



    public void addRecord(DataRecord record) {
        records.addElement(record);
        rindex.put(record.getId(), record);
    }

    public void copyCurrentAttribute(int type, String name) {
        String id = "ATTR" + getAttrCount();
        Attribute a = getSelectedAttr();
        Attribute root = a.getParent();
        Attribute attr = new Attribute(id, name, type);
        addAttribute(attr, new Vector(), root);
        for (int i = 0; i < getRecordCount(); i++)
            getRecord(i).setData(getData(i), id);
        setSelectedAttr(attr.getId());
    }

    public int drawLegend(Graphics g, int x, int y, int lineSpacing) {
        Attribute a = getSelectedAttr();
        if (a != null) g.drawString(a.getName(), x, y);
        return 1;
    }

    protected void fireAttrSelected() {
        DataEvent e = new DataEvent(this);
        for (int i = 0; i < listeners.size(); i++)
            ((DataListener) listeners.elementAt(i)).attrSelected(e);
    }

    public int getAttrCount() {
        return attrs.size();
    }

    public int getAttrIndex(String id) {
        return attrs.indexOf(getAttribute(id));
    }

    public Attribute getAttribute(int index) {
        return index != -1 ? (Attribute) attrs.elementAt(index) : null;
    }

    public Attribute getAttribute(String id) {
        return (Attribute) aindex.get(id);
    }

    public Object getData(int index) {
        return getRecord(index).getData(selectedAttrIndex);
    }

    public Object getData(String id) {
        DataRecord r = getRecord(id);
        return r == null ? null : r.getData(selectedAttrIndex);
    }

    public DataColumn getDataColumn() {
        return getDataColumn(selectedAttrIndex);
    }

    public DataColumn getDataColumn(int index) {
        if (index < 0 || index >= getAttrCount()) {
            return null;
        }
        return new DataColumn(this, index);
//    return new DataColumn(data, id, getAttribute(index).getType());
    }

    public DataSample getDataSample() {
        return sample;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
    public DataSource getRegNameSource() {
        return rnSource;
    }

    public DataRecord getRecord(int index) {
        return (DataRecord) records.elementAt(index);
    }

    public DataRecord getRecord(String id) {
        return (DataRecord) rindex.get(id);
    }

    public int getRecordCount() {
        return records.size();
    }

    public int getRecordIndex(String id) {
        return records.indexOf(getRecord(id));
    }

    public Attribute getSelectedAttr() {
        return getAttribute(selectedAttrIndex);
    }

    public int[] getSelectedAttrIndexes() {
        return selectedAttrIndexes;
    }

    public int[] getSelectedAttrIndexesIfOnly() {
        return new int[]{selectedAttrIndex};
    }

    public Statistics getStatistics() {
        if (stat == null)
            stat = new Statistics(this);
        return stat;
    }

    private String getNameForAttr(Attribute a, Attribute parent) {
        if (a == null)
            return "";
        String name = a.getName();
        while (a.getParent() != null && a.getParent().getParent() != null && a.getParent() != parent) {
            a = a.getParent();
            name = a.getName() + ". " + name;
        }
        return name;
    }

    public String getText() {
        Attribute a = getSelectedAttr();
        if(a==null)
            return "";
        return getNameForAttr(a, null) + ": ";
//        return a != null ? a.getName() + ": " : "";
    }

    public int getType() {
        if (selectedAttrIndex == -1) return Attribute.NULL;
        return getAttribute(selectedAttrIndex).getType();
    }

    public void removeAttribute(Attribute a) {
        int index = getAttrIndex(a.getId());
        for (int i = 0; i < records.size(); i++) {
            getRecord(i).setData(null, index);
            getRecord(i).data.removeElementAt(index);
        }
        attrs.removeElement(a);
        aindex.remove(a.getId());
    }

    public void removeDataListener(DataListener l) {
        listeners.removeElement(l);
    }

    public void removeRecord(DataRecord record) {
        records.removeElement(record);
        rindex.remove(record);
    }

    public void setDataSample(DataSample s) {
        sample = s;
    }

    public void setDataSource(DataSource src) {
        dataSource = src;
    }
    public void setRegNameSource(DataSource src) {
        rnSource = src;
    }

    public void setSelectedAttr(List attrList) {
        setSelectedAttr(attrList.getSelectedIndexes());
    }

    public void setSelectedAttr(int[] indexes) {
        selectedAttrIndexes = indexes;
        selectedAttrIndex = indexes.length != 0 ? indexes[0] : -1;
        if (selectedAttrIndex != -1) {
            stat = new Statistics(this);
        } else {
            stat = null;
        }
        fireAttrSelected();
    }

    public void setSelectedAttr(String id) {
        selectedAttrIndex = getAttrIndex(id);
        stat = new Statistics(this);
        fireAttrSelected();
    }

    public Object clone1() {
        DataTable dt = new DataTable();
        for (int i = 0; i < this.getAttrCount(); i++) {
            //dt.attrs.addElement(this.attrs.elementAt(i));
            dt.addAttribute((Attribute) this.getAttribute(i).clone(), new Vector(), (Attribute) this.getAttribute(i).getParent().clone());
        }
        /*for (int i = 0; i < this.getRecordCount();i++){
          dt.addRecord((DataRecord)this.getRecord(i).clone());
        }*/

        //dt.setDataSource((DataSource)this.getDataSource().clone());
        //dt.setDataSample((DataSample)this.getDataSample().clone());
        return dt;
    }

    public Object clone() {
        DataTable dt = new DataTable();
        for (int i = 0; i < this.getAttrCount(); i++) {
            //dt.attrs.addElement(this.attrs.elementAt(i));
            dt.addAttribute((Attribute) this.getAttribute(i).clone(), new Vector(), (Attribute) this.getAttribute(i).getParent().clone());
        }
        /*for (int i = 0; i < this.getRecordCount();i++){
          dt.addRecord((DataRecord)this.getRecord(i).clone());
        }*/

        dt.setDataSource((DataSource) this.getDataSource().clone());
        //dt.setDataSample((DataSample)this.getDataSample().clone());
        return dt;
    }

    private void reformatAttribute(Attribute oldroot, Attribute root, String addtionalID, DataTable dtab, ArrayList list) {
        int count = oldroot.getAttrCount();
        for(int i = 0; i< count; i++) {
            Attribute old_attr = oldroot.getAttribute(i);
            if(old_attr.isGrouped()) {
                ArrayList mappings = old_attr.getMappings();
                for(Iterator it = mappings.iterator(); it.hasNext();) {
                    Mapping mapping = (Mapping) it.next();
                    if(mapping.mapAll) {
                        ArrayList values = new ArrayList();
                        for(Iterator rec_it = records.iterator(); rec_it.hasNext(); ){
                            DataRecord dr = (DataRecord) rec_it.next();
                            Object value = dr.getData(old_attr.getId());
                            if(!values.contains(value))
                                values.add(value);
                        }
                        for(Iterator value_it = values.iterator(); value_it.hasNext();) {
                            Object value_o = value_it.next();
                            String value = value_o.toString();
                            String name = mapping.name.replaceFirst("\\?", value);
                            Attribute new_attr = new Attribute(value+addtionalID, name, Attribute.STRING);
                            root.addAttribute(new_attr);
                            ArrayList new_list = new ArrayList();
                            for(Iterator n_it = list.iterator();n_it.hasNext();) {
                                DataRecord dr = (DataRecord) n_it.next();
                                if(value.equals(dr.getData(old_attr.getId())))
                                    new_list.add(dr);
                            }
                            reformatAttribute(old_attr, new_attr, new_attr.getId(), dtab, new_list);
                        }
                    } else {
                        Attribute new_attr = new Attribute(mapping.id+addtionalID, mapping.name, Attribute.STRING);
                        root.addAttribute(new_attr);
                        ArrayList new_list = new ArrayList();
                        for(Iterator n_it = list.iterator();n_it.hasNext();) {
                            DataRecord dr = (DataRecord) n_it.next();
                            if(mapping.value.equals(dr.getData(old_attr.getId())))
                                new_list.add(dr);
                        }
                        reformatAttribute(old_attr, new_attr, new_attr.getId(), dtab, new_list);
                    }
                }
            } else {
                Attribute new_attr = (Attribute) old_attr.clone();
                String s = new_attr.getId();
                new_attr.setId((s==null?"":s) + addtionalID);
                root.addAttribute(new_attr);
                if(old_attr.getAttrCount()==0) {
                    for(Iterator it = list.iterator(); it.hasNext();) {
                        ((DataRecord)it.next()).attrMap.put(old_attr, new_attr);
                    }
                    dtab.addAttribute(new_attr);
                }
                else
                    reformatAttribute(old_attr, new_attr, new_attr.getId(), dtab, list);
            }
        }

    }

    public DataTable reformat() {
        DataTable dtab = new DataTable();
        dtab.dataSource = dataSource;
        if(dataSource.getNameAttr()==null) {
            for(int i=0;i<records.size();i++) {
                DataRecord drec = (DataRecord)records.get(i);
                Object str_name = names.get(drec.getId());
                if(str_name!=null)
                    drec.setName(str_name.toString());
            }
        }
        dtab.root = new Attribute("R00T", "R00T", Attribute.STRING);
        reformatAttribute(root, dtab.root, "", dtab, new ArrayList(records));
        if(aindex.size()==dtab.aindex.size())
            return this;
        HashMap new_recs = new HashMap();
        for(Iterator it = records.iterator();it.hasNext();) {
            DataRecord rec = (DataRecord) it.next();
            String id = rec.getId();
            DataRecord n_rec = (DataRecord) new_recs.get(id);
            if(n_rec==null) {
                n_rec = new DataRecord(id, rec.getName(), dtab);
                new_recs.put(id, n_rec);
            }
            int count = rec.getAttrCount();
            for(int i=0; i<count; i++) {
                Object attr = rec.attrMap.get(rec.getAttribute(i));
                if(attr!=null)
                    n_rec.setData(rec.getData(i), ((Attribute)attr).getId());
            }
        }
        Object[] a = new_recs.keySet().toArray();
        for(int i = 0; i<a.length; i++)
            a[i] = Integer.parseInt((String)a[i]);
        Arrays.sort(a);
        for(int i = 0; i<a.length; i++)
            dtab.addRecord((DataRecord) new_recs.get(""+a[i]));
        return dtab;
    }

    private void cleaningAttribute(Attribute root, Attribute oldroot, ArrayList rem, DataTable dtab, HashMap attrs) {
        for(int i = 0; i<oldroot.getAttrCount(); i++) {
            Attribute oldattr = oldroot.getAttribute(i);
            if(rem.contains(oldattr))
                continue;
            Attribute attr = (Attribute) oldattr.clone();
            root.addAttribute(attr);
            if(oldattr.getAttrCount()==0) {
                dtab.addAttribute(attr);
                attrs.put(attr, oldattr);
            } else
                cleaningAttribute(attr, oldattr, rem, dtab, attrs);

        }
    }

    public DataTable cleaning() {
        ArrayList rem = new ArrayList();
        ArrayList check = new ArrayList();
        for(int i = 0; i<getAttrCount();i++) {
            boolean exist = false;
            for(Iterator it_rec = records.iterator(); it_rec.hasNext();)
                if(((DataRecord) it_rec.next()).getData(i)!=null) {
                    exist = true;
                    break;
                }
            if(!exist) {
                Attribute attr = getAttribute(i);
                rem.add(attr);
                attr = attr.getParent();
                if(attr!=null && !check.contains(attr))
                    check.add(attr);

            }
        }
        while (check.size()>0) {
            ArrayList t = new ArrayList();
            for(Iterator it = check.iterator();it.hasNext();) {
                Attribute attr = (Attribute) it.next();
                boolean exist = false;
                for(int i =0; i<attr.getAttrCount();i++) {
                    if(!rem.contains(attr.getAttribute(i))) {
                        exist = true;
                        break;
                    }
                }
                if(!exist) {
                    rem.add(attr);
                    attr = attr.getParent();
                    if(attr!=null && !t.contains(attr))
                        t.add(attr);
                }
            }
            check = t;
        }

        DataTable dtab = new DataTable();
        dtab.dataSource = dataSource;
        HashMap attrs = new HashMap();
        dtab.root = new Attribute("R00T", "R00T", Attribute.STRING);
        cleaningAttribute(dtab.root, root, rem, dtab, attrs);
        for(Iterator it = records.iterator();it.hasNext();) {
            DataRecord old_rec = (DataRecord) it.next();
            String id = old_rec.getId();
            DataRecord rec = new DataRecord(id, old_rec.getName(), dtab);
            int count = rec.getAttrCount();
            for(int i=0; i<count; i++) {
                Attribute oldattr = (Attribute) attrs.get(rec.getAttribute(i));
                rec.setData(old_rec.getData(oldattr.getId()),i);
            }
            dtab.addRecord(rec);
        }
        return dtab;
    }

    public void clear() {
        aindex = null;
        root = null;
        attrs = null;
        records = null;
        rindex = null;
        sample = null;
        stat = null;
        listeners = null;
        dataSource = null;
    }

    private int columnsort=-2;
    public void sortObjects() {
        if(columnsort==-1) {
            Collections.sort(records, new Comparator<DataRecord>() {
                public int compare(DataRecord o1, DataRecord o2) {
                    return -StringComparator.comp(o1.getName(), o2.getName());
                }
            });
            columnsort = -2;
        } else {
            Collections.sort(records, new Comparator<DataRecord>() {
                public int compare(DataRecord o1, DataRecord o2) {
                    return StringComparator.comp(o1.getName(), o2.getName());
                }
            });
            columnsort = -1;
        }
    }

    public void sortObjects(final int column) {
        if(columnsort==column) {
            Collections.sort(records, new Comparator<DataRecord>() {
                public int compare(DataRecord o1, DataRecord o2) {
                    Object d1 = o1.getData(column);
                    Object d2 = o2.getData(column);
                    if(d1!=null && d2!=null && d1 instanceof Number && d2 instanceof Number)
                        return (int) -Math.signum(((Number)d1).doubleValue()-((Number)d2).doubleValue());
                    return -StringComparator.comp(d1, d2);
                }
            });
            columnsort = -2;
        } else {
            Collections.sort(records, new Comparator<DataRecord>() {
                public int compare(DataRecord o1, DataRecord o2) {
                    Object d1 = o1.getData(column);
                    Object d2 = o2.getData(column);
                    if(d1!=null && d2!=null && d1 instanceof Number && d2 instanceof Number)
                        return (int) Math.signum(((Number)d1).doubleValue()-((Number)d2).doubleValue());
                    return StringComparator.comp(d1, d2);
                }
            });
            columnsort = column;
        }
    }

    public void sortAttr(int column) {

    }
}
