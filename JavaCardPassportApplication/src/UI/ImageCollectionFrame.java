/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author luca
 */
public class ImageCollectionFrame extends JFrame implements Runnable {

    private JPanel camera;
    private CreatePanel back;
    private JButton capture;
    private JButton cancel;
    private BufferedImage photo;
    private Mat frame;
    private VideoCapture cv;
    private boolean taken = false;

    public ImageCollectionFrame(CreatePanel panel) {
        cv = new VideoCapture(0);
        frame = new Mat();
        back = panel;

        if (!cv.isOpened()) {
            System.out.println("Camera ERROR");
            return;
        }

        camera = new JPanel();
        camera.setBackground(Color.black);
        camera.setPreferredSize(new Dimension(150, 200));

        capture = new JButton("Capture");
        capture.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                taken = true;
                MatOfByte mob = new MatOfByte();
                MatOfInt params = new MatOfInt(Imgcodecs.CV_IMWRITE_JPEG_QUALITY, 100);

                cv.read(frame);

                Imgcodecs.imencode(".jpg", frame, mob, params);

                byte[] ba = mob.toArray();
                try {
                    photo = ImageIO.read(new ByteArrayInputStream(ba));
                    File picture = new File("PictureTaken.jpg");
                    ImageIO.write(photo, "jpg", picture);
                    back.placeImage(picture);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                
                dispose();
            }

        });
        capture.setPreferredSize(new Dimension(100, 50));

        cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        cancel.setPreferredSize(new Dimension(100, 50));

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent ev){
                cv.release();
            }
        });

        initUI();
    }

    private void initUI() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.gridheight = 2;
        this.add(camera, c);

        c.gridheight = 1;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 2;
        this.add(capture, c);

        c.gridx = 2;
        this.add(cancel, c);

        this.setSize(300, 400);
        this.setTitle("Taking Pictures");
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.requestFocus();
        
        new Thread(this).start();
        

    }

    @Override
    public void run() {
        while (!taken) {
            MatOfByte mob = new MatOfByte();
            MatOfInt params = new MatOfInt(Imgcodecs.CV_IMWRITE_JPEG_QUALITY, 100);
            
            System.out.println(cv.toString());
            cv.read(frame);
            System.out.println(cv.toString());

            Imgcodecs.imencode(".jpg", frame, mob, params);

            byte[] ba = mob.toArray();
            try {
                photo = ImageIO.read(new ByteArrayInputStream(ba));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Graphics g = camera.getGraphics();
            g.drawImage(photo, 0, 0, camera.WIDTH, camera.HEIGHT, camera);
            camera.repaint();
        }
    }

}
