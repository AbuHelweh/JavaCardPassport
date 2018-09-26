/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UI;

import javax.swing.JPanel;
import org.opencv.core.Core;

/**
 *
 * @author luca
 */
public class Main{

    JPanel mainPanel;
    
    public static void main(String[] args){
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.load("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/FingerPrintBridgeLib/dist/FingerPrintBridgeLib.so");
            System.load("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/StasmBridgeLib/dist/StasmBridgeLib.so");
            new MainFrame();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}