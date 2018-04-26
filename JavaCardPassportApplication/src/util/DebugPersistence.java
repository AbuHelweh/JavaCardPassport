/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.jmrtd.cert.CVCPrincipal;

/**
 *
 * @author luca
 */
public class DebugPersistence {

    private KeyPair ECKeyPair;
    private KeyPair RSAKeyPair;

    private CVCPrincipal caRef;
    private CVCPrincipal holderRef;

    private KeyPair DHKey;

    private static DebugPersistence Instance = null;

    private DebugPersistence() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("ECDH", "BC");
            gen.initialize(ECNamedCurveTable.getParameterSpec("c2tnb191v3"));//"prime256v1"));
            DHKey = gen.genKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DebugPersistence getInstance() {
        if (Instance == null) {
            Instance = new DebugPersistence();
        }
        return Instance;
    }

    public KeyPair getDHKey(){
        return DHKey;
    }
    
    public void saveHolderRef(CVCPrincipal ref) {
        holderRef = ref;
    }

    public void saveCaRef(CVCPrincipal ref) {
        caRef = ref;
    }

    public CVCPrincipal getHolderRef() {
        if (holderRef != null) {
            return holderRef;
        }
        System.out.println("null holder ref");
        return null;
    }

    public CVCPrincipal getCARef() {
        if (holderRef != null) {
            return holderRef;
        }
        System.out.println("null ca ref");
        return null;
    }

    public void saveECPair(KeyPair pair) {
        ECKeyPair = pair;
    }

    public void saveRSAPair(KeyPair pair) {
        RSAKeyPair = pair;
    }

    public KeyPair getRSAPair() {
        if (RSAKeyPair != null) {
            return RSAKeyPair;
        }
        System.out.println("null card pair");
        return null;
    }

    public KeyPair getECPair() {
        if (ECKeyPair != null) {
            return ECKeyPair;
        }
        
        System.out.println("null terminal pair");
        return null;
    }
}
