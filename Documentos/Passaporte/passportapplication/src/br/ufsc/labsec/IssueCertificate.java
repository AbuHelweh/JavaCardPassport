package br.ufsc.labsec;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;

import net.sourceforge.scuba.data.Gender;

import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.XmlRpcRequestConfig;
import org.jmrtd.Passport;
import org.apache.http.client.ClientProtocolException;


public class IssueCertificate implements XmlRpcRequest { //tipo o CalculadoraCliente

	static String crlf = "\r\n";
	static String twoHyphens = "--";
	static String boundary =  "*****";
	
	Passport passport;
	GenerateRequest request = new GenerateRequest();
	ParserData parser;
	static String requestPath = "C:\\Users\\thais\\Downloads\\req.pem";
	String certificatePath;
	
	public IssueCertificate(Passport passport) {
		this.passport = passport;
		try {
			this.parser = new ParserData(passport);
			this.issue();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	public void issue() throws IOException, ParseException {
		
		JOptionPane.showMessageDialog(null, "Insira um smart card e clique em OK");
		try {
			generateRequest(); //gera a req
			SSLSettings();
			InputStream responseStream = importRequest(); //importa no sgci
			String url = parseReqId(responseStream);
			approveRequest(url); //aprova no sgci
			String id = parseCertId();
			downloadCertificate(id); //faz download do certificado
			
			//validar req com http://www.sslshopper.com/csr-decoder.html ou https://ssl-tools.verisign.com/checker/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public byte[] generateRequest() throws Exception {
		byte[] csr = request.generatePKCS10(parser.getName() + " " + parser.getSurname(), "LabSEC", "RNP", "Florianopolis", "SC", parser.getNationality(), parser.getDob());		
		writeInFile(new String(csr));
		return csr;
	}
	
	public static void downloadCertificate(String id) throws IOException {

		URL obj = new URL("https://150.162.66.22:40443" + id);
		HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
		
		InputStream responseStream =  new BufferedInputStream(conn.getInputStream());
		BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
		StringBuilder stringBuilder = new StringBuilder();
		
		String line = "";
		while ((line = responseStreamReader.readLine()) != null)
		{
		    stringBuilder.append(line).append("\n");
		   
		}
		JOptionPane.showMessageDialog(null, stringBuilder.toString());
		System.out.println(stringBuilder.toString());
	}
	
	public static String parseCertId() throws IOException {
		URL obj = new URL("https://150.162.66.22:40443/raoper/Certificates/list");
		HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
		InputStream responseStream =  new BufferedInputStream(conn.getInputStream());
		
		//descobrir o id do œltimo certificado emitido
		BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
		String line = "";
		StringBuilder stringBuilder = new StringBuilder();
		String urlApprove = "";
		while ((line = responseStreamReader.readLine()) != null)
		{
			if(line.contains("common/certificates/download/id/"))
				urlApprove = line;
		    stringBuilder.append(line).append("\n");
		}
		String s[] = urlApprove.split("\"");  
		JOptionPane.showMessageDialog(null, "Certificado com id " + s[1]);
		return s[1];
	}
	
	public static void approveRequest(String url) throws IOException {
		URL obj = new URL(url);
		HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
		conn.setRequestMethod("POST");
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

		DataOutputStream request = new DataOutputStream(conn.getOutputStream());
		request.writeBytes(twoHyphens + boundary + crlf);
		request.writeBytes("Content-Disposition: form-data; name=\"actions[password]\"" + crlf);
		request.writeBytes("Content-Type: text/plain;" + crlf);
		request.writeBytes(crlf);
		request.writeBytes("operador" + crlf);
		//request.writeBytes(crlf);
		request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
		request.flush();
		request.close();
		
		InputStream responseStream =  new BufferedInputStream(conn.getInputStream());
		BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
		String line, approved = "";
		StringBuilder stringBuilder = new StringBuilder();
		while ((line = responseStreamReader.readLine()) != null)
		{
		    stringBuilder.append(line).append("\n");
		    if(line.contains("pendente encontrada"))
		    	approved = line;
		    //se tiver um Nenhuma requisiÃ§Ã£o pendente encontrada
		}
		//System.out.println(stringBuilder.toString());
		if (!approved.equals(""))
			JOptionPane.showMessageDialog(null, "Requisicao aprovada");
	}
	
	public static String parseReqId(InputStream responseStream) throws IOException {
		BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
		String line = "";
		StringBuilder stringBuilder = new StringBuilder();
		String urlApprove = "";
		while ((line = responseStreamReader.readLine()) != null)
		{
			if(line.contains("href=\"/raoper/requests/approve?requests[]="))
				urlApprove = line;
		    stringBuilder.append(line).append("\n");
		}
		JOptionPane.showMessageDialog(null, "Requisicao importada, numero " + urlApprove);
		responseStreamReader.close();
		String s[] = urlApprove.split("\"");  
		return "https://150.162.66.22:40443" + s[1];
	}
	
	public static InputStream importRequest () throws IOException {
		//String path = "/Users/thais/Dropbox/workspace/TesteCurl/src/req.pem";
		//requestPath = requestPath.replaceAll("//",File.separator);
		File f = new File(requestPath); 
		Scanner sc = new Scanner(f);
		String str = "";
		while(sc.hasNextLine()){
		    str = str+sc.nextLine() + "\n";     
		}
		
		String url = "https://150.162.66.22:40443/raoper/requests/importrequest";
		URL obj = new URL(url);
		
		HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
		conn.setRequestMethod("POST");
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

		DataOutputStream request = new DataOutputStream(conn.getOutputStream());
		request.writeBytes(twoHyphens + boundary + crlf);
		request.writeBytes("Content-Disposition: form-data; name=\"destinyEntity\"" + crlf);
		request.writeBytes("Content-Type: text/plain; charset=UTF-8" + crlf);
		request.writeBytes(crlf);
		request.writeBytes(6 + crlf);
		request.writeBytes(twoHyphens + boundary + crlf);
		
		request.writeBytes("Content-Disposition: form-data; name=request; filename=" + requestPath + "\"" + crlf);
		//request.writeBytes("Content-Disposition: attachment;"+ path + "\"" + crlf);
		request.writeBytes("Content-Type: text/plain;" + crlf);
		//request.writeBytes("Content-Transfer-Encoding: base64" + crlf);
		//request.writeBytes("Content-Type: application/txt" + crlf); //application/octet-stream
		request.writeBytes(crlf);
		request.writeBytes(str + crlf);
		request.writeBytes(crlf);
		request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
		request.flush();
		request.close();
		
		return new BufferedInputStream(conn.getInputStream());
	}
	
	private void SSLSettings() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(new KeyManager[0],
				new TrustManager[] { new DefaultTrustManager() },
				new SecureRandom());
		SSLContext.setDefault(ctx); // para validar o path do certificado

		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {//resolver o common name do certificado
					public boolean verify(String string, SSLSession ssls) {
						return true;
					}
				});
		
	}
	
	private static class DefaultTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public void writeInFile(String requisicao) {
		try {
			FileOutputStream fop = null;
			File file;
			file = new File(requestPath);
			fop = new FileOutputStream(file);
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			// get the content in bytes
			byte[] contentInBytes = requisicao.getBytes();
 
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
 
			JOptionPane.showMessageDialog(null, "Requisicao gerada com sucesso"); 
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	@Override
	public XmlRpcRequestConfig getConfig() {
		return null;
	}

	@Override
	public String getMethodName() {
		return null;
	}

	@Override
	public Object getParameter(int arg0) {
		return null;
	}

	@Override
	public int getParameterCount() {
		return 0;
	}
	
	public void a() {
//		SubjectAlternativeName subjectAltNameBuilder = new SubjectAlternativeName();
//
//		// Todas as extensões da ICP-Brasil devem estar dentro do mesmo subject alternative name.
//		subjectAltNameBuilder.addSimpleOtherName(new IcpbrasilExtension131("220619921111111111111111111111444444444444444SSPSC"));
//
//		Vector<ASN1ObjectIdentifier> extensionsOids = new Vector<ASN1ObjectIdentifier>();
//		Vector<X509Extension> extensionsValues = new Vector<X509Extension>();
//
//		extensionsOids.add(SubjectAlternativeName.getOid());
//		extensionsValues.add(new X509Extension(false, new DEROctetString(subjectAltNameBuilder.getEncoded())));
//
//		/**
//		* Outras extensões podem ser adicionadas nessa parte do código
//		*/
//		X509KeyUsage keyUsage = this.createKeyUsage();
//		extensionsOids.add(X509Extension.keyUsage);
//		extensionsValues.add(new X509Extension(true, new DEROctetString(keyUsage.getEncoded())));
//
//		X509Extensions X509Extensions = new X509Extensions(extensionsOids, extensionsValues);
//
//		/**
//		* No nosso caso usamos um certTemplateBuilder do BouncyCastle
//		* 
//		*/
//
//		CertTemplateBuilder certTemplateBuilder = new CertTemplateBuilder();
//		/**
//		* Método createSubjectName cria a subject
//		*/
//		certTemplateBuilder.setSubject(this.createSubjectName());
//		certTemplateBuilder.setExtensions(X509Extensions);
	}
}
