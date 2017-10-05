/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myjmrtdcardapplication;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.List;
import javax.imageio.ImageIO;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import net.sf.scuba.smartcards.CardServiceException;
import net.sf.scuba.smartcards.TerminalCardService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.protocol.BACResult;

/**
 *
 * @author luca
 */
public class CardReader {
    
    private DG1File dg1;
    private DG2File dg2;
    private PassportService service;
    
    public CardReader(){
        try {
            TerminalFactory terminal = TerminalFactory.getDefault();
            //listam-se eles
            List<CardTerminal> readers = terminal.terminals().list();
            //escolhe-se a primeira
            CardTerminal reader = readers.get(0);
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
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Faz a autenticação do cartão atualizando o serviçoe de envio de
     * informação TODO: PACE, EAC, PA, CA
     *
     * @param backey Chave para fazer o Basic Access Controll
     * @throws CardServiceException
     */
    public void doSecurity(BACKeySpec backey) throws CardServiceException {
        BACResult result = service.doBAC(backey);
        System.out.println(result.toString());

        //PublicKey AAkey = readDG15();
        //System.out.println(service.doAA(AAkey, "SHA512", "RSA", service.sendGetChallenge()));
        //perso = new PassportPersoService(service, result.getWrapper());
    }
    
    /**
     * Verifica se um arquivo é selecionavel
     *
     * @param service - PassportService conectado ao cartao
     * @param fid - identificador do arquivo
     * @return - true se o arquivo estiver disponivel, false se falta alguma
     * seguranca ou o arquivo nao existe
     */
    private boolean canSelectFile(short fid) {
        try {
            service.sendSelectFile(fid);
            return true;
        } catch (CardServiceException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Le o arquivo DG1
     *
     * @throws CardServiceException
     * @throws IOException
     */
    public MRZInfo readDG1() throws CardServiceException, IOException {
        InputStream dg1Stream;

        if (canSelectFile(service.EF_DG1)) {
            System.out.println("DG1 FILE PRESENT");
            //Pega o DG1 existente e imprime
            dg1Stream = service.getInputStream(service.EF_DG1);
            this.dg1 = new DG1File(dg1Stream);
            return dg1.getMRZInfo();
            //System.out.println(input.getMRZInfo().toString());
        } else {
            System.out.println("Não foi possível acessar o arquivo DG1");
            return null;
        }
    }
    
    /**
     * Le o arquivo DG2 do cartão
     *
     * @throws CardServiceException
     * @throws IOException
     */
    public BufferedImage readDG2() throws CardServiceException, IOException {
        InputStream dg2Input;

        if (canSelectFile(service.EF_DG2)) {
            System.out.println("DG2 FILE PRESENT");
            dg2Input = service.getInputStream(service.EF_DG2);
            this.dg2 = new DG2File(dg2Input);

            FaceImageInfo image = this.dg2.getFaceInfos().get(0).getFaceImageInfos().get(0);  //Pega a primeira foto

            FileOutputStream imgOut = new FileOutputStream(this.dg1.getMRZInfo().getDocumentNumber() + ".jpg");

            byte[] imgBytes = new byte[image.getImageLength()];

            new DataInputStream(image.getImageInputStream()).readFully(imgBytes);

            imgOut.write(imgBytes);
            imgOut.close();
            
            return ImageIO.read(new File(this.dg1.getMRZInfo().getDocumentNumber() + ".jpg"));

        } else {
            System.out.println("Não foi possível acessar o arquivo DG2");
            return null;
        }
    }
    
}
