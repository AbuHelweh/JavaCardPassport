/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UI;

import java.io.IOException;
import javax.swing.JFrame;
import org.opencv.core.Core;

/**
 *
 * @author luca
 */
public class MainFrame extends JFrame {

    public MainFrame() throws Exception{
        initUI();
    }

    private void initUI() throws IOException {

        this.add(new MainPanel(this));
        this.setSize(400,300);
        this.setTitle("JMRTD Card Builder and checker, does't verify, that's with Brandão");
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setFocusable(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
