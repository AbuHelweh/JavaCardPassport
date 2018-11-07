/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Security;

import Security.notMine.CRLVerifier;
import Security.notMine.CertificateValidationException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import util.GlobalFlags;

/**
 *
 * @author luca
 */
public class CertificateValidator {

    public static CertificateValidationResult validate(X509Certificate cert, Set<X509Certificate> additionalCerts) throws Exception {
        if (isSelfSigned(cert)) {
            if (GlobalFlags.DEBUG) {
                System.err.println("DEBUG: Self Signed Certificate Validated");
                return new CertificateValidationResult(true, null);
            } else {
                return new CertificateValidationResult(new Exception("Self Signed Certificate"));
            }
        }
        
        //System.out.println(cert);

        try {
            cert.checkValidity();
        } catch (CertificateExpiredException e) {
            return new CertificateValidationResult(e);
        } catch (CertificateNotYetValidException e) {
            return new CertificateValidationResult(e);
        }
        
        ArrayList<X509Certificate> chain = simpleCertificateChainBuilder(cert, additionalCerts);
        
        if (chain == null){
            return new CertificateValidationResult(new Exception("Unable to build certificate chain"));
        }

        for (X509Certificate c : chain) {
            try {
                CRLVerifier.verifyCertificateCRLs(c);   //Provavelmente só vai funcionar com certificado decente e não os de debug
            } catch (CertificateValidationException e) {
                e.printStackTrace();
                return new CertificateValidationResult(e);
            }
        }

        if (chain.size() > 0) {
            return new CertificateValidationResult(true, chain);
        } else {
            return new CertificateValidationResult(new Exception("Unable to build certificate chain"));
        }
    }

    //build bottom up;
    public static ArrayList<X509Certificate> simpleCertificateChainBuilder(X509Certificate cert, Set<X509Certificate> certs) {

        ArrayList<X509Certificate> chain = new ArrayList();
        X509Certificate current = cert;
        chain.add(cert);
            
        boolean ok = true;
        while (ok) {
            ok = false;
            
            for (X509Certificate c : certs) {
                try{
                    if (GlobalFlags.DEBUG) {
                        System.err.println("Verify " + current.getIssuerX500Principal() + " with " + c.getIssuerX500Principal());
                    }
                    
                    if(cert.equals(c)){
                        chain.add(c);
                        break;
                    }
                    
                    current.verify(c.getPublicKey(), "BC");
                    chain.add(c);
                    current = c;
                    certs.remove(c);
                    System.out.println("Encontrou certificado : ");
                    System.out.println("I: " + c.getIssuerX500Principal());
                    System.out.println("S: " + c.getSubjectX500Principal());
                    ok = true;
                    break;
                } catch (Exception e){
                    //System.out.println("Nope");
                }

            }
                
            
        }

        if(chain.size() > 1){
            boolean foundAnchor = false;
            for(X509Certificate c : chain){
                try{
                    if(isSelfSigned(c)){
                        foundAnchor = true;
                        break;
                    }
                }catch(Exception e ){
                    e.printStackTrace();
                }
            }
            if(foundAnchor){
                return chain;
            }
        }
    
        return null;
        

    }

    /**
     * Checks whether given X.509 certificate is self-signed.
     */
    public static boolean isSelfSigned(X509Certificate cert)
            throws CertificateException, NoSuchAlgorithmException,
            NoSuchProviderException {
        try {
            // Try to verify certificate signature with its own public key
            PublicKey key = cert.getPublicKey();
            cert.verify(key);
            return true;
        } catch (SignatureException sigEx) {
            // Invalid signature --> not self-signed
            return false;
        } catch (InvalidKeyException keyEx) {
            // Invalid key --> not self-signed
            return false;
        }
    }
}
