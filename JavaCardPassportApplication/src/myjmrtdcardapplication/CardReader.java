/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myjmrtdcardapplication;

import Security.EACResult;
import Security.SecurityProtocols;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import net.sf.scuba.smartcards.CardServiceException;
import net.sf.scuba.smartcards.TerminalCardService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.DataGroup;
import org.jmrtd.lds.icao.COMFile;
import org.jmrtd.lds.icao.DG14File;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.icao.DG3File;
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FingerInfo;
import org.jmrtd.protocol.BACResult;

/**
 * TODO do PA and AA here before showing anything
 * @author luca
 */
public class CardReader {
    
    private DataGroup[] files;
    private PassportService service;
    
    public CardReader(){
        try {
            TerminalFactory terminal = TerminalFactory.getDefault();
            //listam-se eles
            List<CardTerminal> readers = terminal.terminals().list();
            //escolhe-se a primeira
            if(readers.isEmpty()){
                System.out.println("No Readers Found");
                System.exit(-1);
            }
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
            files = new DataGroup[16];
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Faz a autenticação do cartão atualizando o serviçoe de envio de
     * informação TODO: EAC, PA, CA, TA
     *
     * @param backey Chave para fazer o Basic Access Controll
     * @throws CardServiceException
     */
    public void doSecurity(BACKeySpec backey) throws CardServiceException, Exception {
        SecurityProtocols sec = SecurityProtocols.getInstance(service, this);
        BACResult result = sec.doBAC(backey);
        System.out.println(result.toString());

        DG14File dg14 = this.readDG14();
        if(dg14 != null){
          EACResult eacresult = sec.doEAC(dg14);  
        }
        

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
            //e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Lê o arquivo COM que contém as informações sobre o que está no cartão
     * @return
     * @throws CardServiceException
     * @throws IOException 
     */
    public COMFile readCOM() throws CardServiceException, IOException{
        InputStream COMStream;
        
        if(canSelectFile(service.EF_COM)){
            System.out.println("COM FILE PRESENT");
            //Pega o DG1 existente e imprime
            COMStream = service.getInputStream(service.EF_COM);
            return new COMFile(COMStream);
        } else {
            return null;
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
            this.files[1] = new DG1File(dg1Stream);
            return ((DG1File)files[1]).getMRZInfo();
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
            this.files[2] = new DG2File(dg2Input);

            FaceImageInfo image = ((DG2File)files[2]).getFaceInfos().get(0).getFaceImageInfos().get(0);  //Pega a primeira foto

            FileOutputStream imgOut = new FileOutputStream(((DG1File)this.files[1]).getMRZInfo().getDocumentNumber() + ".jpg");

            byte[] imgBytes = new byte[image.getImageLength()];

            new DataInputStream(image.getImageInputStream()).readFully(imgBytes);

            imgOut.write(imgBytes);
            imgOut.close();
            
            return ImageIO.read(new File(((DG1File)this.files[1]).getMRZInfo().getDocumentNumber() + ".jpg"));

        } else {
            System.out.println("Não foi possível acessar o arquivo DG2");
            return null;
        }
    }
    
    public ArrayList<FingerInfo> readDG3() throws CardServiceException, IOException{
        
        InputStream dg3Input;
        
        if(canSelectFile(service.EF_DG3)){
            System.out.println("DG3 FILE PRESENT");
            dg3Input = service.getInputStream(service.EF_DG3);
            this.files[3] = new DG3File(dg3Input);
            
            if(this.files[3] == null){
                System.out.println("file null");
            }
            
            return (ArrayList<FingerInfo>)((DG3File)this.files[3]).getFingerInfos();
            
        } else {
            System.out.println("Não foi possível acessar o arquivo DG3");
            return null;
        }
        
        
    }
    
    public DG14File readDG14() throws IOException, CardServiceException{
        InputStream dg14Input;
        
        if(canSelectFile(service.EF_DG14)){
            System.out.println("DG14 FILE PRESENT");
            dg14Input = service.getInputStream(service.EF_DG14);
            this.files[14] = new DG14File(dg14Input);
            return (DG14File)this.files[14];
        }
        System.out.println("Não foi possivel acessar o arquivo DG14");
        return null;
    }
    
    public DataGroup getDGFile(int file){
        return files[file];
    }
    
}
