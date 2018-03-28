/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myjmrtdcardapplication;

import UI.ImageWorks;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jnitestfingerprint.FPrintController;
import static myjmrtdcardapplication.CardCom.DOCUMENTNUMBER;
import net.sf.scuba.data.Gender;
import net.sf.scuba.smartcards.CardServiceException;
import net.sf.scuba.smartcards.TerminalCardService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.LDSFile;
import org.jmrtd.lds.icao.COMFile;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.icao.DG3File;
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FaceInfo;
import org.jmrtd.lds.iso19794.FingerImageInfo;
import org.jmrtd.lds.iso19794.FingerInfo;
import org.jmrtd.protocol.BACResult;
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

    public CardSender() {

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
                perso = new PassportPersoService(service);
                BouncyCastleProvider provider = new BouncyCastleProvider();
                Security.addProvider(provider);
                
            }
        } catch (Exception e){
            e.printStackTrace();
            if(e instanceof CardServiceException){
                JOptionPane.showMessageDialog(null,"ERRO DE LEITURA COM O CARTÂO");
            }
        }
    }
    
    public void SendSecurityInfo(BACKeySpec backey) throws CardServiceException {
        if (!perso.isOpen()) {
            perso.open();
        }

        System.out.println("Sending BAC");
        perso.setBAC(backey.getDocumentNumber(), backey.getDateOfBirth(), backey.getDateOfExpiry());

        //doSecurity(backey);
    }

    public void doSecurity(BACKeySpec backey) throws CardServiceException {
        BACResult result = service.doBAC(backey);
        System.out.println(result.toString());

        if (perso != null) {
            perso.setWrapper(result.getWrapper());
        }
    }
    
    public void SendCOM() throws CardServiceException {

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
     * Envia o arquivo DG1 para o cartão
     * @param info  as informações do MRZ
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
    }
    
    /**
     * Envia o arquivo DG2 para o cartão
     * @param file  o Arquivo contendo a imagem da pessoa
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
        
        //* TO DO Make conversion work to not modify content
        Mat temp = Imgcodecs.imread("finger_standardized.pgm"); //Utiliza o OpenCV para ler a imagem pgm
        MatOfInt params = new MatOfInt(Imgcodecs.CV_IMWRITE_JPEG_QUALITY, 100);  //com este parametro
        Imgcodecs.imwrite(DOCUMENTNUMBER + ".Finger" + 0 + ".jpg", temp, params);    //Salva a imagem em formato JPEG

        //File pgmFile = new File("finger_standardized1.pgm"); 
        //BufferedImage portrait = ImageIO.read(pgmFile);
        BufferedImage portrait = ImageIO.read(new File(DOCUMENTNUMBER + ".Finger" + 0 + ".jpg"));    //Carrega a imagem Jpeg
        ByteArrayOutputStream baos = new ByteArrayOutputStream();   //Retira os bytes
        ImageIO.write(portrait, "jpg", baos);
        baos.flush();
        byte[] imageBytes = baos.toByteArray();       
        
        

        FingerImageInfo finger = new FingerImageInfo(FingerImageInfo.POSITION_RIGHT_INDEX_FINGER,
                1, 1, 100, //view count, view number, quality%
                FingerImageInfo.IMPRESSION_TYPE_SWIPE, //impression type
                portrait.getWidth(), portrait.getHeight(), //Dimensions w,h
                new ByteArrayInputStream(imageBytes), //image bytes
                imageBytes.length, //image size in bytes
                FingerInfo.COMPRESSION_JPEG); //compression type          //Cria uma entrada de digital para um dedo

        //*/
        
        System.out.println(imageBytes.length);
        
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
     * Fecha o cartão, o deixando não editável
     *
     * @throws CardServiceException
     */
    public void LockCard() throws CardServiceException {
        perso.lockApplet();
        perso.close();
    }
}
