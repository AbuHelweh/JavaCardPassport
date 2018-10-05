/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import UI.CreatePanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import jnitestfingerprint.FPrintController;
import org.apache.commons.io.FileUtils;
import org.jmrtd.lds.iso19794.FingerImageInfo;
import org.jmrtd.lds.iso19794.FingerInfo;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author luca
 */
public class FingerPrintCollection extends javax.swing.JFrame {

    /**
     * Creates new form FingerPrintCollection
     */
    boolean isPrint = false;
    CreatePanel parent;
    FingerInfo[] fingers;
    boolean leftHand = false;
    JPanel[] panels = new JPanel[5];

    public FingerPrintCollection(CreatePanel parent) {
        initComponents();

        panels[0] = Thumb;
        panels[1] = Pointer;
        panels[2] = Middle;
        panels[3] = Ring;
        panels[4] = Little;

        for (JPanel p : panels) {
            p.setBorder(BorderFactory.createLineBorder(Color.black));
        }

        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.parent = parent;
        this.fingers = parent.getFingerInfos();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowOpened(WindowEvent e) {
                updateImages();
            }

            @Override
            public void windowActivated(WindowEvent e) {
                updateImages();
            }
        });

        for (int i = 0; i < panels.length; i++) {

            final int j = i;

            this.panels[i].addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if (e.getClickCount() > 1) {
                        mouseHandler(e, j);
                    }

                }
            });

        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateImages();
            }
        });

    }

    public void mouseHandler(MouseEvent e, int finger) {

        if(isPrint){
            return;
        } else {
            isPrint = true;
        }
        try {
            int offset = 0;

            if (leftHand) {
                offset = 5;
            }

            FingerInfo f; //um dedo
            ArrayList<FingerImageInfo> fiil = new ArrayList();
            FingerImageInfo fii;

            fii = takePrint(finger + offset);
            fiil.add(fii);

            f = new FingerInfo(0, //capture device ID 0= unspecified
                    30, //aquisition level 30 = 500dpi
                    FingerInfo.SCALE_UNITS_PPI, //scale units
                    160, 500, //dimension picture w,h
                    160, 500, //dimension reader w,h
                    8, //pixel depth
                    FingerInfo.COMPRESSION_JPEG,
                    fiil);      //cria uma entrada de série de digitais

            fingers[finger + offset] = f;

            //*/
            BufferedImage img = ImageIO.read(fii.getImageInputStream());

            Graphics g = panels[finger].getGraphics();
            g.fillRect(0, 0, 115, 150);
            g.drawImage(img, 0, 0, 115, 150, panels[finger]);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            isPrint = false;
        }
    }

    public void updateImages() {
        int offset = 0;
        if (leftHand) {
            offset = 5;
        }
        Graphics g;
        InputStream imageInput;

        for (int i = 0; i < panels.length; i++) {
            g = panels[i].getGraphics();

            if (fingers[i + offset] == null) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, 115, 150);
                continue;
            }

            imageInput = fingers[i + offset].getFingerImageInfos().get(0).getImageInputStream();
            try {
                g.drawImage(ImageIO.read(imageInput), 0, 0, 115, 150, null);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Tira uma digital e armazena a posição correta no array para ser enviada
     * depois
     *
     * @param finger Qual dedo será armazenado
     * @return
     */
    public FingerImageInfo takePrint(int finger) {

        //Os codigos dos dedos no JMRTD comecam no dedao direito com 1 e vao ate o mindinho esquerdo com 10
        try {

            ControlledDialog.showMessageDialog("Escaneie sua digital agora");
            
            Thread t = new Thread(new Runnable() {
                public void run() {
                     new FPrintController().scanImage();
                }
            });
            t.start();

            //Talvez salvar as imagens no banco de dados seja uma boa ideia depois
           Thread.sleep(1000);
           
           t.join();

            ControlledDialog.closeMessageDialog();

            //-------------------- Leitura e conversão da imagem, tá lindo e maravilhoso nao alterar ---------------------------------
            byte[] img = FileUtils.readFileToByteArray(new File("finger_standardized.pgm")); //<< Lê a imagem direto como byteArray
            Mat temp = Imgcodecs.imdecode(new MatOfByte(img), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED); //<----------Works!  decodifica em uma mat

            System.out.println(img.length);

            //---------------Converter Para JPeg--------------------------------
            
            Imgproc.resize(temp, temp, new Size(), 0.5, 0.5, Imgproc.INTER_LINEAR);
            
            MatOfInt params = new MatOfInt(Imgcodecs.CV_IMWRITE_JPEG_QUALITY, 30);  //com este parametro
            Imgcodecs.imwrite(finger + ".jpg", temp, params);    //Salva a imagem em formato JPEG

            byte[] imgJPG = FileUtils.readFileToByteArray(new File(finger + ".jpg")); //<< Lê a imagem JPG direto como byteArray

            System.out.println(imgJPG.length);

            //------------------- Fim leitura da imagem
            return new FingerImageInfo(finger + 1,
                    1, 1, 100, //view count, view number, quality%
                    FingerImageInfo.IMPRESSION_TYPE_SWIPE, //impression type
                    temp.width(), temp.height(), //Dimensions w,h
                    new ByteArrayInputStream(imgJPG), //image bytes
                    imgJPG.length, //image size in bytes
                    FingerInfo.COMPRESSION_JPEG); //compression type          //Cria uma entrada de digital para um dedo

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Thumb = new javax.swing.JPanel();
        Pointer = new javax.swing.JPanel();
        Middle = new javax.swing.JPanel();
        Ring = new javax.swing.JPanel();
        Little = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout ThumbLayout = new javax.swing.GroupLayout(Thumb);
        Thumb.setLayout(ThumbLayout);
        ThumbLayout.setHorizontalGroup(
            ThumbLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 115, Short.MAX_VALUE)
        );
        ThumbLayout.setVerticalGroup(
            ThumbLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout PointerLayout = new javax.swing.GroupLayout(Pointer);
        Pointer.setLayout(PointerLayout);
        PointerLayout.setHorizontalGroup(
            PointerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 115, Short.MAX_VALUE)
        );
        PointerLayout.setVerticalGroup(
            PointerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout MiddleLayout = new javax.swing.GroupLayout(Middle);
        Middle.setLayout(MiddleLayout);
        MiddleLayout.setHorizontalGroup(
            MiddleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 115, Short.MAX_VALUE)
        );
        MiddleLayout.setVerticalGroup(
            MiddleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout RingLayout = new javax.swing.GroupLayout(Ring);
        Ring.setLayout(RingLayout);
        RingLayout.setHorizontalGroup(
            RingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 115, Short.MAX_VALUE)
        );
        RingLayout.setVerticalGroup(
            RingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout LittleLayout = new javax.swing.GroupLayout(Little);
        Little.setLayout(LittleLayout);
        LittleLayout.setHorizontalGroup(
            LittleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 115, Short.MAX_VALUE)
        );
        LittleLayout.setVerticalGroup(
            LittleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );

        jButton1.setText("Salvar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setText("M�o Direita");

        jButton2.setText("Trocar m�o");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Cancelar");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel2.setText("Polegar");

        jLabel3.setText("Indicador");

        jLabel4.setText("M�dio");

        jLabel5.setText("Anelar");

        jLabel6.setText("M�nimo");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Thumb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(jLabel2)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(Pointer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(Middle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(Ring, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(75, 75, 75)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(108, 108, 108)
                                .addComponent(jLabel4)))
                        .addGap(131, 131, 131)
                        .addComponent(jLabel5)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(Little, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(67, 67, 67))))
            .addGroup(layout.createSequentialGroup()
                .addGap(162, 162, 162)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(237, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Little, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Ring, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Middle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Pointer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Thumb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addGap(28, 28, 28)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addGap(38, 38, 38))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        parent.placeFingerInfos(fingers);
        System.out.println("Saving Prints");
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        leftHand = !leftHand;
        jLabel1.setText(leftHand ? "Mão esquerda" : "Mão direita");
        updateImages();

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Little;
    private javax.swing.JPanel Middle;
    private javax.swing.JPanel Pointer;
    private javax.swing.JPanel Ring;
    private javax.swing.JPanel Thumb;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    // End of variables declaration//GEN-END:variables
}
