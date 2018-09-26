/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import UI.MainPanel;
import UI.VerifyPanel;
import java.security.Security;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import net.sf.scuba.smartcards.CardServiceException;
import net.sf.scuba.smartcards.TerminalCardService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jmrtd.PassportService;

/**
 *
 * @author luca
 */
public class CardConnection {

    static CardTerminal reader;
    static PassportService service;

    public static PassportService connectPassportService() throws CardException, CardServiceException {

        //Encontra-se a factory
        TerminalFactory terminal = TerminalFactory.getDefault();

        //listam-se os terminais
        List<CardTerminal> readers = terminal.terminals().list();

        if (readers.isEmpty()) {
            System.out.println("No Readers Found");
            System.exit(-1);
        }
        
        for (CardTerminal reader : readers){
            System.out.println(reader);
        }

        //escolhe-se o primeiro
        reader = readers.get(0);

        if (GlobalFlags.DEBUG) {
            System.err.println("Reader: " + reader);
            System.err.println(reader.isCardPresent());
            System.err.println("Por favor insira um cartão");
        }

        for (int i = 0; i < 10 && !reader.isCardPresent(); i++) {
            ControlledDialog.showMessageDialog("Por favor insira um cartão " + i);
            reader.waitForCardPresent(1000);
            System.err.println("Cartão " + (reader.isCardPresent() ? "" : "não ") + "conectado " + i);
        }
        try {
            if (reader.isCardPresent()) {

                service = new PassportService(new TerminalCardService(reader));
                service.open();
                service.sendSelectApplet(false);
                BouncyCastleProvider provider = new BouncyCastleProvider();
                Security.addProvider(provider);

            } else {
                throw new CardNotPresentException("Cartão não encontrado");
            }
        } finally {
            ControlledDialog.closeMessageDialog();
        }
        return service;
    }

    public static void closeConnection() {

        if (service == null) {
            return;
        }
        service.close();

        try {

            if (reader.isCardPresent()) {
                if (GlobalFlags.DEBUG) {
                    System.err.println("Por favor retire o cartão");
                }
                ControlledDialog.showMessageDialog("Por favor retire o cartão");
            }

            while (reader.isCardPresent()) {
                System.out.println("Remova o cartão atual");
                reader.waitForCardAbsent(1000);
            }

            ControlledDialog.closeMessageDialog();

        } catch (CardException ex) {
            Logger.getLogger(VerifyPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
