/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
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
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import myjmrtdcardapplication.JMRTDSecurityProvider;
import net.sf.scuba.data.Country;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.ejbca.cvc.exception.ConstructionException;
import org.jmrtd.cert.CVCAuthorizationTemplate;
import org.jmrtd.cert.CVCPrincipal;
import org.jmrtd.cert.CVCertificateBuilder;
import org.jmrtd.cert.CardVerifiableCertificate;
import org.joda.time.*;

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
    
    public KeyPair getEACECPair(){
        return pair;
    }
    
    public KeyPair getEACRSAPair(){
        return RSApair;
    }
    
    public CVCPrincipal getCAREF(){
        return caRef;
    }
    
    public CVCPrincipal getHOLDERREF(){
        return holderRef;
    }

    /**
     * Generates a CVCPrincipal object that wraps the ejbca implementation on
     * certificates
     *
     * @return
     */
    public CVCPrincipal generateCardVerifiableCertificatePrincipal(Country country) {

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
        KeyPairGenerator gen = KeyPairGenerator.getInstance("ECDH", bcProvider);    //Curva Eliptica

        gen.initialize(ECNamedCurveTable.getParameterSpec("prime192v1"));   //Campo binario f2m 239bits
        KeyPair pair = gen.genKeyPair();

        return pair;
    }

    /**
     * Gera uma chave RSA compativel com os certificados do cartao
     *
     * @return
     * @throws Exception
     */
    private KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
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
        holderRef = generateCardVerifiableCertificatePrincipal(country);
        caRef = generateCardVerifiableCertificatePrincipal(country);

        pair = generateEllipticCurveKeyPair();
        RSApair = generateRSAKeyPair();

        String CertificateKeyAlgorithm = "SHA1withRSA";//"SHA256WITHECDSA";//

        certificate = CVCertificateBuilder.createCertificate(RSApair.getPublic(), //Sha1-RSA... mas por que???
                RSApair.getPrivate(), CertificateKeyAlgorithm, caRef, holderRef,
                new CVCAuthorizationTemplate(CVCAuthorizationTemplate.Role.CVCA, CVCAuthorizationTemplate.Permission.READ_ACCESS_DG3_AND_DG4),
                new DateTime().toDate(), new DateTime().plusYears(validYears).toDate(), "BC");  //FixedDates using jodatime

        return certificate;
    }

    /**
     * Gera um certificado x509 assinado pelo par de chaves
     * @param dnName
     * @param validity
     * @param keyPair
     * @param signatureAlgorithm
     * @return
     * @throws CertificateException
     * @throws InvalidKeyException
     * @throws SignatureException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException 
     */
    public Certificate generateSignedX509Certificate(X500Name dnName, long validity, PublicKey pk, PrivateKey sk, String signatureAlgorithm, boolean isCA) throws CertificateException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, NoSuchProviderException {

        Date lastDate;
        try {

            if (pk == null) {
                throw new NullPointerException("Public Key NULL");
            }
            if (sk == null) {
                throw new NullPointerException("Private Key NULL");
            }

            long now = System.currentTimeMillis();
            Date startDate = new Date(now);
            
            BigInteger certSerialNumber = new BigInteger(Long.toString(now)); // <-- Using the current timestamp as the certificate serial number

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.YEAR, 10); // <-- 10 Yr validity

            Date endDate = calendar.getTime();

            ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(sk);

            JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, pk);

            // Extensions --------------------------
            // Basic Constraints
            BasicConstraints basicConstraints = new BasicConstraints(isCA); // <-- true for CA, false for EndEntity

            certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); // Basic Constraints is usually marked as critical.

            // -------------------------------------
            return new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(certBuilder.build(contentSigner));

        } catch (Exception e) {

            throw new CertificateEncodingException("getSelfCert: "
                    + e.getMessage());
        }
    }

    /**
     * DebugPurposes gera um certificado assinado pela corrente de certificados
     * @return 
     */
    public Certificate generateTestSignedX509Certificate() {
        try {

            generateTestCertificateChain();

            KeyStore ks = KeyStore.getInstance("JKS");
            String pw = "123456";
            FileInputStream fis = new FileInputStream("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/Documentos/mykeystore.ks");
            ks.load(fis, pw.toCharArray());

            if (ks.getCertificate("DocSignCertificate") != null) {
                System.out.println("Certificado existe");
                //return (Certificate) ks.getCertificate("DocSignCertificate");
            }

            KeyPair certGen = getRSAKeyPair();

            // prepare the validity of the certificate
            long validSecs = (long) 365 * 24 * 60 * 60; // valid for one year

            Certificate cert = generateSignedX509Certificate(
                    new X500Name("CN=card.labsec,O=LABSEC/UFSC,L=FLORIANOPOLIS,C=DE"),
                    validSecs,
                    certGen.getPublic(),
                    lastCertificateChainPrivateKey,
                    "SHA256WithRSA", false);

            lastCertificateChainPrivateKey = certGen.getPrivate();

            ks.setKeyEntry("DocSignCertificate", certGen.getPrivate(), pw.toCharArray(),
                    new Certificate[]{cert});

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
    public Certificate generateSelfSignedX509Certificate() throws Exception {

        KeyStore ks = KeyStore.getInstance("JKS");
        String pw = "123456";
        FileInputStream fis = new FileInputStream("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/Documentos/mykeystore.ks");
        ks.load(fis, pw.toCharArray());

        if (ks.getCertificate("DocSignCertificate") != null) {
            System.out.println("Certificado existe");
            //return (Certificate) ks.getCertificate("DocSignCertificate");
        }
        
        KeyPair keyPair = getRSAKeyPair();

        long now = System.currentTimeMillis();
        Date startDate = new Date(now);

        X500Name dnName = new X500Name("CN=MyJMRTD,O=LABSEC/UFSC,L=FLORIANOPOLIS,C=DE");
        BigInteger certSerialNumber = new BigInteger(Long.toString(now)); // <-- Using the current timestamp as the certificate serial number

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.YEAR, 10); // <-- 10 Yr validity

        Date endDate = calendar.getTime();

        String signatureAlgorithm = "SHA256WithRSA"; // <-- Use appropriate signature algorithm based on your keyPair algorithm.

        ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(keyPair.getPrivate());

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, keyPair.getPublic());

        // Extensions --------------------------
        // Basic Constraints
        BasicConstraints basicConstraints = new BasicConstraints(true); // <-- true for CA, false for EndEntity

        certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); // Basic Constraints is usually marked as critical.

        // -------------------------------------
        Certificate cert =  new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(certBuilder.build(contentSigner));

        System.out.println(cert.toString());

        // set the certificate and the key in the keystore
        ks.setKeyEntry("DocSignCertificate", keyPair.getPrivate(), pw.toCharArray(),
                new Certificate[]{cert});

        FileOutputStream fos = new FileOutputStream("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/Documentos/mykeystore.ks");

        ks.store(fos, pw.toCharArray());
        return cert;
    }

    public KeyPair getRSAKeyPair() throws NoSuchAlgorithmException{
        if (RSApair != null) {
            return RSApair;
        }
        RSApair = generateRSAKeyPair();
        return RSApair;
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
        PrivateKey rootkey;

        if (ks.getCertificate("LabsecRootAnchor") != null) {

            cert = (X509Certificate) ks.getCertificate("LabsecRootAnchor");
            rootkey = (PrivateKey) ks.getKey("LabsecRootAnchor", pw.toCharArray());
            lastCertificateChainPrivateKey = (PrivateKey) ks.getKey("LabsecRootAnchor", pw.toCharArray());
            certChain.add(cert);

        } else {
            cert = (X509Certificate) generateSelfSignedX509Certificate();

            rootkey = getRSAKeyPair().getPrivate();
            lastCertificateChainPrivateKey = getRSAKeyPair().getPrivate();

            ks.setKeyEntry("LabsecRootAnchor", rootkey, pw.toCharArray(),
                    new Certificate[]{cert});

            certChain.add(cert);
        }

        FileOutputStream fos = new FileOutputStream("/home/" + System.getProperty("user.name") + "/workspace/JavaCardPassport/Documentos/mykeystore.ks");

        ks.store(fos, pw.toCharArray());

        return certChain;
    }

}
