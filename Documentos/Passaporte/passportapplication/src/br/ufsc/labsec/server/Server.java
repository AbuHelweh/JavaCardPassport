package br.ufsc.labsec.server;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;

public class Server {   //eh o RodarServidor e o paraTeste
private static Server euMesmo = null;
	
	private Server() {
		try {
			WebServer server = new WebServer(8185);
			XmlRpcServer servidor = server.getXmlRpcServer();
			PropertyHandlerMapping phm = new PropertyHandlerMapping();
			phm.addHandler("AR", AR.class);
			servidor.setHandlerMapping(phm);
			server.start();
		} catch (Exception exception) {
			System.err.println("JavaServer: " + exception);
		}
	}
	
	public static Server obterInstancia() {
		if (euMesmo == null)
			euMesmo = new Server();
		return euMesmo;
	}
	
	public static void main(String[] args) {
		Server Servidor = Server.obterInstancia();
	}
}
