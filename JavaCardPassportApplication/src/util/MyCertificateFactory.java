/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import myjmrtdcardapplication.JMRTDSecurityProvider;
import net.sf.scuba.data.Country;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.ejbca.cvc.exception.ConstructionException;
import org.jmrtd.cert.CVCAuthorizationTemplate;
import org.jmrtd.cert.CVCPrincipal;
import org.jmrtd.cert.CVCertificateBuilder;
import org.jmrtd.cert.CardVerifiableCertificate;
import org.joda.time.*;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

/**
 *
 * @author luca
 */
public class MyCertificateFactory {

    private static MyCertificateFactory instance;
    private Provider bcProvider = JMRTDSecurityProvider.getBouncyCastleProvider();
    private Provider jmrtdProvider = JMRTDSecurityProvider.getInstance();

    //Test purposes need to extract from files ASAP
    private CVCPrincipal holderRef;
    private CVCPrincipal caRef;
    private KeyPair pair;
    private KeyPair RSApair;
    private CardVerifiableCertificate certificate;

    PrivateKey lastCertificateChainPrivateKey;

    public static MyCertificateFactory getInstance() {
        if (instance == null) {
            instance = new MyCertificateFactory();
        }
        return instance;
    }

    private MyCertificateFactory() {

    }

    /**
     * Generates a CVCPrincipal object that wraps the ejbca implementation on
     * certificates
     *
     * @return
     */
    public CVCPrincipal generateCardVerifiableCertificatePrincipal(Country country, String sequenceNumber) {

        CVCPrincipal ref = new CVCPrincipal(country, country.getName().substring(0, 4), "00001");
        System.out.println(ref.toString());
        return ref;
    }

    /**
     * Gera uma chave de curva eliptica compativel com os certificados do cartao
     *
     * @return
     * @throws Exception
     */
    public KeyPair generateEllipticCurveKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("ECDSA", bcProvider);    //Curva Eliptica

        gen.initialize(ECNamedCurveTable.getParameterSpec("c2tnb239v3"));   //Campo binario f2m 239bits
        KeyPair pair = gen.genKeyPair();

        return pair;
    }

    /**
     * Gera uma chave RSA compativel com os certificados do cartao
     *
     * @return
     * @throws Exception
     */
    public KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", bcProvider);

        gen.initialize(2048);
        KeyPair RSApair = gen.genKeyPair();

        return RSApair;
    }

    /**
     * Gera um certificado
     *
     * @param country
     * @return
     * @throws Exception
     */
    public CardVerifiableCertificate generateCVCertificate(Country country, int validYears) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException, ConstructionException, InvalidAlgorithmParameterException {
        holderRef = generateCardVerifiableCertificatePrincipal(country, "00001");
        caRef = generateCardVerifiableCertificatePrincipal(country, "00001");

        pair = generateEllipticCurveKeyPair();
        RSApair = generateRSAKeyPair();

        //Debug purposes only
        DebugPersistence persistence = DebugPersistence.getInstance();

        persistence.saveCaRef(caRef);
        persistence.saveHolderRef(holderRef);
        persistence.saveRSAPair(RSApair);
        persistence.saveECPair(pair);

        String CertificateKeyAlgorithm = "SHA1withRSA";//"SHA256WITHECDSA";//

        certificate = CVCertificateBuilder.createCertificate(RSApair.getPublic(), //Sha1-RSA... mas por que???
                RSApair.getPrivate(), CertificateKeyAlgorithm, caRef, holderRef,
                new CVCAuthorizationTemplate(CVCAuthorizationTemplate.Role.CVCA, CVCAuthorizationTemplate.Permission.READ_ACCESS_DG3_AND_DG4),
                new DateTime().toDate(), new DateTime().plusYears(validYears).toDate(), "BC");  //FixedDates using jodatime

        return certificate;
    }

    /**
     * Modified from CertAndKeyGen, generates an X509Certificate and signs it
     * with a custom private key Gambiarra? Maybe
     *
     * @param myname
     * @param validity
     * @param publicKey Certificate Publice
     * @param privateKey Signer Private
     * @param sigAlg
     * @return
     * @throws CertificateException
     * @throws InvalidKeyException
     * @throws SignatureException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public X509Certificate generateSignedX509Certificate(X500Name myname, long validity, PublicKey publicKey, PrivateKey privateKey, String sigAlg) throws CertificateException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, NoSuchProviderException {
        X509CertImpl cert;

        Date lastDate;
        try {

            if (publicKey == null) {
                throw new NullPointerException("Public Key NULL");
            }
            if (privateKey == null) {
                throw new NullPointerException("Private Key NULL");
            }

            lastDate = new Date();
            lastDate.setTime(lastDate.getTime() + validity * 1000);
            CertificateValidity interval = new CertificateValidity(new Date(), lastDate);
            X509CertInfo info = new X509CertInfo();

            // Add all mandatory attributes
            info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
            info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new java.util.Random().nextInt() & 0x7fffffff));

            AlgorithmId algID = AlgorithmId.get(sigAlg);

            if (algID == null) {
                throw new NullPointerException("Private Key NULL");
            }

            info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algID));
            info.set(X509CertInfo.SUBJECT, myname);
            info.set(X509CertInfo.KEY, new CertificateX509Key(publicKey));
            info.set(X509CertInfo.VALIDITY, interval);
            info.set(X509CertInfo.ISSUER, myname);

            cert = new X509CertImpl(info);

            cert.sign(privateKey, sigAlg, "BC");

            return (X509Certificate) cert;

        } catch (IOException e) {

            throw new CertificateEncodingException("getSelfCert: "
                    + e.getMessage());
        }
    }

    /**
     * DebugPurposes gera um certificado assinado pela corrente de certificados
     * @return 
     */
    public X509Certificate generateTestSignedX509Certificate() {
        try {

            generateTestCertificateChain();

            KeyStore ks = KeyStore.getInstance("JKS");
            String pw = "123456";
            FileInputStream fis = new FileInputStream("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/Documentos/mykeystore.ks");
            ks.load(fis, pw.toCharArray());

            if (ks.getCertificate("DocSignCertificate") != null) {
                System.out.println("Certificado existe");
                return (X509Certificate) ks.getCertificate("DocSignCertificate");
            }

            KeyPair certGen = generateRSAKeyPair();

            // prepare the validity of the certificate
            long validSecs = (long) 365 * 24 * 60 * 60; // valid for one year

            X509Certificate cert = generateSignedX509Certificate(
                    new X500Name("CN=card.labsec,O=LABSEC/UFSC,L=FLORIANOPOLIS,C=DE"),
                    validSecs,
                    certGen.getPublic(),
                    lastCertificateChainPrivateKey,
                    "SHA256WithRSA");

            lastCertificateChainPrivateKey = certGen.getPrivate();

            ks.setKeyEntry("DocSignCertificate", certGen.getPrivate(), pw.toCharArray(),
                    new X509Certificate[]{cert});

            FileOutputStream fos = new FileOutputStream("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/Documentos/mykeystore.ks");

            ks.store(fos, pw.toCharArray());

            return cert;

        } catch (Exception ex) {
            Logger.getLogger(MyCertificateFactory.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * gera um certificado auto assinado do jeito certo, sem gambiarra...
     * @return
     * @throws Exception 
     */
    public X509Certificate generateSelfSignedX509Certificate() throws Exception {

        KeyStore ks = KeyStore.getInstance("JKS");
        String pw = "123456";
        FileInputStream fis = new FileInputStream("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/Documentos/mykeystore.ks");
        ks.load(fis, pw.toCharArray());

        if (ks.getCertificate("DocSignCertificate") != null) {
            System.out.println("Certificado existe");
            return (X509Certificate) ks.getCertificate("DocSignCertificate");
        }

        // generate the certificate
        // first parameter  = Algorithm
        // second parameter = signrature algorithm
        // third parameter  = the provider to use to generate the keys (may be null or
        //                    use the constructor without provider)
        CertAndKeyGen certGen = new CertAndKeyGen("RSA", "SHA256WithRSA", "BC");

        // generate it with 2048 bits
        certGen.generate(2048);

        // prepare the validity of the certificate
        long validSecs = (long) 365 * 24 * 60 * 60; // valid for one year

        // add the certificate information, currently only valid for one year.
        X509Certificate cert = certGen.getSelfCertificate(
                // enter your details according to your application
                new X500Name("CN=MyJMRTD,O=LABSEC/UFSC,L=FLORIANOPOLIS,C=DE"), validSecs);

        System.out.println(cert.toString());

        // set the certificate and the key in the keystore
        ks.setKeyEntry("DocSignCertificate", certGen.getPrivateKey(), pw.toCharArray(),
                new X509Certificate[]{cert});
        DebugPersistence.getInstance().saveRSAPair(new KeyPair(certGen.getPublicKey(), certGen.getPrivateKey()));

        FileOutputStream fos = new FileOutputStream("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/Documentos/mykeystore.ks");

        ks.store(fos, pw.toCharArray());

        return cert;
    }

    public KeyPair getGeneratedCertificateKeyPair() {
        if (RSApair != null) {
            return RSApair;
        }
        return null;
    }

    /**
     * Debug Purposes
     * gera uma corrente de certificados.
     * @return
     * @throws Exception 
     */
    public Set<X509Certificate> generateTestCertificateChain() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        String pw = "123456";
        FileInputStream fis = new FileInputStream("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/Documentos/mykeystore.ks");
        ks.load(fis, pw.toCharArray());

        Set<X509Certificate> certChain = new HashSet<X509Certificate>();
        X509Certificate cert;
        CertAndKeyGen certGen = new CertAndKeyGen("RSA", "SHA256WithRSA", "BC");
        certGen.generate(2048);
        long validSecs = (long) 365 * 24 * 60 * 60;
        
        PrivateKey rootkey = null;

        if (ks.getCertificate("LabsecRootAnchor") != null) {

            cert = (X509Certificate) ks.getCertificate("LabsecRootAnchor");
            rootkey = (PrivateKey) ks.getKey("LabsecRootAnchor", pw.toCharArray());
            lastCertificateChainPrivateKey = (PrivateKey) ks.getKey("LabsecRootAnchor", pw.toCharArray());
            certChain.add(cert);

        } else {
            cert = certGen.getSelfCertificate(
                    new X500Name("CN=root.labsec,O=LABSEC/UFSC,L=FLORIANOPOLIS,C=DE"), validSecs);

            rootkey = certGen.getPrivateKey();
            lastCertificateChainPrivateKey = certGen.getPrivateKey();

            ks.setKeyEntry("LabsecRootAnchor", certGen.getPrivateKey(), pw.toCharArray(),
                    new X509Certificate[]{cert});

            certChain.add(cert);
        }

        if (ks.getCertificate("LabsecIntermidiateAnchor") != null) {

            cert = (X509Certificate) ks.getCertificate("LabsecIntermidiateAnchor");
            certChain.add(cert);

        } else {

            certGen = new CertAndKeyGen("RSA", "SHA256WithRSA", "BC");
            certGen.generate(2048);
            
            KeyPair kp = generateRSAKeyPair();

            cert = generateSignedX509Certificate(
                    new X500Name("CN=intermidiate.labsec,O=LABSEC/UFSC,L=FLORIANOPOLIS,C=DE"),
                    validSecs,
                    kp.getPublic(),
                    rootkey,
                    "SHA256WithRSA");

            lastCertificateChainPrivateKey = kp.getPrivate();

            ks.setKeyEntry("LabsecIntermidiateAnchor", kp.getPrivate(), pw.toCharArray(),
                    new X509Certificate[]{cert});

            certChain.add(cert);
        }
        
        if (ks.getCertificate("LabsecIntermidiateAnchor1") != null) {

            cert = (X509Certificate) ks.getCertificate("LabsecIntermidiateAnchor1");
            certChain.add(cert);

        } else {

            certGen = new CertAndKeyGen("RSA", "SHA256WithRSA", "BC");
            certGen.generate(2048);
            
            KeyPair kp = generateRSAKeyPair();

            cert = generateSignedX509Certificate(
                    new X500Name("CN=intermidiate1.labsec,O=LABSEC/UFSC,L=FLORIANOPOLIS,C=DE"),
                    validSecs,
                    kp.getPublic(),
                    rootkey,
                    "SHA256WithRSA");

            lastCertificateChainPrivateKey = kp.getPrivate();

            ks.setKeyEntry("LabsecIntermidiateAnchor1", kp.getPrivate(), pw.toCharArray(),
                    new X509Certificate[]{cert});

            certChain.add(cert);
        }

        FileOutputStream fos = new FileOutputStream("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/Documentos/mykeystore.ks");

        ks.store(fos, pw.toCharArray());

        return certChain;
    }

}
