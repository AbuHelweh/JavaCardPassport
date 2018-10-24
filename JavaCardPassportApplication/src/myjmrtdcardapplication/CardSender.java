/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myjmrtdcardapplication;

import util.ImageWorks;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import net.sf.scuba.data.Gender;
import net.sf.scuba.smartcards.CardServiceException;
import org.apache.commons.io.FileUtils;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.cert.CardVerifiableCertificate;
import org.jmrtd.lds.ChipAuthenticationInfo;
import org.jmrtd.lds.ChipAuthenticationPublicKeyInfo;
import org.jmrtd.lds.LDSFile;
import org.jmrtd.lds.SODFile;
import org.jmrtd.lds.SecurityInfo;
import org.jmrtd.lds.TerminalAuthenticationInfo;
import org.jmrtd.lds.icao.COMFile;
import org.jmrtd.lds.icao.DG14File;
import org.jmrtd.lds.icao.DG15File;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.icao.DG3File;
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FaceInfo;
import org.jmrtd.lds.iso19794.FingerImageInfo;
import org.jmrtd.lds.iso19794.FingerInfo;
import util.CardConnection;
import util.MyCertificateFactory;

/**
 *
 * @author luca
 */
public class CardSender {

    PassportService service;
    PassportPersoService perso;

    MessageDigest digest;
    HashMap<Integer, byte[]> dataGroupHashes; //Doc 7 part 2.1 "A hash for each Data Group in use SHALL be stored in the Document Security Object (EF.SOD)"
    ArrayList<Integer> dataGroupComTagList = new ArrayList();

    public CardSender() {

        dataGroupHashes = new HashMap();

        try {

            digest = MessageDigest.getInstance("SHA-256");

            service = CardConnection.connectPassportService();

            perso = new PassportPersoService(service);

        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof CardServiceException) {
                JOptionPane.showMessageDialog(null, "ERRO DE LEITURA COM O CARTÃO");
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
    public void SendSecurityInfo(BACKeySpec backey, CardVerifiableCertificate certificate) throws Exception {
        if (!perso.isOpen()) {
            perso.open();
        }
        

        if (certificate != null) {

            System.out.println("Enviando Chave Privada EAC/ ECDH");
            perso.putPrivateEACKey(MyCertificateFactory.getInstance().getEACECPair().getPrivate()); //<<CVCA Private Key??

            System.out.println("Sending Certificate");
            perso.putCVCertificate(certificate); //<-- CVCA Public Key?
        }

        System.out.println("Sending BAC");
        perso.setBAC(backey.getDocumentNumber(), backey.getDateOfBirth(), backey.getDateOfExpiry());

    }

    /**
     * Envia o arquivo COM para o cartao, ele possui uma lista dos arquivos
     * existentes dentro do cartao
     *
     * @throws CardServiceException
     */
    public void SendCOM() throws CardServiceException {

        dataGroupComTagList.add(LDSFile.EF_COM_TAG);
        
        int[] a = new int[17];
        
        a = dataGroupComTagList.stream().mapToInt(Integer::intValue).toArray();
        
        COMFile com = new COMFile("01.08", "08.00.00", a); //LDS Version and UTF version

        dataGroupHashes.put(0, digest.digest(com.getEncoded()));

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

        dataGroupHashes.put(1, digest.digest(dg1.getEncoded()));
        dataGroupComTagList.add(LDSFile.EF_DG1_TAG);
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
        BufferedImage portrait = ImageIO.read(file);        //Carrega a imagem jp2 em java modificar com o metodo do fingerprint 
        file.delete();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(portrait, "jpeg 2000", baos);
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

        dataGroupHashes.put(2, digest.digest(dg2.getEncoded()));
        dataGroupComTagList.add(LDSFile.EF_DG2_TAG);
    }

    /**
     * Envia o arquivo DG3 para o cartão com todas as digitais coletadas
     *
     * @param finger
     * @throws IOException
     * @throws CardServiceException
     */
    public void SendDG3(FingerInfo[] fingers) throws IOException, CardServiceException {
        
        byte[] dummyImg = FileUtils.readFileToByteArray(new File("dummy.jpg"));

        ArrayList<FingerImageInfo> dummyArray = new ArrayList();
        dummyArray.add(new FingerImageInfo(0,
                    1, 1, 100, //view count, view number, quality%
                    FingerImageInfo.IMPRESSION_TYPE_SWIPE, //impression type
                    0, 0, //Dimensions w,h
                    new ByteArrayInputStream(dummyImg), //image bytes
                    0, //image size in bytes
                    FingerInfo.COMPRESSION_JPEG) //compression type          //Cria uma entrada de digital para um dedo
        );
        
        FingerInfo dummy = new FingerInfo(0, //capture device ID 0= unspecified
                    0, //aquisition level 30 = 500dpi
                    FingerInfo.SCALE_UNITS_PPI, //scale units
                    0, 0, //dimension picture w,h
                    0, 0, //dimension reader w,h
                    8, //pixel depth
                    FingerInfo.COMPRESSION_JPEG,
                    dummyArray);      //cria uma entrada de série de digitais
        
        ArrayList<FingerInfo> fingerInfos = new ArrayList<>();

        boolean willSend = false;
        
        for (FingerInfo fingerInfo : fingers) {
            if(fingerInfo != null){
                fingerInfos.add(fingerInfo);
                willSend = true;
            }
            else{
                fingerInfos.add(dummy);
            }
        }
        
        if(!willSend){
            return;
        }


        DG3File dg3 = new DG3File(fingerInfos);

        if (!perso.isOpen()) {
            perso.open();
        }

        perso.createFile(service.EF_DG3, (short) dg3.getEncoded().length);
        perso.selectFile(service.EF_DG3);

        System.out.println("Enviando arquivo DG3");
        perso.writeFile(service.EF_DG3, new ByteArrayInputStream(dg3.getEncoded()));

        dataGroupHashes.put(3, digest.digest(dg3.getEncoded()));
        dataGroupComTagList.add(LDSFile.EF_DG3_TAG);
    }

    /**
     * Envia o arquivo DG14 que contem as informacoes necessarias para o
     * protocolo CA, parte do EAC
     *
     * @param DHpublicKey
     * @throws CardServiceException
     */
    public void SendDG14(PublicKey DHpublicKey) throws CardServiceException {
        ArrayList<SecurityInfo> infos = new ArrayList();

        SecurityInfo caInfo = new ChipAuthenticationInfo(SecurityInfo.ID_CA_DH_AES_CBC_CMAC_256, 2);  //Encontrar o ID Certo
        SecurityInfo caKInfo = new ChipAuthenticationPublicKeyInfo(DHpublicKey);
        SecurityInfo taInfo = new TerminalAuthenticationInfo();

        infos.add(caInfo);
        infos.add(caKInfo);
        infos.add(taInfo);

        DG14File dg14 = new DG14File(infos);

        if (!perso.isOpen()) {
            perso.open();
        }

        perso.createFile(service.EF_DG14, (short) dg14.getEncoded().length);
        perso.selectFile(service.EF_DG14);

        System.out.println("Enviando arquivo DG14");
        perso.writeFile(service.EF_DG14, new ByteArrayInputStream(dg14.getEncoded()));

        dataGroupHashes.put(14, digest.digest(dg14.getEncoded()));
        dataGroupComTagList.add(LDSFile.EF_DG14_TAG);
    }

    public void SendDG15() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair aapair = keyGen.genKeyPair();
        DG15File dg15 = new DG15File(aapair.getPublic());

        System.out.println("Enviando informa��es para AA");
        perso.putPrivateKey(aapair.getPrivate());

        perso.createFile(service.EF_DG15, (short) dg15.getEncoded().length);
        perso.selectFile(service.EF_DG15);

        System.out.println("Enviando arquivo DG15");
        perso.writeFile(service.EF_DG15, new ByteArrayInputStream(dg15.getEncoded()));

        dataGroupHashes.put(15, digest.digest(dg15.getEncoded()));
        dataGroupComTagList.add(LDSFile.EF_DG15_TAG);
    }

    public void sendSOD() throws Exception {
        //PrivateKey to sign the data = Private key do Certificado?
        //Document Signer Certificate

        X509Certificate docSignCert = (X509Certificate) MyCertificateFactory.getInstance().generateTestSignedX509Certificate();

        KeyStore ks = KeyStore.getInstance("JKS");
        String pw = "123456";
        FileInputStream fis = new FileInputStream("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/Documentos/mykeystore.ks");
        ks.load(fis, pw.toCharArray());

        PrivateKey key = (PrivateKey) ks.getKey("DocSignCertificate", pw.toCharArray());

        /*
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

         */
        System.out.println("Hashes.size " + dataGroupHashes.size());
        SODFile sod = new SODFile("SHA-256", "SHA256withRSA", dataGroupHashes, key, docSignCert, "BC");

        if (!perso.isOpen()) {
            perso.open();
        }

        perso.createFile(service.EF_SOD, (short) sod.getEncoded().length);
        perso.selectFile(service.EF_SOD);

        System.out.println("Enviando arquivo SOD");

        perso.writeFile(service.EF_SOD, new ByteArrayInputStream(sod.getEncoded()));
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
}
