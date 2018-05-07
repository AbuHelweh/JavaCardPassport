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

        try {
            cert.checkValidity();
        } catch (CertificateExpiredException e) {
            return new CertificateValidationResult(e);
        } catch (CertificateNotYetValidException e) {
            return new CertificateValidationResult(e);
        }

        Set<X509Certificate> chain = simpleCertificateChainBuilder(cert, additionalCerts, new HashSet<X509Certificate>());

        for (X509Certificate c : chain) {
            try {
                CRLVerifier.verifyCertificateCRLs(c);   //Provavelmente só vai funcionar com certificado decente e não os de debug
            } catch (CertificateValidationException e){
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

    public static Set<X509Certificate> simpleCertificateChainBuilder(X509Certificate cert, Set<X509Certificate> anchors, Set<X509Certificate> chain) {

        for (X509Certificate c : anchors) {
            if (c == cert) {
                if (GlobalFlags.DEBUG) {
                    System.err.println("DEBUG: ClassValidator.class :: Same certificate compared:");
                    System.err.println(cert.getIssuerX500Principal() + " :: " + c.getIssuerX500Principal());
                }
                continue;
            }

            try {
                cert.verify(c.getPublicKey(), "BC");
            } catch (Exception e) {
                if (GlobalFlags.DEBUG) {
                    System.err.println("DEBUG: ClassValidator.class :: " + e.getMessage());
                    System.err.println(cert.getIssuerX500Principal() + " :: " + c.getIssuerX500Principal());
                }
                continue;
            }

            if (GlobalFlags.DEBUG) {
                System.err.println("DEBUG: ClassValidator.class :: Match:");
                System.err.println(cert.getIssuerX500Principal() + " :: " + c.getIssuerX500Principal());
            }
            //Detecção de ciclo
            if (chain.contains(c)) {
                return chain;
            }
            chain.add(c);

            return simpleCertificateChainBuilder(c, anchors, chain);
        }

        return chain;
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
