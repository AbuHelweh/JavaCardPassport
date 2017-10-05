/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UI;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author luca
 */
public class ProgramFrame extends JFrame {

    private JFrame parent;

    public ProgramFrame(JFrame parent) {
        this.parent = parent;
        addWindowListener(new WindowAdapter() {
            
            @Override
            public void windowClosed(WindowEvent e) {
                parent.setVisible(true);
                parent.setAlwaysOnTop(true);
            }
        });
    }

    public void init(JPanel program) {
        initUI(program);
        program.setVisible(true);
    }

    private void initUI(JPanel mode) {
        this.add(mode);
        this.setSize(600, 400);
        this.setTitle("Passport");
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

}
