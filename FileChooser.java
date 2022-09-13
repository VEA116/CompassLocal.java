package ru.iitp.gis.local.gui;

import ru.iitp.gis.common.gui.OptionPane;
import ru.iitp.gis.common.Config;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FileDialog;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FileChooser extends Panel
        implements ActionListener {

    public FileChooser() {
        setLayout(new BorderLayout(4, 0));
        tf = new TextField(30);
        add(tf, "Center");
        Button b = new Button("...");
        b.addActionListener(this);
        add(b, "East");
    }

    public void actionPerformed(ActionEvent e) {
        FileDialog fd = new FileDialog(OptionPane.getFrameForComponent(this),
                Config.getUIProperty("main.dialog-title.select-file"));
        fd.setDirectory(directory);
        fd.show();
        String s = fd.getFile();
        if (s != null) {
            directory = fd.getDirectory();
            tf.setText(directory + s);
        }
    }

    public String getSelectedFile() {
        String file = tf.getText().trim();
        if (file.length() == 0)
            return null;
        File f = new File(file);
        if (f.isAbsolute())
            return f.getAbsolutePath();
        else
            return directory + file;
    }

    public static void setCurrentDirectory(String directory) {
        File f = new File(directory);
        if (f.exists())
            directory = f.getAbsolutePath();
    }

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static String directory = System.getProperty("user.dir") + FILE_SEPARATOR;
    private TextField tf;

}
