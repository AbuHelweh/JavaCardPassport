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
public class Control {

    private char[] image;

    public Control() {
    }

    public void command() {
        FPrintController controller = new FPrintController();
        controller.sayHello();
        int[] size = {540,160};

        image = controller.scanImage();
        
        /*
        for(char c : image){
            System.out.println((short)c + " ");
        }
         */
        controller.verifyImage(image,size[0],size[1]);
    }

}
