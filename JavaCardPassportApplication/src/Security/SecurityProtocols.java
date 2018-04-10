/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import myjmrtdcardapplication.CardReader;
import net.sf.scuba.smartcards.CardServiceException;
import org.bouncycastle.util.Arrays;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.ChipAuthenticationInfo;
import org.jmrtd.lds.ChipAuthenticationPublicKeyInfo;
import org.jmrtd.lds.LDSFile;
import org.jmrtd.lds.SODFile;
import org.jmrtd.lds.SecurityInfo;
import org.jmrtd.lds.icao.COMFile;
import org.jmrtd.lds.icao.DG14File;
import org.jmrtd.protocol.AAResult;
import org.jmrtd.protocol.BACResult;
import org.jmrtd.protocol.CAResult;

/**
 *
 * @author luca
 */
public class SecurityProtocols {

    private static SecurityProtocols Instance;
    private PassportService service;
    private String digestAlgorithm = "SHA-256";
    private CardReader reader;

    public static SecurityProtocols getInstance(PassportService service, CardReader reader) {
        if (Instance == null) {
            Instance = new SecurityProtocols(service, reader);
        }
        return Instance;
    }

    private SecurityProtocols(PassportService service, CardReader reader) {
        this.service = service;
        this.reader = reader;
    }

    /**
     * Handler for BAC Protocol
     * @param key
     * @return
     * @throws CardServiceException 
     */
    public BACResult doBAC(BACKeySpec key) throws CardServiceException {
        return service.doBAC(key);
    }

    /**
     * Does the Passive Authentication Protocol for validating data authenticity
     * It first checks the certificate in SOD File for its validity
     * Then checks the hashes in the SOD with the hashes from the files read and compares them for structural changes
     * @param com
     * @param sod
     * @return
     * @throws NoSuchAlgorithmException 
     */
    public PAResult doPA(COMFile com, SODFile sod) throws NoSuchAlgorithmException {
        //Certify SOD File with certificate

        boolean SODValidity = true;
        //Certify files with hashes
        HashMap<Integer, Boolean> result = new HashMap();
        HashMap<Integer, byte[]> sodHashes = (HashMap) sod.getDataGroupHashes();
        int[] tags = {LDSFile.EF_DG1_TAG, LDSFile.EF_DG2_TAG, LDSFile.EF_DG3_TAG, LDSFile.EF_DG4_TAG, LDSFile.EF_DG5_TAG, LDSFile.EF_DG6_TAG, LDSFile.EF_DG7_TAG, LDSFile.EF_DG8_TAG, LDSFile.EF_DG9_TAG, LDSFile.EF_DG10_TAG, LDSFile.EF_DG11_TAG, LDSFile.EF_DG12_TAG, LDSFile.EF_DG13_TAG, LDSFile.EF_DG14_TAG, LDSFile.EF_DG15_TAG, LDSFile.EF_DG16_TAG};

        int[] comtags = com.getTagList();
        byte[] currentHash;

        for (int i = 0; i < tags.length; i++) {
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

        return new PAResult(SODValidity, result);
    }
    
    /**
     * Handler for the Extended Access Control Protocol
     * It consists of the Card Authentication and Terminal Authentication Protocols
     * 
     * @return 
     */
    public EACResult doEAC(DG14File dg14) throws CardServiceException, Exception{
        
        ChipAuthenticationInfo chipinfo = null;
        ChipAuthenticationPublicKeyInfo chipauthinfo = null;
        
        Collection<SecurityInfo> infos = dg14.getSecurityInfos();
        for(SecurityInfo i : infos){
            if(i instanceof ChipAuthenticationInfo){
                chipinfo = (ChipAuthenticationInfo)i;
            }
            if(i instanceof ChipAuthenticationPublicKeyInfo){
                chipauthinfo = (ChipAuthenticationPublicKeyInfo) i;
            }
        }
        
        if(chipinfo == null || chipauthinfo == null){
            throw new Exception("dg14 file empty");
        }
        
        CAResult cares = service.doCA(chipauthinfo.getKeyId(), chipinfo.getObjectIdentifier(), SecurityInfo.ID_PK_ECDH, chipauthinfo.getSubjectPublicKey());
        System.out.println(cares);
        //TAResult tares = service.doTA(caReference, terminalCertificates, terminalKey, taAlg, cares, DOCUMENTNUMBER);
        
        return new EACResult(null,null);
    }
    
    public AAResult doAA(){
        //return service.doAA(publicKey, digestAlgorithm, digestAlgorithm, challenge);
        return null;
    }

}
