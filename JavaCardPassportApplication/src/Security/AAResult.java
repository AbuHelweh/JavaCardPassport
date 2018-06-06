/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Security;

import java.security.PublicKey;
import net.sf.scuba.util.Hex;
import org.jmrtd.Util;

public class AAResult extends org.jmrtd.protocol.AAResult {

    private boolean result;

    public AAResult(PublicKey publicKey, String digestAlgorithm, String signatureAlgorithm, byte[] challenge, byte[] response, boolean result) {
        super(publicKey, digestAlgorithm, signatureAlgorithm, challenge, response);
        this.result = result;
    }

    public boolean getResult() {
        return result;
    }

    @Override
    public String toString() {
        return (new StringBuilder())
                .append("AAResult [")
                .append("publicKey: ").append(Util.getDetailedPublicKeyAlgorithm(super.getPublicKey()))
                .append(", digestAlgorithm: ").append(super.getDigestAlgorithm())
                .append(", signatureAlgorithm: ").append(super.getSignatureAlgorithm())
                .append(", challenge: ").append(Hex.bytesToHexString(super.getChallenge()))
                .append(", response: ").append(Hex.bytesToHexString(super.getResponse()))
                .append(", result: ").append(result ? "OK" : "FAILED")
                .toString();
    }

}
