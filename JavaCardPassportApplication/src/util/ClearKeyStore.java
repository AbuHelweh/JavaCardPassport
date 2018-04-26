/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.FileOutputStream;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author luca
 */
public class ClearKeyStore {
    
    public static void main(String[] args){
        clear();
    }
    
    public static void clear(){
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            String pw = "123456";
            
            FileOutputStream fos = new FileOutputStream("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/Documentos/mykeystore.ks");
            ks.load(null);
            ks.store(fos, pw.toCharArray());

        } catch (Exception ex) {
            Logger.getLogger(ClearKeyStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
