/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import UI.VerifyPanel;
import java.security.Security;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.CardException;
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

        if (service != null) {
            return service;
        }
        //Encontra-se a factory
        TerminalFactory terminal = TerminalFactory.getDefault();
        //listam-se os terminais
        List<CardTerminal> readers = terminal.terminals().list();

        if (readers.isEmpty()) {
            System.out.println("No Readers Found");
            System.exit(-1);
        }

        //escolhe-se o primeiro
        reader = readers.get(0);

        System.out.println("Reader: " + reader);

        System.out.println("Por favor insira um cartão");

        for (int i = 0; i < 3 || !reader.isCardPresent(); i++) {
            reader.waitForCardPresent(10000);
            System.out.println("Cartão " + (reader.isCardPresent() ? "" : "não ") + "conectado");
        }
        if (reader.isCardPresent()) {
            service = new PassportService(new TerminalCardService(reader));
            service.open();
            service.sendSelectApplet(false);
            BouncyCastleProvider provider = new BouncyCastleProvider();
            Security.addProvider(provider);

        }

        return service;
    }

    public static void closeConnection() {

        try {

            if (reader.isCardPresent()) {
                if (GlobalFlags.DEBUG) {
                    System.err.println("Por favor retire o cartão");
                }
                ControlledDialog.showMessageDialog("Por favor retire o cartão", "Fim");
            }

            while (reader.isCardPresent()) {
                System.out.println("Remova o cartão atual");
                reader.waitForCardAbsent(100000);
            }

            ControlledDialog.closeMessageDialog();

        } catch (CardException ex) {
            Logger.getLogger(VerifyPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        service.close();
        service = null;

    }

}
