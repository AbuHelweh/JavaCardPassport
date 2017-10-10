package br.ufsc.labsec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.X509Extension;



import br.ufsc.labsec.ramodule.request.SubjectAlternativeName;
import br.ufsc.labsec.ramodule.request.exception.DataFormatException;
import br.ufsc.labsec.ramodule.request.icpbrasil.IcpbrasilExtension131;
import sun.security.pkcs.PKCS10;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class GenerateRequest {
	
	protected PrivateKey privateKey;
	protected PublicKey publicKey;
	
	public void gerarChave() {
		String deviceName = "SmartCard_" + Math.random();
		String pkcs11config = "name = " + deviceName
		+ "\nlibrary = C:\\Windows\\System32\\aetpkss1.dll\nattributes(*,CKO_PUBLIC_KEY,*)={ CKA_TOKEN=true }\nshowInfo = true";
		byte[] pkcs11configBytes = pkcs11config.getBytes();

		ByteArrayInputStream configStream = new ByteArrayInputStream(pkcs11configBytes);
		Provider pkcs11Provider = new sun.security.pkcs11.SunPKCS11("", configStream);
		int result = Security.addProvider(pkcs11Provider);
		System.out.println(result);

		KeyStore keyStore;
		try {
			keyStore = KeyStore.getInstance("PKCS11", pkcs11Provider);
			keyStore.load(null, "123456".toCharArray());
			
			// generate keys
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", pkcs11Provider);
			kpg.initialize(1024);
			KeyPair pair = kpg.generateKeyPair();

			privateKey = pair.getPrivate();
			publicKey = pair.getPublic();
			
			System.out.println("\n\nPrivKey:\n" + privateKey.toString());
			System.out.println("\n\nPubKey:\n" + publicKey.toString());
						
			X509Certificate[] chain = new X509Certificate[1];
	        chain[0] = generateCertificate();
	        keyStore.setKeyEntry("certificate", privateKey.getEncoded(), chain);
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	X509Certificate generateCertificate() throws IOException, CertificateException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException
	{
	  X509CertInfo info = new X509CertInfo();
	  Date from = new Date();
	  Date to = new Date(from.getTime() + 365 * 86400000l);
	  CertificateValidity interval = new CertificateValidity(from, to);
	  BigInteger sn = new BigInteger(64, new SecureRandom());
	  X500Name x500Name = new X500Name("Certificado temporario", "LabSEC", "RNP", "Florianopolis", "SC", "BR");
	 
	  info.set(X509CertInfo.VALIDITY, interval);
	  info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
	  info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(x500Name));
	  info.set(X509CertInfo.ISSUER, new CertificateIssuerName(x500Name));
	  info.set(X509CertInfo.KEY, publicKey);
	  info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
	  AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
	  info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
	 
	  // Sign the cert to identify the algorithm that's used.
	  X509CertImpl cert = new X509CertImpl(info);
	  cert.sign(privateKey, "RSA");
	 
	  // Update the algorith, and resign.
	  algo = (AlgorithmId)cert.get(X509CertImpl.SIG_ALG);
	  info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
	  cert = new X509CertImpl(info);
	  cert.sign(privateKey, "RSA");
	  return cert;
	}   
	
	public byte[] generatePKCS10(String CN, String OU, String O,
			String L, String S, String C, String dob) throws Exception {
		
		gerarChave();
		
		// generate PKCS10 certificate request
		String sigAlg = "SHA256WithRSA";
		PKCS10 pkcs10 = new PKCS10(publicKey);
		Signature signature = Signature.getInstance(sigAlg);
		signature.initSign(privateKey);
		
		// common, orgUnit, org, locality, state, country
		//X509Extension e = generateExtensions(dob);
	    //e.getValue();
		X500Name x500Name = new X500Name(CN, OU, O, L, S, C.substring(0, 2));	
		System.out.println("COUNTRY " + C.substring(0, 2));
		//X500Signer s = new X500Signer(signature, x500Name);
		pkcs10.encodeAndSign(x500Name, signature);
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bs);
		pkcs10.print(ps);
		byte[] c = bs.toByteArray();
		try {
			if (ps != null)
				ps.close();
			if (bs != null)
				bs.close();
		} catch (Throwable th) {
		}
		return c;
	}
	
	public X509Extension generateExtensions(String dob) throws DataFormatException, IOException {
		SubjectAlternativeName subjectAltNameBuilder = new SubjectAlternativeName();

		// Todas as extensões da ICP-Brasil devem estar dentro do mesmo subject alternative name.
		subjectAltNameBuilder.addSimpleOtherName(new IcpbrasilExtension131(dob + "1111111111111111111111444444444444444SSPSC"));

		//Vector<ASN1ObjectIdentifier> extensionsOids = new Vector<ASN1ObjectIdentifier>();
		//Vector<X509Extension> extensionsValues = new Vector<X509Extension>();

		//extensionsOids.add(SubjectAlternativeName.getOid());
		//extensionsValues.add(new X509Extension(false, new DEROctetString(subjectAltNameBuilder.getEncoded())));
		
		X509Extension e = new X509Extension(false, new DEROctetString(subjectAltNameBuilder.getEncoded()));
		//X509Extensions x509Extensions = new X509Extensions(extensionsOids, extensionsValues);
		//add no request
		
		return e;
	}
}
