/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Security;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.Set;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import myjmrtdcardapplication.CardReader;
import net.sf.scuba.smartcards.CardServiceException;
import org.bouncycastle.util.Arrays;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.cert.CVCPrincipal;
import org.jmrtd.lds.ChipAuthenticationInfo;
import org.jmrtd.lds.ChipAuthenticationPublicKeyInfo;
import org.jmrtd.lds.LDSFile;
import org.jmrtd.lds.SODFile;
import org.jmrtd.lds.SecurityInfo;
import org.jmrtd.lds.TerminalAuthenticationInfo;
import org.jmrtd.lds.icao.COMFile;
import org.jmrtd.lds.icao.DG14File;
import org.jmrtd.lds.icao.DG15File;
import org.jmrtd.protocol.AAResult;
import org.jmrtd.protocol.BACResult;
import org.jmrtd.protocol.CAResult;
import util.MyCertificateFactory;

/**
 *
 * @author luca
 */
public class SecurityProtocols {

    private static SecurityProtocols Instance;
    private PassportService service;
    private String digestAlgorithm = "SHA-256";
    private String sigAlg = "SHA256withRSA";
    private CardReader reader;

    public static SecurityProtocols getInstance(PassportService service, CardReader reader) {
        if (Instance == null) {
            Instance = new SecurityProtocols(service, reader);
        }
        Instance.service = service;
        Instance.reader = reader;
        return Instance;
    }

    private SecurityProtocols(PassportService service, CardReader reader) {
        this.service = service;
        this.reader = reader;
    }

    /**
     * Handler for BAC Protocol
     *
     * @param key
     * @return
     * @throws CardServiceException
     */
    public BACResult doBAC(BACKeySpec key) throws CardServiceException {
        //System.out.println("service.doBac() " + service.hashCode());
        return service.doBAC(key);
    }

    /**
     * Does the Passive Authentication Protocol for validating data authenticity
     * It first checks the certificate in SOD File for its validity Then checks
     * the hashes in the SOD with the hashes from the files read and compares
     * them for structural changes
     *
     * @param com
     * @param sod
     * @return
     * @throws NoSuchAlgorithmException
     */
    public PAResult doPA(COMFile com, SODFile sod) throws NoSuchAlgorithmException, CardServiceException, IOException, CertificateException {
        //Certify SOD File with certificate

        boolean SODValidity = true;

        CertificateValidationResult certCheck = checkForCertificateValidity(sod.getDocSigningCertificate());

        if (certCheck.isValid()) {
            for (X509Certificate c : certCheck.getChain()) {
                System.out.println(c.getIssuerX500Principal());
            }
        }

        if (com == null) {
            System.out.println("PA COM NULL");
            return null;
        }

        if (sod == null) {
            System.out.println("PA SOD NULL");
            return null;
        }

        //Certify files with hashes
        HashMap<Integer, Boolean> result = new HashMap();
        Map<Integer, byte[]> sodHashes = sod.getDataGroupHashes();
        int[] tags = {LDSFile.EF_COM_TAG, LDSFile.EF_DG1_TAG, LDSFile.EF_DG2_TAG, LDSFile.EF_DG3_TAG, LDSFile.EF_DG4_TAG, LDSFile.EF_DG5_TAG, LDSFile.EF_DG6_TAG, LDSFile.EF_DG7_TAG, LDSFile.EF_DG8_TAG, LDSFile.EF_DG9_TAG, LDSFile.EF_DG10_TAG, LDSFile.EF_DG11_TAG, LDSFile.EF_DG12_TAG, LDSFile.EF_DG13_TAG, LDSFile.EF_DG14_TAG, LDSFile.EF_DG15_TAG, LDSFile.EF_DG16_TAG};

        int[] comtags = com.getTagList();
        byte[] currentHash;

        if (Arrays.contains(comtags, tags[0])) {
            currentHash = MessageDigest.getInstance(digestAlgorithm).digest(reader.readCOM().getEncoded());
            if (java.util.Arrays.equals(currentHash, sodHashes.get(0))) {
                result.put(0, Boolean.TRUE);
            } else {
                result.put(0, Boolean.FALSE);
                SODValidity = false;
            }
        }

        for (int i = 1; i < tags.length; i++) {

            if (Arrays.contains(comtags, tags[i])) {
                currentHash = MessageDigest.getInstance(digestAlgorithm).digest(reader.getDGFile(i).getEncoded());

                if (java.util.Arrays.equals(currentHash, sodHashes.get(i))) {
                    result.put(i, Boolean.TRUE);
                } else {
                    result.put(i, Boolean.FALSE);
                    SODValidity = false;
                }
            }
        }

        return new PAResult(SODValidity && certCheck.isValid(), result);
    }

    /**
     * Handler for the Extended Access Control Protocol It consists of the Card
     * Authentication and Terminal Authentication Protocols
     *
     * @return
     */
    public EACResult doEAC(DG14File dg14) throws CardServiceException, Exception {

        ChipAuthenticationInfo chipinfo = null;
        ChipAuthenticationPublicKeyInfo chipauthinfo = null;
        TerminalAuthenticationInfo tainfo = null;

        Collection<SecurityInfo> infos = dg14.getSecurityInfos();
        for (SecurityInfo i : infos) {
            if (i instanceof ChipAuthenticationInfo) {
                chipinfo = (ChipAuthenticationInfo) i;
            }
            if (i instanceof ChipAuthenticationPublicKeyInfo) {
                chipauthinfo = (ChipAuthenticationPublicKeyInfo) i;
            }
            if(i instanceof TerminalAuthenticationInfo){
                tainfo = (TerminalAuthenticationInfo) i;
            }
        }

        if (chipinfo == null || chipauthinfo == null) {
            throw new Exception("dg14 file empty");
        }        
        
        CAResult cares = service.doCA(chipauthinfo.getKeyId(), chipinfo.getObjectIdentifier(), SecurityInfo.ID_PK_ECDH, chipauthinfo.getSubjectPublicKey());
        //TAResult tares = service.doTA(caReference, terminalCertificates, cares.getPCDPrivateKey(), SecurityInfo.ID_TA_ECDSA_SHA_256, cares, DOCUMENTNUMBER);

        System.out.println("cares:");
        System.out.println(cares);
        
        return new EACResult(cares, null);
    }

    /**
     * Handler for active authentication protocol, sends challenge to card that encrypts it, receives, decrypt and validate
     * @param dg15 - DG15 file wher public key is located
     * @return - AA Result with protocol result
     * @throws CardServiceException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException 
     */
    public Security.AAResult doAA(DG15File dg15) throws CardServiceException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        PublicKey publicKey = dg15.getPublicKey();
        byte[] challenge = new byte[8];
        new Random().nextBytes(challenge);
        // new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};

        AAResult res = service.doAA(publicKey, "SHA1", "SHA1withRSA", challenge);

        StringBuilder sb = new StringBuilder();

        Cipher rsaCiph = Cipher.getInstance("RSA", "BC"); //<<----- OK
        rsaCiph.init(Cipher.DECRYPT_MODE, publicKey);

        int index = 0;
        
        byte[] ciph = rsaCiph.doFinal(res.getResponse());
        
        for (byte b : ciph) {  //<<------- OK
            if (b != 0x00 && index != 0 && index != ciph.length-1) {
                sb.append(String.format("%02X ", b));
            }
            index++;
        }

        StringBuilder sb1 = new StringBuilder();

        byte[] c = new byte[114];

        for (int i = 113; i > 113 - 8; i--) {
            c[i] = challenge[i - 114 + 8];
        }

        for (byte b : MessageDigest.getInstance("SHA1").digest(c)) {  //<----- OK
            sb1.append(String.format("%02X ", b));
        }
        System.out.println(sb);
        System.out.println(sb1);
        boolean result = sb.toString().equals(sb1.toString());
        System.out.println(result);

        return new Security.AAResult(res.getPublicKey(),res.getDigestAlgorithm(),res.getSignatureAlgorithm(),res.getChallenge(),res.getResponse(),result);
    }

    /**
     * Confere a validade de um certificado
     *
     * @param cert
     * @return
     */
    public CertificateValidationResult checkForCertificateValidity(X509Certificate cert) {
        try {
            Set<X509Certificate> additionalCerts = MyCertificateFactory.getInstance().generateTestCertificateChain();

            return CertificateValidator.validate(cert, additionalCerts);
        } catch (Exception e) {
            e.printStackTrace();
            return new CertificateValidationResult(e);
        }
    }

    public void setAlgorithms(SODFile sod) {

        digestAlgorithm = sod.getDigestAlgorithm();
        sigAlg = sod.getDigestEncryptionAlgorithm();
    }

}
