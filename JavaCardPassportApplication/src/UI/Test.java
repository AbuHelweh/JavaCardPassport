package UI;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigInteger;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.Scanner;
import myjmrtdcardapplication.JMRTDSecurityProvider;

public class Test {

    public static void main(String[] args) {
        try {
            // read file
            File file = new File("icaoPKD.ldif");
            Scanner sc = new Scanner(file);

            boolean isCert = false;
            boolean isCN = false;

            String name = "";
            String cert = "";
            BigInteger serial = BigInteger.ZERO;

            while (sc.hasNextLine()) {
                String s = sc.nextLine();

                if (s.startsWith("dn: ")) {
                    System.out.println(name);
                    System.out.println(cert);
                    System.out.println(serial);

                    CertificateFactory cf = CertificateFactory.getInstance("X.509",JMRTDSecurityProvider.getBouncyCastleProvider());
                    Certificate c = cf.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(cert)));
                    System.out.println(c);
                    
                    name = "";
                    cert = "";
                    serial = BigInteger.ZERO;
                    isCN = false;
                    isCert = false;
                    
                    continue;
                }
                if (s.startsWith("cn: ")) {
                    isCN = true;
                    isCert = false;
                }
                if (s.startsWith("objectClass")) {
                    continue;
                }
                if (s.startsWith("sn: ")) {
                    isCN = false;
                    isCert = false;
                    String temp = s.replace(("sn: "), "").trim();
                    serial = new BigInteger(temp, 16);
                }
                if (s.startsWith("userCertificate;binary:: ")) {
                    isCN = false;
                    isCert = true;
                }
                if (isCN) {
                    name += s.replace("cn: ", "").trim();
                }
                if (isCert) {
                    cert += s.replace("userCertificate;binary:: ", "").trim();
                }

            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();

            return;
        }
    }

}
