/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myjmrtdcardapplication;

import util.DebugPersistence;
import util.ImageWorks;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import javax.swing.JOptionPane;
import jnitestfingerprint.FPrintController;
import static myjmrtdcardapplication.CardCom.DOCUMENTNUMBER;
import net.sf.scuba.data.Gender;
import net.sf.scuba.smartcards.CardServiceException;
import net.sf.scuba.smartcards.TerminalCardService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.cert.CardVerifiableCertificate;
import org.jmrtd.lds.ChipAuthenticationInfo;
import org.jmrtd.lds.ChipAuthenticationPublicKeyInfo;
import org.jmrtd.lds.LDSFile;
import org.jmrtd.lds.SODFile;
import org.jmrtd.lds.SecurityInfo;
import org.jmrtd.lds.icao.COMFile;
import org.jmrtd.lds.icao.DG14File;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.icao.DG3File;
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FaceInfo;
import org.jmrtd.lds.iso19794.FingerImageInfo;
import org.jmrtd.lds.iso19794.FingerInfo;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author luca
 */
public class CardSender {

    PassportService service;
    PassportPersoService perso;

    MessageDigest digest;
    HashMap<Integer, byte[]> sodHashes; //Doc 7 part 2.1 "A hash for each Data Group in use SHALL be stored in the Document Security Object (EF.SOD)"
    int[] tagList = new int[17];

    public CardSender() {

        sodHashes = new HashMap();

        try {

            digest = MessageDigest.getInstance("SHA-256");

            //Encontra-se a factory
            TerminalFactory terminal = TerminalFactory.getDefault();
            //listam-se os terminais
            List<CardTerminal> readers = terminal.terminals().list();
            //escolhe-se o primeiro
            CardTerminal reader = readers.get(0);
            System.out.println("Reader: " + reader);

            System.out.println("Por favor insira um cartão");

            for (int i = 0; i < 3 || !reader.isCardPresent(); i++) {
                reader.waitForCardPresent(10000);   //Se espera por um cartão
                System.out.println("Cartão " + (reader.isCardPresent() ? "" : "não ") + "conectado");
            }
            if (reader.isCardPresent()) {
                service = new PassportService(new TerminalCardService(reader)); //abre o processo do passaporte
                service.open();

                service.sendSelectApplet(false);    //envia o comando para se selecionar o aplicativo, PACE PODE SER FEITO ANTES PARA AUTENTICAR O CARTAO

                perso = new PassportPersoService(service);  //abre o servico de edicao do cartao
                BouncyCastleProvider provider = new BouncyCastleProvider();
                Security.addProvider(provider);

            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof CardServiceException) {
                JOptionPane.showMessageDialog(null, "ERRO DE LEITURA COM O CARTÂO");
            }
        }
    }

    /**
     * Envia as informacoes de seguranca para o cartao Problema se executamos do
     * Security logo apos para enviar as informacoes criptografadas.
     *
     * @param backey - chave bac gerada das informacoes postas no MRZ
     * @throws CardServiceException
     */
    public void SendSecurityInfo(BACKeySpec backey, CardVerifiableCertificate certificate) throws CardServiceException {
        if (!perso.isOpen()) {
            perso.open();
        }

        if (certificate != null) {

            System.out.println("Enviando Chave Privada EAC/ ECDH");
            perso.putPrivateEACKey(DebugPersistence.getInstance().getECPair().getPrivate());

            System.out.println("Sending Certificate");
            perso.putCVCertificate(certificate);
        }

        System.out.println("Sending BAC");
        perso.setBAC(backey.getDocumentNumber(), backey.getDateOfBirth(), backey.getDateOfExpiry());

        //doSecurity(backey);
    }

    /**
     * Envia o arquivo COM para o cartao, ele possui uma lista dos arquivos
     * existentes dentro do cartao
     *
     * @throws CardServiceException
     */
    public void SendCOM() throws CardServiceException {

        tagList[0] = LDSFile.EF_COM_TAG;

        tagList[4] = 0;//LDSFile.EF_DG4_TAG;
        tagList[5] = 0;//LDSFile.EF_DG5_TAG;
        tagList[6] = 0;//LDSFile.EF_DG6_TAG;
        tagList[7] = 0;//LDSFile.EF_DG7_TAG;
        tagList[8] = 0;//LDSFile.EF_DG8_TAG;
        tagList[9] = 0;//LDSFile.EF_DG9_TAG;
        tagList[10] = 0;//LDSFile.EF_DG10_TAG;
        tagList[11] = 0;//LDSFile.EF_DG11_TAG;
        tagList[12] = 0;//LDSFile.EF_DG12_TAG;
        tagList[13] = 0;//LDSFile.EF_DG13_TAG;

        tagList[15] = 0;//LDSFile.EF_DG15_TAG;

        COMFile com = new COMFile("01.08", "08.00.00", tagList); //LDS Version and UTF version

        if (!perso.isOpen()) {
            perso.open();
        }

        System.out.println("Enviando arquivo COM");
        perso.createFile(service.EF_COM, (short) com.getEncoded().length);
        perso.selectFile(service.EF_COM);
        perso.writeFile(service.EF_COM, new ByteArrayInputStream(com.getEncoded()));

    }

    /**
     * Envia o arquivo DG1 para o cartão
     *
     * @param info as informações do MRZ
     * @throws CardServiceException
     * @throws IOException
     */
    public void SendDG1(MRZInfo info) throws CardServiceException, IOException {

        //Cria um bloco MRZ com os dados acima coletados.
        System.out.println(info.toString());
        //Com o bloco MRZ cria-se um arquivo DG1
        DG1File dg1 = new DG1File(info);

        if (!perso.isOpen()) {
            perso.open();
        }

        perso.createFile(service.EF_DG1, (short) dg1.getEncoded().length);
        perso.selectFile(service.EF_DG1);

        System.out.println("Enviando arquivo DG1");
        perso.writeFile(service.EF_DG1, new ByteArrayInputStream(dg1.getEncoded()));

        sodHashes.put(1, digest.digest(dg1.getEncoded()));
        tagList[1] = LDSFile.EF_DG1_TAG;
    }

    /**
     * Envia o arquivo DG2 para o cartão
     *
     * @param file o Arquivo contendo a imagem da pessoa
     * @throws IOException
     * @throws CardServiceException
     */
    public void SendDG2(File file) throws IOException, CardServiceException {

        ArrayList<FaceInfo> faces = new ArrayList<>();
        ArrayList<FaceImageInfo> images = new ArrayList<>();

        if (file == null) {
            return;
        }

        FaceImageInfo.FeaturePoint[] fps = ImageWorks.extractPointsFromImageAndResolve(file);

        //Imagem para o cartão
        BufferedImage portrait = ImageIO.read(file);        //Carrega a imagem jpg em java modificar com o metodo do fingerprint 
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(portrait, "jpg", baos);
        baos.flush();
        byte[] imageBytes = baos.toByteArray();     //Transforma em byteArray
        //-Imagem para o cartão

        FaceImageInfo i1 = new FaceImageInfo(Gender.UNSPECIFIED, //Gender
                FaceImageInfo.EyeColor.UNSPECIFIED, //EyeColor
                0x0000, //FeatureMask
                (int) FaceImageInfo.HAIR_COLOR_UNSPECIFIED, //HairColor
                (int) FaceImageInfo.EXPRESSION_UNSPECIFIED, //Expression
                new int[3], //poseAngle
                new int[3], //poseAngleUncertainty
                (int) FaceImageInfo.FACE_IMAGE_TYPE_BASIC,//faceImagetype
                (int) FaceImageInfo.IMAGE_COLOR_SPACE_UNSPECIFIED,// image color space
                (int) FaceImageInfo.SOURCE_TYPE_UNKNOWN, //image source
                0x0000, //deviceType
                0x0000, //quality 
                fps, //featurePoints 
                portrait.getWidth(), portrait.getHeight(), //Image Dimensions
                new ByteArrayInputStream(imageBytes), //ImageInputstream
                imageBytes.length, //imageLength
                FaceImageInfo.IMAGE_DATA_TYPE_JPEG);    //nova foto

        images.add(i1);

        FaceInfo f1 = new FaceInfo(images);

        faces.add(f1);

        DG2File dg2 = new DG2File(faces);

        if (!perso.isOpen()) {
            perso.open();
        }

        perso.createFile(service.EF_DG2, (short) dg2.getEncoded().length);
        perso.selectFile(service.EF_DG2);

        System.out.println("Enviando arquivo DG2");
        perso.writeFile(service.EF_DG2, new ByteArrayInputStream(dg2.getEncoded()));

        sodHashes.put(2, digest.digest(dg2.getEncoded()));
        tagList[2] = LDSFile.EF_DG2_TAG;
    }

    /**
     * Envia o arquivo DG3 para o cartão com todas as digitais coletadas
     *
     * @param finger
     * @throws IOException
     * @throws CardServiceException
     */
    public void SendDG3(FingerInfo[] fingers) throws IOException, CardServiceException {

        if(fingers == null){
            return;
        }
        
        ArrayList<FingerInfo> fingerInfos = new ArrayList<>();

        /*
        for (FingerInfo fingerInfo : fingers) {
            if(fingerInfo != null)
                fingerInfos.add(fingerInfo);
        }
        */
        fingerInfos.add(fingers[0]);
        
        DG3File dg3 = new DG3File(fingerInfos);

        if (!perso.isOpen()) {
            perso.open();
        }

        perso.createFile(service.EF_DG3, (short) dg3.getEncoded().length);
        perso.selectFile(service.EF_DG3);

        System.out.println("Enviando arquivo DG3");
        perso.writeFile(service.EF_DG3, new ByteArrayInputStream(dg3.getEncoded()));

        sodHashes.put(3, digest.digest(dg3.getEncoded()));
        tagList[3] = LDSFile.EF_DG3_TAG;
    }

    /**
     * Envia o arquivo DG14 que contem as informacoes necessarias para o
     * protocolo CA, parte do EAC
     *
     * @param RSApublicKey
     * @throws CardServiceException
     */
    public void SendDG14(PublicKey RSApublicKey) throws CardServiceException {
        ArrayList<SecurityInfo> infos = new ArrayList();

        SecurityInfo caInfo = new ChipAuthenticationInfo(SecurityInfo.ID_CA_ECDH_AES_CBC_CMAC_256, 2);  //Encontrar o ID Certo
        SecurityInfo caKInfo = new ChipAuthenticationPublicKeyInfo(RSApublicKey);
        //SecurityInfo taInfo = new TerminalAuthenticationInfo();

        infos.add(caInfo);
        infos.add(caKInfo);
        //infos.add(taInfo);

        DG14File dg14 = new DG14File(infos);

        if (!perso.isOpen()) {
            perso.open();
        }

        perso.createFile(service.EF_DG14, (short) dg14.getEncoded().length);
        perso.selectFile(service.EF_DG14);

        System.out.println("Enviando arquivo DG14");
        perso.writeFile(service.EF_DG14, new ByteArrayInputStream(dg14.getEncoded()));

        sodHashes.put(14, digest.digest(dg14.getEncoded()));
        tagList[14] = LDSFile.EF_DG14_TAG;
    }

    public void sendSOD() throws GeneralSecurityException {
        // Establish digestAlgorithm -> SHA256?
        // Establish digestEncryptionAlgorithm
        // Hashes ok
        //PrivateKey to sign the data... wtf?
        //Document Signer Certificate

        PrivateKey key = DebugPersistence.getInstance().getECPair().getPrivate();

        sodHashes.put(4, null);
        sodHashes.put(5, null);
        sodHashes.put(6, null);
        sodHashes.put(7, null);
        sodHashes.put(8, null);
        sodHashes.put(9, null);
        sodHashes.put(10, null);
        sodHashes.put(11, null);
        sodHashes.put(12, null);
        sodHashes.put(13, null);
        sodHashes.put(15, null);
        sodHashes.put(16, null);

        SODFile sod = new SODFile("SHA256", "SHA256withRSA", sodHashes, key, null);

        tagList[16] = LDSFile.EF_SOD_TAG;
    }

    /**
     * Fecha o cartão, o deixando não editável
     *
     * @throws CardServiceException
     */
    public void LockCard() throws CardServiceException {
        perso.lockApplet();
        perso.close();
    }

    private void doPA() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
