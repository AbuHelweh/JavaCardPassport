/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UI;

import myjmrtdcardapplication.GlobalFlags;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import org.jmrtd.lds.icao.MRZInfo;

/**
 *
 * @author luca
 */
public class CreatePanel extends javax.swing.JPanel {

    private JFrame container;
    private HashMap<Integer, Integer> daysMonths;
    private File chosenImage;
    private static CreatePanel Instance;

    /**
     * Creates new form CreatePanel
     */
    public CreatePanel(JFrame container) {
        initComponents();
        Instance = this;
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
                        JOptionPane.showMessageDialog(null, "Loading Camera", "Image Capture", JOptionPane.PLAIN_MESSAGE);
                        new ImageCollectionFrame(Instance);
                    }
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

        OKButton.setText("Upload");
        OKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OKButtonActionPerformed(evt);
            }
        });

        CancelButton.setText("Cancel");
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                    .addComponent(jLabel8)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(valSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(jLabel9)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(PassportNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(OKButton, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(110, 110, 110)
                            .addComponent(CancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jLabel1)
                        .addComponent(jLabel2)
                        .addComponent(jLabel3)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(DayBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(MonthBox, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(YearBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(TextFieldNome, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                            .addComponent(TextFieldSobreNome, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel4)
                                .addComponent(NacCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel6)
                                .addComponent(SexBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(52, 52, 52)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel7)
                                .addComponent(CPFField, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(EmitCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel5)))))
                .addContainerGap(39, Short.MAX_VALUE))
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                            .addComponent(jLabel9))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        try {

            System.out.println(this.YearBox.getItemAt(this.YearBox.getSelectedIndex()).trim().substring(2)
                    + (this.MonthBox.getSelectedIndex() < 9 ? "0" + (this.MonthBox.getSelectedIndex() + 1) : (this.MonthBox.getSelectedIndex() + 1))
                    + (this.DayBox.getSelectedIndex() < 9 ? "0" + (this.DayBox.getSelectedIndex() + 1) : (this.DayBox.getSelectedIndex() + 1)));

            CardSender sender = new CardSender();
            MRZInfo mrz = new MRZInfo("P", this.EmitCombo.getItemAt(this.EmitCombo.getSelectedIndex()).trim().substring(0, 3),
                    this.TextFieldNome.getText().trim().toUpperCase(),
                    this.TextFieldSobreNome.getText().trim().toUpperCase(),
                    this.PassportNumber.getText().trim().toUpperCase(),
                    this.NacCombo.getItemAt(this.NacCombo.getSelectedIndex()).trim().substring(0, 3),
                    this.YearBox.getItemAt(this.YearBox.getSelectedIndex()).trim().substring(2)
                    + (this.MonthBox.getSelectedIndex() < 9 ? "0" + (this.MonthBox.getSelectedIndex() + 1) : (this.MonthBox.getSelectedIndex() + 1))
                    + (this.DayBox.getSelectedIndex() < 9 ? "0" + (this.DayBox.getSelectedIndex() + 1) : (this.DayBox.getSelectedIndex() + 1)),
                    this.SexBox.getSelectedIndex() == 0 ? Gender.MALE : (this.SexBox.getSelectedIndex() == 1 ? Gender.FEMALE : Gender.UNSPECIFIED),
                    this.YearBox.getItemAt(this.YearBox.getSelectedIndex()).trim().substring(2)
                    + (this.MonthBox.getSelectedIndex() < 9 ? "0" + (this.MonthBox.getSelectedIndex() + 1) : (this.MonthBox.getSelectedIndex() + 1))
                    + (this.DayBox.getSelectedIndex() < 9 ? "0" + (this.DayBox.getSelectedIndex() + 1) : (this.DayBox.getSelectedIndex() + 1)),
                    this.CPFField.getText().trim());

            BACKey key = new BACKey(mrz.getDocumentNumber(), mrz.getDateOfBirth(), mrz.getDateOfExpiry());
            sender.SendSecurityInfo(key);
            sender.SendCOM();
            sender.SendDG1(mrz);
            sender.SendDG2(chosenImage);
            sender.LockCard();
        } catch (Exception ex) {
            Logger.getLogger(CreatePanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        JOptionPane.showMessageDialog(null, "Card Successfully Uploaded!");
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField CPFField;
    private javax.swing.JButton CancelButton;
    private javax.swing.JComboBox<String> DayBox;
    private javax.swing.JComboBox<String> EmitCombo;
    private javax.swing.JComboBox<String> MonthBox;
    private javax.swing.JComboBox<String> NacCombo;
    private javax.swing.JButton OKButton;
    private javax.swing.JTextField PassportNumber;
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
