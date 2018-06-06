/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jnitestfingerprint;

import org.opencv.core.Core;

/**
 *
 * @author luca
 */
public class Main {

    static {
        System.out.println(System.getProperty("os.name"));
        System.out.println(System.getProperty("user.name"));
        if (System.getProperty("os.name").contains("Windows")) {
            System.load("C:/Users/" + System.getProperty("user.name") + "/Downloads/FingerPrintLib/FingerPrintBridgeLib.so");
        }

        if (System.getProperty("os.name").contains("Linux")) {
            System.load("/home/" + System.getProperty("user.name") + "/TCC/JavaCardPassport/FingerPrintBridgeLib/dist/FingerPrintBridgeLib.so");
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Control().command();
    }

}
