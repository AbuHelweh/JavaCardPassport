/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UI;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

/**
 *
 * @author luca
 */
public class ImageGetThread implements Runnable{

    ImageGetFrame video;
    VideoCapture cv;
    
    public ImageGetThread(ImageGetFrame frame, VideoCapture vc){
        video = frame;
        cv = vc;
    }
    
    @Override
    public void run() {
        try {
            Mat frame = new Mat();
            
            while (!video.taken()) {
                MatOfByte mob = new MatOfByte();
                MatOfInt params = new MatOfInt(Imgcodecs.CV_IMWRITE_JPEG_QUALITY, 100);

                System.out.println("VideoWorking");

                if (cv.read(frame)) {

                    if (frame.empty()) {
                        System.out.print("Failed to capture Image");
                        return;
                    }

                    Imgcodecs.imencode(".jpg", frame, mob, params);

                    byte[] ba = mob.toArray();

                    BufferedImage photo = ImageIO.read(new ByteArrayInputStream(ba));

                    video.drawImage(photo);

                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    
}
