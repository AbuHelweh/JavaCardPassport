/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myjmrtdcardapplication;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import javax.imageio.ImageIO;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import javax.swing.JFileChooser;
import jnitestfingerprint.FPrintController;
import net.sf.scuba.data.Country;
import net.sf.scuba.data.Gender;
import net.sf.scuba.smartcards.*;
import org.jmrtd.*;
import org.jmrtd.cert.CVCAuthorizationTemplate;
import org.jmrtd.cert.CVCPrincipal;
import org.jmrtd.cert.CVCertificateBuilder;
import org.jmrtd.cert.CardVerifiableCertificate;
import org.jmrtd.lds.ChipAuthenticationInfo;
import org.jmrtd.lds.ChipAuthenticationPublicKeyInfo;
import org.jmrtd.lds.LDSFile;
import org.jmrtd.lds.SecurityInfo;
import org.jmrtd.lds.TerminalAuthenticationInfo;
import org.jmrtd.lds.icao.COMFile;
import org.jmrtd.lds.icao.DG14File;
import org.jmrtd.lds.icao.DG15File;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.icao.DG3File;
import org.jmrtd.lds.icao.DG4File;
import org.jmrtd.lds.icao.DG5File;
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo.FeaturePoint;
import org.jmrtd.lds.iso19794.FaceInfo;
import org.jmrtd.lds.iso19794.FingerImageInfo;
import org.jmrtd.lds.iso19794.FingerInfo;
import org.jmrtd.protocol.BACResult;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.bouncycastle.jce.ECNamedCurveTable;

/**
 *
 * @author Luca Fachini Campelli 16/06/17
 */
public class CardCom {

    public static String DOCUMENTNUMBER = "123456789"; //RG - requerido 9digitos
    public static String DATEOFBIRTH = "150831"; // requerido - yymmdd
    public static String DATEOFEXPIRY = "150831"; // gerado
    //public static String DATEOFBIRTH = "150831"; // PUTIN
    //public static String DATEOFEXPIRY = "150831"; // PUTIN
    String code = "P";
    String issuingState = "BRA";
    String lastName = "CAMPELLI";
    String firstNames = "LUCA";
    String nationality = "BRA";
    Gender gender = Gender.UNKNOWN;
    String cpf = "";
    PassportService service = null;
    PassportPersoService perso;
    BACKey key;
    KeyStore ks;

    Provider bcProvider = JMRTDSecurityProvider.getBouncyCastleProvider();
    Provider jmrtdProvider = JMRTDSecurityProvider.getInstance();

    //Test purposes need to extract from files ASAP
    CVCPrincipal holderRef;
    CVCPrincipal caRef;
    KeyPair pair;
    CardVerifiableCertificate passportCertificate;
    CardVerifiableCertificate terminalCertificate;

    /**
     * Teste para comunicação do cartão
     *
     * @param opt se é para colocar ou tirar o bloco DG1
     * @throws CardServiceException
     * @throws CardException
     * @throws IOException
     */
    public CardCom(boolean opt, int type) throws CardServiceException, CardException, IOException, GeneralSecurityException {

        //load keystore onde estão os dois certificados
        //cada um tem um alias, terminal e passport
        //as keys são key para terminal e PassportKey para passaporte
        ks = KeyStore.getInstance("JKS");
        String pw = "123456";
        FileInputStream fis = new FileInputStream("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/Documentos/mykeystore.ks");
        ks.load(fis, pw.toCharArray());

        key = new BACKey(DOCUMENTNUMBER, DATEOFBIRTH, DATEOFEXPIRY);
        System.out.println(key.toString());

        //Abre uma terminal factory para detectar os terminais
        TerminalFactory terminal = TerminalFactory.getDefault();
        //listam-se eles
        List<CardTerminal> readers = terminal.terminals().list();
        //escolhe-se a primeira
        CardTerminal reader = readers.get(0);
        System.out.println("Reader: " + reader);

        System.out.println("Por favor insira um cartão");

        for (int i = 0; i < 3 && !reader.isCardPresent(); i++) {
            reader.waitForCardPresent(5000);
            System.out.println("Cartão " + (reader.isCardPresent() ? "" : "não ") + "conectado");
        }
        if (reader.isCardPresent()) {
            service = new PassportService(new TerminalCardService(reader));
            service.open();
            service.sendSelectApplet(false);
            bcProvider.put("CertificateFactory.CVC", jmrtdProvider.get("CertificateFactory.CVC"));
            Security.addProvider(bcProvider);
            Security.addProvider(jmrtdProvider);

            //*
            if (!opt) {
                doSecurity(key);
                switch (type) {
                    case 1:
                        readDG1();
                        break;
                    case 2:
                        readDG2();
                        break;
                    case 3:
                        readDG3();
                        break;
                    case 4:
                        readDG4();
                        break;
                    case 5:
                        readDG5();
                        break;
                    default:
                        break;
                }

            } else {
                perso = new PassportPersoService(service);

                SendSecurityInfo(key);
                SendCOM();
                SendDG1();
                SendDG2();
                //SendDG3();
                //SendDG15(keys);
                //SendSOD();
                LockCard();
            }
            //*/

        } else {
            System.out.println("Excedeu tempo limite");
        }

    }

    /**
     * Faz a inserção das informações de segurança no cartão.
     *
     * @param keys chaves privada e pública a serem inseridas no cartão
     * @throws CardServiceException
     */
    private void SendSecurityInfo(BACKeySpec backey) {
        try {
            if (!perso.isOpen()) {
                perso.open();
            }

            System.out.println("Sending BAC");
            perso.setBAC(backey.getDocumentNumber(), backey.getDateOfBirth(), backey.getDateOfExpiry());

            holderRef = new CVCPrincipal(Country.getInstance("BRA"), "Brazil", "00001");
            caRef = new CVCPrincipal(Country.getInstance("BRA"), "Brazil", "00001");
            KeyPairGenerator gen = KeyPairGenerator.getInstance("ECDSA", bcProvider);    //Curva Eliptica

            gen.initialize(ECNamedCurveTable.getParameterSpec("c2tnb239v3"));   //Campo binario f2m 239bits
            pair = gen.genKeyPair();

            gen = KeyPairGenerator.getInstance("RSA", bcProvider);
            gen.initialize(1024);
            KeyPair RSApair = gen.genKeyPair();

            passportCertificate = CVCertificateBuilder.createCertificate(RSApair.getPublic(), //Sha1-RSA... mas por que???
                    RSApair.getPrivate(), "SHA1withRSA", caRef, holderRef,
                    new CVCAuthorizationTemplate(CVCAuthorizationTemplate.Role.CVCA, CVCAuthorizationTemplate.Permission.READ_ACCESS_DG3_AND_DG4),
                    new Date(2017, 10, 10), new Date(2018, 10, 10), "BC");
            terminalCertificate = CVCertificateBuilder.createCertificate(RSApair.getPublic(),
                    RSApair.getPrivate(), "SHA1withRSA", caRef, holderRef,
                    new CVCAuthorizationTemplate(CVCAuthorizationTemplate.Role.CVCA, CVCAuthorizationTemplate.Permission.READ_ACCESS_DG3_AND_DG4),
                    new Date(2017, 10, 10), new Date(2018, 10, 10), "BC");

            System.out.println("Sending EAC Private");
            perso.putPrivateEACKey(pair.getPrivate());
            System.out.println("Sending Certificate");
            perso.putCVCertificate(passportCertificate);

            //doSecurity(backey);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Faz a autenticação do cartão atualizando o serviços de envio de
     * informação TODO: PACE, TA, PA, CA
     *
     * @param backey Chave para fazer o Basic Access Controll
     * @throws CardServiceException
     */
    private void doSecurity(BACKeySpec backey) throws CardServiceException, IOException {
        BACResult result = service.doBAC(backey);
        System.out.println(result.toString());

        //DG14File dg14 = SendDG14();
        //ArrayList<SecurityInfo> info = (ArrayList<SecurityInfo>) dg14.getSecurityInfos();
        //CAResult cares = service.doCA(new BigInteger(SecurityInfo.ID_CA_DH_AES_CBC_CMAC_256), SecurityInfo.ID_CA_DH_AES_CBC_CMAC_256, SecurityInfo.ID_PK_DH, pair.getPublic());
        //TAResult tares = service.doTA(CVCPrincipal caReference, List<CardVerifiableCertificates> terminalCertificates, Private Key terminalKey, String taAlg, chipAuthenticationResult cares, String DocumentNumber);
        if (perso != null) {
            perso.setWrapper(result.getWrapper());
        }
    }

    private void SendCOM() throws CardServiceException {

        int[] tagList = new int[17];
        tagList[0] = LDSFile.EF_COM_TAG;
        tagList[1] = LDSFile.EF_DG1_TAG;
        tagList[2] = LDSFile.EF_DG2_TAG;
        tagList[3] = LDSFile.EF_DG3_TAG;
        tagList[4] = LDSFile.EF_DG4_TAG;
        tagList[5] = LDSFile.EF_DG5_TAG;
        tagList[6] = LDSFile.EF_DG6_TAG;
        tagList[7] = LDSFile.EF_DG7_TAG;
        tagList[8] = LDSFile.EF_DG8_TAG;
        tagList[9] = LDSFile.EF_DG9_TAG;
        tagList[10] = LDSFile.EF_DG10_TAG;
        tagList[11] = LDSFile.EF_DG11_TAG;
        tagList[12] = LDSFile.EF_DG12_TAG;
        tagList[13] = LDSFile.EF_DG13_TAG;
        tagList[14] = LDSFile.EF_DG14_TAG;
        tagList[15] = LDSFile.EF_DG15_TAG;
        tagList[16] = LDSFile.EF_SOD_TAG;

        COMFile com = new COMFile("0.1", "0.0.1", tagList);

        if (!perso.isOpen()) {
            perso.open();
        }

        perso.createFile(service.EF_COM, (short) com.getEncoded().length);
        perso.selectFile(service.EF_COM);
        perso.writeFile(service.EF_COM, new ByteArrayInputStream(com.getEncoded()));

    }

    /**
     * Configura e envia o arquivo SOD que guarda as informações de segurança
     *
     * @throws CardServiceException
     * @throws IOException
     */
    private void SendSOD() throws CardServiceException, IOException {
        //SODFile sod = new SODFile("SHA512", "RSA",);
    }

    /**
     * Le o arquivo DG1
     *
     * @throws CardServiceException
     * @throws IOException
     */
    private void readDG1() throws CardServiceException, IOException {
        InputStream dg1Stream;
        DG1File input;

        if (canSelectFile(service.EF_DG1)) {
            System.out.println("DG1 FILE PRESENT");
            //Pega o DG1 existente e imprime
            dg1Stream = service.getInputStream(service.EF_DG1);
            input = new DG1File(dg1Stream);
            System.out.println(input.getMRZInfo().toString());
        } else {
            System.out.println("Não foi possível acessar o arquivo DG1");
        }
    }

    /**
     * Envia o MRZ do arquivo DG1
     *
     * @param service - PassportService
     * @param dg1 - Arquivo DG1
     * @throws CardServiceException
     * @throws IOException
     */
    private void SendDG1() throws CardServiceException, IOException {

        //Cria um bloco MRZ com os dados acima coletados.
        MRZInfo info = new MRZInfo(code, issuingState, lastName, firstNames, DOCUMENTNUMBER, nationality, DATEOFBIRTH, gender, DATEOFEXPIRY, cpf);
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

    }

    /**
     * Le o arquivo DG2 do cartão
     *
     * @throws CardServiceException
     * @throws IOException
     */
    private void readDG2() throws CardServiceException, IOException {
        InputStream dg2Input;
        DG2File dg2;

        if (canSelectFile(service.EF_DG2)) {
            dg2Input = service.getInputStream(service.EF_DG2);
            dg2 = new DG2File(dg2Input);

            FaceImageInfo image = dg2.getFaceInfos().get(0).getFaceImageInfos().get(0);  //Pega a primeira foto

            FileOutputStream imgOut = new FileOutputStream(DOCUMENTNUMBER + ".jpg");

            byte[] imgBytes = new byte[image.getImageLength()];

            new DataInputStream(image.getImageInputStream()).readFully(imgBytes);

            imgOut.write(imgBytes);
            imgOut.close();

        } else {
            System.out.println("Não foi possível acessar o arquivo DG2");
        }
    }

    /**
     * Envia uma fota para o cartão
     *
     * @throws IOException
     * @throws CardServiceException
     */
    private void SendDG2() throws IOException, CardServiceException {

        //Escolher a foto
        File file = null;
        JFileChooser chooser = new JFileChooser();
        int choice = chooser.showOpenDialog(null);

        if (choice == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
        }

        ArrayList<FaceInfo> faces = new ArrayList<>();
        ArrayList<FaceImageInfo> images = new ArrayList<>();

        if (file == null) {
            return;
        }

        //Reconhecimento facial
        float[] extractedFeatures = new stasmlib.StasmController().getImageFeaturePoints(file.getPath());   //TODO: Extrai features e organiza elas conforme ISO

        FeaturePoint[] fps = resolveFPS(extractedFeatures);

        //Imagem para o cartão
        BufferedImage portrait = ImageIO.read(file);        //Carrega a imagem jpg em java
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

    }

    private void readDG3() throws CardServiceException, IOException {
        InputStream dg3Input;
        DG3File dg3;

        if (canSelectFile(service.EF_DG3)) {
            System.out.print("DG3 Exists");
            dg3Input = service.getInputStream(service.EF_DG3);
            dg3 = new DG3File(dg3Input);
            FingerImageInfo image = dg3.getFingerInfos().get(0).getFingerImageInfos().get(0);

            FileOutputStream imgOut = new FileOutputStream(DOCUMENTNUMBER + ".Finger" + 1 + ".jpg"); //retira imagem do cartão

            byte[] imgBytes = new byte[image.getImageLength()];

            new DataInputStream(image.getImageInputStream()).readFully(imgBytes);

            imgOut.write(imgBytes);
            imgOut.close();

            Mat temp = Imgcodecs.imread(DOCUMENTNUMBER + ".Finger" + 1 + ".jpg");    //Converte
            MatOfInt params = new MatOfInt(Imgcodecs.CV_IMWRITE_PXM_BINARY, 1);                                     //set params
            Imgcodecs.imwrite(DOCUMENTNUMBER + ".Finger" + 1 + ".pgm", temp, params);                                 //pra pgm

            File pgmImg = new File(DOCUMENTNUMBER + ".Finger" + 1 + ".pgm"); //Carrega ela
            BufferedImage print = ImageIO.read(pgmImg);        //Carrega a imagem jpg em java
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(print, "pnm", baos);// aqui deve dar merda...

            imgBytes = baos.toByteArray();     //Transforma em byteArray

            int offset = 57;

            char[] imgChar = new char[imgBytes.length];  //charArray

            for (int i = 0; i < imgBytes.length; i++) {
                imgChar[i] = (char) (imgBytes[i] & 0xFF);        //carrega os bytes como char
            }

            for (int i = 0; i < 100; i++) {
                System.out.print(imgChar[i]);
            }

            System.out.println("Image Loaded, now to verification");

            while (true) {
                new FPrintController().verifyImage(imgChar, 540, 160);      //go
                String choice = new Scanner(System.in).next();
                if (choice.equals("N")) {
                    pgmImg.deleteOnExit();
                    break;
                }
            }

        } else {
            System.out.println("Não foi possível acessar o arquivo DG3");
        }
    }

    /**
     * Envia o arquivo DG3, Coletando uma digital como teste
     *
     * @throws IOException
     * @throws CardServiceException
     */
    private void SendDG3() throws IOException, CardServiceException {

        ArrayList<FingerImageInfo> fingerImages = new ArrayList<>();

        //Talvez colocar o dedo como entrada para ter todos os dedos como entrada
        char[] image = new FPrintController().scanImage();      //get image charArray from print but image is already in memory as finger_standardized.pgm

        for (int i = 0; i < 100; i++) {
            System.out.print(image[i]);
        }

        Mat temp = Imgcodecs.imread("finger_standardized.pgm"); //Utiliza o OpenCV para ler a imagem pgm
        MatOfInt params = new MatOfInt(Imgcodecs.CV_IMWRITE_JPEG_QUALITY, 100);  //com este parametro
        Imgcodecs.imwrite(DOCUMENTNUMBER + ".Finger" + 0 + ".jpg", temp, params);    //Salva a imagem em formato JPEG

        BufferedImage portrait = ImageIO.read(new File(DOCUMENTNUMBER + ".Finger" + 0 + ".jpg"));    //Carrega a imagem Jpeg
        ByteArrayOutputStream baos = new ByteArrayOutputStream();   //Retira os bytes
        ImageIO.write(portrait, "jpg", baos);
        baos.flush();
        byte[] imageBytes = baos.toByteArray();

        FingerImageInfo finger = new FingerImageInfo(FingerImageInfo.POSITION_RIGHT_INDEX_FINGER,
                1, 1, 100, //view count, view number, quality%
                FingerImageInfo.IMPRESSION_TYPE_SWIPE, //impression type
                temp.cols(), temp.rows(), //Dimensions w,h
                new ByteArrayInputStream(imageBytes), //image bytes
                imageBytes.length, //image size in bytes
                FingerInfo.COMPRESSION_JPEG); //compression type          //Cria uma entrada de digital para um dedo

        fingerImages.add(finger);       //add no array

        FingerInfo fingerInfo = new FingerInfo(0, //capture device ID 0= unspecified
                30, //aquisition level 30 = 500dpi
                FingerInfo.SCALE_UNITS_PPI, //scale units
                160, 500, //dimension picture w,h
                160, 500, //dimension reader w,h
                8, //pixel depth
                FingerInfo.COMPRESSION_JPEG,
                fingerImages);      //cria uma entrada de série de digitais

        ArrayList<FingerInfo> fingerInfos = new ArrayList<>();

        fingerInfos.add(fingerInfo);

        DG3File dg3 = new DG3File(fingerInfos);

        if (!perso.isOpen()) {
            perso.open();
        }

        perso.createFile(service.EF_DG3, (short) dg3.getEncoded().length);
        perso.selectFile(service.EF_DG3);

        System.out.println("Enviando arquivo DG3");
        perso.writeFile(service.EF_DG3, new ByteArrayInputStream(dg3.getEncoded()));
    }

    /**
     * Talvez seja necessário
     *
     * @throws CardServiceException
     * @throws IOException
     */
    private void readDG4() throws CardServiceException, IOException {
        InputStream dg4Input;
        DG4File dg4;

        if (canSelectFile(service.EF_DG2)) {
            dg4Input = service.getInputStream(service.EF_DG4);
            dg4 = new DG4File(dg4Input);

        } else {
            System.out.println("Não foi possível acessar o arquivo DG4");
        }
    }

    /**
     * Talvez seja necessário
     *
     * @throws CardServiceException
     * @throws IOException
     */
    private void readDG5() throws CardServiceException, IOException {
        InputStream dg5Input;
        DG5File dg5;

        if (canSelectFile(service.EF_DG5)) {
            System.out.println("DG5 FILE PRESENT");
            dg5Input = service.getInputStream(service.EF_DG5);
            dg5 = new DG5File(dg5Input);

        } else {
            System.out.println("Não foi possível acessar o arquivo DG5");
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
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fecha o cartão, o deixando não editável
     *
     * @throws CardServiceException
     */
    private void LockCard() throws CardServiceException {
        perso.lockApplet();
        perso.close();
    }

    private DG14File readDG14() throws CardServiceException, IOException {
        InputStream dg14Input;
        DG14File dg14;

        if (canSelectFile(service.EF_DG14)) {
            System.out.println("DG14 FILE PRESENT");
            dg14Input = service.getInputStream(service.EF_DG14);
            dg14 = new DG14File(dg14Input);
            return dg14;

        } else {
            System.out.println("Não foi possível acessar o arquivo DG14");
            return null;
        }
    }

    private DG14File SendDG14() throws CardServiceException {
        TerminalAuthenticationInfo TAInfo = new TerminalAuthenticationInfo();
        ChipAuthenticationInfo CAInfo = new ChipAuthenticationInfo(SecurityInfo.ID_CA_DH_AES_CBC_CMAC_256, 2);
        ChipAuthenticationPublicKeyInfo CAPkInfo = new ChipAuthenticationPublicKeyInfo(pair.getPublic()); //PK

        //Precisa do Terminal e mais muita coisa nossa... que saco...
        ArrayList<SecurityInfo> sInfos = new ArrayList();

        DG14File dg14 = new DG14File(sInfos);

        perso.createFile(service.EF_DG14, (short) dg14.getEncoded().length);
        perso.selectFile(service.EF_DG14);

        System.out.println("Enviando arquivo DG14");
        perso.writeFile(service.EF_DG14, new ByteArrayInputStream(dg14.getEncoded()));

        return dg14;
    }

    /**
     *
     * @return chave Publica do arquivo DG15
     * @throws IOException
     * @throws CardServiceException
     */
    private PublicKey readDG15() throws IOException, CardServiceException {
        InputStream dg15Input;
        DG15File dg15;

        if (canSelectFile(service.EF_DG15)) {
            System.out.println("DG15 FILE PRESENT");
            dg15Input = service.getInputStream(service.EF_DG15);
            dg15 = new DG15File(dg15Input);
            return dg15.getPublicKey();

        } else {
            System.out.println("Não foi possível acessar o arquivo DG15");
            return null;
        }
    }

    /**
     * Envia a chave publica que é um field por enquanto, talvez vire parametro?
     *
     * @throws CardServiceException
     */
    private void SendDG15(KeyPair keys) throws CardServiceException {

        DG15File dg15 = new DG15File(keys.getPublic());
        if (!perso.isOpen()) {
            perso.open();
        }

        //perso.setBAC(DOCUMENTNUMBER, DATEOFBIRTH, DATEOFEXPIRY);
        perso.createFile(service.EF_DG15, (short) dg15.getEncoded().length);
        perso.selectFile(service.EF_DG15);

        System.out.println("Enviando arquivo DG15");
        perso.writeFile(service.EF_DG15, new ByteArrayInputStream(dg15.getEncoded()));
    }

    private FeaturePoint[] resolveFPS(float[] fs) {
        FeaturePoint[] fps = new FeaturePoint[57];
        int type = 0;

        fps[0] = new FeaturePoint(type, 2, 1, (int) fs[12], (int) fs[13]);        //Ponta do Queixo
        fps[1] = new FeaturePoint(type, 2, 2, (int) fs[134], (int) fs[135]);      //Labio Superior Interno Meio
        fps[2] = new FeaturePoint(type, 2, 3, (int) fs[140], (int) fs[141]);      //Labio Inferior Interno Meio
        fps[3] = new FeaturePoint(type, 2, 4, (int) fs[130], (int) fs[131]);      //Canto Interno da Boca Esquerdo
        fps[4] = new FeaturePoint(type, 2, 5, (int) fs[118], (int) fs[119]);      //Canto Interno da Boca Direito
        fps[5] = new FeaturePoint(type, 2, 6, (int) fs[132], (int) fs[133]);      //Labio Superior Interno Esquerdo
        fps[6] = new FeaturePoint(type, 2, 7, (int) fs[136], (int) fs[137]);      //Labio Superior Interno Direito
        fps[7] = new FeaturePoint(type, 2, 8, (int) fs[142], (int) fs[143]);      //Labio Inferior Interno Esquerdo
        fps[8] = new FeaturePoint(type, 2, 9, (int) fs[138], (int) fs[139]);      //Labio Inferior Interno Direito
        fps[9] = new FeaturePoint(type, 2, 11, (int) fs[14], (int) fs[15]);      //Angulo Esquerco do Queixo
        fps[10] = new FeaturePoint(type, 2, 12, (int) fs[10], (int) fs[11]);      //Angulo Direito do Queixo
        fps[11] = new FeaturePoint(type, 2, 13, (int) fs[8], (int) fs[9]);        //Meio Maxilar Esquerda
        fps[12] = new FeaturePoint(type, 2, 14, (int) fs[16], (int) fs[17]);      //Meio Maxilar Direita
        //3.1 -> 3.4 N/A (Iris Sup e Inf Esq e Dir)
        fps[13] = new FeaturePoint(type, 3, 5, (int) fs[78], (int) fs[79]);       //Pupila Esquerda
        fps[14] = new FeaturePoint(type, 3, 6, (int) fs[76], (int) fs[77]);       //Pupila Direita
        fps[15] = new FeaturePoint(type, 3, 7, (int) fs[88], (int) fs[89]);       //Canto Externo Olho Esquerdo
        fps[16] = new FeaturePoint(type, 3, 8, (int) fs[60], (int) fs[61]);       //Canto Interno Olho Direito
        fps[17] = new FeaturePoint(type, 3, 9, (int) fs[92], (int) fs[93]);       //Palpebra Inferior Esquerda
        fps[18] = new FeaturePoint(type, 3, 10, (int) fs[72], (int) fs[73]);      //Palpebra Inferior Direita
        fps[19] = new FeaturePoint(type, 3, 11, (int) fs[80], (int) fs[81]);      //Canto Interno Olho Esquerdo
        fps[20] = new FeaturePoint(type, 3, 12, (int) fs[68], (int) fs[69]);      //Canto Externo Olho Direito

        fps[21] = new FeaturePoint(type, 4, 1, (int) fs[44], (int) fs[45]);       //Canto Interno Sobrancelha Esquerda
        fps[22] = new FeaturePoint(type, 4, 2, (int) fs[42], (int) fs[43]);       //Canto Interno Sobrancelha Direita
        fps[23] = new FeaturePoint(type, 4, 3, (int) fs[48], (int) fs[49]);       //Borda Superior Sobrancelha Esquerda
        fps[24] = new FeaturePoint(type, 4, 4, (int) fs[34], (int) fs[35]);       //Borda Superior Sobrancelha Direita
        fps[25] = new FeaturePoint(type, 4, 5, (int) fs[50], (int) fs[51]);       //Canto Externo Sobrancelha Esquerda
        fps[26] = new FeaturePoint(type, 4, 6, (int) fs[36], (int) fs[37]);       //Canto Externo Sobrancelha Direita
        fps[27] = new FeaturePoint(type, 8, 1, (int) fs[124], (int) fs[125]);     //Labio Superior Externo Meio
        fps[28] = new FeaturePoint(type, 8, 2, (int) fs[128], (int) fs[129]);     //Labio Inferior Externo Meio
        fps[29] = new FeaturePoint(type, 8, 3, (int) fs[130], (int) fs[131]);     //Canto Externo da Boca Esquerdo
        fps[30] = new FeaturePoint(type, 8, 4, (int) fs[118], (int) fs[119]);     //Canto Externo da Boca Direito
        fps[31] = new FeaturePoint(type, 8, 5, (int) fs[128], (int) fs[129]);     //Labio Superior Esquerda
        fps[32] = new FeaturePoint(type, 8, 6, (int) fs[120], (int) fs[121]);     //Labio Superior Direito
        fps[33] = new FeaturePoint(type, 8, 7, (int) fs[146], (int) fs[147]);     //Labio Inferior Esquerdo
        fps[34] = new FeaturePoint(type, 8, 8, (int) fs[150], (int) fs[151]);     //Labio Inferior Direito
        fps[35] = new FeaturePoint(type, 8, 9, (int) fs[122], (int) fs[123]);     //Filtro Labial Direito
        fps[36] = new FeaturePoint(type, 8, 10, (int) fs[126], (int) fs[127]);    //Filtro Labial Esquerdo

        fps[37] = new FeaturePoint(type, 9, 1, (int) fs[108], (int) fs[109]);     //Borda Externa Narina Esquerda
        fps[38] = new FeaturePoint(type, 9, 2, (int) fs[116], (int) fs[117]);     //Borda Externa Narina Direita
        fps[39] = new FeaturePoint(type, 9, 3, (int) fs[104], (int) fs[105]);     //Ponta do Nariz
        fps[40] = new FeaturePoint(type, 9, 4, (int) fs[114], (int) fs[115]);     //Base Direita Nariz
        fps[41] = new FeaturePoint(type, 9, 5, (int) fs[110], (int) fs[111]);     //Base Esquerda Nariz
        //9.6 e 9.7 N/A (Topo do corpo nasal)
        fps[42] = new FeaturePoint(type, 9, 12, (int) fs[98], (int) fs[99]);      //Corpo Nasal Meio
        fps[43] = new FeaturePoint(type, 9, 13, (int) fs[96], (int) fs[97]);      //Corpo Nasal Esquerda
        fps[44] = new FeaturePoint(type, 9, 14, (int) fs[100], (int) fs[101]);    //Corpo Nasal Direita
        fps[45] = new FeaturePoint(type, 9, 15, (int) fs[112], (int) fs[113]);    //Ponte Nasal

        //10.1 ->10.6 N/A (Orelhas)
        fps[46] = new FeaturePoint(type, 10, 7, (int) fs[4], (int) fs[5]);        //Base da Orelha Esquerda
        fps[47] = new FeaturePoint(type, 10, 8, (int) fs[20], (int) fs[21]);      //Base da Orelha Direita
        fps[48] = new FeaturePoint(type, 10, 9, (int) fs[2], (int) fs[3]);        //Costeleta Esquerda
        fps[49] = new FeaturePoint(type, 10, 10, (int) fs[22], (int) fs[23]);     //Costeleta Direita

        fps[50] = new FeaturePoint(type, 11, 1, (int) fs[28], (int) fs[29]);      //Meio da Linha do Cabelo
        fps[51] = new FeaturePoint(type, 11, 2, (int) fs[30], (int) fs[31]);      //Linha do Cabelo Direita
        fps[52] = new FeaturePoint(type, 11, 3, (int) fs[26], (int) fs[27]);      //Linha do Cabelo Esquerda
        //11.4 ->11.5 N/A (Topo da Cabeca e Topo do Cabelo)
        fps[53] = new FeaturePoint(type, 12, 1, (int) (fs[88] + fs[80] + fs[92] + fs[84]) / 4, (int) (fs[89] + fs[81] + fs[93] + fs[85]) / 4);    //Centro do Olho Esquerdo
        fps[54] = new FeaturePoint(type, 12, 2, (int) (fs[64] + fs[72] + fs[60] + fs[68]) / 4, (int) (fs[65] + fs[73] + fs[61] + fs[69]) / 4);    //Centro do Olho Direito
        fps[55] = new FeaturePoint(type, 12, 3, (int) fs[106], (int) fs[107]);    //Narina Esquerda
        fps[56] = new FeaturePoint(type, 12, 4, (int) fs[102], (int) fs[104]);    //Narina Direita

        return fps;
    }

    private Certificate readCertFromFile(File file, String algorithmName) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance(algorithmName);
            return cf.generateCertificate(new FileInputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
