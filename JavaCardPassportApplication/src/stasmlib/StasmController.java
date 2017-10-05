/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stasmlib;

/**
 *
 * @author luca
 */
public class StasmController {
    public native float[] getImageFeaturePoints(String filename); //retorna como {x,y,x1,y1...}
}
