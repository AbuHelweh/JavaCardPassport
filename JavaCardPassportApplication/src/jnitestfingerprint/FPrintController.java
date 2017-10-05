/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jnitestfingerprint;

/**
 *
 * @author luca
 */
public class FPrintController {
    
    public native void sayHello();
    public native int init();
    public native int verifyPrint();
    public native char[] scanImage();
    public native boolean verifyImage(char[] a,int img_height, int img_width);
}
