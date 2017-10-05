/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myjmrtdcardapplication;

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
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FaceInfo;
import org.jmrtd.protocol.BACResult;

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

        //PublicKey AAkey = readDG15();
        //System.out.println(service.doAA(AAkey, "SHA512", "RSA", service.sendGetChallenge()));
        //perso = new PassportPersoService(service, result.getWrapper());
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

        //Reconhecimento facial
        float[] extractedFeatures = new stasmlib.StasmController().getImageFeaturePoints(file.getPath());   //TODO: Extrai features e organiza elas conforme ISO

        FaceImageInfo.FeaturePoint[] fps = resolveFPS(extractedFeatures);

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
     * Resolve os pontos faciais do Stasm para o padrao ICAO9303
     * @param fs pontos faciais do Stasm, tamanho = 2 * numero de pontos, cada par forma uma coordenada x y para o ponto facial
     * @return os feature points do JMRTD
     */
    private FaceImageInfo.FeaturePoint[] resolveFPS(float[] fs) {
        FaceImageInfo.FeaturePoint[] fps = new FaceImageInfo.FeaturePoint[57];
        int type = 0;

        fps[0] = new FaceImageInfo.FeaturePoint(type, 2, 1, (int) fs[12], (int) fs[13]);        //Ponta do Queixo
        fps[1] = new FaceImageInfo.FeaturePoint(type, 2, 2, (int) fs[134], (int) fs[135]);      //Labio Superior Interno Meio
        fps[2] = new FaceImageInfo.FeaturePoint(type, 2, 3, (int) fs[140], (int) fs[141]);      //Labio Inferior Interno Meio
        fps[3] = new FaceImageInfo.FeaturePoint(type, 2, 4, (int) fs[130], (int) fs[131]);      //Canto Interno da Boca Esquerdo
        fps[4] = new FaceImageInfo.FeaturePoint(type, 2, 5, (int) fs[118], (int) fs[119]);      //Canto Interno da Boca Direito
        fps[5] = new FaceImageInfo.FeaturePoint(type, 2, 6, (int) fs[132], (int) fs[133]);      //Labio Superior Interno Esquerdo
        fps[6] = new FaceImageInfo.FeaturePoint(type, 2, 7, (int) fs[136], (int) fs[137]);      //Labio Superior Interno Direito
        fps[7] = new FaceImageInfo.FeaturePoint(type, 2, 8, (int) fs[142], (int) fs[143]);      //Labio Inferior Interno Esquerdo
        fps[8] = new FaceImageInfo.FeaturePoint(type, 2, 9, (int) fs[138], (int) fs[139]);      //Labio Inferior Interno Direito
        fps[9] = new FaceImageInfo.FeaturePoint(type, 2, 11, (int) fs[14], (int) fs[15]);      //Angulo Esquerco do Queixo
        fps[10] = new FaceImageInfo.FeaturePoint(type, 2, 12, (int) fs[10], (int) fs[11]);      //Angulo Direito do Queixo
        fps[11] = new FaceImageInfo.FeaturePoint(type, 2, 13, (int) fs[8], (int) fs[9]);        //Meio Maxilar Esquerda
        fps[12] = new FaceImageInfo.FeaturePoint(type, 2, 14, (int) fs[16], (int) fs[17]);      //Meio Maxilar Direita
        //3.1 -> 3.4 N/A (Iris Sup e Inf Esq e Dir)
        fps[13] = new FaceImageInfo.FeaturePoint(type, 3, 5, (int) fs[78], (int) fs[79]);       //Pupila Esquerda
        fps[14] = new FaceImageInfo.FeaturePoint(type, 3, 6, (int) fs[76], (int) fs[77]);       //Pupila Direita
        fps[15] = new FaceImageInfo.FeaturePoint(type, 3, 7, (int) fs[88], (int) fs[89]);       //Canto Externo Olho Esquerdo
        fps[16] = new FaceImageInfo.FeaturePoint(type, 3, 8, (int) fs[60], (int) fs[61]);       //Canto Interno Olho Direito
        fps[17] = new FaceImageInfo.FeaturePoint(type, 3, 9, (int) fs[92], (int) fs[93]);       //Palpebra Inferior Esquerda
        fps[18] = new FaceImageInfo.FeaturePoint(type, 3, 10, (int) fs[72], (int) fs[73]);      //Palpebra Inferior Direita
        fps[19] = new FaceImageInfo.FeaturePoint(type, 3, 11, (int) fs[80], (int) fs[81]);      //Canto Interno Olho Esquerdo
        fps[20] = new FaceImageInfo.FeaturePoint(type, 3, 12, (int) fs[68], (int) fs[69]);      //Canto Externo Olho Direito

        fps[21] = new FaceImageInfo.FeaturePoint(type, 4, 1, (int) fs[44], (int) fs[45]);       //Canto Interno Sobrancelha Esquerda
        fps[22] = new FaceImageInfo.FeaturePoint(type, 4, 2, (int) fs[42], (int) fs[43]);       //Canto Interno Sobrancelha Direita
        fps[23] = new FaceImageInfo.FeaturePoint(type, 4, 3, (int) fs[48], (int) fs[49]);       //Borda Superior Sobrancelha Esquerda
        fps[24] = new FaceImageInfo.FeaturePoint(type, 4, 4, (int) fs[34], (int) fs[35]);       //Borda Superior Sobrancelha Direita
        fps[25] = new FaceImageInfo.FeaturePoint(type, 4, 5, (int) fs[50], (int) fs[51]);       //Canto Externo Sobrancelha Esquerda
        fps[26] = new FaceImageInfo.FeaturePoint(type, 4, 6, (int) fs[36], (int) fs[37]);       //Canto Externo Sobrancelha Direita
        fps[27] = new FaceImageInfo.FeaturePoint(type, 8, 1, (int) fs[124], (int) fs[125]);     //Labio Superior Externo Meio
        fps[28] = new FaceImageInfo.FeaturePoint(type, 8, 2, (int) fs[128], (int) fs[129]);     //Labio Inferior Externo Meio
        fps[29] = new FaceImageInfo.FeaturePoint(type, 8, 3, (int) fs[130], (int) fs[131]);     //Canto Externo da Boca Esquerdo
        fps[30] = new FaceImageInfo.FeaturePoint(type, 8, 4, (int) fs[118], (int) fs[119]);     //Canto Externo da Boca Direito
        fps[31] = new FaceImageInfo.FeaturePoint(type, 8, 5, (int) fs[128], (int) fs[129]);     //Labio Superior Esquerda
        fps[32] = new FaceImageInfo.FeaturePoint(type, 8, 6, (int) fs[120], (int) fs[121]);     //Labio Superior Direito
        fps[33] = new FaceImageInfo.FeaturePoint(type, 8, 7, (int) fs[146], (int) fs[147]);     //Labio Inferior Esquerdo
        fps[34] = new FaceImageInfo.FeaturePoint(type, 8, 8, (int) fs[150], (int) fs[151]);     //Labio Inferior Direito
        fps[35] = new FaceImageInfo.FeaturePoint(type, 8, 9, (int) fs[122], (int) fs[123]);     //Filtro Labial Direito
        fps[36] = new FaceImageInfo.FeaturePoint(type, 8, 10, (int) fs[126], (int) fs[127]);    //Filtro Labial Esquerdo

        fps[37] = new FaceImageInfo.FeaturePoint(type, 9, 1, (int) fs[108], (int) fs[109]);     //Borda Externa Narina Esquerda
        fps[38] = new FaceImageInfo.FeaturePoint(type, 9, 2, (int) fs[116], (int) fs[117]);     //Borda Externa Narina Direita
        fps[39] = new FaceImageInfo.FeaturePoint(type, 9, 3, (int) fs[104], (int) fs[105]);     //Ponta do Nariz
        fps[40] = new FaceImageInfo.FeaturePoint(type, 9, 4, (int) fs[114], (int) fs[115]);     //Base Direita Nariz
        fps[41] = new FaceImageInfo.FeaturePoint(type, 9, 5, (int) fs[110], (int) fs[111]);     //Base Esquerda Nariz
        //9.6 e 9.7 N/A (Topo do corpo nasal)
        fps[42] = new FaceImageInfo.FeaturePoint(type, 9, 12, (int) fs[98], (int) fs[99]);      //Corpo Nasal Meio
        fps[43] = new FaceImageInfo.FeaturePoint(type, 9, 13, (int) fs[96], (int) fs[97]);      //Corpo Nasal Esquerda
        fps[44] = new FaceImageInfo.FeaturePoint(type, 9, 14, (int) fs[100], (int) fs[101]);    //Corpo Nasal Direita
        fps[45] = new FaceImageInfo.FeaturePoint(type, 9, 15, (int) fs[112], (int) fs[113]);    //Ponte Nasal

        //10.1 ->10.6 N/A (Orelhas)
        fps[46] = new FaceImageInfo.FeaturePoint(type, 10, 7, (int) fs[4], (int) fs[5]);        //Base da Orelha Esquerda
        fps[47] = new FaceImageInfo.FeaturePoint(type, 10, 8, (int) fs[20], (int) fs[21]);      //Base da Orelha Direita
        fps[48] = new FaceImageInfo.FeaturePoint(type, 10, 9, (int) fs[2], (int) fs[3]);        //Costeleta Esquerda
        fps[49] = new FaceImageInfo.FeaturePoint(type, 10, 10, (int) fs[22], (int) fs[23]);     //Costeleta Direita

        fps[50] = new FaceImageInfo.FeaturePoint(type, 11, 1, (int) fs[28], (int) fs[29]);      //Meio da Linha do Cabelo
        fps[51] = new FaceImageInfo.FeaturePoint(type, 11, 2, (int) fs[30], (int) fs[31]);      //Linha do Cabelo Direita
        fps[52] = new FaceImageInfo.FeaturePoint(type, 11, 3, (int) fs[26], (int) fs[27]);      //Linha do Cabelo Esquerda
        //11.4 ->11.5 N/A (Topo da Cabeca e Topo do Cabelo)
        fps[53] = new FaceImageInfo.FeaturePoint(type, 12, 1, (int) (fs[88] + fs[80] + fs[92] + fs[84]) / 4, (int) (fs[89] + fs[81] + fs[93] + fs[85]) / 4);    //Centro do Olho Esquerdo
        fps[54] = new FaceImageInfo.FeaturePoint(type, 12, 2, (int) (fs[64] + fs[72] + fs[60] + fs[68]) / 4, (int) (fs[65] + fs[73] + fs[61] + fs[69]) / 4);    //Centro do Olho Direito
        fps[55] = new FaceImageInfo.FeaturePoint(type, 12, 3, (int) fs[106], (int) fs[107]);    //Narina Esquerda
        fps[56] = new FaceImageInfo.FeaturePoint(type, 12, 4, (int) fs[102], (int) fs[104]);    //Narina Direita

        return fps;
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
