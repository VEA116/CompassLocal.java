// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   CompassLocal.java

package ru.iitp.gis.local;

import org.jfree.io.IOUtils;
import ru.iitp.gis.common.Compass;
import ru.iitp.gis.common.Config;
import ru.iitp.gis.common.gui.OptionPane;
import ru.iitp.gis.common.model.MapLayer;
import ru.iitp.gis.local.export.XMLWriter;
import ru.iitp.gis.local.gui.FileChooser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CompassLocal extends Frame {

    public CompassLocal(String path) {
        super("Compass Local");
        Config.init(path);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

        });
        setLayout(new BorderLayout());
        compass = new Compass();
        compass.setButtonPane(createButtonPane());
        add("Center", compass);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((d.width - 700) / 2, (d.height - 500) / 2, 700, 500);
        setVisible(true);
    }

    private Component createButtonPane() {
        Panel p = new Panel();
        Button b = new Button(Config.getUIProperty("main.button.save-map"));
        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                FileChooser fc = new FileChooser();
                int value = OptionPane.showDialog(compass, fc,
                        Config.getUIProperty("main.dialog-title.select-file"), 3);
                if (value == 1) {
                    final String fname = fc.getSelectedFile();
                    final Dialog d = (new OptionPane("Saving map, please wait", 4)).createDialog(CompassLocal.this, "title");
                    if (fname != null) {
                        Thread t = new Thread() {
                            public void run() {
                                XMLWriter.saveMap(fname, compass.getMap());
                                d.dispose();
                            }
                        };
                        t.start();
                        d.show();
                    }
                }
            }

        });
        //p.add(comments);
        p.add(b);
        return p;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java ru.iitp.gis.CompassLocal map.xml");
        } else {
            String p = args[0];
            if (args[0].toLowerCase().trim().endsWith("compass5")) {
                try {
                    p = new String(Files.readAllBytes(Path.of(args[0])));
                } catch (Exception e) {
                    try {
                        URL u = new URL(args[0]);
                        try (InputStream in = u.openStream()) {
                            p = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                        } catch (IOException ignored) {
                        }
                    } catch (MalformedURLException ignored) {
                    }
                }
            }
            int pos = Math.max(p.lastIndexOf('/'), p.lastIndexOf('\\'));
            String path = null;
            String file;
            if (pos == -1) {
                file = p;
            } else {
                path = p.substring(0, pos + 1);
                file = p.substring(pos + 1);
            }
            Config.MAP = file;
            new CompassLocal(path);
        }
    }

    private Compass compass;
}
