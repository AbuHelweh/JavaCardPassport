/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import util.DebugPersistence;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SignatureException;
import myjmrtdcardapplication.JMRTDSecurityProvider;
import net.sf.scuba.data.Country;
import org.bouncycastle.jce.ECNamedCurveTable;
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

        CVCPrincipal ref =  new CVCPrincipal(country, country.getName().substring(0, 4), "00001");
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
    public KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException{
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", bcProvider);

        gen.initialize(1024);
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
    public CardVerifiableCertificate generateCertificate(Country country, int validYears) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException, ConstructionException, InvalidAlgorithmParameterException {
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
    
    public KeyPair getGeneratedCertificateKeyPair(){
        if(RSApair != null){
            return RSApair;
        }
        return null;
    }

}
