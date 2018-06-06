/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myjmrtdcardapplication;

import org.jmrtd.*;
import net.sf.scuba.smartcards.*;
import org.opencv.core.Core;

/**
 *
 * @author Luca Fachini Campelli 16/06/17
 */
public class MyJMRTDCardApplication {

    /**
     * @param args the command line arguments
     */
    
    static boolean WRITE = true;
    static boolean READ = false;
    
    static{
        if (System.getProperty("os.name").contains("Linux")) {
            //System.load("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/FingerPrintBridgeLib/dist/FingerPrintBridgeLib.so");
            System.load("/home/" + System.getProperty("user.name") + "/TCC/JavaCardPassport/FingerPrintBridgeLib/dist/FingerPrintBridgeLib.so");
            //System.load("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/StasmBridgeLib/dist/StasmBridgeLib.so");
            System.load("/home/" + System.getProperty("user.name") + "/TCC/JavaCardPassport/StasmBridgeLib/dist/StasmBridgeLib.so");
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        }
    }
    
    public static void main(String[] args) {
        
        CardCom com;
        try {
            /*
            if (args.length > 0) {
                if (args[0].equals("-set")) {
                    com = new CardCom(true, 0);
                }
                if (args[0].equals("-get")) {
                    for (String s : args) {
                        if (s.equals("-DG1")) {
                            com = new CardCom(false, 1);
                        }
                        if (s.equals("-DG2")) {
                            com = new CardCom(false, 2);
                        }
            
                        if (s.equals("-DG3")) {
                            com = new CardCom(false, 3);
                        }
                        if (s.equals("-DG5")) {
                            com = new CardCom(false, 5);
                        }
                    }
                }
            }*/
            com = new CardCom(WRITE, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

}
