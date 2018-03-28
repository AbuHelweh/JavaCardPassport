/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jnitestfingerprint;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;
import static myjmrtdcardapplication.CardCom.DOCUMENTNUMBER;
import org.apache.commons.io.FileUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

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
        int[] size = {540, 160};

        image = controller.scanImage();
        
        System.out.println(image.length);

        try {

            byte[] img = FileUtils.readFileToByteArray(new File("finger_standardized.pgm")); //<< Lê a imagem direto como byteArray
            Mat temp = Imgcodecs.imdecode(new MatOfByte(img), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED); //<----------Works!  decodifica em uma mat
            
            System.out.println(img.length);
            
            //---------------Converter Para JPeg--------------------------------
            
            MatOfInt params = new MatOfInt(Imgcodecs.CV_IMWRITE_JPEG_QUALITY, 100);  //com este parametro
            Imgcodecs.imwrite("testJPG.jpg", temp, params);    //Salva a imagem em formato JPEG
            
            byte[] imgJPG = FileUtils.readFileToByteArray(new File("testJPG.jpg")); //<< Lê a imagem JPG direto como byteArray
            Mat tempJPG = Imgcodecs.imdecode(new MatOfByte(imgJPG), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED); //<----------Works! decodifica em uma mat
            
            System.out.println(imgJPG.length);
            
            
            //---------------Converter para PGM---------------------------------
            
            params = new MatOfInt(Imgcodecs.CV_IMWRITE_PXM_BINARY,0);   //Parametros PGM
            Imgcodecs.imwrite("testPGM.pgm", tempJPG);  //Salva a Imagem PGM
            
            byte[] imgPGM = FileUtils.readFileToByteArray(new File("testPGM.pgm")); //Lê a imagem PGM direto como byteArray
            
            System.out.println(imgPGM.length);
            
            
            //---------------Fim Conversoes-------------------------------------

            
            char[] imgchar = new char[imgPGM.length];
            
            for(int i = 0; i < imgPGM.length; i++){
                imgchar[i] = (char) (imgPGM[i] & 0xFF);     //Converte para char
            }
            
            while (true) {
                controller.verifyImage(imgchar, size[0], size[1]);  //Tretas
            }

            //------------------------------------------//Received
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void printImgBytes(byte[] bytes, int offset) {
        System.out.println("");
        System.out.println("");
        System.out.println("");
        for (byte b : bytes) {
            System.out.print(((char) (b & 0xFF) - offset) + " ");
        }
    }

}
