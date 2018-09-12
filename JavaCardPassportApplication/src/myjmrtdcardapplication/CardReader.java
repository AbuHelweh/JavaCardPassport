/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myjmrtdcardapplication;

import Security.EACResult;
import Security.PAResult;
import Security.SecurityProtocols;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import net.sf.scuba.smartcards.CardServiceException;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.DataGroup;
import org.jmrtd.lds.SODFile;
import org.jmrtd.lds.icao.COMFile;
import org.jmrtd.lds.icao.DG14File;
import org.jmrtd.lds.icao.DG15File;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.icao.DG3File;
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FingerInfo;
import org.jmrtd.protocol.BACResult;
import util.CardConnection;

/**
 * TODO do PA and AA here before showing anything
 *
 * @author luca
 */
public class CardReader {

    private DataGroup[] files;
    private SODFile SOD = null;
    private COMFile COM = null;
    private PassportService service;

    public CardReader() throws Exception {
        try {
            
            service = CardConnection.connectPassportService();
            

            files = new DataGroup[16];
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void doBAC(BACKeySpec backey) throws CardServiceException{
        SecurityProtocols sec = SecurityProtocols.getInstance(service, this);
        System.out.println(backey);
        try {
            BACResult result = sec.doBAC(backey);
            System.out.println(result.toString());
        } catch (CardServiceException e) {
            System.out.println("BAC ERROR");
            throw new CardServiceException(e.toString());
        }
    }
    
    /**
     * Faz a autenticação do cartão atualizando o serviçoe de envio de
     * informação TODO: EAC, PA, CA, TA
     *
     * @param backey Chave para fazer o Basic Access Controll
     * @throws CardServiceException
     */
    public void executeSecurityProtocols(COMFile com, SODFile sod) {
        SecurityProtocols sec = SecurityProtocols.getInstance(service, this);
        /*
        sec.setAlgorithms(sod);

        try {
            DG14File dg14 = this.readDG14();
            if (dg14 != null) {
                EACResult eacres = sec.doEAC(dg14);
                System.out.println(eacres.toString());
            }
        } catch (Exception e) {
            System.out.println("EAC ERROR");
            e.printStackTrace();
        }
        */
        try{
            DG15File dg15 = this.readDG15();
            if(dg15 != null){
                Security.AAResult aares = sec.doAA(dg15);
                System.out.println(aares);
                if(!aares.getResult()){
                    JOptionPane.showMessageDialog(null, "Falha na Autenticação Ativa, Cartão Inválido");
                }
            }
        } catch (Exception e){
            System.out.println("AA ERROR");
            e.printStackTrace();
        }
        /*
        try {
            PAResult pares = sec.doPA(com, sod);
            System.out.println(pares.toString());
            if(!pares.veredict()){
                JOptionPane.showMessageDialog(null, "Falha na Autenticação Passiva, Cartão Modificado");
            }
        } catch (Exception e) {
            System.out.println("PA ERROR");
            e.printStackTrace();
        }
        */
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
     *
     * @return
     * @throws CardServiceException
     * @throws IOException
     */
    public COMFile readCOM() throws CardServiceException, IOException {
        InputStream COMStream;

        if (COM != null) {
            return COM;
        }

        if (canSelectFile(service.EF_COM)) {
            System.out.println("COM FILE PRESENT");
            //Pega o DG1 existente e imprime
            COMStream = service.getInputStream(service.EF_COM);
            COM = new COMFile(COMStream);
            return COM;
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
            this.files[0] = new DG1File(dg1Stream);
            return ((DG1File) files[0]).getMRZInfo();
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
            this.files[1] = new DG2File(dg2Input);

            FaceImageInfo image = ((DG2File) files[1]).getFaceInfos().get(0).getFaceImageInfos().get(0);  //Pega a primeira foto

            FileOutputStream imgOut = new FileOutputStream(((DG1File) this.files[0]).getMRZInfo().getDocumentNumber() + ".jp2");

            byte[] imgBytes = new byte[image.getImageLength()];

            new DataInputStream(image.getImageInputStream()).readFully(imgBytes);

            imgOut.write(imgBytes);
            imgOut.close();

            return ImageIO.read(new File(((DG1File) this.files[0]).getMRZInfo().getDocumentNumber() + ".jp2"));

        } else {
            System.out.println("Não foi possível acessar o arquivo DG2");
            return null;
        }
    }

    public ArrayList<FingerInfo> readDG3() throws CardServiceException, IOException {

        InputStream dg3Input;

        if (canSelectFile(service.EF_DG3)) {
            System.out.println("DG3 FILE PRESENT");
            dg3Input = service.getInputStream(service.EF_DG3);
            this.files[2] = new DG3File(dg3Input);

            return (ArrayList<FingerInfo>) ((DG3File) this.files[2]).getFingerInfos();

        } else {
            System.out.println("Não foi possível acessar o arquivo DG3");
            return null;
        }

    }

    public DG14File readDG14() throws IOException, CardServiceException {
        InputStream dg14Input;

        if (canSelectFile(service.EF_DG14)) {
            System.out.println("DG14 FILE PRESENT");
            dg14Input = service.getInputStream(service.EF_DG14);
            this.files[13] = new DG14File(dg14Input);
            return (DG14File) this.files[13];
        }
        System.out.println("Não foi possivel acessar o arquivo DG14");
        return null;
    }

    public DG15File readDG15() throws IOException, CardServiceException{
        InputStream dg15Input;
        
        if(canSelectFile(service.EF_DG15)) {
            System.out.println("DG15 FILE PRESENT");
            dg15Input = service.getInputStream(service.EF_DG15);
            this.files[14] = new DG15File(dg15Input);
            return (DG15File)this.files[14];
        }
        System.out.println("Não foi possivel acessar o arquivo DG15");
        return null;
    }
    
    public SODFile readSOD() throws IOException, CardServiceException {
        InputStream SODInput;

        if (SOD != null) {
            return SOD;
        }

        if (canSelectFile(service.EF_SOD)) {
            System.out.println("SOD FILE PRESENT");
            SODInput = service.getInputStream(service.EF_SOD);
            this.SOD = new SODFile(SODInput);
            return SOD;
        }
        System.out.println("Não foi possivel acessar o arquivo SOD");
        return null;

    }

    public DataGroup getDGFile(int file) {
        return this.files[file-1];
    }

}
