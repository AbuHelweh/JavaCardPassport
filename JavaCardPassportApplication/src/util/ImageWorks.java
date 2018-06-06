/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.File;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo.FeaturePoint;

/**
 *
 * @author luca
 */
public class ImageWorks {

    /**
     * TODO
     *
     * @param a
     * @param b
     * @return percentage of certainty
     */

    public static float matchImageFeaturePoints(FaceImageInfo.FeaturePoint[] a, FaceImageInfo.FeaturePoint[] b){
        ///////////////////////////////A
        //Sombrancelhas:
        FeaturePoint a21  = a[21];
        FeaturePoint a22  = a[22];
        //Olho A:
        FeaturePoint a34  = a[34];
        FeaturePoint a30  = a[30];
        FeaturePoint a40  = a[40];
        FeaturePoint a44  = a[44];
        //Nariz A:
        FeaturePoint a58  = a[58];
        FeaturePoint a54  = a[54];
        FeaturePoint a56  = a[56];
        //Boca A:
        FeaturePoint a59  = a[59];
        FeaturePoint a65  = a[65];
        ////////////////////////////////B
        //Sombrancelhas B:
        FeaturePoint b21  = b[21];
        FeaturePoint b22  = b[22];
        //Olho B:
        FeaturePoint b34  = b[34];
        FeaturePoint b30  = b[30];
        FeaturePoint b40  = b[40];
        FeaturePoint b44  = b[44];
        //Nariz B:
        FeaturePoint b58  = b[58];
        FeaturePoint b54  = b[54];
        FeaturePoint b56  = b[56];
        //Boca B:
        FeaturePoint b59  = b[59];
        FeaturePoint b65  = b[65];
        
        double mediaA = 0.0;
        double mediaB = 0.0;
        double multiplicadorA = 0.0;
        double multiplicadorB = 0.0;
        
        //Concertanto possíveis distorções de tamanho com uso de multiplicadores:
        if(a22.getX() - a21.getX() >= b22.getX() - b21.getX()){ //Se a maior ou igual que b
            multiplicadorA = (b22.getX() - b21.getX())/(a22.getX() - a21.getX());//Mult = b/a
        }else{
            multiplicadorB = (a22.getX() - a21.getX())/(b22.getX() - b21.getX());//Mult = a/b
        }
        
        //Calculando médias:
        
        //Média de A:
        //Olhos:
        mediaA += Math.sqrt(Math.pow(a30.getX() - a34.getX(), 2) + (Math.pow(a30.getY() - a34.getY(), 2))) * multiplicadorA;
        //Nariz:
        //Boca:
        
        //Média de B:
        
        return 0.0f;
        
    }

    /**
     * Utiliza o Stasm para extrair os pontos de controle da Imagem e resolvelos
     * para dentro do padrão.
     *
     * @param file - Image alvo
     * @return -- pontos de controle resolvidos no padrao ICAO9303//TODO: Extrai
     * features e organiza elas conforme ISO
     */
    public static FaceImageInfo.FeaturePoint[] extractPointsFromImageAndResolve(File file) {
        //Reconhecimento facial
        float[] extractedFeatures = new stasmlib.StasmController().getImageFeaturePoints(file.getPath());

        FaceImageInfo.FeaturePoint[] fps = resolveFPS(extractedFeatures);

        return fps;
    }

    /**
     * Resolve os pontos faciais do Stasm para o padrao ICAO9303
     *
     * @param fs pontos faciais do Stasm, tamanho = 2 * numero de pontos, cada
     * par forma uma coordenada x y para o ponto facial
     * @return os feature points do JMRTD
     */
    private static FaceImageInfo.FeaturePoint[] resolveFPS(float[] fs) {
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

        if (GlobalFlags.DEBUG) {
            int i = 0;
            for (FaceImageInfo.FeaturePoint fp : fps) {

                System.out.println("FP " + i + " : " + fp.getX() + ", " + fp.getY());
                i++;
            }
        }

        return fps;
    }
}
