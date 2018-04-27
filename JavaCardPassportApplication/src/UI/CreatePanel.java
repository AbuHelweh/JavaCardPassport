/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UI;

import util.MyCertificateFactory;
import util.DebugPersistence;
import util.GlobalFlags;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import myjmrtdcardapplication.CardSender;
import net.sf.scuba.data.Country;
import net.sf.scuba.data.Gender;
import org.jmrtd.BACKey;
import org.jmrtd.cert.CardVerifiableCertificate;
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.lds.iso19794.FingerInfo;
import util.ControlledDialog;

/**
 *
 * @author luca
 */
public class CreatePanel extends javax.swing.JPanel {

    private JFrame container;
    private HashMap<Integer, Integer> daysMonths;
    private File chosenImage;
    private CardVerifiableCertificate certificate = null;
    private FingerInfo[] fingers = new FingerInfo[10]; //Ordenado de dedao a mindinho mao direita e esquerda

    /**
     * Creates new form CreatePanel
     */
    public CreatePanel(JFrame container) {
        initComponents();
        this.daysMonths = new HashMap();
        daysMonths.put(1, 31);
        daysMonths.put(2, 28);
        daysMonths.put(3, 31);
        daysMonths.put(4, 30);
        daysMonths.put(5, 31);
        daysMonths.put(6, 30);
        daysMonths.put(7, 31);
        daysMonths.put(8, 31);
        daysMonths.put(9, 30);
        daysMonths.put(10, 31);
        daysMonths.put(11, 30);
        daysMonths.put(12, 31);

        this.container = container;

        this.jPanel1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() > 1) {
                    mouseHandler();
                }

                if (chosenImage == null) {
                    System.out.println("Image Error");
                    return;
                }

                Graphics g = jPanel1.getGraphics();
                g.fillRect(0, 0, 150, 200);
                try {
                    g.drawImage(ImageIO.read(chosenImage), 0, 0, 150, 200, jPanel1);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Calendar cal = Calendar.getInstance();

        MonthBox.removeAllItems();
        for (int i = 0; i < this.getMonths().length; i++) {
            MonthBox.addItem(this.getMonths()[i]);
        }
        MonthBox.setSelectedIndex(cal.get(Calendar.MONTH));

        DayBox.setSelectedIndex(cal.get(Calendar.DATE) - 1);

        YearBox.removeAllItems();
        for (int i = 0; i <= 100; i++) {
            YearBox.addItem((cal.get(Calendar.YEAR) - i) + "");
        }

        Country[] countries = Country.values();

        NacCombo.removeAllItems();
        EmitCombo.removeAllItems();
        SexBox.removeAllItems();

        for (int i = 0; i < countries.length; i++) {
            NacCombo.addItem(countries[i].toAlpha3Code() + " " + countries[i].getName());
            EmitCombo.addItem(countries[i].toAlpha3Code() + " " + countries[i].getName());
        }

        SexBox.addItem("Masculino");
        SexBox.addItem("Feminino");
        SexBox.addItem("Indefinido");

        jPanel1.setBackground(Color.black);

        valSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
        
        

    }

    public void mouseHandler() {
        if (GlobalFlags.DEBUG) {

            JFileChooser chooser = new JFileChooser();
            File file = null;
            int choice = chooser.showOpenDialog(null);

            if (choice == JFileChooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();
            }

            if (file == null) {
                System.out.println("File Error");
                return;
            }

            chosenImage = file;

        } else {
            ControlledDialog.showMessageDialog("Loading Camera", "Please Wait");
            new ImageGetFrame(this);
        }
    }

    public void placeFingerInfos(FingerInfo[] fingers) {
        this.fingers = fingers;
        System.out.println("Saved Images");
    }

    public FingerInfo[] getFingerInfos() {
        return fingers;
    }

    public void placeImage(BufferedImage photo) {

        if (photo == null) {
            System.out.println("ImageError");
        }

        Graphics g = jPanel1.getGraphics();
        g.fillRect(0, 0, 150, 200);

        g.drawImage(photo, 0, 0, 150, 200, jPanel1);

    }

    public void placeImage(File photo) {

        chosenImage = photo;

        if (chosenImage == null) {
            System.out.println("Image Error");
            return;
        }

        Graphics g = jPanel1.getGraphics();
        g.fillRect(0, 0, 150, 200);
        try {
            g.drawImage(ImageIO.read(chosenImage), 0, 0, 150, 200, jPanel1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private int getDays() {
        int choice = MonthBox.getSelectedIndex();
        if (choice < 0) {
            choice = 0;
        }
        return daysMonths.get(choice + 1);
    }

    private String[] getMonths() {
        String[] temp = new java.text.DateFormatSymbols().getMonths();
        if (temp[temp.length - 1].equals("")) {
            String[] ret = new String[temp.length - 1];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = temp[i];
            }
            return ret;
        }
        return temp;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        jPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        TextFieldNome = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        TextFieldSobreNome = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        NacCombo = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        EmitCombo = new javax.swing.JComboBox<>();
        SexBox = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        CPFField = new javax.swing.JTextField();
        OKButton = new javax.swing.JButton();
        CancelButton = new javax.swing.JButton();
        MonthBox = new javax.swing.JComboBox<>();
        DayBox = new javax.swing.JComboBox<>();
        YearBox = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        valSpinner = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        PassportNumber = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        CertificateButton = new javax.swing.JButton();
        EACPrivButton = new javax.swing.JButton();
        PrintButton = new javax.swing.JButton();

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 198, Short.MAX_VALUE)
        );

        jLabel1.setText("Primeiro Nome:");

        TextFieldNome.setText("PrimeiroNome");
        TextFieldNome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TextFieldNomeActionPerformed(evt);
            }
        });

        jLabel2.setText("Sobrenomes:");

        TextFieldSobreNome.setText("Sobrenome");
        TextFieldSobreNome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TextFieldSobreNomeActionPerformed(evt);
            }
        });

        jLabel3.setText("Data de Nascimento:");

        NacCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        NacCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NacComboActionPerformed(evt);
            }
        });

        jLabel4.setText("Nascionalidade:");

        jLabel5.setText("Estado Emissor:");

        EmitCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        EmitCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EmitComboActionPerformed(evt);
            }
        });

        SexBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        SexBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SexBoxActionPerformed(evt);
            }
        });

        jLabel6.setText("Sexo:");

        jLabel7.setText("CPF:");

        CPFField.setText("123456789");
        CPFField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CPFFieldActionPerformed(evt);
            }
        });

        OKButton.setText("Enviar");
        OKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OKButtonActionPerformed(evt);
            }
        });

        CancelButton.setText("Cancelar");
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });

        MonthBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        MonthBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MonthBoxActionPerformed(evt);
            }
        });

        DayBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        DayBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DayBoxActionPerformed(evt);
            }
        });

        YearBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        YearBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                YearBoxActionPerformed(evt);
            }
        });

        jLabel8.setText("Valido por:");

        jLabel9.setText("ano(s).");

        PassportNumber.setText("123456789");

        jLabel10.setText("Num Pass:");

        CertificateButton.setText("Certificado");
        CertificateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertificateButtonActionPerformed(evt);
            }
        });

        EACPrivButton.setText("EAC Priv");
        EACPrivButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EACPrivButtonActionPerformed(evt);
            }
        });

        PrintButton.setText("Enviar Digital");
        PrintButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PrintButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addComponent(jLabel8)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(valSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(jLabel9))
                    .addComponent(PrintButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(CertificateButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(EACPrivButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(TextFieldNome)
                    .addComponent(TextFieldSobreNome)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(DayBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(MonthBox, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(YearBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(NacCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(SexBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(52, 52, 52)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(EmitCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(CPFField)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(PassportNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(85, 85, 85)
                        .addComponent(OKButton, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(CancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TextFieldNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TextFieldSobreNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(MonthBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(DayBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(YearBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(NacCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(EmitCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SexBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(CPFField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(24, 24, 24)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel10)
                            .addComponent(PassportNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(OKButton)
                            .addComponent(CancelButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(valSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(PrintButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(CertificateButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(EACPrivButton)
                        .addGap(0, 12, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void CPFFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CPFFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_CPFFieldActionPerformed

    private void NacComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NacComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NacComboActionPerformed

    private void EmitComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EmitComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_EmitComboActionPerformed

    private void SexBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SexBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SexBoxActionPerformed

    private void TextFieldSobreNomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TextFieldSobreNomeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TextFieldSobreNomeActionPerformed

    private void TextFieldNomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TextFieldNomeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TextFieldNomeActionPerformed

    private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelButtonActionPerformed
        container.dispose();
    }//GEN-LAST:event_CancelButtonActionPerformed

    private void OKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OKButtonActionPerformed

        boolean worked = true;
        try {
            

            System.out.println(this.YearBox.getItemAt(this.YearBox.getSelectedIndex()).trim().substring(2)
                    + (this.MonthBox.getSelectedIndex() < 9 ? "0" + (this.MonthBox.getSelectedIndex() + 1) : (this.MonthBox.getSelectedIndex() + 1))
                    + (this.DayBox.getSelectedIndex() < 9 ? "0" + (this.DayBox.getSelectedIndex() + 1) : (this.DayBox.getSelectedIndex() + 1)));


            MRZInfo mrz = parseMRZ();

            System.out.println(mrz);

            CardSender sender = new CardSender();
            BACKey key = new BACKey(mrz.getDocumentNumber(), mrz.getDateOfBirth(), mrz.getDateOfExpiry());
            sender.SendSecurityInfo(key, certificate);
            
            sender.SendDG1(mrz);
            sender.SendDG2(chosenImage);
            sender.SendDG3(fingers);
            
            //Certificado do cartão, provavelmente uma versao CV do certificado SOD
            if (certificate != null) {
                sender.SendDG14(DebugPersistence.getInstance().getDHKey().getPublic());
            }
                        
            sender.SendCOM();
            
            sender.sendSOD();  //needs X509 Certificate... WTF

            sender.LockCard();
        } catch (Exception ex) {
            worked = false;
            JOptionPane.showMessageDialog(null, "A problem happened check debug status!");
            Logger.getLogger(CreatePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (worked) {
            JOptionPane.showMessageDialog(null, "Card Successfully Uploaded!");
        }
        this.container.dispose();

    }//GEN-LAST:event_OKButtonActionPerformed

    private void MonthBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MonthBoxActionPerformed
        DayBox.removeAllItems();
        for (int i = 1; i <= this.getDays(); i++) {
            DayBox.addItem(i + "");
        }
    }//GEN-LAST:event_MonthBoxActionPerformed

    private void DayBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DayBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_DayBoxActionPerformed

    private void YearBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_YearBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_YearBoxActionPerformed

    private void CertificateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertificateButtonActionPerformed
        try {
            // TODO Save Terminal Certificate and References in files, or better yet, GET them from files.
            certificate = MyCertificateFactory.getInstance().generateCVCertificate(Country.getInstance("BRA"), 10);
            System.out.println(certificate.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_CertificateButtonActionPerformed

    private void PrintButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PrintButtonActionPerformed
        new FingerPrintCollection(this);
    }//GEN-LAST:event_PrintButtonActionPerformed

    private void EACPrivButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EACPrivButtonActionPerformed
        // TODO Send EAC privateKey.
    }//GEN-LAST:event_EACPrivButtonActionPerformed

    
    public MRZInfo parseMRZ(){
        
        Calendar cal = Calendar.getInstance();
        
        return new MRZInfo("P", this.EmitCombo.getItemAt(this.EmitCombo.getSelectedIndex()).trim().substring(0, 3),
                    this.TextFieldNome.getText().trim().toUpperCase(),
                    this.TextFieldSobreNome.getText().trim().toUpperCase(),
                    this.PassportNumber.getText().trim().toUpperCase(),
                    this.NacCombo.getItemAt(this.NacCombo.getSelectedIndex()).trim().substring(0, 3),
                    this.YearBox.getItemAt(this.YearBox.getSelectedIndex()).trim().substring(2)
                    + (this.MonthBox.getSelectedIndex() < 9 ? "0" + (this.MonthBox.getSelectedIndex() + 1) : (this.MonthBox.getSelectedIndex() + 1))
                    + (this.DayBox.getSelectedIndex() < 9 ? "0" + (this.DayBox.getSelectedIndex() + 1) : (this.DayBox.getSelectedIndex() + 1)),
                    this.SexBox.getSelectedIndex() == 0 ? Gender.MALE : (this.SexBox.getSelectedIndex() == 1 ? Gender.FEMALE : Gender.UNSPECIFIED),
                    (cal.get(Calendar.YEAR) + (int) valSpinner.getValue() + "").substring(2)
                    + ((cal.get(Calendar.MONTH) + 1) <= 9 ? "0" + (cal.get(Calendar.MONTH) + 1) : (cal.get(Calendar.MONTH) + 1))
                    + ((cal.get(Calendar.DAY_OF_MONTH)) <= 9 ? "0" + (cal.get(Calendar.DAY_OF_MONTH)) : (cal.get(Calendar.DAY_OF_MONTH))),
                    this.CPFField.getText().trim());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField CPFField;
    private javax.swing.JButton CancelButton;
    private javax.swing.JButton CertificateButton;
    private javax.swing.JComboBox<String> DayBox;
    private javax.swing.JButton EACPrivButton;
    private javax.swing.JComboBox<String> EmitCombo;
    private javax.swing.JComboBox<String> MonthBox;
    private javax.swing.JComboBox<String> NacCombo;
    private javax.swing.JButton OKButton;
    private javax.swing.JTextField PassportNumber;
    private javax.swing.JButton PrintButton;
    private javax.swing.JComboBox<String> SexBox;
    private javax.swing.JTextField TextFieldNome;
    private javax.swing.JTextField TextFieldSobreNome;
    private javax.swing.JComboBox<String> YearBox;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSpinner valSpinner;
    // End of variables declaration//GEN-END:variables
}
