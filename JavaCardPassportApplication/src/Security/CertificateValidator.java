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

        Set<X509Certificate> chain = simpleCertificateChainBuilder(cert, additionalCerts);

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

    //retirar recursão para fazer com que ele siga a lista
    public static Set<X509Certificate> simpleCertificateChainBuilder(X509Certificate cert, Set<X509Certificate> certs) {

        Set<X509Certificate> chain = new HashSet();
        X509Certificate current = null;
        try {
            for (X509Certificate c : certs) {
                if (isSelfSigned(c)) {
                    if (GlobalFlags.DEBUG) {
                        System.err.println("Found Anchor " + c.getIssuerX500Principal());
                    }
                    current = c;
                    chain.add(c);
                    break;
                }
            }
            certs.remove(current);

            boolean ok = true;
            while (ok) {
                ok = false;

                for (X509Certificate c : certs) {
                    try {
                        c.verify(current.getPublicKey(), "BC");
                        if (GlobalFlags.DEBUG) {
                            System.err.println("Verify " + c.getIssuerX500Principal() + " with " + current.getIssuerX500Principal());
                        }
                        current = c;
                        chain.add(c);
                        certs.remove(c);
                        ok = true;
                        break;
                    } catch (Exception e) {
                        if (GlobalFlags.DEBUG) {
                            System.err.println("Failed " + c.getIssuerX500Principal() + " with " + current.getIssuerX500Principal());
                        }
                    }
                }

            }

        } catch (Exception e) {

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
