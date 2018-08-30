/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jnitestfingerprint.FPrintController;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
public class FingerPrintVerification extends javax.swing.JFrame {
    
    FingerInfo[] fingers = new FingerInfo[10];
    boolean leftHand = false;
    JPanel[] panels = new JPanel[5];

    /**
     * Creates new form FingerPrintVerification
     */
    public FingerPrintVerification(FingerInfo[] fingers) {
        initComponents();

        this.fingers = fingers;
       
        panels[0] = Thumb;
        panels[1] = Pointer;
        panels[2] = Middle;
        panels[3] = Ring;
        panels[4] = Little;
        
        for(JPanel p : panels){
            p.setBorder(BorderFactory.createLineBorder(Color.black));
        }

        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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
        
        java.awt.EventQueue.invokeLater(new Runnable(){
            @Override
            public void run(){
                updateImages();
            }
        });

    }

    public void mouseHandler(MouseEvent e, int finger) {

        try {
            int offset = 0;

            if (leftHand) {
                offset = 5;
            }
            
            
            //--------------------Inicio conversao, tá maravilhoso nao alterar------------------------
            
            MatOfInt params;
            byte[] imgJPG = IOUtils.toByteArray(fingers[finger+offset].getFingerImageInfos().get(0).getImageInputStream());
            Mat tempJPG = Imgcodecs.imdecode(new MatOfByte(imgJPG), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED); //<----------Works! decodifica em uma mat
            
            System.out.println(imgJPG.length);
            
            Imgproc.resize(tempJPG, tempJPG, new Size(),2,2,Imgproc.INTER_LINEAR);
            
            //---------------Converter para PGM---------------------------------
            
            params = new MatOfInt(Imgcodecs.CV_IMWRITE_PXM_BINARY,0);   //Parametros PGM
            Imgcodecs.imwrite("fingerPGM.pgm", tempJPG);  //Salva a Imagem PGM
            
            byte[] imgPGM = FileUtils.readFileToByteArray(new File("fingerPGM.pgm")); //Lê a imagem PGM direto como byteArray
            
            System.out.println(imgPGM.length);
            
            
            //---------------Fim Conversoes-------------------------------------

            
            char[] imgchar = new char[imgPGM.length];
            
            for(int i = 0; i < imgPGM.length; i++){
                imgchar[i] = (char) (imgPGM[i] & 0xFF);     //Converte para char
            }
            
            //------------------------------------FIM DA CONVERSÃO----------------------------------
            
            boolean match;
            
            ControlledDialog.showMessageDialog("Escaneie seu dedo agora");
            
            new Thread(new Runnable(){
                @Override
                public void run(){
                    FPrintController fp = new FPrintController();
                    showMatch(fp.verifyImage(imgchar, 500, 160));
                    ControlledDialog.closeMessageDialog();
                }
            }).start();
            

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void showMatch(boolean match){
        JOptionPane.showMessageDialog(null, "Match: " + match);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton3 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        Thumb = new javax.swing.JPanel();
        Pointer = new javax.swing.JPanel();
        Middle = new javax.swing.JPanel();
        Ring = new javax.swing.JPanel();
        Little = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton3.setText("Voltar");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel2.setText("Dedão");

        jLabel3.setText("Indicador");

        jLabel4.setText("Meio");

        jLabel5.setText("Anelar");

        jLabel6.setText("Mínimo");

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

        jLabel1.setText("Mão Direita");

        jButton2.setText("Trocar mão");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

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
                        .addComponent(jLabel3)
                        .addGap(108, 108, 108)
                        .addComponent(jLabel4)
                        .addGap(131, 131, 131)
                        .addComponent(jLabel5)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(Little, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(67, 67, 67))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(242, 242, 242)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(319, 319, 319)
                        .addComponent(jLabel1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addGap(38, 38, 38))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        leftHand = !leftHand;
        jLabel1.setText(leftHand ? "Mão esquerda" : "Mão direita");
        updateImages();
    }//GEN-LAST:event_jButton2ActionPerformed

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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Little;
    private javax.swing.JPanel Middle;
    private javax.swing.JPanel Pointer;
    private javax.swing.JPanel Ring;
    private javax.swing.JPanel Thumb;
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
