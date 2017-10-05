/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UI;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.opencv.core.Core;

/**
 *
 * @author luca
 */
public class Main extends JFrame {

    JPanel mainPanel;
    
    public static void main(String[] args){
        new Main();
    }

    public Main() {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.load("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/FingerPrintBridgeLib/dist/FingerPrintBridgeLib.so");
            System.load("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/StasmBridgeLib/dist/StasmBridgeLib.so");
            initUI();
        } catch (Exception e){
            e.printStackTrace();
        }
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
